package com.udacity.project4.authentication

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    companion object {
        const val TAG = "AuthenticationActivity"
        const val SIGN_IN_RESULT_CODE = 1001
    }

    // get a reference to authentication state ViewModel
    private val _authenticationStateViewModel: AuthenticationActivityViewModel by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_authentication)
        val binding = DataBindingUtil.setContentView<ActivityAuthenticationBinding>(
            this,
            R.layout.activity_authentication
        )
        binding.authButton.setOnClickListener {
            launchSignInFlow()
        }
        _authenticationStateViewModel.authenticationState.observe(
            this,
            Observer { authenticationState ->
                when (authenticationState) {
                    AuthenticationActivityViewModel.AuthenticationState.AUTHENTICATED -> {
                        Timber.tag(TAG).d("Login successful -> transit to RemindersActivity")
                        transitToRemindersActivity()
                    }
                    else -> {
                        Timber.tag(TAG).d("Login failed")
                    }
                }
            })
    }

    private fun launchSignInFlow() {
        // Give users the option to sign in / register with their email or Google account. If users
        // choose to register with their email, they will need to create a password as well.
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // custom layout for sign in page
        val customLayout = AuthMethodPickerLayout.Builder(R.layout.login_screen)
            .setGoogleButtonId(R.id.google_button)
            .setEmailButtonId(R.id.email_button)
            .build()

        // Create and launch sign-in intent. We listen to the response of this activity with the
        // SIGN_IN_RESULT_CODE code.
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false) // disable save credential on this project
                .setAuthMethodPickerLayout(customLayout)
                .setTheme(R.style.AppTheme)
                .build(), SIGN_IN_RESULT_CODE
        )
    }

    @SuppressLint("BinaryOperationInTimber")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in user.
                Timber.tag(TAG)
                    .d("Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!")
            } else {
                // Sign in failed. If response is null the user canceled the sign-in flow using
                // the back button. Otherwise check response.getError().getErrorCode() and handle
                // the error.
                Timber.tag(TAG).d("Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

    /**
     * transition to RemindersActivity after successful logged in
     */
    private fun transitToRemindersActivity() {
        // transition to ReminderListActivity
        val startReminderListIntent =
            Intent(applicationContext, RemindersActivity::class.java)
        startActivity(startReminderListIntent)
    }
}
