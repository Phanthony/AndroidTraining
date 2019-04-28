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
            .baseUrl("https://api.github.com")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        val service = retrofit.create(GitHubApi::class.java)
        val result = service.getRepo()

        result.enqueue(object : Callback<GitHubRepoList>{
            override fun onFailure(call: Call<GitHubRepoList>, t: Throwable) {
                print(t.message)
            }

            override fun onResponse(call: Call<GitHubRepoList>, response: Response<GitHubRepoList>) {
                if(response.body() != null){
                    val test = response.body()!!
                    for (i in test.items) {
                        Log.i("GitHub Repo", "NAME = ${i.name}, STARS = ${i.stargazers_count}")
                    }
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
