package com.AERYZ.treasurefind.main.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.view.LayoutInflater
import android.widget.ImageView
import com.AERYZ.treasurefind.R

class TimerDialog {
    companion object {
        fun clockDialog(context: Context): Dialog {
            val dialog = Dialog(context)
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_timer, null)
            dialog.setContentView(view)
            dialog.setCancelable(false)
            dialog.window!!.setBackgroundDrawableResource(R.drawable.dialog_rounded_background)
            val clockView: ImageView = view.findViewById(R.id.clockVector)
            val drawable = clockView.drawable
            val animation = drawable as AnimatedVectorDrawable
            animation.start()
            return dialog
        }
    }
}