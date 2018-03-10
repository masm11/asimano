package jp.ddo.masm11.asimano

import android.app.Activity
import android.content.SharedPreferences
import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager

class PrefActivity: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
	super.onCreate(savedInstanceState)
	getFragmentManager().beginTransaction()
		.replace(android.R.id.content, PrefFragment())
		.commit()
    }
    
    companion object {
	fun getURL(ctx: Context): String {
	    val settings = PreferenceManager.getDefaultSharedPreferences(ctx)
	    return settings.getString("url", "https://localhost")
	}
	
	fun getMailAccount(ctx: Context): String {
	    val settings = PreferenceManager.getDefaultSharedPreferences(ctx)
	    return settings.getString("mail_account", "/dev/null@localhost")
	}
    }
}
