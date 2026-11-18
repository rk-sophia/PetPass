package com.petpass.tutor.data.api;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Agendamento implements Serializable {
    @SerializedName("id")
    private String id;
    @SerializedName("tutor_id")
    private String tutorId;
    @SerializedName("pet_id")
    private String petId;
    @SerializedName("pet_nome")
    private String petNome;
    @SerializedName("petshop_id")
    private String petshopId;
    @SerializedName("petshop_nome")
    private String petshopNome;
    @SerializedName("servico")
    private String servico;
    @SerializedName("data_hora")
    private String dataHora;

    public Agendamento(String tutorId, String petId, String petshopId, String servico, String dataHora) {
        this.tutorId = tutorId;
        this.petId = petId;
        this.petshopId = petshopId;
        this.servico = servico;
        this.dataHora = dataHora;
    }

    public String getId() { return id; }
    public String getPetNome() { return petNome; }
    public String getPetshopNome() { return petshopNome; }
    public String getServico() { return servico; }
    public String getDataHora() { return dataHora; }
}