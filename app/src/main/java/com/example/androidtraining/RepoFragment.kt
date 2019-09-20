package com.example.androidtraining

import android.os.Bundle
import android.os.Handler
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.androidtraining.extension.getErrorDialog
import com.levibostian.teller.cachestate.OnlineCacheState
import kotlinx.coroutines.*
import java.lang.Runnable

class RepoFragment : Fragment() {

    private var lastTime: Long? = null
    private var timeCycle = true
    lateinit var adapter: RecyclerViewAdapter
    lateinit var coTimer : CoroutineScope

    override fun onCreate(savedInstanceState: Bundle?) {
        adapter = RecyclerViewAdapter(arrayListOf(), activity!!)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.repo_fragment_layout, container, false)

        val repoList = view.findViewById<RecyclerView>(R.id.RepoList)
        val informationToast = Toast.makeText(activity, getString(R.string.fetchRepos), Toast.LENGTH_SHORT)

        val gitHubViewModel = activity!!.run {
            ViewModelProviders.of(this)[GitHubViewModelDependencies::class.java]
        }

        //Set up The RecycleView with Swipe Refresh
        val repoSwipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.RecycleViewSwipeRefresh)
        repoList.layoutManager = LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)
        repoList.adapter = adapter


        repoSwipeRefresh.setOnRefreshListener {
            informationToast.show()
            gitHubViewModel.userRefresh()
        }

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

                        }
                        else -> {
                            // update shown cache
                            adapter.clear()
                            adapter.addAll(cache)
                        }
                    }
                    if (justSuccessfullyFetched) {
                        informationToast.cancel()
                        repoSwipeRefresh.isRefreshing = false
                        lastTime = lastSuccessfulFetch.time
                    }
                }
            }
        })

        gitHubViewModel.userRefresh()

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