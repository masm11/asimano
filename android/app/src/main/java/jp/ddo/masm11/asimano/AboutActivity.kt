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
