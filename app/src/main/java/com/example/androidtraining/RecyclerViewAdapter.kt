package com.example.androidtraining

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.repolayout.view.*
import java.util.*
import android.content.res.Resources


class RecyclerViewAdapter(private val repoList: ArrayList<GitHubRepo>) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>(){

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val repoName = itemView.RepoNameAuthor!!
        val repoDesc = itemView.RepoDescription!!
        val repoStars = itemView.RepoStars!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewAdapter.ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.repolayout,parent,false)
        return ViewHolder(layout)
    }

    override fun getItemCount(): Int {
        return repoList.size
    }

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        val currentRepo = repoList[position]

        val textToBe = when(currentRepo.currentPeriodStars){
            1 -> Resources.getSystem().getString(R.string.dailyStars).format("${currentRepo.currentPeriodStars}","")
            else -> Resources.getSystem().getString(R.string.dailyStars).format("${currentRepo.currentPeriodStars}","s")
        }
        holder.repoStars.text = textToBe
        holder.repoName.text = Resources.getSystem().getString(R.string.authorAndRepoName).format(currentRepo.author,currentRepo.name)
        holder.repoDesc.text = currentRepo.description
    }

    fun clear(){
        repoList.clear()
        Log.i("Update","Cleared Data Set")
    }

    fun add(item:GitHubRepo){
        repoList.add(item)
        Log.i("Update","Updated Data Set")
    }


}