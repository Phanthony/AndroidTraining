package com.example.androidtraining.ui

import android.content.Context
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
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

        val issueLayout = view.findViewById<LinearLayout>(R.id.issueLayout)
        val nothingToShow = view.findViewById<LinearLayout>(R.id.nothing_to_show)

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
                            nothingToShow.visibility = View.INVISIBLE
                            issueLayout.visibility = View.VISIBLE
                            val nav = this@IssuesFragment.activity!!.findNavController(R.id.nav_host_fragment)
                            // update shown cache
                            lastTime = lastSuccessfulFetch.time
                            issueAdapter.clear()
                            val newList = cache.map { issue ->
                                Pair(issue,View.OnClickListener{
                                    val user = activity!!.getSharedPreferences("github", Context.MODE_PRIVATE).getString("user","null")
                                    gitHubViewModel.changeIssueComment(issue.number,issue.repository.getName(),user!!,issue.id)
                                    nav.navigate(R.id.issue_comment_dest)
                                })
                            }
                            issueAdapter.addAll(newList)
                        }
                    }
                    if (justSuccessfullyFetched) {
                        if (cacheExistsAndEmpty){
                            nothingToShow.visibility = View.VISIBLE
                            issueLayout.visibility = View.INVISIBLE
                        }
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