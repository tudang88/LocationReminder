package com.udacity.project4.locationreminders.description

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result.*
import com.udacity.project4.locationreminders.data.dto.convertToDomain
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
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
class ReminderDescriptionViewModelTest {
    private val reminder1 = ReminderDTO(
        "Title1", "Description1", "Location1", 100.0, 100.0, "ID_1"
    )
    private val reminder2 = ReminderDTO(
        "Title2", "Description2", "Location2", 102.0, 102.0, "ID_2"
    )

    //TODO: provide testing to the SaveReminderView and its live data objects
    // switch main dispatcher to test dispatcher
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components
    // this rule will run all architecture components have background jobs in the same thread
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var reminderDescriptionViewModel: ReminderDescriptionViewModel
    private lateinit var datasource: FakeDataSource

    @Before
    fun setupViewModel() {
        // initialize fake data source and view model before each test
        datasource = FakeDataSource()
        // add some test data
        datasource.reminders = mutableListOf(reminder1, reminder2)
        // create view model for test
        reminderDescriptionViewModel = ReminderDescriptionViewModel(
            datasource
        )
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun saveItem_confirmLiveData() {
        // GIVEN: test reminder to show
        val reminder3 = ReminderDataItem(
            "Title3", "Description3", "Location3", 103.0, 103.0, "ID_3"
        )
        // WHEN: save to live data
        reminderDescriptionViewModel.saveItem(reminder3)
        // THEN: the live data should storage the reminder
        MatcherAssert.assertThat(
            reminderDescriptionViewModel.reminderItem.getOrAwaitValue(),
            CoreMatchers.`is`(reminder3)
        )
    }

    @Test
    fun deleteItem_removeItemOnDatasource() {
        // GIVEN: the reminder has already stored to show
        reminderDescriptionViewModel.saveItem(reminder1.convertToDomain())
        // WHEN: user click delete
        reminderDescriptionViewModel.deleteItem()
        // THEN: the stored reminder should be cleared
        // the item also removed from datasource
        mainCoroutineRule.runBlockingTest {
            val result = datasource.getReminder(reminder1.id)
            MatcherAssert.assertThat(result as Error, `is`(Error("reminder id: ${reminder1.id} not found")))
        }
        MatcherAssert.assertThat(
            reminderDescriptionViewModel.reminderItem.getOrAwaitValue(),
            CoreMatchers.`is`(nullValue())
        )
    }
}