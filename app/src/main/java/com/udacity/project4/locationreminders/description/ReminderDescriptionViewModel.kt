package com.udacity.project4.locationreminders.description

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch
import timber.log.Timber

class ReminderDescriptionViewModel(private val dataSource: ReminderDataSource) : ViewModel() {
    private val _reminderItem = MutableLiveData<ReminderDataItem>()
    val reminderItem: LiveData<ReminderDataItem>
        get() = _reminderItem

    /**
     * save id
     */
    fun saveItem(item: ReminderDataItem) {
        _reminderItem.value = item
    }

    fun getItemId() = reminderItem.value?.id

    /**
     * delete item
     */
    fun deleteItem() {
        viewModelScope.launch {
            Timber.d("delete entryId ${_reminderItem.value?.id}")
            _reminderItem.value?.let { dataSource.deleteReminder(it.id) }
        }
        _reminderItem.value = null
    }
}