package com.petpass.tutor.data.api;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class AuthResponse implements Serializable {
    @SerializedName("token")
    private String token;
    @SerializedName("tutor_id")
    private String tutorId;
    @SerializedName("nome")
    private String nome;

    public String getToken() { return token; }
    public String getTutorId() { return tutorId; }
    public String getNome() { return nome; }
}