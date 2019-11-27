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
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.androidtraining.GitHubViewModelDependencies
import com.example.androidtraining.R
import com.example.androidtraining.RecyclerViewIssueCommentAdapter
import com.example.androidtraining.extension.getErrorDialog
import com.example.androidtraining.service.GitHubIssueComment
import com.levibostian.teller.cachestate.OnlineCacheState
import kotlinx.coroutines.*

class IssueCommentFragment: Fragment() {
    private var lastTime: Long? = null
    private var timeCycle = true
    lateinit var coTimer : CoroutineScope
    lateinit var adapter : RecyclerViewIssueCommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        adapter = RecyclerViewIssueCommentAdapter(arrayListOf(),this.context!!)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.issuecomment_fragment_layout,container,false)

        val commentList = view.findViewById<RecyclerView>(R.id.IssueCommentList)
        val informationToast = Toast.makeText(activity, getString(R.string.fetchIssues), Toast.LENGTH_SHORT)

        val gitHubViewModel = activity!!.run {
            ViewModelProviders.of(this)[GitHubViewModelDependencies::class.java]
        }

        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.IssueCommentRecycleViewSwipeRefresh)

        commentList.layoutManager = LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)
        commentList.adapter = adapter

        swipeRefresh.setOnRefreshListener {
            informationToast.show()
            gitHubViewModel.userCommentsRefresh()
        }

        gitHubViewModel.getIssueCommentObservable().observe(this, Observer<OnlineCacheState<PagedList<GitHubIssueComment>>> { cacheStatus ->
            cacheStatus.apply {
                whenNoCache { isFetching, errorDuringFetch ->
                    if (errorDuringFetch != null) {
                        informationToast.cancel()
                        swipeRefresh.isRefreshing = false
                        this@IssueCommentFragment.getErrorDialog(errorDuringFetch.message!!, this@IssueCommentFragment.context!!).show()
                    }
                }
                whenCache { cache, lastSuccessfulFetch, isFetching, justSuccessfullyFetched, errorDuringFetch ->
                    if (errorDuringFetch != null) {
                        lastTime = lastSuccessfulFetch.time
                        informationToast.cancel()
                        swipeRefresh.isRefreshing = false
                        this@IssueCommentFragment.getErrorDialog(errorDuringFetch.message!!, this@IssueCommentFragment.context!!).show()
                    }
                    when (cache) {
                        null -> {
                            //empty cache nothing to show
                            lastTime = lastSuccessfulFetch.time
                        }
                        else -> {
                            // update shown cache
                            lastTime = lastSuccessfulFetch.time
                            adapter.clear()
                            adapter.addAll(cache)
                        }
                    }
                    if (justSuccessfullyFetched) {
                        informationToast.cancel()
                        swipeRefresh.isRefreshing = false
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

    private fun timeHandler(textView: TextView?) {
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