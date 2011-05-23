/**
 * 
 */
package ltg.phenomena;

import ltg.phenomena.helioroom.Helioroom;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * @author tebemis
 *
 */
public class SimulationService extends Service {
	
	private static final String PHENOMENA_ID="hr1";

	private final IBinder localBinder = new LocalBinder();
	private XMPPThread nt = null;

	@Override
	public void onCreate() {
		super.onCreate();
		nt = new XMPPThread(PHENOMENA_ID+"_cp", PHENOMENA_ID+"_cp","textNotifier");
		nt.start();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		nt.disconnect();
		nt.interrupt();
	}

	public IBinder onBind(Intent i) {
		return localBinder;
	}
	
	public class LocalBinder extends Binder {
        SimulationService getService() {
            return SimulationService.this;
        }
    }
	
	public void sendMessage(String message) {
		nt.sendTo(PHENOMENA_ID+"@ltg.evl.uic.edu", message);
	}
	
	
	public void linkData(Helioroom hr) {
		nt.linkToData(hr);
	}
}
