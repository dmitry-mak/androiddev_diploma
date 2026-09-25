package ru.netology.nework.ui.common

import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.databinding.AvatarRowBinding
import ru.netology.nework.util.UrlUtils


data class AvatarItem(
    val name: String,
    val avatar: String?
)

fun Fragment.bindAvatarRow(
    row: LinearLayout,
    users: List<AvatarItem>,
    keepChildren: Int = 2,
    onMoreClick: () -> Unit
) {
    while (row.childCount > keepChildren) row.removeViewAt(row.childCount - 1)

    fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
    val slotSize = dpToPx(40)
    val slotMargin = dpToPx(8)

    users.take(5).forEach { user ->
        val slot = AvatarRowBinding.inflate(layoutInflater, row, false)
        val url = UrlUtils.avatarUrl(user.avatar)

        if (url != null) {
            slot.avatarInit.isVisible = false
            slot.avatarImage.isVisible = true
            Glide.with(slot.avatarImage)
                .load(url)
                .centerCrop()
                .into(slot.avatarImage)
        } else {
            slot.avatarImage.isVisible = false
            slot.avatarInit.isVisible = true
            slot.avatarInit.text = user.name.take(1).uppercase()
        }
        row.addView(
            slot.root,
            LinearLayout.LayoutParams(slotSize, slotSize)
                .apply { marginStart = slotMargin })
    }
    if (users.size > 5) {
        val more = TextView(requireContext()).apply {
            setBackgroundResource(R.drawable.bg_avatar_circle)
            text = "+"
            gravity = Gravity.CENTER
            setTextColor(android.graphics.Color.WHITE)
            textSize = 18f
            setOnClickListener { onMoreClick() }
        }
        row.addView(
            more,
            LinearLayout.LayoutParams(slotSize, slotSize).apply { marginStart = slotMargin })
    }
}