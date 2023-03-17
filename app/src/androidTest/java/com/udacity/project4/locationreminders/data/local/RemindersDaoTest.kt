package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    //    TODO: Add testing implementation to the RemindersDao.kt
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun saveReminder_thenGetById() = runBlockingTest {
        // GIVEN - save a reminder
        val reminder = ReminderDTO(
            "Title1", "Description1", "Location1", 100.0, 100.0, "ID_1"
        )
        database.reminderDao().saveReminder(reminder)
        // WHEN - get reminder by id
        val loaded = database.reminderDao().getReminderById(reminder.id)
        // THEN - the loaded reminder should be identical to the input reminder
        assertThat(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
        assertThat(loaded.location, `is`(reminder.location))
    }

    @Test
    fun getReminders_loadAllRemindersInDatabase() = runBlockingTest {
        // GIVEN - insert two reminder item
        val reminder1 = ReminderDTO(
            "Title1", "Description1", "Location1", 100.0, 100.0, "ID_1"
        )
        val reminder2 = ReminderDTO(
            "Title2", "Description2", "Location2", 90.0, 90.0, "ID_2"
        )
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        // WHEN load all reminders
        val remindersList = database.reminderDao().getReminders()
        // THEN the loaded list should be identical to {reminder1, reminder2}
        assertThat(remindersList, `is`(listOf(reminder1, reminder2)))

    }

    @Test
    fun deleteReminder_deleteAllReminder() = runBlockingTest {
        // GIVEN - insert 3 reminder items
        val reminder1 = ReminderDTO(
            "Title1", "Description1", "Location1", 100.0, 100.0, "ID_1"
        )
        val reminder2 = ReminderDTO(
            "Title2", "Description2", "Location2", 90.0, 90.0, "ID_2"
        )
        val reminder3 = ReminderDTO(
            "Title3", "Description3", "Location3", 90.0, 90.0, "ID_3"
        )
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)
        // WHEN delete reminder1
        database.reminderDao().deleteEntry(reminder1.id)
        // THEN the reminder1 should be removed from database
        val load = database.reminderDao().getReminderById(reminder1.id)
        assertThat(load, `is`(nullValue()))
        // one more test
        // WHEN: delete all
        database.reminderDao().deleteAllReminders()
        // THEN: all reminder should be cleared
        val reminderList = database.reminderDao().getReminders()
        assertThat(reminderList, `is`(emptyList()))
    }

    @Test
    fun updateExistingReminder_getReminderById() = runBlockingTest {
        // GIVEN - insert reminder
        val reminder = ReminderDTO(
            "Title1", "Description1", "Location1", 100.0, 100.0, "ID_1"
        )
        database.reminderDao().saveReminder(reminder)
        // WHEN: update tile of reminder
        val updateReminder = database.reminderDao().getReminderById(reminder.id)?.apply {
            description = "Description1_updated"
            location = "Location1_updated"
        }
        if (updateReminder != null) {
            database.reminderDao().saveReminder(updateReminder)
        }
        // THEN: the reminder entry after the updated should be identical to updateReminder
        val afterUpdateLoaded = database.reminderDao().getReminderById(reminder.id)
        assertThat(afterUpdateLoaded, `is`(updateReminder))
    }
}