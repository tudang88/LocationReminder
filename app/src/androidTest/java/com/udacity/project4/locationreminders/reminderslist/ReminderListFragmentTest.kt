package com.udacity.project4.locationreminders.reminderslist


import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.FakeAndroidTestDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.reminderdetails.ReminderDetailViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
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
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.test.runBlockingTest
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {
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
    fun clickAddReminderButton_navigateToSaveReminderFragment() = runBlockingTest {
        // GIVEN - show list screen with no data
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        // confirm no data display
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))

        // WHEN - click add button
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN - verify that screen is navigated to the save reminder screen
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder(null))
    }

    @Test
    fun clickReminderItemOnList_navigateToReminderDetailFragment() = runBlockingTest {
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
        repository.saveReminder(
            ReminderDTO(
                "TITLE2",
                "DESCRIPTION2",
                "LOCATION2",
                90.0,
                90.0,
                "ID_2"
            )
        )
        // GIVEN - show list screen with two 2 items
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - click the first reminder
        onView(withId(R.id.reminderssRecyclerView)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("TITLE1")),
                click()
            )
        )

        // THEN - verify that screen is navigated to the save reminder screen
        verify(navController).navigate(ReminderListFragmentDirections.toDetailsReminder("ID_1"))
    }
}