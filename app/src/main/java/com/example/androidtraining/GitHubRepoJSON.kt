package com.example.androidtraining

import android.os.Parcel
import android.os.Parcelable

class GitHubRepo(var name: String, var owner: GitHubRepoOwner, var stargazers_count: Int, var description: String) :
    Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readParcelable(GitHubRepoOwner::class.java.classLoader),
        parcel.readInt(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeParcelable(owner, flags)
        parcel.writeInt(stargazers_count)
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<GitHubRepo>
        {
            override fun createFromParcel(parcel: Parcel): GitHubRepo {
                return GitHubRepo(parcel)
            }

            override fun newArray(size: Int): Array<GitHubRepo?> {
                return arrayOfNulls(size)
            }
        }
    }
}

class GitHubRepoList(var items: List<GitHubRepo>)


class GitHubRepoOwner(var login: String) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(login)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<GitHubRepoOwner>
        {
            override fun createFromParcel(parcel: Parcel): GitHubRepoOwner {
                return GitHubRepoOwner(parcel)
            }

            override fun newArray(size: Int): Array<GitHubRepoOwner?> {
                return arrayOfNulls(size)
            }
        }
    }
}