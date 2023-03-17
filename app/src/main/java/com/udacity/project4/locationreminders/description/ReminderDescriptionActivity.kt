package com.udacity.project4.locationreminders.description

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.removeGeofence
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        //        receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    //use Koin to retrieve the ViewModel instance
    private val _viewModel: ReminderDescriptionViewModel by viewModel()
    private lateinit var binding: ActivityReminderDescriptionBinding
    private lateinit var geofencingClient: GeofencingClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_reminder_description
        )
        geofencingClient =
            LocationServices.getGeofencingClient(applicationContext)
        // binding data to layout variable

        _viewModel.saveItem(intent?.getSerializableExtra(EXTRA_ReminderDataItem) as ReminderDataItem)

        binding.reminderDataItem = _viewModel.reminderItem.value
        // binding button done
        binding.actDoneFab.setOnClickListener{
            navigateToRemindersActivity()
        }
    }

    /**
     * load layout of option menu
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.reminder_details_fragment_menu, menu)
        return true
    }

    /**
     * delete button click
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_delete -> {
                val id = _viewModel.getItemId()
                if (id != null) {
                    _viewModel.deleteItem()
                    removeGeofence(geofencingClient, id,
                        {
                            Timber.d("Success to remove geofence id: $id")
                            // navigate to main activity
                            navigateToRemindersActivity()
                        },
                        {
                            Timber.d("Failed to remove geofence id $id")
                        })
                }
                true
            }
            else -> false
        }
    }

    /**
     * navigate to ReminderActivity
     */
    private fun navigateToRemindersActivity() {
        val intent = Intent(applicationContext, RemindersActivity::class.java)
        startActivity(intent)
    }
}
