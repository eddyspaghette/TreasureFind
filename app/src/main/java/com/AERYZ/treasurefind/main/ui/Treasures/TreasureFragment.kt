package com.AERYZ.treasurefind.main.ui.Treasures

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.databinding.FragmentTreasureBinding
import com.AERYZ.treasurefind.db.Treasure
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class TreasureFragment : Fragment() {

    private var _binding: FragmentTreasureBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val treasureViewModel =
            ViewModelProvider(this).get(TreasureViewModel::class.java)

        _binding = FragmentTreasureBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val listView = root.findViewById<ListView>(R.id.listView)


        val myListViewAdapter = MyListViewAdapter(requireActivity(), treasureViewModel.treasureList)
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
                    treasureViewModel.treasureList.add(Treasure(title, desc))
                }
                myListViewAdapter.updatelist(treasureViewModel.treasureList)
                listView.adapter = myListViewAdapter
                Log.d("Debug", "firebase size: ${treasureViewModel.treasureList.size}")
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