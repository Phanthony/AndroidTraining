package com.example.androidtraining.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.androidtraining.R
import com.example.androidtraining.service.GitHubIssue
import kotlinx.android.synthetic.main.issuelayout.view.*

class RecyclerViewIssueAdapter(private val issueList: ArrayList<Pair<GitHubIssue,View.OnClickListener>>, val context: Context): RecyclerView.Adapter<RecyclerViewIssueAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val stateImage = itemView.IssueStateImage
        val issueName = itemView.IssueTitle
        val issueDesc = itemView.IssueDesc
        val issueCommentNum = itemView.IssueCommentNum
        val issueLayout = itemView.issueLayout
    }

    override fun getItemCount(): Int {
        return issueList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentIssue = issueList[position].first
        val state = if(currentIssue.state == "open"){
            R.drawable.open
        } else{
            R.drawable.close
        }
        holder.stateImage.setImageResource(state)
        holder.issueCommentNum.text = currentIssue.comments.toString()
        holder.issueName.apply {
            text = currentIssue.title
            isSelected = true
        }
        holder.issueDesc.apply {
            text = context.getString(R.string.issueDesc).format(currentIssue.user.login,currentIssue.repository.getName())
            isSelected = true
        }
        holder.issueLayout.setOnClickListener(issueList[position].second)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.issuelayout,parent,false)
        return ViewHolder(
            layout
        )
    }

    fun clear(){
        issueList.clear()
        notifyDataSetChanged()

    }

    fun addAll(itemList: List<Pair<GitHubIssue,View.OnClickListener>>){
        for (issue in itemList){
            issueList.add(issue)
        }
        notifyDataSetChanged()
    }
}