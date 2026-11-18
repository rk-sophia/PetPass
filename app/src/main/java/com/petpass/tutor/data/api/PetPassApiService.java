package com.petpass.tutor.data.api;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
public interface PetPassApiService {

    @POST("auth/login")
    Call<AuthResponse> login(@Body AuthRequest request);

    @POST("auth/cadastro/iniciar")
    Call<Map<String, String>> iniciarCadastro(@Body AuthRequest request);

    @POST("auth/verificar-telefone")
    Call<Map<String, String>> verificarTelefone(@Body Map<String, String> payload);

    @POST("auth/verificar-email")
    Call<AuthResponse> verificarEmailFinalizar(@Body Map<String, String> payload);

    @GET("tutores/{tutorId}/pets")
    Call<List<Pet>> listarPets(@Path("tutorId") String tutorId);

    @Multipart
    @POST("pets")
    Call<Pet> cadastrarPetComFoto(
            @Part("tutor_id") RequestBody tutorId,
            @Part("nome") RequestBody nome,
            @Part("especie") RequestBody especie,
            @Part("porte") RequestBody porte,
            @Part MultipartBody.Part foto
    );

    @POST("assinaturas/checkout")
    Call<Map<String, String>> contratarPlano(@Body Map<String, String> payload);

    @GET("petshops")
    Call<List<PetShop>> listarPetShops();

    @POST("agendamentos")
    Call<Agendamento> criarAgendamento(@Body Agendamento agendamento);

    @GET("tutores/{tutorId}/agendamentos")
    Call<List<Agendamento>> listarAgendamentosAtivos(@Path("tutorId") String tutorId);

    @GET("checkin/totp/{agendamentoId}")
    Call<Map<String, String>> obterTokenTotp(@Path("agendamentoId") String agendamentoId);
}