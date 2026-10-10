package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.databinding.ItemLoadStateBinding


class LoadStateFooterAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<LoadStateFooterAdapter.LoadStateViewHolder>() {

    override fun onBindViewHolder(
        holder: LoadStateViewHolder,
        loadState: LoadState
    ) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): LoadStateViewHolder =
        LoadStateViewHolder(
            ItemLoadStateBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            retry
        )


    class LoadStateViewHolder(
        private val binding: ItemLoadStateBinding,
        retry: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.btnRetry.setOnClickListener { retry() }
        }

        fun bind(loadState: LoadState) = with(binding) {
            loadProgress.isVisible = loadState is LoadState.Loading
            btnRetry.isVisible = loadState is LoadState.Error
            errorMsg.isVisible = loadState is LoadState.Error
            if (loadState is LoadState.Error) {
                errorMsg.text = loadState.error.message
            }
        }
    }
}