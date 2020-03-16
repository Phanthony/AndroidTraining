package com.example.androidtraining.recyclerview

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.androidtraining.R
import com.example.androidtraining.database.GitHubRepo
import kotlinx.android.synthetic.main.repolayout.view.*


class RecyclerViewRepoAdapter(private val repoList: ArrayList<GitHubRepo>,
                              private val context: Context) : RecyclerView.Adapter<RecyclerViewRepoAdapter.ViewHolder>(){

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val repoName = itemView.RepoNameAuthor!!
        val repoDesc = itemView.RepoDescription!!
        val repoStars = itemView.RepoStars!!
        val repoImage = itemView.RepoImage!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.repolayout,parent,false)
        return ViewHolder(
            layout
        )
    }

    override fun getItemCount() = repoList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentRepo = repoList[position]
        val textToBeRepoStars = when(currentRepo.stargazers_count){
            1 -> context.getString(R.string.dailyStars).format("${currentRepo.stargazers_count}","")
            else ->  context.getString(R.string.dailyStars).format("${currentRepo.stargazers_count}","s")
        }
        val textToBeRepoDesc = when(currentRepo.description){
            null -> context.getString(R.string.noDescAvail)
            else -> currentRepo.description
        }
        holder.repoStars.text = textToBeRepoStars
        holder.repoName.text = context.getString(R.string.authorAndRepoName).format(currentRepo.owner.login,currentRepo.name)
        holder.repoDesc.text = textToBeRepoDesc
        Glide.with(context).load(currentRepo.owner.avatar_url).into(holder.repoImage)
    }

    fun clear(){
        repoList.clear()
        Log.i("Update","Cleared Data Set")
        notifyDataSetChanged()
    }

    fun add(item: GitHubRepo){
        repoList.add(item)
        Log.i("Update","Updated Data Set")
        notifyDataSetChanged()
    }

    fun addAll(list:List<GitHubRepo>?) {
        Log.i("Update","Updated Data Set")
        if (list != null) {
            repoList.addAll(list)
            notifyDataSetChanged()
        }
    }
}