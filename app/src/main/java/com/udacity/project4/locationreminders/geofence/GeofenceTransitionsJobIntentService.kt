package com.udacity.project4.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.errorMessage
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573

        fun enqueueWork(context: Context, intent: Intent) {
            Timber.d("enqueueWork action = ${intent.action}")
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        Timber.d("onHandleWork action = ${intent.action}")
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            Timber.d("onHandleWork geofencingEvent error()")
            val errorMessage = errorMessage(applicationContext, geofencingEvent.errorCode)
            Timber.e(errorMessage)
            return
        }
        // only process enter event
        if (Geofence.GEOFENCE_TRANSITION_ENTER == geofencingEvent.geofenceTransition) {
            Timber.d("onHandleWork GEOFENCE_TRANSITION_ENTER")
            // send notification
            sendNotification(geofencingEvent.triggeringGeofences)
        }
    }

    //TODO: get the request id of the current geofence
    private fun sendNotification(triggeringGeofences: List<Geofence>) {
        val requestId = triggeringGeofences[0].requestId
        Timber.d("onHandleWork GEOFENCE_TRANSITION_ENTER reminder_id: $requestId")
        //Get the local repository instance
        val remindersLocalRepository: ReminderDataSource by inject()
//        Interaction to the repository has to be through a coroutine scope
        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
            //get the reminder with the request id
            val result = remindersLocalRepository.getReminder(requestId)
            Timber.d("onHandleWork GEOFENCE_TRANSITION_ENTER reminder: $result")
            if (result is Result.Success<ReminderDTO>) {
                val reminderDTO = result.data
                Timber.d("onHandleWork GEOFENCE_TRANSITION_ENTER reminder: $reminderDTO")
                //send a notification to the user with the reminder details
                sendNotification(
                    this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                        reminderDTO.title,
                        reminderDTO.description,
                        reminderDTO.location,
                        reminderDTO.latitude,
                        reminderDTO.longitude,
                        reminderDTO.id
                    )
                )
            }
        }
    }

}
