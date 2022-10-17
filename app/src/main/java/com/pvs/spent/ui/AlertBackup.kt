package com.pvs.spent.ui

import android.app.Activity
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.pvs.spent.R
import kotlinx.android.synthetic.main.dialog_backup.view.*

class AlertBackup(private val activity: Activity) {

    private lateinit var dialog: AlertDialog
    private lateinit var dialogView : View

    fun show() {

        dialogView  = activity.layoutInflater.inflate(R.layout.dialog_backup, null)

        dialog = AlertDialog.Builder(activity)
            .setView(dialogView)
            .setTitle("Backup expense")
            .setMessage("Please wait for the backup to complete before closing the app")
            .setCancelable(false)
            .create()

        dialog.show()
    }

    fun updateExpenseProgress(progress:String){
        dialogView.expenseBackupProgress.text = progress
    }

    fun dismiss() {
        dialog.dismiss()
    }
}