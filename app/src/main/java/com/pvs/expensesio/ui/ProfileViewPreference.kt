package com.pvs.expensesio.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.bumptech.*
import com.bumptech.glide.Glide


class ProfileViewPreference(
    private val mContext: Context,
    mAttrs: AttributeSet
) :
    Preference(mContext, mAttrs) {

    private lateinit var initImage: TextView
    private var nameInit = "XY"

    private lateinit var userImage: ImageView
    private var imageUrl: String? = null
    private var drawableResId = com.pvs.expensesio.R.drawable.ic_baseline_person_24

    private lateinit var userName: TextView
    private var name = ""

    private lateinit var userEmail: TextView
    private var email = ""

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        initImage = holder.findViewById(com.pvs.expensesio.R.id.initialTv) as TextView
        userImage = holder.findViewById(com.pvs.expensesio.R.id.userImage) as ImageView

        if (imageUrl != null) { // Set image from Google and Facebook

            Glide.with(mContext).load(imageUrl).into(userImage)
            initImage.visibility = View.GONE
            userImage.visibility = View.VISIBLE
        } else { // Set image from Email + Password
            initImage.text = nameInit
            initImage.visibility = View.VISIBLE
            userImage.visibility = View.GONE
        }

        // Set name
        userName = holder.findViewById(com.pvs.expensesio.R.id.userName) as TextView
        userName.text = name

        // Set email
        userEmail = holder.findViewById(com.pvs.expensesio.R.id.userEmail) as TextView
        userEmail.text = email
    }

    fun setDrawableResId(id: Int): ProfileViewPreference {
        this.drawableResId = id
        this.imageUrl = null
        return this
    }

    fun setImage(imageUrl: String?): ProfileViewPreference {
        this.imageUrl = imageUrl
        return this
    }

    fun setInit(i: String): ProfileViewPreference {
        this.nameInit = i
        super.notifyChanged()
        return this
    }

    fun setUserName(name: String): ProfileViewPreference {
        this.name = name
        super.notifyChanged()
        return this
    }

    fun setUserEmail(email: String): ProfileViewPreference {
        this.email = email
        return this
    }

}