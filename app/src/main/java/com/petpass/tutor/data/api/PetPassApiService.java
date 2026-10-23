package com.petpass.tutor.data.api;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface PetPassApiService {

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("petshops/proximos")
    Call<List<PetShop>> listarPetShopsProximos(@Query("lat") double lat, @Query("lng") double lng);
}