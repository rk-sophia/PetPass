package com.petpass.tutor.data.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ViaCepService {
    @GET("{cep}/json/")
    Call<EnderecoResponse> buscarCEP(@Path("cep") String cep);
}
