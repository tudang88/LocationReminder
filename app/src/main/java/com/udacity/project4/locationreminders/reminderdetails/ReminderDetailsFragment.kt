package com.udacity.project4.locationreminders.reminderdetails

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.navArgs
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentReminderDetailsBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragmentDirections
import com.udacity.project4.utils.removeGeofence
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/**
 * A fragment for showing detail of reminder selected from list
 */
class ReminderDetailsFragment : BaseFragment() {
    //use Koin to retrieve the ViewModel instance
    override val _viewModel: ReminderDetailViewModel by viewModel()
    private lateinit var binding: FragmentReminderDetailsBinding
    private val args: ReminderDetailsFragmentArgs by navArgs()
    private lateinit var geofencingClient: GeofencingClient
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_reminder_details, container, false
            )
        binding.viewModel = _viewModel

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        // start loading entry
        _viewModel.loadReminderEntry(args.entryId)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        geofencingClient =
            LocationServices.getGeofencingClient(requireActivity().applicationContext)
        binding.editReminder.setOnClickListener {
            // navigate to saveReminderFragment for edit
            navigateToAddReminder(args.entryId)
        }
    }

    private fun navigateToAddReminder(id: String) {
        //use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderDetailsFragmentDirections.actionReminderDetailsFragmentToSaveReminderFragment(
                    id
                )
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    /**
     * delete item when user push delete icon
     * delete item also remove geofence
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_delete -> {
                _viewModel.deleteItem()
                removeGeofence(geofencingClient, args.entryId,
                    {
                        Timber.d("Success to remove geofence id: ${args.entryId}")
                    },
                    {
                        Timber.d("Failed to remove geofence id ${args.entryId}")
                    })
                true
            }
            else -> false
        }
    }

    /**
     * load option menu
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.reminder_details_fragment_menu, menu)
    }
}