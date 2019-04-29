package com.example.androidtraining

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.repolayout.*
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.repolayout.view.*

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

        holder.repoDesc.text = currentRepo.description
        holder.repoStars.text = currentRepo.currentPeriodStars.toString() + " stars recently"
        holder.repoName.text = "${currentRepo.author} / ${currentRepo.name}"
    }


}