package com.example.androidtraining

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var informationToast: Toast
    private lateinit var repoSwipeRefresh: SwipeRefreshLayout
    lateinit var currentTime: String
    var lastRefreshed = getTime()
    lateinit var timeFormat: DateTimeFormatter
    val adapter = RecyclerViewAdapter(arrayListOf(),this)
    lateinit var dateFormat: DateTimeFormatter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        //timeFormat = DateTimeFormatter.ofPattern("k:m")

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
            .baseUrl("https://api.github.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        val service = retrofit.create(GitHubApi::class.java)
        val result = service.getRepo("created:%3E${getYesterday()}+language:kotlin+stars:%3E0")
        Log.i("Qis","created:>${getYesterday()}+language:kotlin+stars:>0")

        //Get the repos and update the refresh time
        callRepos(result)

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
            val timePassed = timePassed(lastRefreshed,getTime())
            TextViewRefreshTime.text = getString(R.string.minutesPassedSinceRefresh).format("$timePassed","s")
            lastRefreshed = getTime()

        }

    }

    private fun callRepos(call:Call<GitHubRepoList>){
        call.clone().enqueue(object : Callback<GitHubRepoList>{
            override fun onFailure(call: Call<GitHubRepoList>, t: Throwable) {
                Log.e("Network Error","",t)
                val dialog = networkDialog(this@MainActivity)
                dialog.show()
                repoSwipeRefresh.isRefreshing = false
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

    fun getTime() = (DateTimeFormatter.ofPattern("k:m")
        .withLocale(Locale.US)
        .withZone(ZoneId.systemDefault()))
        .format(Instant.now())

    private fun getYesterday():String{
        val test=
        (DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withLocale(Locale.US)
            .withZone(ZoneId.systemDefault()))
            .format(Instant.now().minus(Duration.ofDays(1)))
        return test
    }


    fun timePassed(initialTime:String, currentTime:String): Int{
        val initialTimeList = initialTime.split(":")
        val currentTimeList = currentTime.split(":")
        var minutesPassed = 0

        val hoursPassed = currentTimeList[0].toInt() - initialTimeList[0].toInt()
        minutesPassed += 60*hoursPassed
        minutesPassed += currentTimeList[1].toInt() - initialTimeList[1].toInt()
        return minutesPassed
    }

    fun networkDialog(context: Context) : AlertDialog.Builder{
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Error")
        builder.setMessage("A network failure occurred")
        builder.setPositiveButton("Ok") { _: DialogInterface?, _: Int ->
        }
        return builder
    }


}
