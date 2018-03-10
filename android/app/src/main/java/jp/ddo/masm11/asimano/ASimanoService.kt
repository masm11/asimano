/*  ASimano - Simple Mail Notification for Android.
    Copyright (C) 2018 Yuuki Harano

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package jp.ddo.masm11.asimano

import android.app.Service
import android.app.PendingIntent
import android.app.NotificationManager
import android.app.NotificationChannel
import android.app.Notification
import android.app.TaskStackBuilder
import android.os.Bundle
import android.os.IBinder
import android.os.Binder
import android.content.ComponentName
import android.content.Intent
import android.content.Context
import android.net.Uri
import java.time.ZonedDateTime
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.FileNotFoundException

class ASimanoService: Service() {
    
    interface OnStatusChangedListener {
        fun onStatusChanged(nr: Int)
    }
    
    private lateinit var notificationManager: NotificationManager
    private lateinit var statusChangedListeners: MutableSet<OnStatusChangedListener>
    
    override fun onCreate() {
	notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
	val channel_01 = NotificationChannel("new_mail_01", getResources().getString(R.string.channel_new_mail_01), NotificationManager.IMPORTANCE_DEFAULT)
	val channel_02 = NotificationChannel("new_mail_02", getResources().getString(R.string.channel_new_mail_02), NotificationManager.IMPORTANCE_LOW)
	val channel_fore_01 = NotificationChannel("fore_01", getResources().getString(R.string.channel_fore_01), NotificationManager.IMPORTANCE_LOW)
	notificationManager.createNotificationChannel(channel_01)
	notificationManager.createNotificationChannel(channel_02)
	notificationManager.createNotificationChannel(channel_fore_01)
	
        statusChangedListeners = MutableWeakSet<OnStatusChangedListener>()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
	if (intent != null) {
	    val action = intent.getAction()
	    android.util.Log.d("ASimanoService", "action=" + action)
	    if (action == "jp.ddo.masm11.asimano.MESSAGE")
	        handleMessage(intent)
	}
	
	return START_STICKY
    }
    
    private fun handleMessage(intent: Intent) {
	setForeNotification(true)
	
	try {
	    var stamp: ZonedDateTime
	    var unread: Int
	    try {
		stamp = intent.getSerializableExtra("jp.ddo.masm11.asimano.STAMP") as ZonedDateTime
		unread = intent.getIntExtra("jp.ddo.masm11.asimano.UNREAD", -1)
		android.util.Log.d("ASimanoService", "stamp=" + stamp)
		android.util.Log.d("ASimanoService", "unread=" + unread)
	    } catch (e: Exception) {
		android.util.Log.e("ASimanoService", e.toString(), e)
		return
	    }
	    
	    if (unread < 0) {
		android.util.Log.e("ASimanoService", "No unread parameter.")
		return
	    }
	    
	    var old_unread: Int = 0
	    try {
		BufferedReader(InputStreamReader(openFileInput("state.txt"))).use<BufferedReader, Unit> {
		    val old_stamp_str = it.readLine()
		    val old_unread_str = it.readLine()
		    val old_stamp = ZonedDateTime.parse(old_stamp_str)
		    old_unread = old_unread_str.toInt()
		    
		    if (stamp.compareTo(old_stamp) <= 0) {
			android.util.Log.d("ASimanoService", "incoming stamp is older.")
			return
		    }
		}
	    } catch (e: FileNotFoundException) {
		android.util.Log.d("ASimanoService", e.toString(), e)
	    } catch (e: Exception) {
		android.util.Log.e("ASimanoService", e.toString(), e)
	    }
	    
	    try {
		android.util.Log.d("ASimanoService", "saving state.")
		BufferedWriter(OutputStreamWriter(openFileOutput("state.txt", MODE_PRIVATE))).use<BufferedWriter, Unit> {
		    it.write("${stamp.toString()}\n${unread}\n")
		}
		android.util.Log.d("ASimanoService", "saving state done.")
	    } catch (e: Exception) {
		android.util.Log.e("ASimanoService", e.toString(), e)
	    }
	    
	    setNotification(unread, old_unread == 0 && unread > 0)
	} finally {
	    setForeNotification(false)
	}
    }
    
    private fun setForeNotification(state: Boolean) {
        if (state) {
            val builder = Notification.Builder(this, "fore_01")
	    builder.setSmallIcon(R.drawable.launcher)
            val intent = Intent(this, MainActivity::class.java)
            val tsBuilder = TaskStackBuilder.create(this)
            tsBuilder.addParentStack(MainActivity::class.java)
            tsBuilder.addNextIntent(intent)
            val pendingIntent = tsBuilder.getPendingIntent(
                    0, PendingIntent.FLAG_UPDATE_CURRENT)
            builder.setContentIntent(pendingIntent)
            startForeground(1, builder.build())			// respond to startForegroundService()
        } else {
            stopForeground(true)
        }
    }
    
    private fun setNotification(nr: Int, do_sound: Boolean) {
	if (nr != 0) {
	    val intent = Intent(Intent.ACTION_VIEW)
	    intent.setComponent(ComponentName("com.google.android.gm", "com.google.android.gm.ConversationListActivityGmail"));
//	    intent.setData(Uri.parse("content://gmail-ls/messages/${PrefActivity.getMailAccount(this)}/labels/^all"))
//	    intent.setData(Uri.parse("content://gmail-ls/account/${PrefActivity.getMailAccount(this)}/label/^i"))
//	    intent.setData(Uri.parse("content://gmail-ls/conversations/${PrefActivity.getMailAccount(this)}"));
//	    intent.setData(Uri.parse("content://gmail-ls/account/${PrefActivity.getMailAccount(this)}/label/^i"));
//	    intent.setData(Uri.parse("content://gmail-ls/account/${PrefActivity.getMailAccount(this)}/label/^l"));
/*
	    intent.setClassName("com.google.android.gm", "com.google.android.gm.ConversationListActivityGmail")
	    intent.putExtra("label", "^f")
	    intent.putExtra("account", PrefActivity.getMailAccount(this))
*/
	    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
	    
	    var pending: PendingIntent?
	    try {
		pending = TaskStackBuilder.create(this)
			.addNextIntentWithParentStack(intent)
			.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
	    } catch (e: IllegalArgumentException) {
		val i = Intent(this, MainActivity::class.java)
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		
		pending = TaskStackBuilder.create(this)
			.addNextIntentWithParentStack(i)
			.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
	    }
	    
	    val notification = Notification.Builder(this)
		    .setSmallIcon(R.drawable.notification)
		    .setContentTitle(getResources().getString(R.string.app_name))
		    .setContentText(getResources().getString(R.string.new_mail, nr))
		    .setContentIntent(pending)
		    .setOngoing(true)
		    .setChannelId(if (do_sound) "new_mail_01" else "new_mail_02")
		    .build()
	    
	    notificationManager.notify(0, notification)
	} else {
	    notificationManager.cancel(0)
	}
	
        for (listener in statusChangedListeners)
            listener.onStatusChanged(nr)
    }
    
    fun setOnStatusChangedListener(listener: OnStatusChangedListener) {
        statusChangedListeners.add(listener)
    }
    
    inner class ASimanoServiceBinder : Binder() {
        fun setOnStatusChangedListener(listener: OnStatusChangedListener) {
            this@ASimanoService.setOnStatusChangedListener(listener)
        }
	fun getStatus(): Int {
	    try {
		BufferedReader(InputStreamReader(openFileInput("state.txt"))).use<BufferedReader, Unit> {
		    val old_stamp_str = it.readLine()
		    val old_unread_str = it.readLine()
		    // val old_stamp = ZonedDateTime.parse(old_stamp_str)
		    val old_unread = old_unread_str.toInt()
		    return old_unread
		}
	    } catch (e: FileNotFoundException) {
		android.util.Log.d("ASimanoService", e.toString(), e)
	    } catch (e: Exception) {
		android.util.Log.e("ASimanoService", e.toString(), e)
	    }
	    return 0
	}
    }
    
    override fun onBind(intent: Intent): IBinder? {
	return ASimanoServiceBinder()
    }
    
    override fun onDestroy() {
	// Log.d("")
    }
}
