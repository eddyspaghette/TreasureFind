package com.AERYZ.treasurefind.main.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.view.LayoutInflater
import android.widget.ImageView
import com.AERYZ.treasurefind.R

class ProgressDialog {

    companion object {
        fun progressDialog(context: Context): Dialog {
            val dialog = Dialog(context)
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_progress, null)
            dialog.setContentView(view)
            dialog.setCancelable(false)
            dialog.window!!.setBackgroundDrawableResource(R.drawable.dialog_rounded_background)
            return dialog
        }

        fun successDialog(context: Context): Dialog {
            val dialog = Dialog(context)
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_success, null)
            dialog.setContentView(view)
            dialog.setCancelable(false)
            dialog.window!!.setBackgroundDrawableResource(R.drawable.dialog_rounded_background)
            val successView: ImageView = view.findViewById(R.id.successVector)
            val drawable = successView.drawable
            val animation = drawable as AnimatedVectorDrawable
            animation.start()
            return dialog
        }
    }
}