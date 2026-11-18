package com.petpass.tutor.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "PetPassSession";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_TUTOR_ID = "tutorId";
    private static final String KEY_TUTOR_NOME = "tutorNome";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void salvarSessao(String token, String tutorId, String nome) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_TUTOR_ID, tutorId)
                .putString(KEY_TUTOR_NOME, nome)
                .apply();
    }

    public boolean estaLogado() {
        return prefs.getString(KEY_TOKEN, null) != null;
    }

    public String getTutorId() { return prefs.getString(KEY_TUTOR_ID, ""); }
    public String getTutorNome() { return prefs.getString(KEY_TUTOR_NOME, "Tutor"); }

    public void sair() {
        prefs.edit().clear().apply();
    }
}