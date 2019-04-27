package com.example.androidtraining

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.*

import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/v2/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        val service = retrofit.create(PokemonApi::class.java)
        val result = service.getPokemon("charizard")

        result.enqueue(object : Callback<PokemonData>{
            override fun onFailure(call: Call<PokemonData>, t: Throwable) {
                print(t.message)
            }

            override fun onResponse(call: Call<PokemonData>, response: Response<PokemonData>) {
                if(response.body() != null){
                    val test = response.body()!!
                    Log.i("Pokemon Information","NAME = ${test.name}, ID = ${test.id}, WEIGHT = ${test.weight}")
                }
            }

        })

    }

    fun displayName(view: View){
        var firstName = ""
        if (UserNameEditText.text != null){
            firstName = UserNameEditText.text.toString()
        }
        if (firstName != ""){
            nameDisplayTextView.text = "It's nice to have you."
            WelcomeTextView.text = "Welcome, $firstName!"
        }
        else{
            nameDisplayTextView.text = "What is your name?"
            WelcomeTextView.text = "Welcome!"
        }
    }



}
