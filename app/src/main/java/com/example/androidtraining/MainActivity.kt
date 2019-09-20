package com.example.androidtraining

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var gitHubViewModel: GitHubViewModelDependencies

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //set up toast to display information
        /*informationToast = Toast.makeText(this@MainActivity, getString(R.string.fetchRepos), Toast.LENGTH_SHORT)*/
        //set up ViewModel
        gitHubViewModel = ViewModelProviders.of(this).get(GitHubViewModelDependencies::class.java)
        //set up observers
        /*gitHubViewModel.getRepoObservable().observe(this, Observer<OnlineCacheState<List<GitHubRepo>>> { cacheStatus ->
            cacheStatus.apply {
                whenNoCache { isFetching, errorDuringFetch ->
                    if (!isFetching) {
                        informationToast.cancel()
                        if (errorDuringFetch != null) {
                            informationToast.cancel()
                            repoSwipeRefresh.isRefreshing = false
                            networkDialog(this@MainActivity, errorDuringFetch.message).show()
                        }
                    } else {
                        informationToast.show()
                    }
                }
                whenCache { cache, lastSuccessfulFetch, isFetching, justSuccessfullyFetched, errorDuringFetch ->
                    if (errorDuringFetch != null) {
                        informationToast.cancel()
                        repoSwipeRefresh.isRefreshing = false
                        networkDialog(this@MainActivity, errorDuringFetch.message).show()
                    } else {
                        if (cache != null) {
                            adapter.clear()
                            adapter.addAll(cache)
                        }
                        if (justSuccessfullyFetched) {
                            informationToast.cancel()
                            repoSwipeRefresh.isRefreshing = false
                            lastTime = lastSuccessfulFetch.time
                        }
                    }
                }
            }
        })*/

        //Set up Toolbar
        ToolBar.title = getString(R.string.trending)
        ToolBar.setTitleTextColor(android.graphics.Color.WHITE)
        setSupportActionBar(ToolBar)

        //Set up Navigation Host and Controller
        val host: NavHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment? ?: return
        val navController = host.navController

        //set up bottom navigation
        setupBottomNav(navController)

        //Set up The RecycleView with Swipe Refresh
        /*repoSwipeRefresh = RecycleViewSwipeRefresh
        RepoList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        RepoList.adapter = adapter*/

        //Set up the Refresh Listener to update the recycle view
        /*repoSwipeRefresh.setOnRefreshListener {
            informationToast.show()
            gitHubViewModel.userRefresh()
        }*/
    }

    private fun setupBottomNav(controller : NavController){
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.setupWithNavController(controller)
    }

    override fun onStop() {
        super.onStop()
        gitHubViewModel.getComposite().clear()
    }

}
