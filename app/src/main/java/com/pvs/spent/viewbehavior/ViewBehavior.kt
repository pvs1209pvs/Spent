package com.pvs.spent.viewbehavior

import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Rect
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout

object ViewBehavior {

    fun isDarkThemeOn(resources: Resources) = resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    fun getNoDataViewVisibility(list: List<*>, imageView: ImageView, textView: TextView) {
        val visibility = if (list.isEmpty()) View.VISIBLE else View.INVISIBLE
        imageView.visibility = visibility
        textView.visibility = visibility
    }

    fun addMarginToLastItem() = object : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            if (parent.getChildAdapterPosition(view) == parent.adapter!!.itemCount - 1) {
                if (outRect.bottom != 88) {
                    outRect.bottom = ((88 * Resources.getSystem().displayMetrics.density).toInt())
                }
            } else {
                outRect.bottom = ((0 * Resources.getSystem().displayMetrics.density).toInt())

            }
        }
    }


    fun tilErrorMsg(
        textInputLayout: TextInputLayout,
        isValid: Boolean,
        errorMsg: String
    ) {
        textInputLayout.errorIconDrawable = null
        textInputLayout.error = if (isValid) null else errorMsg
    }

}