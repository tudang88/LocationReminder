package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
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

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var datasource: FakeDataSource

    @Before
    fun setupViewModel() {
        applicationContext = ApplicationProvider.getApplicationContext()
        // initialize fake data source and view model before each test
        datasource = FakeDataSource()
        // add some test data
        datasource.reminders = mutableListOf(reminder1, reminder2)
        // create view model for test
        saveReminderViewModel = SaveReminderViewModel(
            applicationContext,
            dataSource = datasource
        )
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    /**
     * check showLoading status when add new reminder
     */
    @Test
    fun saveReminder_showLoadingAndToast() {
        // temporary pause the coroutine until the first assert done
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.saveReminder(reminder3)
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))
        // resume to confirm next state of showLoading state
        mainCoroutineRule.resumeDispatcher()
        // THEN: the saveItem, toast message and loading state should be as following
        assertThat(saveReminderViewModel.saveItem.getOrAwaitValue(), `is`(reminder3))
        assertThat(
            saveReminderViewModel.showToast.getOrAwaitValue(), `is`(
                applicationContext.resources.getString(
                    R.string.reminder_saved
                )
            )
        )
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun loadReminderEntry_idNullStateAdd() {
        // GIVEN: default state ADD
        // WHEN: input null id
        saveReminderViewModel.loadReminderEntry(null)
        // THEN: the operation state remain ADD
        assertThat(
            saveReminderViewModel.operationState.getOrAwaitValue(),
            `is`(SaveReminderViewModel.AddEditState.ADD)
        )
    }


    @Test
    fun loadReminderEntry_idNotExistError() {
        // GIVEN: default state ADD
        val testErrorId = "123"
        // temporary pause the coroutine until the first assert done
        mainCoroutineRule.pauseDispatcher()
        // WHEN: input null id
        saveReminderViewModel.loadReminderEntry(testErrorId)
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))
        // resume to confirm next state of showLoading state
        mainCoroutineRule.resumeDispatcher()
        // THEN: the operation state EDIT, error message show
        assertThat(
            saveReminderViewModel.operationState.getOrAwaitValue(),
            `is`(SaveReminderViewModel.AddEditState.EDIT)
        )
        // showSnackBar error message
        assertThat(
            saveReminderViewModel.showSnackBar.getOrAwaitValue(),
            `is`("reminder id: $testErrorId not found")
        )
        // loading anime stop
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun validateEnteredData_reminderNoTitle() {
        // GIVEN
        val reminderNoTitle = ReminderDataItem(
            "", "Description4", "Location4", 103.0, 103.0, "ID_4"
        )
        // WHEN: validate
        saveReminderViewModel.validateAndSaveReminder(reminderNoTitle)

        // THEN: show SnackBar with error
        assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title)
        )
    }

    @Test
    fun validateEnteredData_reminderNoLocation() {
        // GIVEN
        val reminderNoLocation = ReminderDataItem(
            "Title5", "Description3", "", 103.0, 103.0, "ID_5"
        )
        // WHEN: validate
        saveReminderViewModel.validateAndSaveReminder(reminderNoLocation)

        // THEN: show SnackBar with error
        assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_select_location)
        )
    }

    @Test
    fun updateSelectedPoi_saveLatLng() {
        // GIVEN
        val testPoi = PointOfInterest(LatLng(90.0, 100.0),"",  "Selected location")

        // WHEN: add poi to view Model
        saveReminderViewModel.updateSelectedPoi(testPoi)

        // THEN
        assertThat(
            saveReminderViewModel.selectedPOI.getOrAwaitValue(), `is`(testPoi)
        )
        assertThat(
            saveReminderViewModel.latitude.getOrAwaitValue(), `is`(testPoi.latLng.latitude)
        )
        assertThat(
            saveReminderViewModel.longitude.getOrAwaitValue(), `is`(testPoi.latLng.longitude)
        )
        assertThat(
            saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(),
            `is`("Selected location")
        )
    }
}