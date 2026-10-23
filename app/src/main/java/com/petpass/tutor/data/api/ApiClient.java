package com.petpass.tutor.data.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // A barra no final é OBRIGATÓRIA (Slide 17)
    private static final String BASE_URL = "https://api.petpass.com.br/v1/";
    private static Retrofit retrofit = null;

    public static PetPassApiService getService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()) // Conversor Gson
                    .build();
        }
        return retrofit.create(PetPassApiService.class);
    }
}