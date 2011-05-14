package ltg.phenomena;

import ltg.phenomena.SimulationService.LocalBinder;
import ltg.phenomena.SimulationView.CanvasThread;
import ltg.phenomena.helioroom.Helioroom;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TableLayout;
import android.widget.TextView;

public class SimulationTab extends Activity {
	
	private Helioroom data = new Helioroom();
	private SimulationService service;
	private boolean mBound = false;
    private CanvasThread canvasThread;
    private SimulationView simView;
    private TableLayout planetsTable;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Binds the service in charge of XMPP communications
		Intent intent = new Intent(this, SimulationService.class);
		getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		// Setup layout
		setContentView(R.layout.simulation);
        simView = (SimulationView) findViewById(R.id.canvas);
        canvasThread = simView.getThread();
        // give the SimulationView a handle to the TextView used for messages
        simView.setTextView((TextView) findViewById(R.id.text));
        // give the SimulationView a handle to the data to be rendered
        data.addObserver(simView);
        // get a hold of the table Layout to draw planets
        planetsTable = (TableLayout) findViewById(R.id.planetTable);
        drawTable();
	}
	
	
	
private void drawTable() {
		// TODO
	}
	

	@Override
	protected void onResume() {
		super.onResume();
		canvasThread.unpause();
	}


	@Override
    protected void onPause() {
        super.onPause();
        canvasThread.pause();
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
			service.addObserver(data);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};
}
