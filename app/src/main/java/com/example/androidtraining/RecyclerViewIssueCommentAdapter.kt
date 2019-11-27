package com.example.androidtraining

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.androidtraining.service.GitHubIssueComment
import kotlinx.android.synthetic.main.issuecommentlayout.view.*

class RecyclerViewIssueCommentAdapter(private val commentList: ArrayList<GitHubIssueComment>, private val context: Context): PagedListAdapter<GitHubIssueComment,RecyclerViewIssueCommentAdapter.ViewHolder>(
    DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.issuecommentlayout,parent,false)
        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentComment = commentList[position]
        holder.commentUser.text = currentComment.user.login
        holder.commentBody.text = currentComment.body
        Glide.with(context).load(currentComment.user.avatar_url).into(holder.commentImage)
    }

    fun addAll(arrayList: List<GitHubIssueComment>){
        for(comment in arrayList){
            commentList.add(comment)
        }
    }

    fun clear(){
        commentList.clear()
    }

    companion object{
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<GitHubIssueComment>(){
            override fun areItemsTheSame(oldItem: GitHubIssueComment, newItem: GitHubIssueComment): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: GitHubIssueComment, newItem: GitHubIssueComment): Boolean = oldItem == newItem
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val commentUser = itemView.IssueCommentDesc
        val commentImage = itemView.IssueCommentUserImage
        val commentBody = itemView.IssueCommentBody
    }
}