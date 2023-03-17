package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.FakeAndroidTestDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.reminderdetails.ReminderDetailViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import com.udacity.project4.R
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import kotlinx.coroutines.test.runBlockingTest
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class SaveReminderFragmentTest : AutoCloseKoinTest() {
    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val koinModule = module {
        //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
        viewModel {
            RemindersListViewModel(
                get(),
                get() as ReminderDataSource
            )
        }
        viewModel {
            ReminderDetailViewModel(
                get(),
                get() as ReminderDataSource
            )
        }
        single {
            SaveReminderViewModel(
                appContext,
                get() as ReminderDataSource
            )
        }
        // use fake repository instead of the real one
        single { FakeAndroidTestDataSource() as ReminderDataSource }
    }

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
        //declare a new koin module
        startKoin {
            androidContext(appContext)
            modules(listOf(koinModule))
        }
        //Get our real repositor
        repository = get()
        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Test
    fun addNewReminder_onlyShowHintInfo_saveEmptyInfo_showError() {
        // GIVEN - the bundle to start fragment contain null reminder_id
        val bundle = SaveReminderFragmentArgs(null).toBundle()
        // WHEN - display the fragment
        launchFragmentInContainer<SaveReminderFragment>(bundle, R.style.AppTheme)

        // THEN - the screen only show hint info
        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderTitle)).check(matches(withText("")))
        onView(withId(R.id.reminderDescription)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderDescription)).check(matches(withText("")))
        onView(withId(R.id.selectLocation)).check(matches(isDisplayed()))
        onView(withId(R.id.selectLocation)).check(matches(withText(R.string.reminder_location)))
        onView(withId(R.id.saveReminder)).check(matches(isDisplayed()))

        // WHEN - press save with empty info show error
        onView(withId(R.id.saveReminder)).perform(click())
        // THEN - error message snackbar will be shown
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.err_enter_title)))
    }

    @Test
    fun editExistingReminder_navigateToReminderList() = runBlockingTest {
        // GIVEN - the bundle to start fragment contain id
        repository.saveReminder(
            ReminderDTO(
                "TITLE1",
                "DESCRIPTION1",
                "LOCATION1",
                90.0,
                90.0,
                "ID_1"
            )
        )
        val bundle = SaveReminderFragmentArgs("ID_1").toBundle()
        val navController = mock(NavController::class.java)

        // WHEN - display the fragment with details info
        val scenario = launchFragmentInContainer<SaveReminderFragment>(bundle, R.style.AppTheme)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        // confirm current info
        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderTitle)).check(matches(withText("TITLE1")))
        onView(withId(R.id.reminderDescription)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderDescription)).check(matches(withText("DESCRIPTION1")))
        onView(withId(R.id.selectLocation)).check(matches(isDisplayed()))
        onView(withId(R.id.selectLocation)).check(matches(withText(R.string.reminder_location)))
        onView(withId(R.id.selectedLocation)).check(matches(withText("LOCATION1")))
        onView(withId(R.id.saveReminder)).check(matches(isDisplayed()))
        // click save navigate to ReminderList
        onView(withId(R.id.saveReminder)).perform(click())
        // THEN - verify navigate screen
        verify(navController).popBackStack()
    }

    @Test
    fun editExistingReminder_navigateToSelectLocationFragment() = runBlockingTest {
        // GIVEN - the bundle to start fragment contain id
        repository.saveReminder(
            ReminderDTO(
                "TITLE1",
                "DESCRIPTION1",
                "LOCATION1",
                90.0,
                90.0,
                "ID_1"
            )
        )
        val bundle = SaveReminderFragmentArgs("ID_1").toBundle()
        val navController = mock(NavController::class.java)

        // WHEN - display the fragment with details info
        val scenario = launchFragmentInContainer<SaveReminderFragment>(bundle, R.style.AppTheme)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        // confirm current info
        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderTitle)).check(matches(withText("TITLE1")))
        onView(withId(R.id.reminderDescription)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderDescription)).check(matches(withText("DESCRIPTION1")))
        onView(withId(R.id.selectLocation)).check(matches(isDisplayed()))
        onView(withId(R.id.selectLocation)).check(matches(withText(R.string.reminder_location)))
        onView(withId(R.id.selectedLocation)).check(matches(withText("LOCATION1")))
        onView(withId(R.id.saveReminder)).check(matches(isDisplayed()))
        // click save navigate to ReminderList
        onView(withId(R.id.selectLocation)).perform(click())
        // THEN - verify navigate screen
        verify(navController).navigate(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
    }

    @Test
    fun addNewReminder_navigateToSelectLocationFragment() = runBlockingTest {
        // GIVEN - the bundle to start fragment contain id

        val bundle = SaveReminderFragmentArgs(null).toBundle()
        val navController = mock(NavController::class.java)

        // WHEN - display the fragment with details info
        val scenario = launchFragmentInContainer<SaveReminderFragment>(bundle, R.style.AppTheme)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        // confirm current info
        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderTitle)).check(matches(withText("")))
        onView(withId(R.id.reminderDescription)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderDescription)).check(matches(withText("")))
        onView(withId(R.id.selectLocation)).check(matches(isDisplayed()))
        onView(withId(R.id.selectLocation)).check(matches(withText(R.string.reminder_location)))
        onView(withId(R.id.saveReminder)).check(matches(isDisplayed()))
        // click save navigate to ReminderList
        onView(withId(R.id.selectLocation)).perform(click())
        // THEN - verify navigate screen
        verify(navController).navigate(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
    }
}