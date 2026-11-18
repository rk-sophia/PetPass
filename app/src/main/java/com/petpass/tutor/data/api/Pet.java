package com.petpass.tutor.data.api;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Pet implements Serializable {
    @SerializedName("id")
    private String id;
    @SerializedName("tutor_id")
    private String tutorId;
    @SerializedName("nome")
    private String nome;
    @SerializedName("especie")
    private String especie;
    @SerializedName("porte")
    private String porte;
    @SerializedName("plano_ativo")
    private String planoAtivo;
    @SerializedName("nivel_plano")
    private int nivelPlano;
    @SerializedName("total_checkins")
    private int totalCheckins;

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getEspecie() { return especie; }
    public String getPorte() { return porte; }
    public String getPlanoAtivo() { return planoAtivo; }
    public int getNivelPlano() { return nivelPlano; }
    public int getTotalCheckins() { return totalCheckins; }
}