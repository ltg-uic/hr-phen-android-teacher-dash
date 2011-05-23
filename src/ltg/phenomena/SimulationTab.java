package ltg.phenomena;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import ltg.phenomena.SimulationService.LocalBinder;
import ltg.phenomena.SimulationView.CanvasThread;
import ltg.phenomena.helioroom.Helioroom;
import ltg.phenomena.helioroom.Planet;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class SimulationTab extends Activity implements Observer {
	
	private static final int DIALOG_PAUSED_ID = 0;
	private static final int DIALOG_RESUME_ID = 1;
	private static final int DIALOG_CONFIRM_ID = 2;

	private Helioroom data = new Helioroom();
	private SimulationService service;
	private boolean mBound = false;
	private CanvasThread canvasThread;
	private SimulationView simView;
	private TableLayout planetsTable;
	private Button pauseButton;
	// Other views
	private List<TextView> enteringWindows = new ArrayList<TextView>();
	private List<TextView> timeToEnteringWindows = new ArrayList<TextView>();
	private String movedPlanet = null;


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
		// give the Tab and SimulationView a handle to the data to be rendered
		data.addObserver(this);
		data.addObserver(simView);
		// get a hold of the table Layout to draw planets
		planetsTable = (TableLayout) findViewById(R.id.planetTable);
		pauseButton = (Button) findViewById(R.id.pauseButton);
		pauseButton.setOnClickListener(pauseButtonListener);
	}


	@Override
	public void update(Observable observable, Object data) {
		this.data = ((Helioroom) data);
		if(planetsTable.getChildCount() == 3)
			handler.sendEmptyMessage(0);
		else 
			handler.sendEmptyMessage(1);
	}



	// GUI Handler
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what!=1) {
				drawTable();
				enableGUI();
			} else {
				updateTable();
			}
		}
	};
	
	
	private void drawTable() {
		TableRow row = null; 
		ShapeDrawable icon = null;
		ImageView iv = null;
		TextView text = null;
		int i = 0;
		for(Planet p: data.getPlanets()) {
			row = new TableRow(this);
			// Add planet icon
			icon = new ShapeDrawable(new ArcShape(0, 360));
			icon.getPaint().setColor(Color.parseColor(p.getColor()));
			icon.setIntrinsicHeight(30);
			icon.setIntrinsicWidth(30);
			iv = new ImageView(this);
			iv.setImageDrawable(icon);
			iv.setPadding(0, 2, 0, 2);
			row.addView(iv);
			// Set planet color
			text = new TextView(this);
			text.setTextColor(Color.WHITE);
			text.setText(p.getColorName());
			text.setTextSize(15);
			text.setPadding(0, 5, 0, -1);
			text.setGravity(Gravity.CENTER_VERTICAL);
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
			text = new TextView(this);
			text.setTextColor(Color.WHITE);
			text.setText(p.getNextWin());
			enteringWindows.add(text);
			row.addView(text);
			// Time remaining before entering the next window
			text = new TextView(this);
			text.setTextColor(Color.WHITE);
			text.setText(p.timeToNextWin());
			timeToEnteringWindows.add(text);
			row.addView(text);
			// Draw the row
			row.setBackgroundColor(getColor(i));
			planetsTable.addView(row);
			i++;
		}
	}


	private void updateTable() {
		int i=0;
		if (data.getState().equals(Helioroom.RUNNING))
			for(Planet p: data.getPlanets()) {
				enteringWindows.get(i).setText(p.getNextWin());
				timeToEnteringWindows.get(i).setText(p.timeToNextWin());
				i++;
			}
	}
	
	
	private void enableGUI() {
		// Set the right text for the pause button and enables it
		if (data.getState().equals(Helioroom.PAUSED)) {
			pauseButton.setText(R.string.pauseButtonTextPaused);
		} else {
			pauseButton.setText(R.string.pauseButtonTextRunning);
		}
		pauseButton.setEnabled(true);
		pauseButton.setVisibility(View.VISIBLE);
		// enables planet dragging
		simView.setOnTouchListener(canvasListener);
	}
	
	
	private int getColor(int i) {
		if(i%2==0)
			return Color.argb(255, 40, 40, 40);
		else
			return Color.BLACK;
	}
	
	
	////////////////////////////////////////////////////////////////////
	// GUI listeners (pause button, touch events, dialog boxes) 
	////////////////////////////////////////////////////////////////////
	
	private OnClickListener pauseButtonListener = new OnClickListener() {
	    public void onClick(View v) {
	      if(data.getState().equals(Helioroom.RUNNING)) {
	    	  showDialog(DIALOG_PAUSED_ID);
	      } else {
	    	  showDialog(DIALOG_RESUME_ID);
	      }
	    }
	};


	private OnTouchListener canvasListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			float x = event.getX();
			float y = event.getY();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				simView.touch_start(x, y);
				break;
			case MotionEvent.ACTION_MOVE:
				simView.touch_move(x, y);
				break;
			case MotionEvent.ACTION_UP:
				movedPlanet = null;
				movedPlanet = simView.getGrabbedPlanet();
				if(movedPlanet!=null) {
					showDialog(DIALOG_CONFIRM_ID);
				}
				break;
			}
			return true;

		}
	};
	

	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch(id) {
		case DIALOG_PAUSED_ID:
			builder.setMessage("Are you sure you want to pause the simulation?")
			       .setCancelable(false)
			       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface d, int id) {
			                pauseButton.setText(R.string.pauseButtonTextPaused);
			                long curTime = System.currentTimeMillis() + data.getNtcf();
			                service.sendMessage("<editState ts=\""+curTime+"\">pause</editState>");
			                canvasThread.pause();
			           }
			       })
			       .setNegativeButton("No", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface d, int id) {
			                d.cancel();
			           }
			       });
			dialog = builder.create();
			break;
		case DIALOG_RESUME_ID:
			builder.setMessage("Are you sure you want to resume the simulation?")
			       .setCancelable(false)
			       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface d, int id) {
			                pauseButton.setText(R.string.pauseButtonTextRunning);
			                long curTime = System.currentTimeMillis() + data.getNtcf();
			                service.sendMessage("<editState ts=\"" +curTime + "\">resume</editState>");
			                canvasThread.unpause();
			           }
			       })
			       .setNegativeButton("No", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface d, int id) {
			                d.cancel();
			           }
			       });
			dialog = builder.create();
			break;
		case DIALOG_CONFIRM_ID:
			builder.setMessage("Are you sure you want to move this planet?")
			       .setCancelable(false)
			       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface d, int id) {
			        	   String nsp = simView.moveGrabbedPlanet();
			        	   simView.releaseGrabbedPlanet();
			        	   String message = "<editPlanet name=\"" + movedPlanet + "\">" +
			        	   		"<startPosition>" + nsp + "</startPosition></editPlanet>";
			        	   service.sendMessage(message);
			           }
			       })
			       .setNegativeButton("No", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface d, int id) {
			        	   simView.releaseGrabbedPlanet();
			           }
			       });
			dialog = builder.create();
			break;
		default:
			dialog = null;
		}
		return dialog;
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
			service.linkData(data);
		}

		@Override
		public void onServiceDisconnected(ComponentName cn) {
			mBound = false;
		}
	};
}
