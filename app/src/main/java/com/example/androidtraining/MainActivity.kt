package com.example.androidtraining

import android.os.Bundle
import android.os.Handler
import android.util.Log
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

    private lateinit var informationToast: Toast
    private lateinit var repoSwipeRefresh: SwipeRefreshLayout
    lateinit var lastRefreshed: String
    lateinit var currentTime: String
    var timeFormat = SimpleDateFormat("k:m")
    val adapter = RecyclerViewAdapter(arrayListOf(),this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        repoSwipeRefresh = RecycleViewSwipeRefresh
        informationToast = Toast.makeText(this,"Fetching Repos",Toast.LENGTH_LONG)
        informationToast.show()

        ToolBar.title = "Daily Trending Kotlin Repos"
        setSupportActionBar(ToolBar)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://github-trending-api.now.sh/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        val service = retrofit.create(GitHubApi::class.java)
        val result = service.getRepo()

        RepoList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        RepoList.adapter = adapter

        callRepos(result)
        lastRefreshed = getTime()

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
    fun updateLayout(){
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
                        if (i.currentPeriodStars>0) {
                            adapter.add(i)
                        }
                    }
                    adapter.notifyDataSetChanged()
                    updateLayout()
                }
            }

        })
    }

    fun getTime() = timeFormat.format(Date(System.currentTimeMillis()))!!

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
