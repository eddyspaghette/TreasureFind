package com.AERYZ.treasurefind.main.ui.gallery

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.databinding.FragmentGalleryBinding
import com.AERYZ.treasurefind.main.MyListViewAdapter
import com.AERYZ.treasurefind.main.Treasure
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val galleryViewModel =
            ViewModelProvider(this).get(GalleryViewModel::class.java)

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val listView = root.findViewById<ListView>(R.id.listView)


        val myListViewAdapter = MyListViewAdapter(requireActivity(), galleryViewModel.treasureList)
        val db = Firebase.firestore
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("F", "${document.id} => ${document.data}")
                    val data = document.data
                    val keys = data.keys
                    var title: String = data.get("title").toString()
                    var desc: String = data.get("desc").toString()
                    galleryViewModel.treasureList.add(Treasure(title, desc))
                }
                myListViewAdapter.updatelist(galleryViewModel.treasureList)
                listView.adapter = myListViewAdapter
                Log.d("Debug", "firebase size: ${galleryViewModel.treasureList.size}")
            }
            .addOnFailureListener { exception ->
                Log.w("F", "Error getting documents.", exception)
            }

        //galleryViewModel.treasureList.observe(requireActivity()) {
        //   Log.d("Debug size", it.size.toString())
        //    myListViewAdapter.updatelist(it)
        //    myListViewAdapter.notifyDataSetChanged()
        //
        //}

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}