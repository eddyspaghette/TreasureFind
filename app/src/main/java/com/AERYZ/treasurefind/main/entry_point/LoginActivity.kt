package com.AERYZ.treasurefind.main.entry_point

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.db.MyFirebase
import com.AERYZ.treasurefind.db.MyUser
import com.AERYZ.treasurefind.main.util.Util
import com.AERYZ.treasurefind.main.entry_point.onboarding.OnboardingActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import java.lang.Exception


class LoginActivity : AppCompatActivity(), MyFirebase.UserInsertionListener {
    var myFirebase = MyFirebase()
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build(),
            AuthUI.IdpConfig.EmailBuilder().build(),
        )

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setLogo(R.drawable.tf)
            .setTheme(R.style.LoginTheme)
            .setIsSmartLockEnabled(false)
            .setAvailableProviders(providers)
            .build()
        signInLauncher.launch(signInIntent)
        Util.checkPermissions(this)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            // Show onboarding activity if new user
            if (response != null && response.isNewUser) {
                user?.let {
                    val u = MyUser(
                        it.uid,
                        it.displayName ?: "N/A",
                        it.email ?: "N/A",
                        "",
                        BitmapFactory.decodeResource(resources, R.drawable.tf_logo)
                    )
                    myFirebase.insert(u,this)
                }
            } else {
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

        } else {
            finish()
        }
    }

    /* Send them to the Onboarding activity if they are a new user */

    override fun onSuccess() {
        val intent = Intent(applicationContext, OnboardingActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onFailure(exception: Exception) {

    }
}