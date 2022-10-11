package com.pvs.spent.ui

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.pvs.spent.R

class AlertBackup(private val activity: Activity) {

    private lateinit var dialog: AlertDialog

    fun show() {
        dialog = AlertDialog.Builder(activity)
            .setView(activity.layoutInflater.inflate(R.layout.dialog_backup, null))
            .setTitle("Backup expense")
            .setMessage("Closing the app while cancel the backup")
            .setCancelable(false)
            .create()

        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }
}