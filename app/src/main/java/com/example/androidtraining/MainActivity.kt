package com.example.androidtraining

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var gitHubViewModel: GitHubViewModel
    private lateinit var informationToast: Toast
    private lateinit var repoSwipeRefresh: SwipeRefreshLayout
    private val adapter = RecyclerViewAdapter(arrayListOf(), this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //set up toast to display information
        informationToast = Toast.makeText(this@MainActivity, "Fetching Repos", Toast.LENGTH_LONG)
        //set up ViewModel
        gitHubViewModel = ViewModelProviders.of(this).get(GitHubViewModel::class.java)
        //set up observers
        gitHubViewModel.getRepoList().observe(this, Observer<List<GitHubRepo>> { t ->
            if(t != null) {
                adapter.clear()
                adapter.addAll(t)
            }
        })
        gitHubViewModel.getNetworkError().observe(this, Observer<Int> { errorCode ->
            if (errorCode == 1){
                Log.e("Error","Network Call Unsuccessful")
                networkDialog(this@MainActivity).show()
                informationToast.cancel()
                repoSwipeRefresh.isRefreshing = false
            }
            else if (errorCode == 2){
                Log.i("Update","Network Call Successful")
                TextViewRefreshTime.text = getString(R.string.minutesPassedSinceRefresh).format("0", "s")
                informationToast.cancel()
                repoSwipeRefresh.isRefreshing = false
            }


        })
        gitHubViewModel.getMinSinceLastRefresh().observe(this, Observer<Int> { t ->
            val textToBe: String = when (t) {
                1 -> getString(R.string.minutesPassedSinceRefresh).format("$t", "")
                else -> getString(R.string.minutesPassedSinceRefresh).format("$t", "s")
            }
            TextViewRefreshTime.text = textToBe
        })

        //Set up Toolbar
        ToolBar.title = "Daily Trending Kotlin Repos"
        setSupportActionBar(ToolBar)

        //Set up The RecycleView with Swipe Refresh
        repoSwipeRefresh = RecycleViewSwipeRefresh
        RepoList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        RepoList.adapter = adapter

        //Set up the Refresh Listener to update the recycle view
        repoSwipeRefresh.setOnRefreshListener {
            informationToast.show()
            gitHubViewModel.getRepos()
        }
    }

    private fun networkDialog(context: Context): AlertDialog.Builder {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Error")
        builder.setMessage("A network failure occurred")
        builder.setPositiveButton("Ok") { _: DialogInterface?, _: Int ->
        }
        return builder
    }
}
