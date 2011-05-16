package ltg.phenomena;

import java.util.Observable;
import java.util.Observer;

import ltg.phenomena.SimulationService.LocalBinder;
import ltg.phenomena.SimulationView.CanvasThread;
import ltg.phenomena.helioroom.Helioroom;
import ltg.phenomena.helioroom.Planet;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class SimulationTab extends Activity implements Observer {

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
		// give the Tab and SimulationView a handle to the data to be rendered
		data.addObserver(this);
		data.addObserver(simView);
		// get a hold of the table Layout to draw planets
		planetsTable = (TableLayout) findViewById(R.id.planetTable);
	}


	@Override
	public void update(Observable observable, Object data) {
		this.data = ((Helioroom) data);
		drawTable();
	}


	private void drawTable() {
		TableRow row = null; 
		ShapeDrawable icon = null;
		ImageView iv = null;
		TextView text = null;
		for(Planet p: data.getPlanets()) {
			row = new TableRow(this);
			// Add planet icon
			icon = new ShapeDrawable(new ArcShape(0, 360));
			icon.getPaint().setColor(Color.parseColor(p.getColor()));
			icon.setIntrinsicHeight(30);
			icon.setIntrinsicWidth(30);
			iv = new ImageView(this);
			iv.setImageDrawable(icon);	
			row.addView(iv);
			// Set planet color
			text = new TextView(this);
			text.setTextColor(Color.WHITE);
			text.setText(p.getColorName());
			row.addView(text);
			// Set planet name
			text = new TextView(this);
			text.setTextColor(Color.WHITE);
			text.setText(p.getName());
			row.addView(text);
			// Set revolution time
			text = new TextView(this);
			text.setTextColor(Color.WHITE);
			text.setText(p.getOrbitTime());
			row.addView(text);
			// Set next window to be entered
			// TODO
			// Time remaining before entering the next window
			// TODO
			// Notify GUI thread to draw
			Message m = new Message();
			m.obj = row;
			handler.sendMessage(m);
		}
	}
	
	
	 private Handler handler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
                 planetsTable.addView((TableRow)msg.obj);
         }
 };



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
