package com.example.androidtraining

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.*
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var informationToast: Toast
    lateinit var repoSwipeRefresh: SwipeRefreshLayout
    var repoList = arrayListOf<GitHubRepo>()
    lateinit var lastRefreshed: String
    lateinit var currentTime: String
    var timeFormat = SimpleDateFormat("k:m")
    var getTimeChecker = true

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

        lastRefreshed = getTime()

        val timeHandler = Handler()

        val timeRunnable = object : Runnable {
            override fun run(){
                if (getTimeChecker){
                    currentTime = getTime()
                }
                val minutesPassed = timePassed(lastRefreshed,getTime())
                when(minutesPassed){
                    1 -> TextViewRefreshTime.text = "$minutesPassed minute since the last refresh"
                    else -> TextViewRefreshTime.text = "$minutesPassed minutes since the last refresh"
                }
                timeHandler.postDelayed(this,60000)
            }
        }

        timeRunnable.run()

        repoSwipeRefresh.setOnRefreshListener {
            informationToast = Toast.makeText(this@MainActivity,"Fetching Repos",Toast.LENGTH_LONG)
            informationToast.show()
            repoList.clear()
            callRepos(result)
            TextViewRefreshTime.text = "0 minutes since the last refresh"
            lastRefreshed = getTime()

        }

    }
    fun updateLayout(repoList: ArrayList<GitHubRepo>){
        val recyclerView = RepoList!!
        val adapter = RecyclerViewAdapter(repoList)
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter
        informationToast.cancel()
        repoSwipeRefresh.isRefreshing = false
    }

    private fun callRepos(call:Call<List<GitHubRepo>>){
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

    fun getTime() = timeFormat.format(Date(System.currentTimeMillis()))

    fun timePassed(initial:String, current:String): Int{
        val initialList = initial.split(":")
        val currentList = current.split(":")

        var minutesPassed = 0
        val hoursPassed = currentList[0].toInt() - initialList[0].toInt()
        minutesPassed += 60*hoursPassed
        minutesPassed += currentList[1].toInt() - initialList[1].toInt()
        return minutesPassed
    }


}
