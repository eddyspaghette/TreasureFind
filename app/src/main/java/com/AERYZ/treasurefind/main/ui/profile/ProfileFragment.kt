package com.AERYZ.treasurefind.main.ui.profile

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.viewpager2.widget.ViewPager2
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.db.MyFirebase
import com.AERYZ.treasurefind.main.util.Util.getBitmap
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.mikhaellopez.circularimageview.CircularImageView

class ProfileFragment : Fragment() {
    private lateinit var viewModel: ProfileViewModel
    private var myFirebase = MyFirebase()
    private val tabNames = arrayOf("Own","Found")
    private lateinit var tabLayoutMediator : TabLayoutMediator
    private val options = arrayOf("Open Camera", "Select from Gallery")
    private lateinit var activityResult: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val tabLayout: TabLayout = view.findViewById(R.id.profile_tablayout)
        val viewPager2: ViewPager2 = view.findViewById(R.id.profile_viewpager2)

        val profileImageView: CircularImageView = view.findViewById(R.id.circularImageViewProfile)
        val uid = FirebaseAuth.getInstance().currentUser!!.uid

        //val profileImage = myFirebase.
        //profileImageView.setImageBitmap()

        activityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                it : ActivityResult ->
            if (it.resultCode == Activity.RESULT_OK) {

                //if it.data.data is not null, it.data.data contains Uri of image from the Gallery
                if (it.data != null && it.data!!.data != null)
                {
                    var bitmap = getBitmap(requireActivity(), it.data!!.data!!)
                    profileImageView.setImageBitmap(bitmap)
                    myFirebase.updateProfileImage(uid, bitmap)

                }
            }
        }

        profileImageView.setOnClickListener() {
            activityResult.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
        }

        val list_fragments: ArrayList<Fragment> = ArrayList()
        list_fragments.add(OwnTreasureFragment())
        list_fragments.add(FoundTreasureFragment())

        viewPager2.adapter = ProfileFragmentStateAdapter(requireActivity(), list_fragments)

        val tabConfigurationStrategy = TabLayoutMediator.TabConfigurationStrategy {
                tab: TabLayout.Tab, position: Int ->
            tab.text = tabNames[position]
        }
        tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager2, tabConfigurationStrategy)
        tabLayoutMediator.attach()

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        tabLayoutMediator.detach()
    }

}