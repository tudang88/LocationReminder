package com.udacity.project4.locationreminders

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.authentication.AuthenticationActivityViewModel
import com.udacity.project4.databinding.ActivityRemindersBinding
import kotlinx.android.synthetic.main.activity_reminders.*
import org.koin.android.ext.android.inject
import timber.log.Timber


/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRemindersBinding

    // get a reference to authentication state ViewModel
    private val _authenticationStateViewModel: AuthenticationActivityViewModel by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeAuthenticationState()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reminders)
    }

    /**
     * Observes the authentication state and transits activity accordingly
     * If there is a logged in user -> open ReminderListFragment as usual
     * Else there is no logged in user -> start login activity
     */
    private fun observeAuthenticationState() {
        _authenticationStateViewModel.authenticationState.observe(
            this,
            Observer { authenticationState ->
                when (authenticationState) {
                    AuthenticationActivityViewModel.AuthenticationState.AUTHENTICATED -> {
                        Timber.d("Successful log in")
                    }
                    else -> {
                        Timber.d("Need to login")
                        val intent = Intent(applicationContext, AuthenticationActivity::class.java)
                        startActivity(intent)
                    }
                }
            })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Timber.d("onOptionsItemSelected(): ${item.itemId}")
        when (item.itemId) {
            android.R.id.home -> {
                (nav_host_fragment as NavHostFragment).navController.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
