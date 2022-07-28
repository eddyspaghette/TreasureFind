package com.AERYZ.treasurefind.main.ui.feed

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.AERYZ.treasurefind.R
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class FeedAdapter(private var context: Context, private var feedList: List<StorageReference>) : RecyclerView.Adapter<FeedAdapter.ViewHolder>() {
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView

        init {
            imageView = view.findViewById(R.id.feed_item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_feed_listitem, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // feedList[position] returns a StorageReference
        // https://firebase.google.com/docs/storage/android/create-reference
            GlideApp.with(context)
                .load(feedList[position])
                .error(R.drawable.tf_logo)
                .into(holder.imageView)
//        holder.imageView.setOnClickListener {
//            println("DEBUG: image clicked")
//        }
    }

    override fun getItemCount(): Int {
        return feedList.size
    }

    fun updateList(newList: List<StorageReference>) {
        feedList = newList
    }
}