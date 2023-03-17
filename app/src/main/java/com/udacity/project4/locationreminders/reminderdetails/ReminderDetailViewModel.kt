package com.udacity.project4.locationreminders.reminderdetails

import android.app.Application
import androidx.lifecycle.*
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch
import timber.log.Timber

class ReminderDetailViewModel(app: Application, private val dataSource: ReminderDataSource) :
    BaseViewModel(app) {
    private val _reminderEntryId = MutableLiveData<String>()
    val reminderEntryId: LiveData<String>
        get() = _reminderEntryId

    private val _reminderEntry = MutableLiveData<ReminderDataItem?>()
    val reminderEntry: LiveData<ReminderDataItem?>
        get() = _reminderEntry

    /**
     * load Reminder entry from database
     */
    fun loadReminderEntry(id: String) {
        Timber.d("start loadReminderEntry")
        showLoading.value = true
        viewModelScope.launch {
            //interacting with the dataSource has to be through a coroutine
            val result = dataSource.getReminder(id)
            // Trigger the load
            _reminderEntryId.value = id
            showLoading.postValue(false)
            when (result) {
                is Result.Success<ReminderDTO> -> {
                    //map the reminder data from the DB to the be ready to be displayed on the UI
                    _reminderEntry.value = ReminderDataItem(
                        result.data.title,
                        result.data.description,
                        result.data.location,
                        result.data.latitude,
                        result.data.longitude,
                        result.data.id
                    )
                }
                is Result.Error -> {
                    showSnackBar.value = result.message
                    _reminderEntry.value = null
                    _reminderEntryId.value = null
                }
            }
        }
    }

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        _reminderEntryId.value = null
        _reminderEntry.value = null
    }

    private fun navigationBack() {
        navigationCommand.postValue(NavigationCommand.Back)
    }

    /**
     * delete item
     */
    fun deleteItem() {
        viewModelScope.launch {
            Timber.d("delete entryId ${_reminderEntryId.value}")
            dataSource.deleteReminder(_reminderEntryId.value!!)
            navigationBack()
        }
    }
}