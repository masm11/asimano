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
