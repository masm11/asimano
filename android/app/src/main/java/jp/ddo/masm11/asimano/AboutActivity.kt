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

import android.app.Activity
import android.app.Service
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.view.MenuInflater
import android.widget.TextView
import android.widget.Button
import android.content.Intent
import android.content.ServiceConnection
import android.content.ComponentName
import java.io.BufferedReader
import java.io.InputStreamReader

class AboutActivity: Activity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
	
        val pm = packageManager
        val pi = pm.getPackageInfo("jp.ddo.masm11.asimano", 0)
	
	var tv = findViewById<TextView>(R.id.about)
	tv.setText("ASimano v${pi.versionName}")
	
	val sb = StringBuilder()
	try {
	    BufferedReader(InputStreamReader(getResources().getAssets().open("COPYING.GPLv3"))).use<BufferedReader, Unit> {
		while (true) {
		    val line = it.readLine()
		    if (line == null)
			break
		    sb.append(line).append("\n")
		}
	    }
	} catch (e: Exception) {
	    android.util.Log.d("AboutActivity", e.toString(), e)
	}
	val txt = sb.toString()
	
	tv = findViewById<TextView>(R.id.license_gpl3)
	tv.setText(txt)
    }
}
