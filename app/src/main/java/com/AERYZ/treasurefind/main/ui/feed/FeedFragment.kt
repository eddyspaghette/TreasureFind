package com.AERYZ.treasurefind.main.ui.feed

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.databinding.FragmentFeedBinding
import com.AERYZ.treasurefind.db.MyFirebase

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val myFirebase = MyFirebase()
    private lateinit var feedViewModel: FeedViewModel

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    companion object {
        var tid_KEY = "tid"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        feedViewModel =
            ViewModelProvider(this)[FeedViewModel::class.java]

        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val swipeRefreshLayout: SwipeRefreshLayout = root.findViewById(R.id.swiperefresh)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val listRecyclerView: RecyclerView = root.findViewById(R.id.feed_recyclerview)
        listRecyclerView.layoutManager = layoutManager
        val feedAdapter = FeedAdapter(requireActivity(), arrayListOf())

        feedViewModel.feedList.observe(requireActivity()) {
            feedAdapter.updateList(it)
            feedAdapter.notifyDataSetChanged()
        }
        listRecyclerView.adapter = feedAdapter

        swipeRefreshLayout.setOnRefreshListener {
            myFirebase.getAllTreasures(feedViewModel)
            listRecyclerView.adapter!!.notifyDataSetChanged()
            swipeRefreshLayout.isRefreshing = false
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}