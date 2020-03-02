package com.example.androidtraining.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.androidtraining.GitHubViewModel
import com.example.androidtraining.R
import com.example.androidtraining.extension.getErrorDialog
import com.example.androidtraining.extension.onAttachDiGraph
import com.example.androidtraining.recyclerview.RecyclerViewIssueCommentAdapter
import com.example.androidtraining.service.GitHubIssueComment
import com.levibostian.teller.cachestate.OnlineCacheState
import javax.inject.Inject

class IssueCommentFragment: Fragment() {
    lateinit var adapter : RecyclerViewIssueCommentAdapter
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onAttachDiGraph().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        adapter =
            RecyclerViewIssueCommentAdapter(
                arrayListOf(),
                this.context!!
            )
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.issuecomment_fragment_layout,container,false)

        val commentList = view.findViewById<RecyclerView>(R.id.IssueCommentList)
        val informationToast = Toast.makeText(activity, getString(R.string.fetchComments), Toast.LENGTH_SHORT)

        val gitHubViewModel by viewModels<GitHubViewModel> { viewModelFactory }

        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.IssueCommentRecycleViewSwipeRefresh)

        commentList.layoutManager = LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)
        commentList.adapter = adapter

        swipeRefresh.setOnRefreshListener {
            informationToast.show()
            gitHubViewModel.userCommentsRefresh()
        }

        val commentLayout = view.findViewById<LinearLayout>(R.id.issueCommentLayout)
        val nothingToShow = view.findViewById<LinearLayout>(R.id.nothing_to_show)

        gitHubViewModel.getIssueCommentObservable().observe(this.viewLifecycleOwner, Observer<OnlineCacheState<PagedList<GitHubIssueComment>>> { cacheStatus ->
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
                        informationToast.cancel()
                        swipeRefresh.isRefreshing = false
                        this@IssueCommentFragment.getErrorDialog(errorDuringFetch.message!!, this@IssueCommentFragment.context!!).show()
                    }
                    when(cache){
                        null ->{
                            nothingToShow.visibility = View.VISIBLE
                            commentLayout.visibility = View.INVISIBLE
                        }
                        else ->{
                            nothingToShow.visibility = View.INVISIBLE
                            commentLayout.visibility = View.VISIBLE
                            adapter.clear()
                            adapter.addAll(cache)
                        }
                    }
                    if (justSuccessfullyFetched) {
                        if (cacheExistsAndEmpty){
                            nothingToShow.visibility = View.VISIBLE
                            commentLayout.visibility = View.INVISIBLE
                        }
                        informationToast.cancel()
                        swipeRefresh.isRefreshing = false
                    }
                }
            }
        })

        //Set up runnable for the refresh time.
        return view
    }
}