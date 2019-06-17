package com.example.androidtraining

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.text.format.DateUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.levibostian.teller.cachestate.OnlineCacheState
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var gitHubViewModel: GitHubViewModelDependencies
    private lateinit var informationToast: Toast
    private lateinit var repoSwipeRefresh: SwipeRefreshLayout
    private val adapter = RecyclerViewAdapter(arrayListOf(), this)
    private var lastTime = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //set up last updated time
        timeHandler().run()
        //set up toast to display information
        informationToast = Toast.makeText(this@MainActivity, getString(R.string.fetchRepos), Toast.LENGTH_LONG)
        //set up ViewModel
        gitHubViewModel = ViewModelProviders.of(this).get(GitHubViewModelDependencies::class.java)
        //set up observers
        gitHubViewModel.getRepoObservable().observe(this, Observer<OnlineCacheState<List<GitHubRepo>>> { cacheStatus ->
            cacheStatus.apply {
                whenNoCache { isFetching, errorDuringFetch ->
                    gitHubViewModel.clearDB()
                    if(!isFetching){
                        if(errorDuringFetch != null){
                            informationToast.cancel()
                            repoSwipeRefresh.isRefreshing = false
                            networkDialog(this@MainActivity,errorDuringFetch.message).show()
                        }
                    }
                }
                whenCache { cache, lastSuccessfulFetch, isFetching, justSuccessfullyFetched, errorDuringFetch ->
                    if (!isFetching) {
                        if (errorDuringFetch != null) {
                            informationToast.cancel()
                            repoSwipeRefresh.isRefreshing = false
                            networkDialog(this@MainActivity,errorDuringFetch.message).show()
                        } else {
                            if (cache != null) {
                                adapter.clear()
                                adapter.addAll(cache)
                            }
                            if(justSuccessfullyFetched) {
                                informationToast.cancel()
                                repoSwipeRefresh.isRefreshing = false
                                lastTime = lastSuccessfulFetch.time
                            }
                        }
                    }
                }
            }
        })

        //Set up Toolbar
        ToolBar.title = getString(R.string.trending)
        ToolBar.setTitleTextColor(android.graphics.Color.WHITE)
        setSupportActionBar(ToolBar)

        //Set up The RecycleView with Swipe Refresh
        repoSwipeRefresh = RecycleViewSwipeRefresh
        RepoList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        RepoList.adapter = adapter

        //Set up the Refresh Listener to update the recycle view
        repoSwipeRefresh.setOnRefreshListener {
            informationToast.show()
            gitHubViewModel.userRefresh()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        gitHubViewModel.getComposite().clear()
    }

    private fun networkDialog(context: Context, errorMessage: String?): AlertDialog.Builder {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.error))
        val message = when(errorMessage){
            null -> {getString(R.string.genericNetworkError)}
            else -> errorMessage
        }
        builder.setMessage(message)
        builder.setPositiveButton(getString(R.string.Ok)) { _: DialogInterface?, _: Int ->
        }
        return builder
    }

    private fun timeHandler(): java.lang.Runnable {
        val timeHandler = Handler()
        return object : Runnable {
            override fun run() {
                val lastUpdated = DateUtils.getRelativeTimeSpanString(lastTime,System.currentTimeMillis(),DateUtils.SECOND_IN_MILLIS)
                TextViewRefreshTime.text = getString(R.string.minutesPassedSinceRefresh).format("$lastUpdated")
                timeHandler.postDelayed(this,100)
            }
        }
    }
}
