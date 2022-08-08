package com.AERYZ.treasurefind.main.entry_point

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.databinding.ActivityMainBinding
import com.AERYZ.treasurefind.db.MyFirebase
import com.google.firebase.auth.FirebaseAuth
import com.AERYZ.treasurefind.db.MyUser
import com.AERYZ.treasurefind.db.Treasure
import com.AERYZ.treasurefind.main.ui.hider_map.HiderMapActivity
import com.AERYZ.treasurefind.main.ui.seeker_map.SeekerMapActivity
import com.google.firebase.firestore.ktx.toObject

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var myFirebase = MyFirebase()
    private var uid = FirebaseAuth.getInstance().uid!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarHome.toolbar)


        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_seeker, R.id.nav_hider, R.id.nav_profile, R.id.nav_test
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        myFirebase.getUserDocument(uid)
            .get()
            .addOnCompleteListener { u ->
                val user = u.result.toObject<MyUser>()
                if(user!!.in_session!=""){
                    myFirebase.getTreasureDocument(user.in_session)
                        .get()
                        .addOnCompleteListener { t ->
                            val treasure = t.result.toObject<Treasure>()
                            var intent:Intent
                            if(user.uid == treasure!!.oid){
                                intent=Intent(this,HiderMapActivity::class.java)
                                intent.putExtra(HiderMapActivity.tid_KEY, treasure.tid)
                            }
                            else{
                                intent=Intent(this,SeekerMapActivity::class.java)
                                intent.putExtra(SeekerMapActivity.tid_KEY, treasure.tid)
                            }
                            startActivity(intent)
                        }
                }
            }
            .addOnCanceledListener {
            }
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
