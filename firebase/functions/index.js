/**
 * Cloud Functions do PetPass — 2FA por e-mail sobre o Firebase Auth.
 *
 * Fluxo:
 *  1. Cliente faz login (e-mail/senha) -> chama sendLoginCode: zera o claim "twofa" e envia o código.
 *  2. Cliente chama verifyLoginCode com o código -> função define o claim twofa=true.
 *  3. Cliente renova o ID token; as regras do Firestore/Storage exigem twofa == true.
 *
 * O e-mail é enviado pela extensão "Trigger Email from Firestore" (coleção `mail`).
 * Para testes, defina DEMO_MODE=true (functions/.env) e o código também volta na resposta.
 */
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { initializeApp } = require("firebase-admin/app");
const { getAuth } = require("firebase-admin/auth");
const { getFirestore } = require("firebase-admin/firestore");
const crypto = require("crypto");

initializeApp();
const db = getFirestore();

const DEMO = process.env.DEMO_MODE === "true";
const CODE_TTL_MS = 10 * 60 * 1000;
const MAX_ATTEMPTS = 5;
const NEW_ACCOUNT_WINDOW_MS = 5 * 60 * 1000;

const hash = (code, uid) => crypto.createHash("sha256").update(`${code}:${uid}`).digest("hex");

function requireAuth(request) {
  if (!request.auth) throw new HttpsError("unauthenticated", "Faça login primeiro.");
  return request.auth;
}

exports.sendLoginCode = onCall(async (request) => {
  const { uid, token } = requireAuth(request);
  await getAuth().setCustomUserClaims(uid, { twofa: false });

  const code = String(crypto.randomInt(100000, 1000000));
  await db.doc(`loginCodes/${uid}`).set({
    hash: hash(code, uid),
    expiresAt: Date.now() + CODE_TTL_MS,
    attempts: 0,
  });
  await db.collection("mail").add({
    to: [token.email],
    message: {
      subject: "Seu código de acesso PetPass",
      text: `Seu código é ${code}. Ele vale por 10 minutos.`,
    },
  });
  return { demoCode: DEMO ? code : null };
});

exports.verifyLoginCode = onCall(async (request) => {
  const { uid } = requireAuth(request);
  const ref = db.doc(`loginCodes/${uid}`);
  const snap = await ref.get();
  if (!snap.exists) throw new HttpsError("failed-precondition", "Código expirado. Faça login novamente.");

  const data = snap.data();
  if (data.expiresAt < Date.now() || data.attempts >= MAX_ATTEMPTS) {
    await ref.delete();
    throw new HttpsError("failed-precondition", "Código expirado. Faça login novamente.");
  }

  const informed = Buffer.from(hash(String(request.data?.code ?? "").trim(), uid));
  const expected = Buffer.from(data.hash);
  const ok = informed.length === expected.length && crypto.timingSafeEqual(informed, expected);
  if (!ok) {
    await ref.update({ attempts: data.attempts + 1 });
    throw new HttpsError("permission-denied", "Código incorreto.");
  }

  await ref.delete();
  await getAuth().setCustomUserClaims(uid, { twofa: true });
  return { ok: true };
});

/** Libera a sessão logo após o cadastro (só vale para contas criadas há poucos minutos). */
exports.activateNewAccount = onCall(async (request) => {
  const { uid } = requireAuth(request);
  const user = await getAuth().getUser(uid);
  const created = Date.parse(user.metadata.creationTime);
  if (Date.now() - created > NEW_ACCOUNT_WINDOW_MS) {
    throw new HttpsError("failed-precondition", "Conta não é nova. Faça login com verificação em duas etapas.");
  }
  await getAuth().setCustomUserClaims(uid, { twofa: true });
  return { ok: true };
});
