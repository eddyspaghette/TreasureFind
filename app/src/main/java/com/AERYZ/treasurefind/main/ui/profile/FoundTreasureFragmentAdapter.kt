package com.AERYZ.treasurefind.main.ui.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.db.MyFirebase
import com.AERYZ.treasurefind.db.Treasure
import com.AERYZ.treasurefind.main.ui.feed.GlideApp
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class FoundTreasureFragmentAdapter(private val context: Context, private var foundList: ArrayList<String>) :
    RecyclerView.Adapter<FoundTreasureFragmentAdapter.ViewHolder>() {
    private val myFirebase = MyFirebase()
    private val storage = Firebase.storage
    private var storageRef = storage.reference

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tidTextView: TextView
        val foundTreasureImageView: ImageView

        init {
            tidTextView = view.findViewById(R.id.fragment_profile_tid)
            foundTreasureImageView = view.findViewById(R.id.fragment_profile_iv)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.fragment_profile_listitem, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val treasureRef = myFirebase.getTreasureDocument(foundList[position])
        treasureRef
            .get()
            .addOnSuccessListener {
                holder.tidTextView.text = foundList[position]
                val treasureObj = it.toObject<Treasure>()
                treasureObj?.treasureImagePath?.let { path -> storageRef.child(path) }
                    .let { reference ->
                    GlideApp.with(context)
                        .load(reference)
                        .centerCrop()
                        .into(holder.foundTreasureImageView)
                }
            }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = foundList.size

    fun updateList(newList: ArrayList<String>) {
        foundList = newList
    }

}
