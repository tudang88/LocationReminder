package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {
    // the force error flag for test error case
    private var _shouldReturnError: Boolean = false
    fun setReturnError(value: Boolean) {
        _shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (_shouldReturnError) {
            return Result.Error("Reminders not found")
        }
        reminders?.let {
            return Result.Success(ArrayList(it))
        }
        return Result.Error("Reminders not found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (reminders.isNullOrEmpty()) {
            return Result.Error("reminder id: $id not found")
        }
        for (reminder in reminders!!) {
            if (reminder.id == id) {
                return Result.Success(reminder)
            }
        }
        return Result.Error("reminder id: $id not found")
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }

    override suspend fun deleteReminder(id: String) {
        reminders?.removeIf { id == it.id }
    }


}