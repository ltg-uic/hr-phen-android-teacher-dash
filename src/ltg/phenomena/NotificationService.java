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
public abstract class NotificationService extends Service {
	
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
		nt.interrupt();
	}

	public IBinder onBind(Intent i) {
		return localBinder;
	}
	
	public class LocalBinder extends Binder {
        NotificationService getService() {
            return NotificationService.this;
        }
    }
	
	public void sendMessage(String dest, String message) {
		nt.sendTo(dest, message);
	}

}
