package com.petpass.tutor.data.api;

import java.io.Serializable;

public class AuthRequest implements Serializable {
    private String nome;
    private String telefone;
    private String email;
    private String senha;
    private String cep;
    private String endereco;

    public AuthRequest(String email, String senha) {
        this.email = email;
        this.senha = senha;
    }

    public AuthRequest(String nome, String telefone, String email, String senha, String cep, String endereco) {
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
        this.senha = senha;
        this.cep = cep;
        this.endereco = endereco;
    }
}