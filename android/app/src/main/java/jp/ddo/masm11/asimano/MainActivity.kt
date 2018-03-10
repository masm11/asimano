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

class MainActivity : Activity() {
    private var svc: ASimanoService.ASimanoServiceBinder? = null
    private var conn: ServiceConnection? = null
    
    private inner class ASimanoServiceConnection : ServiceConnection {
        private val listener = object : ASimanoService.OnStatusChangedListener {
	    override fun onStatusChanged(nr: Int) {
		setState(nr)
	    }
        }
	
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            svc = service as ASimanoService.ASimanoServiceBinder
	    
            svc!!.setOnStatusChangedListener(listener)
	    
            setState(svc!!.getStatus())
        }
	
        override fun onServiceDisconnected(name: ComponentName) {
            svc = null
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
	
	ASimanoInstanceIdService.registerToken(this)
	
/*
	val btn_pref = findViewById<Button>(R.id.pref)
	btn_pref.setOnClickListener({ _ -> openPref() })
*/
	
/*
	String hostname = PrefActivity.getHostname(this);
	int port = PrefActivity.getPort(this);
	btn_pref.setText(getResources().getString(R.string.server_format, hostname, port));
*/
//	Log.d("end.");
    }
    
/*
    @Override
    protected void onDestroy() {
	unregisterReceiver(receiver);
	
	super.onDestroy();
    }
*/
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
	val inflater = getMenuInflater()
        inflater.inflate(R.menu.actionbar, menu)
	return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
	    R.id.action_pref -> {
		val intent = Intent(this, PrefActivity::class.java)
		startActivityForResult(intent, 456)
		return true
	    }
	    
            R.id.action_about -> {
		val intent = Intent(this, AboutActivity::class.java)
		startActivityForResult(intent, 789)
		return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }
    
    override protected fun onActivityResult(requestCode: Int, resultCode: Int, dat: Intent?) {
	if (requestCode == 123 || requestCode == 456) {
	    ASimanoInstanceIdService.registerToken(this)
	    
/*
	    val btn_pref = findViewById(R.id.pref) as Button;
	    btn_pref.setText(getResources().getString(R.string.server_format, hostname, port));
*/
	}
	
	super.onActivityResult(requestCode, resultCode, dat);
    }
    
    override fun onStart() {
	super.onStart()
	
	val intent = Intent(this, ASimanoService::class.java)
	conn = ASimanoServiceConnection()
	bindService(intent, conn, Service.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        unbindService(conn)
	
        super.onStop()
    }

    private fun openPref() {
	val intent = Intent(this, PrefActivity::class.java)
	startActivityForResult(intent, 123);
    }
    
    private fun setState(nr: Int) {
	val text = findViewById<TextView>(R.id.state)
	if (nr == 0)
	    text.setText(getResources().getString(R.string.no_mail))
	else
	    text.setText(getResources().getString(R.string.new_mail, nr))
    }
}
