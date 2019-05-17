package com.example.androidtraining

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.repolayout.view.*
import kotlin.collections.ArrayList


class RecyclerViewAdapter(private val repoList: ArrayList<GitHubRepo>,
                          private val context: Context) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>(){

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val repoName = itemView.RepoNameAuthor!!
        val repoDesc = itemView.RepoDescription!!
        val repoStars = itemView.RepoStars!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewAdapter.ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.repolayout,parent,false)
        return ViewHolder(layout)
    }

    override fun getItemCount() = repoList.size

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        val currentRepo = repoList[position]
        val textToBe = when(currentRepo.getStargazers_count()){
            1 -> context.getString(R.string.dailyStars).format("${currentRepo.getStargazers_count()}","")
            else ->  context.getString(R.string.dailyStars).format("${currentRepo.getStargazers_count()}","s")
        }
        holder.repoStars.text = textToBe
        holder.repoName.text = context.getString(R.string.authorAndRepoName).format(currentRepo.getOwner().login,currentRepo.getName())
        holder.repoDesc.text = currentRepo.getDescription()
    }

    fun clear(){
        repoList.clear()
        Log.i("Update","Cleared Data Set")
        notifyDataSetChanged()
    }

    fun add(item:GitHubRepo){
        repoList.add(item)
        Log.i("Update","Updated Data Set")
        notifyDataSetChanged()
    }

    fun addAll(list:List<GitHubRepo>?) {
        if (list != null) {
            repoList.addAll(list)
            notifyDataSetChanged()
        }
    }
}