package com.AERYZ.treasurefind.main.ui.feed

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.databinding.FragmentFeedBinding
import com.AERYZ.treasurefind.db.MyFirebase
import com.AERYZ.treasurefind.db.Treasure
import com.AERYZ.treasurefind.main.entry_point.MainActivity

class FeedFragment : Fragment(), MenuProvider {

    private var _binding: FragmentFeedBinding? = null
    private val myFirebase = MyFirebase()
    private lateinit var feedViewModel: FeedViewModel
    private lateinit var feedAdapter: FeedAdapter
    private lateinit var searchView: SearchView

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
        feedAdapter = FeedAdapter(requireActivity(), arrayListOf())

        feedViewModel.feedList.observe(requireActivity()) {
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
//                    feedAdapter.filter.filter(query)
//                    println("Query: $query")
                    return false
                }

                override fun onQueryTextChange(newQuery: String?): Boolean {
                    //feedAdapter.filter.filter(newQuery)
                    println("Query: new $newQuery")
                    val filteredList = ArrayList<Treasure>()
                    it.filter { (it.tid!!.contains(newQuery!!)) }.forEach{filteredList.add(it)}
                    feedAdapter.updateList(filteredList)
                    feedAdapter.notifyDataSetChanged()
                    return false
                }
            })
            feedAdapter.updateList(it)
            feedAdapter.notifyDataSetChanged()
        }
        listRecyclerView.adapter = feedAdapter

        swipeRefreshLayout.setOnRefreshListener {
            myFirebase.getAllTreasures(feedViewModel)
            listRecyclerView.adapter!!.notifyDataSetChanged()
            swipeRefreshLayout.isRefreshing = false
        }
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.feed_toolbar, menu)
        val menuItem : MenuItem = menu.findItem(R.id.appSearchBar)
        searchView = menuItem.actionView as SearchView

    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }
}