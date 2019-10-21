package com.example.androidtraining

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.androidtraining.service.GitHubIssue
import kotlinx.android.synthetic.main.issuelayout.view.*

class RecyclerViewIssueAdapter(val issueList: ArrayList<GitHubIssue>, val context: Context): RecyclerView.Adapter<RecyclerViewIssueAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val stateImage = itemView.IssueStateImage
        val issueName = itemView.IssueTitle
        val issueDesc = itemView.IssueDesc
        val issueCommentNum = itemView.IssueCommentNum
    }

    override fun getItemCount(): Int {
        return issueList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentIssue = issueList[position]
        val state = if(currentIssue.state == "open"){
            R.drawable.open
        } else{
            R.drawable.close
        }
        holder.stateImage.setImageResource(state)
        holder.issueCommentNum.text = currentIssue.comments.toString()
        holder.issueName.text = currentIssue.title
        holder.issueDesc.text = context.getString(R.string.issueDesc).format(currentIssue.user.login,currentIssue.repository.getName())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.issuelayout,parent,false)
        return ViewHolder(layout)
    }

    fun clear(){
        issueList.clear()
        notifyDataSetChanged()

    }

    fun addAll(itemList: List<GitHubIssue>){
        for (issue in itemList){
            issueList.add(issue)
        }
        notifyDataSetChanged()
    }
}