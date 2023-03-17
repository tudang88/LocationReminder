package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.convertToDatabaseEntry
import kotlinx.coroutines.launch
import timber.log.Timber

class SaveReminderViewModel(val app: Application, val dataSource: ReminderDataSource) :
    BaseViewModel(app) {
    enum class AddEditState {
        ADD,
        EDIT,
        UNKNOWN
    }

    val reminderTitle = MutableLiveData<String>()
    val reminderDescription = MutableLiveData<String>()
    val reminderSelectedLocationStr = MutableLiveData<String>()
    private val _selectedPOI = MutableLiveData<PointOfInterest>()
    val selectedPOI: LiveData<PointOfInterest>
        get() = _selectedPOI
    val latitude = MutableLiveData<Double>()
    val longitude = MutableLiveData<Double>()
    private var editEntryId: String = ""
    private val _operationState = MutableLiveData<AddEditState>()
    val operationState: LiveData<AddEditState>
        get() = _operationState
    private val _saveItem = MutableLiveData<ReminderDataItem>()
    val saveItem: LiveData<ReminderDataItem>
        get() = _saveItem

    init {
        _operationState.value = AddEditState.ADD
    }

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        reminderTitle.value = null
        reminderDescription.value = null
        reminderSelectedLocationStr.value = null
        _selectedPOI.value = null
        latitude.value = null
        longitude.value = null
        editEntryId = ""
        _operationState.value = AddEditState.ADD
        _saveItem.value = null
    }

    /**
     * load Reminder entry from database
     */
    fun loadReminderEntry(id: String?) {
        if (id == null || _operationState.value == AddEditState.EDIT)
            return
        Timber.d("load target entry for edit ")
        _operationState.value = AddEditState.EDIT
        showLoading.value = true
        viewModelScope.launch {
            //interacting with the dataSource has to be through a coroutine
            Timber.d("get entry for edit -> start ")
            val result = dataSource.getReminder(id)
            Timber.d("get entry for edit -> end ")
            when (result) {
                is Result.Success<ReminderDTO> -> {
                    //map the reminder data from the DB to the be ready to be displayed on the UI
                    reminderTitle.value = result.data.title
                    reminderDescription.value = result.data.description
                    reminderSelectedLocationStr.value = result.data.location
                    latitude.value = result.data.latitude
                    longitude.value = result.data.longitude
                    _selectedPOI.value = PointOfInterest(
                        LatLng(latitude.value!!, longitude.value!!),
                        "",
                        reminderSelectedLocationStr.value
                    )
                    editEntryId = id // save for deleting
                }
                is Result.Error -> {
                    showSnackBar.value = result.message
                    editEntryId = ""
                }
            }
            Timber.d("get entry for edit -> end -> stop loading ")
            showLoading.postValue(false)
        }
    }


    /**
     * set POI to viewModel when selecting on map
     */
    fun updateSelectedPoi(poi: PointOfInterest) {
        _selectedPOI.value = poi
        longitude.value = poi.latLng.longitude
        latitude.value = poi.latLng.latitude
        reminderSelectedLocationStr.value = poi.name
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
    fun validateAndSaveReminder(reminderData: ReminderDataItem = getCurrentReminderItem()) {
        if (validateEnteredData(reminderData)) {
            saveReminder(reminderData)
        }
    }

    /**
     * Save the reminder to the data source
     */
    fun saveReminder(reminderData: ReminderDataItem) {
        Timber.d("saveReminder to Database -> start")
        Timber.d("saveItem: $reminderData")
        showLoading.value = true
        viewModelScope.launch {
            dataSource.saveReminder(reminderData.convertToDatabaseEntry())
            _saveItem.postValue(reminderData)
            showLoading.value = false
            showToast.value = app.getString(R.string.reminder_saved)
            navigationCommand.value = NavigationCommand.Back
        }
        Timber.d("saveReminder to Database -> done")
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    private fun validateEnteredData(reminderData: ReminderDataItem): Boolean {
        if (reminderData.title.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (reminderData.location.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_select_location
            return false
        }
        return true
    }

    /**
     * pickup value into a ReminderDataItem
     */
    private fun getCurrentReminderItem(): ReminderDataItem {
        // new reminder
        return if (editEntryId == "") ReminderDataItem(
            reminderTitle.value,
            reminderDescription.value,
            reminderSelectedLocationStr.value,
            latitude.value,
            longitude.value
        ) else // edit reminder
            ReminderDataItem(
                reminderTitle.value,
                reminderDescription.value,
                reminderSelectedLocationStr.value,
                latitude.value,
                longitude.value,
                editEntryId
            )
    }
}