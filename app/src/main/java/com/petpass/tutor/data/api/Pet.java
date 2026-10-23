package com.petpass.tutor.data.api;

public class Pet {
    private String id;
    private String nome;
    private String raca;
    private String porte;

    public Pet(String nome, String raca, String porte) {
        this.nome = nome;
        this.raca = raca;
        this.porte = porte;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getRaca() { return raca; }
    public String getPorte() { return porte; }
}