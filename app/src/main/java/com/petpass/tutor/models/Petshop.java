package com.petpass.tutor.models;

import java.util.List;

public class Petshop {
    private String id;
    private String nome;
    private String endereco;
    private double latitude;
    private double longitude;
    private String planoMinimoExigido; // "SILVER", "GOLD", "PLATINUM"
    private boolean ativo;
    private List<String> servicosDisponiveis;
    private List<String> horariosDisponiveis;

    public Petshop() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getPlanoMinimoExigido() { return planoMinimoExigido; }
    public void setPlanoMinimoExigido(String planoMinimoExigido) { this.planoMinimoExigido = planoMinimoExigido; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public List<String> getServicosDisponiveis() { return servicosDisponiveis; }
    public void setServicosDisponiveis(List<String> servicosDisponiveis) { this.servicosDisponiveis = servicosDisponiveis; }
    public List<String> getHorariosDisponiveis() { return horariosDisponiveis; }
    public void setHorariosDisponiveis(List<String> horariosDisponiveis) { this.horariosDisponiveis = horariosDisponiveis; }
}