package com.example.androidtraining

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.*
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var informationToast: Toast
    private lateinit var repoSwipeRefresh: SwipeRefreshLayout
    lateinit var lastRefreshed: String
    lateinit var currentTime: String
    var timeFormat = SimpleDateFormat("k:m", Locale.US)
    val adapter = RecyclerViewAdapter(arrayListOf(),this)
    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-mm-dd")!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Set up The RecycleView with Swipe Refresh
        repoSwipeRefresh = RecycleViewSwipeRefresh
        informationToast = Toast.makeText(this,"Fetching Repos",Toast.LENGTH_LONG)
        informationToast.show()
        RepoList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        RepoList.adapter = adapter

        //Set up Toolbar
        ToolBar.title = "Daily Trending Kotlin Repos"
        setSupportActionBar(ToolBar)

        //Set up retrofit to call the github API
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        val service = retrofit.create(GitHubApi::class.java)
        val result = service.getRepo(getYesterday())

        //Get the repos and update the refresh time
        callRepos(result)
        lastRefreshed = getTime()

        //Set up handler to auto update the refresh time every minute
        val timeHandler = Handler()
        val timeRunnable = object : Runnable {
            override fun run(){
                currentTime = getTime()
                val minutesPassed = timePassed(lastRefreshed,getTime())
                val textToBe:String
                textToBe = when(minutesPassed){
                    1 -> getString(R.string.minutesPassedSinceRefresh).format("$minutesPassed","")
                    else -> getString(R.string.minutesPassedSinceRefresh).format("$minutesPassed","s")
                }
                TextViewRefreshTime.text = textToBe
                timeHandler.postDelayed(this,60000)
            }
        }
        timeRunnable.run()

        //Set up the Refresh Listener to update the recycle view
        repoSwipeRefresh.setOnRefreshListener {
            informationToast = Toast.makeText(this@MainActivity,"Fetching Repos",Toast.LENGTH_LONG)
            informationToast.show()
            adapter.clear()
            adapter.notifyDataSetChanged()
            callRepos(result)
            TextViewRefreshTime.text = getString(R.string.minutesPassedSinceRefresh).format("0","s")
            lastRefreshed = getTime()

        }

    }

    private fun callRepos(call:Call<GitHubRepoList>){
        call.clone().enqueue(object : Callback<GitHubRepoList>{
            override fun onFailure(call: Call<GitHubRepoList>, t: Throwable) {
                Log.e("Error","",t)
            }

            override fun onResponse(call: Call<GitHubRepoList>, response: Response<GitHubRepoList>) {
                if(response.body() != null){
                    val test = response.body()!!
                    for (i in test.items) {
                        Log.i("GitHub Repo", "${i.name} by ${i.owner.login}. It has gotten ${i.stargazers_count} stars recently.")
                            adapter.add(i)
                    }
                    adapter.notifyDataSetChanged()
                    informationToast.cancel()
                    repoSwipeRefresh.isRefreshing = false
                }
            }

        })
    }

    fun getTime() = timeFormat.format(Date(System.currentTimeMillis()))!!

    private fun getYesterday() = LocalDate.now().minusDays(1).format(dateFormat)!!

    fun timePassed(initialTime:String, currentTime:String): Int{
        val initialTimeList = initialTime.split(":")
        val currentTimeList = currentTime.split(":")
        var minutesPassed = 0

        val hoursPassed = currentTimeList[0].toInt() - initialTimeList[0].toInt()
        minutesPassed += 60*hoursPassed
        minutesPassed += currentTimeList[1].toInt() - initialTimeList[1].toInt()
        return minutesPassed
    }


}
