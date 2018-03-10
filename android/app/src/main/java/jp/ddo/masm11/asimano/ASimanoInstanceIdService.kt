package jp.ddo.masm11.asimano

import android.app.Service
import android.os.Bundle
import android.os.IBinder
import android.os.AsyncTask
import android.content.Intent
import android.content.Context
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceIdService
import com.google.firebase.iid.FirebaseInstanceId
import java.io.OutputStreamWriter
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class ASimanoInstanceIdService: FirebaseInstanceIdService() {
    companion object {
	val TAG = "ASimanoInstanceIdService"
	
	fun registerToken(ctxt: Context) {
	    val token = FirebaseInstanceId.getInstance().getToken();
	    Log.d(TAG, "token: " + token);
	    
	    val task = RegisterTokenTask(ctxt)
	    task.execute(token)
	}
    }
    
    class RegisterTokenTask(val ctxt: Context): AsyncTask<String, Void, Unit>() {
	override protected fun doInBackground(vararg token: String): Unit {
	    var conn: HttpsURLConnection? = null
	    try {
		val url = URL(PrefActivity.getURL(ctxt))
		conn = url.openConnection() as HttpsURLConnection
		conn.setDoOutput(true)
		val json = "{\"token\":\"${token[0]}\"}"
		conn.setFixedLengthStreamingMode(json.length)
		val os = OutputStreamWriter(conn.getOutputStream())
		os.write(json)
		os.flush()
		val code = conn.getResponseCode()
	    } catch (e: Exception) {
		android.util.Log.e(TAG, e.toString(), e)
	    } finally {
		if (conn != null)
		    conn.disconnect()
	    }
	}
    }
    
    override fun onTokenRefresh() {
	registerToken(this)
    }
}
