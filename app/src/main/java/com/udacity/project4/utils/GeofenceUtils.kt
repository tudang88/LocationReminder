package com.udacity.project4.utils

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Geofence utils constants
 */
const val ACTION_GEOFENCE_EVENT = "LocationReminders.action.ACTION_GEOFENCE_EVENT"
const val GEOFENCE_RADIUS_IN_METERS = 100f
val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)

/**
 * Returns the error string for a geofencing error code.
 */
fun errorMessage(context: Context, errorCode: Int): String {
    val resources = context.resources
    return when (errorCode) {
        GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> resources.getString(
            R.string.geofence_not_available
        )
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> resources.getString(
            R.string.geofence_too_many_geofences
        )
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> resources.getString(
            R.string.geofence_too_many_pending_intents
        )
        else -> resources.getString(R.string.geofence_unknown_error)
    }
}

/**
 * Build the Geofence Object
 */
fun buildGeofence(reminder: ReminderDataItem): Geofence? {
    val latitude = reminder.latitude
    val longitude = reminder.longitude
    if (latitude != null && longitude != null) {
        return Geofence.Builder()
            .setRequestId(reminder.id)
            .setCircularRegion(
                latitude,
                longitude,
                GEOFENCE_RADIUS_IN_METERS
            )
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()
    }
    return null
}

/**
 * create Geofencing Request
 */
fun buildGeofencingRequest(geofence: Geofence): GeofencingRequest {
    return GeofencingRequest.Builder()
        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
        .addGeofence(geofence)
        .build()
}

/**
 * add geofence utility
 * the procedure for adding geofence too complicated,
 * so separating it make easier to read
 */
fun addGeofence(
    context: Context,
    geofencingClient: GeofencingClient,
    geofencePendingIntent: PendingIntent,
    reminder: ReminderDataItem,
    successCallback: () -> Unit,
    failureCallback: (error: String) -> Unit
) {
    Timber.tag("GeofenceUtils").d("add geofence Reminder: $reminder")
    val geofence = buildGeofence(reminder)
    if (geofence != null
        && ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        // remove existing geofence of the same id
        geofencingClient.removeGeofences(listOf(reminder.id))?.run {
            // Regardless of success/failure of the removal, add the new geofence
            addOnCompleteListener {
                geofencingClient.addGeofences(
                    buildGeofencingRequest(geofence),
                    geofencePendingIntent
                )
                    .addOnSuccessListener {
                        successCallback()
                    }
                    .addOnFailureListener {
                        failureCallback("Failed to add geofence")
                    }
            }
        }

    }

}

/**
 * remove geofence
 */
fun removeGeofence(
    geofencingClient: GeofencingClient,
    id: String,
    successCallback: () -> Unit,
    failureCallback: (error: String) -> Unit
) {
    geofencingClient.removeGeofences(listOf(id))
        .addOnSuccessListener {
            successCallback()
        }
        .addOnFailureListener {
            failureCallback("Failed to remove geofence")
        }
}

/**
 * remove all geofences
 */
fun removeAllGeofences(
    geofencingClient: GeofencingClient,
    geofence_ids: List<String>,
    successCallback: () -> Unit,
    failureCallback: (error: String) -> Unit
) {
    Timber.d("remove geofence list: $geofence_ids")
    geofencingClient.removeGeofences(geofence_ids)
        .addOnSuccessListener {
            successCallback()
        }
        .addOnFailureListener {
            failureCallback("Failed to remove geofence")
        }
}