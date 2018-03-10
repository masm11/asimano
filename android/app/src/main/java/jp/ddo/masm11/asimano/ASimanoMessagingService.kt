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
import android.os.Bundle
import android.os.IBinder
import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.time.ZonedDateTime

class ASimanoMessagingService: FirebaseMessagingService() {
    
    override fun onMessageReceived(message: RemoteMessage) {
	try {
	    val from = message.getFrom()   // String
	    val datmap = message.getData()    // Map
	    
	    val stamp_str: String? = datmap.get("stamp")
	    val unread_str: String? = datmap.get("unread")
	    
	    android.util.Log.d("ASimanoMessagingService", "from=" + from)
	    android.util.Log.d("ASimanoMessagingService", "stamp=" + stamp_str)
	    android.util.Log.d("ASimanoMessagingService", "unread=" + unread_str)
	    
	    if (stamp_str != null && unread_str != null) {
		val stamp = ZonedDateTime.parse(stamp_str)
		android.util.Log.d("ASimanoMessagingService", "stamp=" + stamp)
		
		val unread = unread_str.toInt()
		android.util.Log.d("ASimanoMessagingService", "unread=" + unread)
		
		val intent = Intent(this, ASimanoService::class.java)
		intent.action = "jp.ddo.masm11.asimano.MESSAGE"
		intent.putExtra("jp.ddo.masm11.asimano.STAMP", stamp)
		intent.putExtra("jp.ddo.masm11.asimano.UNREAD", unread)
		startForegroundService(intent)
	    }
	} catch (e: Exception) {
	    android.util.Log.e("ASimanoMessagingService", e.toString(), e)
	}
    }
    
}
