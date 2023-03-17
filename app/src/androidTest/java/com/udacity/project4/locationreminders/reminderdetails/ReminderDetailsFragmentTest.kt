package com.udacity.project4.locationreminders.reminderdetails

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
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.test.runBlockingTest
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderDetailsFragmentTest : AutoCloseKoinTest() {
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
    fun inputUnavailableReminderIdShowError() = runBlockingTest {
        // prepare repository with one item
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
        // GIVEN - the bundle to start fragment, but specify wrong reminder_id
        val bundle = ReminderDetailsFragmentArgs("ID_2").toBundle()
        // WHEN - display the fragment
        launchFragmentInContainer<ReminderDetailsFragment>(bundle, R.style.AppTheme)

        // THEN - error message snackbar will be shown
        onView(withId(R.id.reminderTitleText)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderTitleText)).check(matches(withText("")))
        onView(withId(R.id.reminderDescriptionText)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderDescriptionText)).check(matches(withText("")))
        onView(withId(R.id.selectLocationText)).check(matches(isDisplayed()))
        onView(withId(R.id.selectLocationText)).check(matches(withText(R.string.reminder_location)))
        onView(withId(R.id.editReminder)).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText("reminder id: ID_2 not found")))
    }

    @Test
    fun showReminderDetails_navigateToEdit() = runBlockingTest {
        // prepare repository with one item
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
        // GIVEN - the bundle to start fragment, but specify wrong reminder_id
        val bundle = ReminderDetailsFragmentArgs("ID_1").toBundle()
        // WHEN - display the fragment
        val navController = mock(NavController::class.java)
        val scenario = launchFragmentInContainer<ReminderDetailsFragment>(bundle, R.style.AppTheme)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        // confirm show detail
        onView(withId(R.id.reminderTitleText)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderTitleText)).check(matches(withText("TITLE1")))
        onView(withId(R.id.reminderDescriptionText)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderDescriptionText)).check(matches(withText("DESCRIPTION1")))
        onView(withId(R.id.selectLocationText)).check(matches(isDisplayed()))
        onView(withId(R.id.selectLocationText)).check(matches(withText(R.string.reminder_location)))
        onView(withId(R.id.selectedLocationText)).check(matches(withText("LOCATION1")))

        onView(withId(R.id.editReminder)).check(matches(isDisplayed()))
        // THEN - navigate to SaveReminderFragment for editing
        onView(withId(R.id.editReminder)).perform(click())
        // verify navigation
        verify(navController).navigate(ReminderDetailsFragmentDirections.actionReminderDetailsFragmentToSaveReminderFragment("ID_1"))
    }
}