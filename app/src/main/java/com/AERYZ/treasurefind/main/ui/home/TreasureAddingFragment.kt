package com.AERYZ.treasurefind.main.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.databinding.FragmentTreasureAddBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class TreasureAddingFragment : Fragment() {

    private var _binding: FragmentTreasureAddBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentTreasureAddBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val trTitle: EditText = root.findViewById(R.id.treasureTitle)
        val trDescription: EditText = root.findViewById(R.id.treasureDescription)

        val addBtn = root.findViewById<Button>(R.id.addButton)
        val db = Firebase.firestore
        addBtn.setOnClickListener {
            val title = trTitle.text.toString()
            val desc = trDescription.text.toString()
            val user = hashMapOf(
                "title" to title,
                "desc" to desc,
            )

            // Add a new document with a generated ID
            db.collection("users")
                .add(user)
                .addOnSuccessListener { documentReference ->
                    Log.d("F", "DocumentSnapshot added with ID: ${documentReference.id}")
                    Toast.makeText(requireActivity(), "Added to firebase!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.w("F", "Error adding document", e)
                }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}