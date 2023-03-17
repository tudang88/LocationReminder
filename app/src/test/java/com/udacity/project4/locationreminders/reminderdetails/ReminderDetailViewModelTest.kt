package com.udacity.project4.locationreminders.reminderdetails

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.convertToDomain
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class ReminderDetailViewModelTest {
    private val reminder1 = ReminderDTO(
        "Title1", "Description1", "Location1", 100.0, 100.0, "ID_1"
    )
    private val reminder2 = ReminderDTO(
        "Title2", "Description2", "Location2", 102.0, 102.0, "ID_2"
    )
    private val reminder3 = ReminderDataItem(
        "Title3", "Description3", "Location3", 103.0, 103.0, "ID_3"
    )

    private lateinit var applicationContext: Application

    //TODO: provide testing to the SaveReminderView and its live data objects
    // switch main dispatcher to test dispatcher
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components
    // this rule will run all architecture components have background jobs in the same thread
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var reminderDetailViewModel: ReminderDetailViewModel
    private lateinit var datasource: FakeDataSource

    @Before
    fun setupViewModel() {
        applicationContext = ApplicationProvider.getApplicationContext()
        // initialize fake data source and view model before each test
        datasource = FakeDataSource()
        // add some test data
        datasource.reminders = mutableListOf(reminder1, reminder2)
        // create view model for test
        reminderDetailViewModel = ReminderDetailViewModel(
            applicationContext,
            dataSource = datasource
        )
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun loadReminderEntry_idNotExistError() {
        // GIVEN: default state ADD
        val testErrorId = "123"
        // temporary pause the coroutine until the first assert done
        mainCoroutineRule.pauseDispatcher()
        // WHEN: input null id
        reminderDetailViewModel.loadReminderEntry(testErrorId)
        MatcherAssert.assertThat(
            reminderDetailViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(true)
        )
        // resume to confirm next state of showLoading state
        mainCoroutineRule.resumeDispatcher()
        // THEN:
        // showSnackBar error message
        MatcherAssert.assertThat(
            reminderDetailViewModel.showSnackBar.getOrAwaitValue(),
            CoreMatchers.`is`("reminder id: $testErrorId not found")
        )
        // reminder livedata and reminder id live data should be null
        MatcherAssert.assertThat(
            reminderDetailViewModel.reminderEntryId.getOrAwaitValue(),
            `is`(nullValue())
        )
        MatcherAssert.assertThat(
            reminderDetailViewModel.reminderEntry.getOrAwaitValue(),
            `is`(nullValue())
        )
        // loading anime stop
        MatcherAssert.assertThat(
            reminderDetailViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(false)
        )
    }

    @Test
    fun loadReminderEntry_foundReminder() {
        // GIVEN: default state ADD
        val testId = reminder1.id
        // temporary pause the coroutine until the first assert done
        mainCoroutineRule.pauseDispatcher()
        // WHEN: input null id
        reminderDetailViewModel.loadReminderEntry(testId)
        MatcherAssert.assertThat(
            reminderDetailViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(true)
        )
        // resume to confirm next state of showLoading state
        mainCoroutineRule.resumeDispatcher()
        // THEN:
        // got reminder from database
        MatcherAssert.assertThat(
            reminderDetailViewModel.reminderEntry.getOrAwaitValue(),
            CoreMatchers.`is`(reminder1.convertToDomain())
        )
        MatcherAssert.assertThat(
            reminderDetailViewModel.reminderEntryId.getOrAwaitValue(),
            CoreMatchers.`is`(reminder1.id)
        )
        // loading anime stop
        MatcherAssert.assertThat(
            reminderDetailViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(false)
        )
    }

    @Test
    fun onClear_resetAllLiveData() {
        // GIVEN: already load one entry
        reminderDetailViewModel.loadReminderEntry(reminder1.id)

        // WHEN: clear all
        reminderDetailViewModel.onClear()
        // THEN: live data should be cleared
        // reminder livedata and reminder id live data should be null
        MatcherAssert.assertThat(
            reminderDetailViewModel.reminderEntryId.getOrAwaitValue(),
            `is`(nullValue())
        )
        MatcherAssert.assertThat(
            reminderDetailViewModel.reminderEntry.getOrAwaitValue(),
            `is`(nullValue())
        )
    }
}