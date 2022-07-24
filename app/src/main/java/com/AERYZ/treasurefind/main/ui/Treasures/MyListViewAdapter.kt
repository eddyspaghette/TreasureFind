package com.AERYZ.treasurefind.main.ui.Treasures

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.db.Treasure

class MyListViewAdapter(private var context: Context, private var treasureList: ArrayList<Treasure>): BaseAdapter() {
    override fun getCount(): Int {
        return treasureList.size
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getItem(p0: Int): Any {
        return treasureList[p0]
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val view = View.inflate(context, R.layout.treasure_listviewadapter, null)
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val textView_1: TextView = view.findViewById(R.id.textview_1)
        val textView_2: TextView = view.findViewById(R.id.textview_2)

        //imageView.setImageBitmap(Util.getBitmap(context, treasureList[p0].uri))

        textView_1.setText(treasureList[p0].title)
        textView_2.setText(treasureList[p0].desc)
        return view
    }

    fun updatelist(list: ArrayList<Treasure>) {
        treasureList = list
    }
}