package ltg.phenomena;

import ltg.phenomena.NotificationService.LocalBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;

public class NotificationTab extends Activity {
	
	protected NotificationService xmppService;
	private boolean mBound = false;
	
	
	 public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.notifications);
	    }
	 
	 
	 @Override
		protected void onStart() {
			super.onStart();
			// Bind to LocalService
	        Intent intent = new Intent(this, NotificationService.class);
	        getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	        // Get UI hooks
			final Button button = (Button) findViewById(R.id.sendButton);
			button.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					xmppService.sendMessage("gugo@ltg.evl.uic.edu", "Hello gugo!");
				}
			});
		}

		
		@Override
		protected void onStop() {
			super.onStart();
			// Unbind from the service
	        if (mBound) {
	            unbindService(mConnection);
	            mBound = false;
	        }
		}



		/** Defines callbacks for service binding, passed to bindService() */
		private ServiceConnection mConnection = new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName className,
					IBinder service) {
				LocalBinder binder = (LocalBinder) service;
				xmppService = binder.getService();
				mBound = true;
			}

			@Override
			public void onServiceDisconnected(ComponentName arg0) {
				mBound = false;
			}
		};
}
