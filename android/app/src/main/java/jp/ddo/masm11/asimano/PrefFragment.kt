package jp.ddo.masm11.asimano

import android.preference.PreferenceFragment
import android.preference.EditTextPreference
import android.preference.Preference
import android.preference.Preference.OnPreferenceChangeListener
import android.os.Bundle

class PrefFragment: PreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
	super.onCreate(savedInstanceState)
	addPreferencesFromResource(R.xml.activity_pref)
	
	var etp: EditTextPreference
	
	etp = findPreference("url") as EditTextPreference
	etp.setSummary(etp.getText())
	etp.setOnPreferenceChangeListener({ pref, value ->
	    pref.setSummary(value.toString())
	    true
	})
	
	etp = findPreference("mail_account") as EditTextPreference
	etp.setSummary(etp.getText())
	etp.setOnPreferenceChangeListener({ pref, value ->
	    pref.setSummary(value.toString())
	    true
	})
    }
}
