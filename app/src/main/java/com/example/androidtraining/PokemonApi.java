package com.example.androidtraining;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PokemonApi {

    @GET("pokemon/{name}")
    Call<PokemonData> getPokemon(@Query(value = "name") String name);

}
