package com.AERYZ.treasurefind.main.ui.feed

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.db.MyFirebase
import com.AERYZ.treasurefind.db.Treasure
import com.AERYZ.treasurefind.db.MyUser
import com.AERYZ.treasurefind.main.ui.treasuredetails.TreasureDetailsActivity
import com.bumptech.glide.signature.ObjectKey
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.text.DateFormat

class FeedAdapter(private var context: Context, private var feedList: ArrayList<Treasure>) : RecyclerView.Adapter<FeedAdapter.ViewHolder>() {
    private val storage = Firebase.storage
    private val storageRef = storage.reference

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView
        val postedTextView: TextView
        val feedDateTextView: TextView
        val profileImageView: ImageView
        val tidTextView: TextView
        val distanceTextView: TextView
        val titleTextView: TextView

        init {
            imageView = view.findViewById(R.id.feed_item)
            postedTextView = view.findViewById(R.id.feed_posted)
            feedDateTextView = view.findViewById(R.id.feed_date)
            profileImageView = view.findViewById(R.id.feed_profile)
            tidTextView = view.findViewById(R.id.feed_id)
            distanceTextView = view.findViewById(R.id.feed_distance)
            titleTextView = view.findViewById(R.id.feed_title)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_feed_listitem, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // https://firebase.google.com/docs/storage/android/create-reference
        holder.titleTextView.text = "Title: ${feedList[position].title}"
        holder.tidTextView.text = "TreasureID: ${feedList[position].tid}"
        holder.distanceTextView.text = "${feedList[position].distanceText}"
        val imageRef = feedList[position].treasureImagePath?.let { storageRef.child(it) }
        if (imageRef != null) {
            GlideApp.with(context)
                .load(imageRef)
                .centerCrop()
                .into(holder.imageView)
        }
        imageRef?.metadata?.addOnSuccessListener {
            val date = DateFormat.getDateInstance().format(it.creationTimeMillis)
            holder.feedDateTextView.text = "Date posted: $date"
        }?.addOnFailureListener {
            holder.feedDateTextView.text = "Date posted: N/A"
        }
        val ownerId = feedList[position].oid
        val myFirebase = MyFirebase()
        if (ownerId != null && ownerId != "") {
            val docRef = myFirebase.getUserDocument(ownerId)
            docRef.get()
                .addOnSuccessListener {
                    val myUser: MyUser? = it.toObject<MyUser>()
                    println("DEBUG: user: $myUser")
                    myUser?.let { user ->
                        println("DEBUG: feedUser $user")
                        holder.postedTextView.text = "Posted by: ${user.userName}"
                        val profileImageRef = storageRef.child(user.profileImagePath)
                        profileImageRef.metadata.addOnSuccessListener { profileMeta ->
                            val updatedTime = profileMeta.updatedTimeMillis
                            GlideApp.with(context)
                                .load(storageRef.child(user.profileImagePath))
                                .signature(ObjectKey(updatedTime))
                                .into(holder.profileImageView)
                        }
                    }
                }
                .addOnFailureListener{
                    println("DEBUG: feedUser $it")
                    holder.postedTextView.text = "N/A"
                }
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(context, TreasureDetailsActivity::class.java)
            val tid = feedList[position].tid
            intent.putExtra(FeedFragment.tid_KEY, tid)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return feedList.size
    }

    fun updateList(newList: ArrayList<Treasure>) {
        feedList = newList
    }
}