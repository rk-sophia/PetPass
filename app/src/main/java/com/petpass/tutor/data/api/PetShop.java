package com.petpass.tutor.data.api;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class PetShop implements Serializable {
    @SerializedName("id")
    private String id;

    @SerializedName("nome")
    private String nome;

    @SerializedName("endereco")
    private String endereco;

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getEndereco() { return endereco; }
}