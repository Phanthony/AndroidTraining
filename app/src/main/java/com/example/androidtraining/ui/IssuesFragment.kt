package com.example.androidtraining.ui

import android.os.Bundle
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
import com.example.androidtraining.GitHubViewModelDependencies
import com.example.androidtraining.R
import com.example.androidtraining.RecyclerViewIssueAdapter
import com.example.androidtraining.extension.getErrorDialog
import com.example.androidtraining.service.GitHubIssue
import com.levibostian.teller.cachestate.OnlineCacheState
import kotlinx.coroutines.*

class IssuesFragment : Fragment() {

    private var lastTime: Long? = null
    private var timeCycle = true
    lateinit var coTimer : CoroutineScope
    lateinit var issueAdapter : RecyclerViewIssueAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        issueAdapter = RecyclerViewIssueAdapter(arrayListOf(),activity!!)
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.issue_fragment_layout,container,false)

        val issueList = view.findViewById<RecyclerView>(R.id.IssueList)
        val informationToast = Toast.makeText(activity, getString(R.string.fetchIssues), Toast.LENGTH_SHORT)

        val gitHubViewModel = activity!!.run {
            ViewModelProviders.of(this)[GitHubViewModelDependencies::class.java]
        }

        val issueSwipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.RecycleViewIssueSwipeRefresh)
        issueList.layoutManager = LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)
        issueList.adapter = issueAdapter

        issueSwipeRefresh.setOnRefreshListener {
            informationToast.show()
            gitHubViewModel.userIssueRefresh()
        }

        gitHubViewModel.getIssueObservable().observe(this, Observer<OnlineCacheState<List<GitHubIssue>>> { cacheStatus ->
            cacheStatus.apply {
                whenNoCache { isFetching, errorDuringFetch ->
                    if (errorDuringFetch != null) {
                        informationToast.cancel()
                        issueSwipeRefresh.isRefreshing = false
                        this@IssuesFragment.getErrorDialog(errorDuringFetch.message!!, this@IssuesFragment.context!!).show()
                    }
                }
                whenCache { cache, lastSuccessfulFetch, isFetching, justSuccessfullyFetched, errorDuringFetch ->
                    if (errorDuringFetch != null) {
                        lastTime = lastSuccessfulFetch.time
                        informationToast.cancel()
                        issueSwipeRefresh.isRefreshing = false
                        this@IssuesFragment.getErrorDialog(errorDuringFetch.message!!, this@IssuesFragment.context!!).show()
                    }
                    when (cache) {
                        null -> {
                            //empty cache nothing to show
                            lastTime = lastSuccessfulFetch.time
                        }
                        else -> {
                            // update shown cache
                            lastTime = lastSuccessfulFetch.time
                            issueAdapter.clear()
                            issueAdapter.addAll(cache)
                        }
                    }
                    if (justSuccessfullyFetched) {
                        informationToast.cancel()
                        issueSwipeRefresh.isRefreshing = false
                        lastTime = lastSuccessfulFetch.time
                    }
                }
            }
        })

        //Set up runnable for the refresh time.
        val lastRefresh = view.findViewById<TextView>(R.id.IssueLastRefreshTime)
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