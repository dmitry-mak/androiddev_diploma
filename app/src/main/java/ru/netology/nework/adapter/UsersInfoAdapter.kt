package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import ru.netology.nework.adapter.UsersInfoAdapter.UserViewHolder
import ru.netology.nework.databinding.UserInfoCardBinding
import ru.netology.nework.dto.UserDto
import ru.netology.nework.util.UrlUtils

class UsersInfoAdapter : ListAdapter<UserDto, UserViewHolder>(UserDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserViewHolder = UserViewHolder(
        UserInfoCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(
        holder: UserViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }


    class UserViewHolder(private val binding: UserInfoCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserDto) = with(binding) {
            userName.text = user.name
            userLogin.text = user.login

            val avatarUrl = UrlUtils.avatarUrl(user.avatar)
            if (avatarUrl != null) {
                avatarInit.isVisible = false
                avatarImage.isVisible = true
                Glide.with(avatarImage).load(avatarUrl).circleCrop().into(avatarImage)
            } else {
                avatarImage.isVisible = false
                avatarInit.isVisible = true
                avatarInit.text = user.name.take(1).uppercase()
            }
        }
    }

}

class UserDiffCallback : DiffUtil.ItemCallback<UserDto>() {
    override fun areItemsTheSame(
        oldItem: UserDto,
        newItem: UserDto
    ) = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: UserDto,
        newItem: UserDto
    ) = oldItem == newItem

}
