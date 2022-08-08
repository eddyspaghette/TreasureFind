package com.AERYZ.treasurefind.main.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.AERYZ.treasurefind.R

class OwnTreasureFragment : Fragment() {
    private val testArray = arrayOf("Mountain", "Car", "Book","Mountain", "Car", "Book","Mountain", "Car", "Book","Mountain", "Car", "Book","Mountain", "Car", "Book")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_own_treasure, container, false)

        val arrayAdapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, testArray)

        val listView: ListView = view.findViewById(R.id.listViewOwn)
        val emptyTreasureOwn: ImageView = view.findViewById(R.id.emptyTreasureOwn)
        val emptyTreasureOwnTextView: TextView = view.findViewById(R.id.emptyTreasureOwnTextView)
        listView.adapter = arrayAdapter

        if (arrayAdapter.count == 0) {
            listView.visibility = View.GONE
            emptyTreasureOwn.visibility = View.VISIBLE
            emptyTreasureOwnTextView.visibility = View.VISIBLE
        }
        else {
            listView.visibility = View.VISIBLE
            emptyTreasureOwn.visibility = View.GONE
            emptyTreasureOwnTextView.visibility = View.GONE
        }
        return view
    }

}