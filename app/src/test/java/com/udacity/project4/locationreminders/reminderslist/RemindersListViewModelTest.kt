package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.convertToDomain
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {
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

    // switch main dispatcher to test dispatcher
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components
    // this rule will run all architecture components have background jobs in the same thread
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var reminderListViewModel: RemindersListViewModel
    private lateinit var datasource: FakeDataSource

    @Before
    fun setupViewModel() {
        applicationContext = ApplicationProvider.getApplicationContext()
        // initialize fake data source and view model before each test
        datasource = FakeDataSource()
        // add some test data
        datasource.reminders = mutableListOf(reminder1, reminder2)
        // create view model for test
        reminderListViewModel = RemindersListViewModel(
            applicationContext,
            dataSource = datasource
        )
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun loadReminders_loadingAnimeIdList() {
        // temporary pause the coroutine until the first assert done
        mainCoroutineRule.pauseDispatcher()
        // WHEN
        reminderListViewModel.loadReminders()
        assertThat(
            reminderListViewModel.showLoading.getOrAwaitValue(),
            `is`(true)
        )
        // resume to confirm next state of showLoading state
        mainCoroutineRule.resumeDispatcher()
        // THEN loading anime stop, and we got a list of ids
        assertThat(
            reminderListViewModel.showLoading.getOrAwaitValue(),
            `is`(false)
        )
        // the id list is same as the id list available on datasource
        assertThat(
            reminderListViewModel.reminderIds,
            `is`(listOf(reminder1.id, reminder2.id))
        )
        // confirm loaded items
        assertThat(
            reminderListViewModel.remindersList.getOrAwaitValue(),
            `is`(listOf(reminder1.convertToDomain(), reminder2.convertToDomain()))
        )
    }

    @Test
    fun loadRemindersError_showErrorMessage() {
        // GIVEN
        datasource.setReturnError(true)
        // WHEN
        reminderListViewModel.loadReminders()
        // THEN
        // showSnackBar error message
        assertThat(
            reminderListViewModel.showSnackBar.getOrAwaitValue(),
            CoreMatchers.`is`("Reminders not found")
        )
    }

    @Test
    fun loadRemindersNoData_showNoData() {
        // GIVEN: data source empty
        mainCoroutineRule.runBlockingTest {
            datasource.deleteAllReminders()
        }
        // WHEN: loadReminders
        reminderListViewModel.loadReminders()
        // THEN: remindersList is empty, showNoData true
        assertThat(reminderListViewModel.remindersList.getOrAwaitValue(), `is`(arrayListOf()))
        assertThat(reminderListViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun clearAllReminders_resetAllVariable() {
        // GIVEN: datasource with two reminder
        // temporary pause the coroutine until the first assert done
        mainCoroutineRule.pauseDispatcher()
        // WHEN: clearAllReminder
        reminderListViewModel.clearAllReminders()
        assertThat(
            reminderListViewModel.showLoading.getOrAwaitValue(),
            `is`(true)
        )
        // THEN
        // resume to confirm next state of showLoading state
        mainCoroutineRule.resumeDispatcher()
        // stop loading
        assertThat(
            reminderListViewModel.showLoading.getOrAwaitValue(),
            `is`(false)
        )
        // empty list items
        assertThat(reminderListViewModel.remindersList.getOrAwaitValue(), `is`(arrayListOf()))
        // empty id list
        assertThat(reminderListViewModel.reminderIds, `is`(emptyList<String>()))
        // showNoData
        assertThat(reminderListViewModel.showNoData.getOrAwaitValue(), `is`(true))
        // show SnackBar
        assertThat(
            reminderListViewModel.showSnackBar.getOrAwaitValue(),
            `is`("Clear all reminders")
        )
    }
}