package com.petpass.tutor.models;

public class Usuario {
    private String uid;
    private String nomeCompleto;
    private String email;
    private String endereco;
    private double latitude;
    private double longitude;
    private String role; // "TUTOR", "GERENTE", "ADMIN"
    private boolean banido;
    private String petshopId; // Preenchido apenas se role == "GERENTE"

    public Usuario() {} // Construtor vazio obrigatório para o Firebase

    public Usuario(String uid, String nomeCompleto, String email, String endereco, String role) {
        this.uid = uid;
        this.nomeCompleto = nomeCompleto;
        this.email = email;
        this.endereco = endereco;
        this.role = role;
        this.banido = false;
    }

    // Getters e Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isBanido() { return banido; }
    public void setBanido(boolean banido) { this.banido = banido; }
    public String getPetshopId() { return petshopId; }
    public void setPetshopId(String petshopId) { this.petshopId = petshopId; }
}