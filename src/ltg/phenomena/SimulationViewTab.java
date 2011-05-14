package ltg.phenomena;

import ltg.phenomena.SimulationService.LocalBinder;
import ltg.phenomena.helioroom.Helioroom;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

public class SimulationViewTab extends Activity {
	
	private Helioroom data = null;
	private SimulationService service;
	private boolean mBound = false;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Binds the service in charge of XMPP communications
		Intent intent = new Intent(this, SimulationService.class);
		getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		// Setup layout and display data
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Unbinds the XMPP service
		if (mBound) {
			getApplicationContext().unbindService(mConnection);
			mBound = false;
		}
	}
	
	
	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className,
				IBinder serv) {
			LocalBinder binder = (LocalBinder) serv;
			service = binder.getService();
			mBound = true;
			// Link to Helioroom model
			data = new Helioroom();
			service.addObserver(data);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};
}
