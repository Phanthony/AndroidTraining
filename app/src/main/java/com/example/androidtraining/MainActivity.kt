package com.example.androidtraining

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.*
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : AppCompatActivity() {

    lateinit var informationToast: Toast
    lateinit var repoSwipeRefresh: SwipeRefreshLayout
    var repoList = arrayListOf<GitHubRepo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        repoSwipeRefresh = RecycleViewSwipeRefresh
        informationToast = Toast.makeText(this,"Fetching Repos",Toast.LENGTH_LONG)
        informationToast.show()

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
                        Log.i("GitHub Repo", "${i.name} by ${i.author}. It has gotten ${i.currentPeriodStars} stars recently.")
                        repoList.add(i)
                    }
                    updateLayout(repoList)
                }
            }

        })

        repoSwipeRefresh.setOnRefreshListener {
            informationToast = Toast.makeText(this@MainActivity,"Fetching Repos",Toast.LENGTH_LONG)
            informationToast.show()
            repoList.clear()
            callRepos(result)
        }


    }
/*
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
    */

    fun updateLayout(repoList: ArrayList<GitHubRepo>){
        val recyclerView = RepoList!!
        val adapter = RecyclerViewAdapter(repoList)
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter
        informationToast.cancel()
        repoSwipeRefresh.isRefreshing = false
    }

    fun callRepos(call:Call<List<GitHubRepo>>){
        call.clone().enqueue(object : Callback<List<GitHubRepo>>{
            override fun onFailure(call: Call<List<GitHubRepo>>, t: Throwable) {
                Log.e("Error","",t)
            }

            override fun onResponse(call: Call<List<GitHubRepo>>, response: Response<List<GitHubRepo>>) {
                if(response.body() != null){
                    val test = response.body()!!
                    for (i in test) {
                        Log.i("GitHub Repo", "${i.name} by ${i.author}. It has gotten ${i.currentPeriodStars} stars recently.")
                        repoList.add(i)
                    }
                    updateLayout(repoList)
                }
            }

        })
    }



}
