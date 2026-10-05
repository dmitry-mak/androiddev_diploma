package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.databinding.JobCardBinding
import ru.netology.nework.dto.JobDto
import ru.netology.nework.util.DateUtils

class JobAdapter(
    private val canDelete: Boolean,
    private val onDelete: (JobDto) -> Unit
) : ListAdapter<JobDto, JobAdapter.JobViewHolder>(JobDiffCallBack()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): JobViewHolder = JobViewHolder(JobCardBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    override fun onBindViewHolder(
        holder: JobViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }


    inner class JobViewHolder(private val binding: JobCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(job: JobDto) = with(binding) {
            jobName.text = job.name
            jobPeriod.text = DateUtils.formatJobPeriod(job.start, job.finish)
            jobPosition.text = job.position
            jobLink.isVisible = !job.link.isNullOrBlank()
            jobLink.text = job.link.orEmpty()
            jobDelete.isVisible = canDelete
            jobDelete.setOnClickListener { onDelete(job) }
        }
    }
}

class JobDiffCallBack : DiffUtil.ItemCallback<JobDto>() {
    override fun areItemsTheSame(
        oldItem: JobDto,
        newItem: JobDto
    ) = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: JobDto,
        newItem: JobDto
    ) = oldItem == newItem

}