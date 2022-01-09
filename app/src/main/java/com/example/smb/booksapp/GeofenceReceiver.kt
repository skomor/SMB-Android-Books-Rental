package com.example.smb.booksapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import android.app.NotificationManager


class GeofenceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent!!)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes
                .getStatusCodeString(geofencingEvent.errorCode)
            Log.e("GEO", errorMessage)
            return
        }
        val geofenceTransition = geofencingEvent.geofenceTransition

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            val triggeringGeofences = geofencingEvent.triggeringGeofences

            sendNotification(context!!,geofenceTransition, triggeringGeofences )
        }

    }

    private fun sendNotification(context: Context, geofenceTransition: Int, triggeringGeofences: List<Geofence>) {

        var builder = NotificationCompat.Builder(context, "aosad")
            .setContentTitle("Geofence alert!")
            .setContentText("Broadcast worked")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        val mNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(1, builder.build())

    }
}
