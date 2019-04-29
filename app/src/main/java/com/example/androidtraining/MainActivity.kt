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
            .baseUrl("https://github-trending-api.now.sh/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        val service = retrofit.create(GitHubApi::class.java)
        val result = service.getRepo()

        result.enqueue(object : Callback<List<GitHubRepo>>{
            override fun onFailure(call: Call<List<GitHubRepo>>, t: Throwable) {
                Log.e("Error","",t)
            }

            override fun onResponse(call: Call<List<GitHubRepo>>, response: Response<List<GitHubRepo>>) {
                if(response.body() != null){
                    val test = response.body()!!
                    for (i in test) {
                        Log.i("GitHub Repo", "${i.name} by ${i.author}. It has ${i.currentPeriodStars} stars today.")
                    }
                }
            }

        })

    }

    fun displayName(){
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
