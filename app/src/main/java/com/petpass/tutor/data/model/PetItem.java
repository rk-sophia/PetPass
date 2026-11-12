package com.petpass.tutor.data.model;

import java.io.Serializable;

public class PetItem implements Serializable {
    private String nome;
    private String tipo;
    private String raca;
    private String porte;
    private String fotoUrl;
    private String nomePlano;
    private double valorBase;
    private double valorFinal;
    private boolean temDesconto;

    public PetItem() {
    }

    public PetItem(String nome, String tipo, String raca, String porte, String fotoUrl, String nomePlano, double valorBase, double valorFinal, boolean temDesconto) {
        this.nome = nome;
        this.tipo = tipo;
        this.raca = raca;
        this.porte = porte;
        this.fotoUrl = fotoUrl;
        this.nomePlano = nomePlano;
        this.valorBase = valorBase;
        this.valorFinal = valorFinal;
        this.temDesconto = temDesconto;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getRaca() {
        return raca;
    }

    public void setRaca(String raca) {
        this.raca = raca;
    }

    public String getPorte() {
        return porte;
    }

    public void setPorte(String porte) {
        this.porte = porte;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public String getNomePlano() {
        return nomePlano;
    }

    public void setNomePlano(String nomePlano) {
        this.nomePlano = nomePlano;
    }

    public double getValorBase() {
        return valorBase;
    }

    public void setValorBase(double valorBase) {
        this.valorBase = valorBase;
    }

    public double getValorFinal() {
        return valorFinal;
    }

    public void setValorFinal(double valorFinal) {
        this.valorFinal = valorFinal;
    }

    public boolean isTemDesconto() {
        return temDesconto;
    }

    public void setTemDesconto(boolean temDesconto) {
        this.temDesconto = temDesconto;
    }
}
