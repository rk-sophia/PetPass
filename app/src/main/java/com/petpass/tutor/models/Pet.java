package com.petpass.tutor.models;

public class Pet {
    private String petId;
    private String nome;
    private String especie; // "Cachorro", "Gato", "Outro"
    private String raca;
    private String porte;   // Apenas para Cachorro
    private String idade;
    private String planoAtivo; // "NENHUM", "SILVER", "GOLD", "PLATINUM"
    private String tokenPagamento; // Conformidade PCI-DSS / LGPD
    private int checkinsRealizados;

    public Pet() {}

    public Pet(String petId, String nome, String especie, String raca, String porte, String idade) {
        this.petId = petId;
        this.nome = nome;
        this.especie = especie;
        this.raca = raca;
        this.porte = porte;
        this.idade = idade;
        this.planoAtivo = "NENHUM";
        this.checkinsRealizados = 0;
    }

    // Getters e Setters
    public String getPetId() { return petId; }
    public void setPetId(String petId) { this.petId = petId; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }
    public String getRaca() { return raca; }
    public void setRaca(String raca) { this.raca = raca; }
    public String getPorte() { return porte; }
    public void setPorte(String porte) { this.porte = porte; }
    public String getIdade() { return idade; }
    public void setIdade(String idade) { this.idade = idade; }
    public String getPlanoAtivo() { return planoAtivo; }
    public void setPlanoAtivo(String planoAtivo) { this.planoAtivo = planoAtivo; }
    public String getTokenPagamento() { return tokenPagamento; }
    public void setTokenPagamento(String tokenPagamento) { this.tokenPagamento = tokenPagamento; }
    public int getCheckinsRealizados() { return checkinsRealizados; }
    public void setCheckinsRealizados(int checkinsRealizados) { this.checkinsRealizados = checkinsRealizados; }
}