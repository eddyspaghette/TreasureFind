package com.AERYZ.treasurefind.main.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AERYZ.treasurefind.R

class OwnTreasureFragment : Fragment() {
    private lateinit var viewModel: ProfileViewModel
    private lateinit var modelFactory: ProfileFragmentViewModelFactory
    private lateinit var listRecyclerView: RecyclerView
    private lateinit var ownTreasureFragmentAdapter: OwnTreasureFragmentAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_own_treasure, container, false)

        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        modelFactory = ProfileFragmentViewModelFactory(requireActivity())
        viewModel = ViewModelProvider(this, modelFactory)[ProfileViewModel::class.java]
        listRecyclerView = view.findViewById(R.id.ownList_recycler_view)
        listRecyclerView.layoutManager = layoutManager
        ownTreasureFragmentAdapter = OwnTreasureFragmentAdapter(requireActivity(), arrayListOf())

        viewModel.ownedList.observe(requireActivity()) {
            ownTreasureFragmentAdapter.updateList(it)
            ownTreasureFragmentAdapter.notifyDataSetChanged()
        }

        listRecyclerView.adapter = ownTreasureFragmentAdapter

        return view
    }

}