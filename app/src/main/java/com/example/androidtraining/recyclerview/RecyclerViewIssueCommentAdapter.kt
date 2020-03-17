package com.example.androidtraining.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.androidtraining.R
import com.example.androidtraining.service.GitHubIssueComment
import kotlinx.android.synthetic.main.issuecommentlayout_left.view.*

class RecyclerViewIssueCommentAdapter(private val context: Context) :
    PagedListAdapter<GitHubIssueComment, RecyclerViewIssueCommentAdapter.ViewHolder>(
        object : DiffUtil.ItemCallback<GitHubIssueComment>() {
            override fun areItemsTheSame(
                oldItem: GitHubIssueComment,
                newItem: GitHubIssueComment
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: GitHubIssueComment,
                newItem: GitHubIssueComment
            ): Boolean = oldItem == newItem
        }
    ) {

    var counter = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (counter % 2 == 0) {
            val layout = LayoutInflater.from(parent.context)
                .inflate(R.layout.issuecommentlayout_left, parent, false)
            counter++
            ViewHolder(
                layout
            )
        } else {
            val layout = LayoutInflater.from(parent.context)
                .inflate(R.layout.issuecommentlayout_right, parent, false)
            counter++
            ViewHolder(
                layout
            )
        }

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentComment = getItem(position)
        if(currentComment != null) {
            holder.commentUser.text = currentComment.user.login
            holder.commentBody.text = currentComment.body
            Glide.with(context).load(currentComment.user.avatar_url).into(holder.commentImage)
        }
    }

    /*
    fun addAll(arrayList: List<GitHubIssueComment>) {
        for (comment in arrayList) {
            commentList.add(comment)
        }
        notifyDataSetChanged()
        Log.i("Update", "Adding all to comment List")
    }

    fun clear() {
        commentList.clear()
        notifyDataSetChanged()
    }
     */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val commentUser = itemView.IssueCommentDesc
        val commentImage = itemView.IssueCommentUserImage
        val commentBody = itemView.IssueCommentBody
    }
}