package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.adapter.UsersInfoAdapter.UserViewHolder
import ru.netology.nework.databinding.UserInfoCardBinding
import ru.netology.nework.dto.UserDto
import ru.netology.nework.util.UrlUtils

class UsersInfoAdapter(
    private val selectMode: Boolean = false,
    initialSelected: Set<Long> = emptySet()
) : ListAdapter<UserDto, UserViewHolder>(UserDiffCallback()) {

    private val selected = initialSelected.toMutableSet()

    fun getSelectedIds(): List<Long> = selected.toList()

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
        holder.bind(getItem(position), selectMode, selected)
    }


    class UserViewHolder(private val binding: UserInfoCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserDto, selectMode: Boolean, selected: MutableSet<Long>) = with(binding) {
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
            checkbox.isVisible = selectMode
            if (selectMode) {
                checkbox.setOnCheckedChangeListener(null)
                checkbox.isChecked = selected.contains(user.id)
                checkbox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) selected.add(user.id) else selected.remove(user.id)
                }
                root.setOnClickListener { checkbox.isChecked = !checkbox.isChecked }
            } else {
                root.setOnClickListener(null)
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
