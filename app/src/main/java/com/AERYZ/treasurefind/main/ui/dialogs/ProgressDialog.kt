package com.AERYZ.treasurefind.main.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import com.AERYZ.treasurefind.R

class ProgressDialog {
    companion object {
        fun progressDialog(context: Context): Dialog {
            val dialog = Dialog(context)
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_progress, null)
            dialog.setContentView(view)
            dialog.setCancelable(false)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.WHITE))
            return dialog
        }
    }
}