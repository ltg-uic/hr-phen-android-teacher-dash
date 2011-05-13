/**
 * 
 */
package ltg.phenomena;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * @author tebemis
 *
 */
public class XMPPService extends Service {
	
	private final IBinder localBinder = new LocalBinder();
	private XMPPThread nt = null;

	@Override
	public void onCreate() {
		super.onCreate();
		nt = new XMPPThread("hr_dev_w_notifier_text", "hr_dev_w_notifier_text","textNotifier");
		nt.start();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		nt.stop(null);
	}

	public IBinder onBind(Intent arg0) {
		return localBinder;
	}
	
	public class LocalBinder extends Binder {
        XMPPService getService() {
            return XMPPService.this;
        }
    }
	
	public void sendMessage(String dest, String message) {
		nt.sendTo(dest, message);
	}
	
	
	

}
