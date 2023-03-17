package com.udacity.project4

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.authentication.AuthenticationActivityViewModel
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.description.ReminderDescriptionViewModel
import com.udacity.project4.locationreminders.reminderdetails.ReminderDetailViewModel
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.junit.After
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

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class AppNavigationTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            viewModel {
                ReminderDetailViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            viewModel {
                ReminderDescriptionViewModel(
                    get() as ReminderDataSource
                )
            }
            single {
                // authentication viewModel
                AuthenticationActivityViewModel()
            }

            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            androidContext(appContext)
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()
        saveReminderViewModel = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * register idling resource for end to en testing
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * unregister after test
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @Test
    fun clickReminder_openDetailFragment_deleteItem_backToReminderList() = runBlocking {
        repository.saveReminder(
            ReminderDTO(
                "TITLE_1",
                "DESCRIPTION_1",
                "LOCATION_1",
                90.0,
                90.0,
                "ID_1"
            )
        )
        repository.saveReminder(
            ReminderDTO(
                "TITLE_2",
                "DESCRIPTION_2",
                "LOCATION_2",
                90.0,
                90.0,
                "ID_2"
            )
        )
        // 1. start activity with two items on list

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        // 2. click first item
        onView(withText("TITLE_1")).perform(click())
        // 3. open Details Fragment
        onView(withId(R.id.reminderTitleText)).check(matches(withText("TITLE_1")))
        onView(withId(R.id.reminderDescriptionText)).check(matches(withText("DESCRIPTION_1")))
        onView(withId(R.id.selectedLocationText)).check(matches(withText("LOCATION_1")))
        // 4. click delete
        onView(withId(R.id.menu_delete)).perform(click())
        // 5. navigate back to reminderList and the deleted item has been removed
        onView(withText("TITLE_1")).check(doesNotExist())
        activityScenario.close()
    }

    @Test
    fun deleteAllItem_showNoData() = runBlocking {
        repository.saveReminder(
            ReminderDTO(
                "TITLE_1",
                "DESCRIPTION_1",
                "LOCATION_1",
                90.0,
                90.0,
                "ID_1"
            )
        )
        repository.saveReminder(
            ReminderDTO(
                "TITLE_2",
                "DESCRIPTION_2",
                "LOCATION_2",
                90.0,
                90.0,
                "ID_2"
            )
        )
        // 1. start activity with two items on list

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        onView(withText("TITLE_1")).check(matches(isDisplayed()))
        onView(withText("TITLE_2")).check(matches(isDisplayed()))
        // 2. click clear all item
        openContextualActionModeOverflowMenu()
        onView(withText(R.string.menu_clear)).perform(click())
        // 5. navigate back to reminderList and the deleted item has been removed
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
        activityScenario.close()
    }
}
