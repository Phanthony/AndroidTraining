package com.example.androidtraining

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.repolayout.view.*
import kotlin.collections.ArrayList


class RecyclerViewAdapter(private val repoList: ArrayList<GitHubRepo>,
                          private val context: Context) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>(){

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val repoName = itemView.RepoNameAuthor!!
        val repoDesc = itemView.RepoDescription!!
        val repoStars = itemView.RepoStars!!
        val repoImage = itemView.RepoImage!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewAdapter.ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.repolayout,parent,false)
        return ViewHolder(layout)
    }

    override fun getItemCount() = repoList.size

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        val currentRepo = repoList[position]
        val textToBeRepoStars = when(currentRepo.getStargazers_count()){
            1 -> context.getString(R.string.dailyStars).format("${currentRepo.getStargazers_count()}","")
            else ->  context.getString(R.string.dailyStars).format("${currentRepo.getStargazers_count()}","s")
        }
        val textToBeRepoDesc = when(currentRepo.getDescription()){
            null -> context.getString(R.string.noDescAvail)
            else -> currentRepo.getDescription()
        }
        holder.repoStars.text = textToBeRepoStars
        holder.repoName.text = context.getString(R.string.authorAndRepoName).format(currentRepo.getOwner().login,currentRepo.getName())
        holder.repoDesc.text = textToBeRepoDesc
        Glide.with(context).load(currentRepo.getOwner().avatar_url).into(holder.repoImage)
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
        Log.i("Update","Updated Data Set")
        if (list != null) {
            repoList.addAll(list)
            notifyDataSetChanged()
        }
    }
}