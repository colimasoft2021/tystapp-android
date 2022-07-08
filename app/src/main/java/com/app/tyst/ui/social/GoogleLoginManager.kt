package com.app.tyst.ui.social

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.app.tyst.R
import com.app.tyst.data.model.Social
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.extension.showSnackBar
import com.app.tyst.utility.helper.LOGApp
import com.crashlytics.android.Crashlytics
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser


/**
 * Created by hb on 11/5/18.
 */
class GoogleLoginManager : BaseActivity() {
    private val TAG = "GoogleActivity"
    private val RC_SIGN_IN = 9001
    private var mGoogleSignInClient: GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        mGoogleSignInClient?.revokeAccess()
        signOut()
//        signIn()
    }

    // [START on_start_check_user]
    public override fun onStart() {
        super.onStart()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (task.isSuccessful) {
                // Google Sign In was successful,
                val account = task.getResult(ApiException::class.java)
                updateUISocial(getSocialUserFromGoogleSignInAccount(account))

            } else {
                // Google Sign In failed, update UI appropriately
                logger.debugEvent("Google Login Failed", task.exception?.toString()?:"")
                LOGApp.e("LoginGmail Cancel: "+task.exception?.message)
                Crashlytics.logException(task.exception)
                "LoginGmail Cancel: "+task.exception?.message?.showSnackBar(this@GoogleLoginManager)
                updateUI(null, true)
            }
        }
    }

    private fun getSocialUserFromGoogleSignInAccount(acc: GoogleSignInAccount?): Social {
        val social = Social("", "", "", "", "", "", "", "")
        val nameList = acc?.displayName?.split("\\s".toRegex())

        social.emailId = acc?.email ?: ""
        social.name = acc?.displayName ?: ""
        social.firstName = acc?.displayName ?: ""
        try {
            social.firstName = nameList?.get(0) ?: ""
            social.lastName = nameList?.get(1) ?: ""
        } catch (e: Exception) {

        }
        social.socialId = acc?.id ?: ""
        social.profileImageUrl = acc?.photoUrl.toString()
        social.type = IConstants.SOCIAL_TYPE_GOOGLE
        return social
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient?.signInIntent

        startActivityForResult(signInIntent, RC_SIGN_IN)
    }


    private fun signOut() {
        // Google sign out
        mGoogleSignInClient?.signOut()?.addOnCompleteListener(this) {
//            updateUI(null)
            signIn()
        }
    }

    private fun revokeAccess() {
        // Google revoke access
        mGoogleSignInClient?.revokeAccess()?.addOnCompleteListener(this) {
            updateUI(null)
        }
    }

    private fun updateUISocial(socialUser: Social?, fromResponse: Boolean = false) {
        if (socialUser != null) {
            val i = Intent()
            i.putExtra("google_data", socialUser)
            setResult(Activity.RESULT_OK, i)
            finish()
        } else {
            if (fromResponse) {
                val i = Intent()
                setResult(Activity.RESULT_CANCELED, i)
                finish()
            }
        }
    }

    private fun updateUI(user: FirebaseUser?, fromResponse: Boolean = false) {

        if (user != null) {
            val social = Social("", "", "", "", "", "", "")
            val nameList = user.displayName!!.split("\\s".toRegex())

            social.emailId = user.email!!
            social.name = user.displayName!!
            social.firstName = user.displayName!!
            try {
                social.firstName = nameList[0]
                social.lastName = nameList[1]
            } catch (e: Exception) {
              e.printStackTrace()
            }
            social.socialId = user.uid
            social.profileImageUrl = user.photoUrl.toString()
            revokeAccess()
            val i = Intent()
            i.putExtra("google_data", social)
            setResult(Activity.RESULT_OK, i)
            finish()

        } else {
            if (fromResponse) {
                val i = Intent()
                setResult(Activity.RESULT_CANCELED, i)
                finish()
            }
        }
    }

    public override fun onStop() {
        super.onStop()
    }

}