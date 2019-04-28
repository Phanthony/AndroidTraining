package com.example.androidtraining;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PokemonApi {

    @GET("pokemon/{name}")
    Call<PokemonData> getPokemon(@Path("name") String name);

}
