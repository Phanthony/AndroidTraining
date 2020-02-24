package com.example.androidtraining.ui

import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.androidtraining.GitHubRepo
import com.example.androidtraining.GitHubViewModel
import com.example.androidtraining.R
import com.example.androidtraining.RecyclerViewRepoAdapter
import com.example.androidtraining.extension.getErrorDialog
import com.levibostian.teller.cachestate.OnlineCacheState
import kotlinx.coroutines.*
import javax.inject.Inject

class RepoFragment : Fragment() {

    private var lastTime: Long? = null
    private var timeCycle = true
    lateinit var repoAdapter: RecyclerViewRepoAdapter
    lateinit var coTimer : CoroutineScope
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory


    override fun onCreate(savedInstanceState: Bundle?) {
        repoAdapter = RecyclerViewRepoAdapter(arrayListOf(), activity!!)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.repo_fragment_layout, container, false)

        val repoList = view.findViewById<RecyclerView>(R.id.RepoList)
        val informationToast = Toast.makeText(activity, getString(R.string.fetchRepos), Toast.LENGTH_SHORT)

        val gitHubViewModel by viewModels<GitHubViewModel> { viewModelFactory }

        //Set up The RecycleView with Swipe Refresh
        val repoSwipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.RecycleViewSwipeRefresh)
        repoList.layoutManager = LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)
        repoList.adapter = repoAdapter


        repoSwipeRefresh.setOnRefreshListener {
            informationToast.show()
            gitHubViewModel.userRepoRefresh()
        }

        val repoLayout = view.findViewById<LinearLayout>(R.id.repoLayout)
        val nothingToShow = view.findViewById<LinearLayout>(R.id.nothing_to_show)

        gitHubViewModel.getRepoObservable().observe(this, Observer<OnlineCacheState<List<GitHubRepo>>> { cacheStatus ->
            cacheStatus.apply {
                whenNoCache { isFetching, errorDuringFetch ->
                    if (errorDuringFetch != null) {
                        informationToast.cancel()
                        repoSwipeRefresh.isRefreshing = false
                        this@RepoFragment.getErrorDialog(errorDuringFetch.message!!, this@RepoFragment.context!!).show()
                    }
                }
                whenCache { cache, lastSuccessfulFetch, isFetching, justSuccessfullyFetched, errorDuringFetch ->
                    if (errorDuringFetch != null) {
                        lastTime = lastSuccessfulFetch.time
                        informationToast.cancel()
                        repoSwipeRefresh.isRefreshing = false
                        this@RepoFragment.getErrorDialog(errorDuringFetch.message!!, this@RepoFragment.context!!).show()
                    }
                    when (cache) {
                        null -> {
                            //empty cache nothing to show
                            lastTime = lastSuccessfulFetch.time
                        }
                        else -> {
                            // update shown cache
                            nothingToShow.visibility = View.INVISIBLE
                            repoLayout.visibility = View.VISIBLE
                            lastTime = lastSuccessfulFetch.time
                            repoAdapter.clear()
                            repoAdapter.addAll(cache)
                        }
                    }
                    if (justSuccessfullyFetched) {
                        if (cacheExistsAndEmpty){
                            nothingToShow.visibility = View.VISIBLE
                            repoLayout.visibility = View.INVISIBLE
                        }
                        informationToast.cancel()
                        repoSwipeRefresh.isRefreshing = false
                        lastTime = lastSuccessfulFetch.time
                    }
                }
            }
        })

        //Set up runnable for the refresh time.
        val lastRefresh = view.findViewById<TextView>(R.id.LastRefreshTime)
        timeHandler(lastRefresh)

        return view
    }

    override fun onDestroyView() {
        coTimer.cancel()
        super.onDestroyView()
    }

    fun timeHandler(textView: TextView?) {
        coTimer = CoroutineScope(Dispatchers.IO)
        coTimer.launch {
            while (timeCycle) {
                if (lastTime != null) {
                    val lastUpdated = DateUtils.getRelativeTimeSpanString(
                        lastTime!!,
                        System.currentTimeMillis(),
                        DateUtils.SECOND_IN_MILLIS
                    )
                    textView?.text = getString(R.string.minutesPassedSinceRefresh).format("$lastUpdated")
                }
                delay(1000)
            }
        }
    }

}