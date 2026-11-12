# Configuração do Firebase

1. **App Android:** no console Firebase, adicione um app Android com o pacote `br.com.petpass`
   (ou ajuste `applicationId` em `app/build.gradle.kts`) e salve o `google-services.json` em `app/`.
2. **Authentication:** ative o provedor *E-mail/senha*.
3. **Firestore** e **Storage:** crie os bancos (modo produção).
4. **Plano Blaze:** necessário para Cloud Functions (o uso do trabalho acadêmico fica dentro da cota gratuita).
5. **E-mail do 2FA:** instale a extensão *Trigger Email from Firestore* (coleção `mail`) com seu SMTP.
   Sem a extensão, o código não chega por e-mail; use `DEMO_MODE=true` para vê-lo na tela.
6. **Deploy** (na pasta `firebase/`):
   ```
   cd functions && npm install && cd ..
   firebase deploy --only functions,firestore:rules,storage
   ```
7. Antes de entregar/produção, mude `DEMO_MODE=false` em `firebase/functions/.env` e `AppConfig.DEMO_MODE` no app.

## Como o 2FA funciona
Firebase Auth não tem 2FA por e-mail nativo (só SMS/TOTP via Identity Platform). Aqui:
login → `sendLoginCode` (zera o claim `twofa` e envia o código) → `verifyLoginCode` (define `twofa=true`)
→ o app renova o token. As regras do Firestore/Storage exigem `twofa == true`.
A sessão persiste (Firebase Auth guarda o login) até o usuário tocar em **Sair**.

## Limitações conhecidas
- O check-in e a checagem de plano são validados no app; para valer de verdade, mova-os para uma Cloud Function.
- O catálogo de petshops e planos continua local (`Catalog.kt`); pode virar uma coleção `establishments` depois.
