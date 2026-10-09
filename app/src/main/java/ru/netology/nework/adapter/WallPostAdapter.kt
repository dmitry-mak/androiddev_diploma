package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import ru.netology.nework.databinding.PostCardBinding
import ru.netology.nework.dto.PostDto


class WallPostAdapter(
    private val onPostInteractionListener: OnPostInteractionListener
) : ListAdapter<PostDto, PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder =
        PostViewHolder(
            PostCardBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onPostInteractionListener
        )

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}