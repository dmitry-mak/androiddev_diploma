package ru.netology.nework.util

import android.annotation.SuppressLint
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.widget.EditText
import androidx.core.content.ContextCompat


    @SuppressLint("ClickableViewAccessibility")
    fun EditText.setupPasswordToggle(eyeNormal: Int, eyeCrossed: Int) {
        var isVisible = false

        fun updateIcon() {
            val icon = ContextCompat.getDrawable(
                context,
                if (isVisible) eyeNormal else eyeCrossed
            )
            setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null)
        }

        fun updateTransformation() {
            transformationMethod = if (isVisible) {
                HideReturnsTransformationMethod.getInstance()
            } else {
                PasswordTransformationMethod.getInstance()
            }
            setSelection(text?.length ?: 0)
        }
        updateIcon()
        updateTransformation()

        setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val end = compoundDrawables[2] ?: return@setOnTouchListener false
                val iconStartX = width - paddingEnd - end.bounds.width()
                if (event.x >= iconStartX) {
                    isVisible = !isVisible
                    updateTransformation()
                    updateIcon()
                    performClick()
                    return@setOnTouchListener true
                }
            }
            false
        }
    }
