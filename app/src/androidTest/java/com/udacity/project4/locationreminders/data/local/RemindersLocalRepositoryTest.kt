package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    private lateinit var dataSource: ReminderDataSource
    private lateinit var database: RemindersDatabase

    @Before
    fun setupDb() {
        // Using an in-memory database for testing, because it doesn't survive killing the process.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        // create data source under test
        dataSource = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDb() {
        database.close()
    }

    // runBlocking is used here because of https://github.com/Kotlin/kotlinx.coroutines/issues/1204
    // Replace with runBlockingTest once issue is resolved
    @Test
    fun saveReminder_retrieveReminderById() = runBlocking {
        // GIVEN - save a reminder
        val reminder = ReminderDTO(
            "Title1", "Description1", "Location1", 100.0, 100.0, "ID_1"
        )
        dataSource.saveReminder(reminder)
        // WHEN - retrieve reminder
        val result = dataSource.getReminder(reminder.id)
        // THEN - result Success
        assertThat(result as Result.Success, `is`(Result.Success(reminder)))
    }

    // runBlocking is used here because of https://github.com/Kotlin/kotlinx.coroutines/issues/1204
    // Replace with runBlockingTest once issue is resolved
    @Test
    fun saveReminders_retrieveAllReminders() = runBlocking {
        // GIVEN - save a list of reminders
        val reminder1 = ReminderDTO(
            "Title1", "Description1", "Location1", 100.0, 100.0, "ID_1"
        )
        val reminder2 = ReminderDTO(
            "Title2", "Description2", "Location2", 100.0, 100.0, "ID_2"
        )
        val reminder3 = ReminderDTO(
            "Title3", "Description3", "Location3", 100.0, 100.0, "ID_3"
        )
        dataSource.saveReminder(reminder1)
        dataSource.saveReminder(reminder2)
        dataSource.saveReminder(reminder3)

        // WHEN - retrieve reminder
        val result = dataSource.getReminders()
        // THEN - result Success with the list of available reminder
        assertThat(
            result as Result.Success,
            `is`(Result.Success(listOf(reminder1, reminder2, reminder3)))
        )
    }

    // runBlocking is used here because of https://github.com/Kotlin/kotlinx.coroutines/issues/1204
    // Replace with runBlockingTest once issue is resolved
    @Test
    fun saveReminders_deleteAll_retrieveAllReminders() = runBlocking {
        // GIVEN - save a list of reminders
        val reminder1 = ReminderDTO(
            "Title1", "Description1", "Location1", 100.0, 100.0, "ID_1"
        )
        val reminder2 = ReminderDTO(
            "Title2", "Description2", "Location2", 100.0, 100.0, "ID_2"
        )
        val reminder3 = ReminderDTO(
            "Title3", "Description3", "Location3", 100.0, 100.0, "ID_3"
        )
        dataSource.saveReminder(reminder1)
        dataSource.saveReminder(reminder2)
        dataSource.saveReminder(reminder3)

        // WHEN - delete all reminders
        dataSource.deleteAllReminders()
        // THEN - result Success with the empty list due to all reminders has been cleared
        val result = dataSource.getReminders()
        assertThat(
            result as Result.Success,
            `is`(Result.Success(emptyList()))
        )
    }

    // runBlocking is used here because of https://github.com/Kotlin/kotlinx.coroutines/issues/1204
    // Replace with runBlockingTest once issue is resolved
    @Test
    fun updateReminder_confirmResult_deleteById() = runBlocking {
        // GIVEN - save a list of reminders
        val reminder1 = ReminderDTO(
            "Title1", "Description1", "Location1", 100.0, 100.0, "ID_1"
        )
        val reminder2 = ReminderDTO(
            "Title2", "Description2", "Location2", 100.0, 100.0, "ID_2"
        )
        val reminder3 = ReminderDTO(
            "Title3", "Description3", "Location3", 100.0, 100.0, "ID_3"
        )
        dataSource.saveReminder(reminder1)
        dataSource.saveReminder(reminder2)
        dataSource.saveReminder(reminder3)

        // WHEN - update the first entry
        val updateEntry1 =
            reminder1.apply { title = "Title1_updated"; location = "Location1_updated" }
        dataSource.saveReminder(updateEntry1)
        // THEN - the first entry should be identical to updateEntry1
        val result = dataSource.getReminder(reminder1.id)
        assertThat(
            result as Result.Success,
            `is`(Result.Success(updateEntry1))
        )

        // one more test
        // WHEN - delete second entry
        dataSource.deleteReminder(reminder2.id)
        // THEN - the 2nd entry should be remove from database
        val result2 = dataSource.getReminder(reminder2.id)
        assertThat(result2 as Result.Error, `is`(Result.Error("Reminder not found!")))
    }
}