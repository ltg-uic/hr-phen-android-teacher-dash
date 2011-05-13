package ltg.phenomena;
import android.app.TabActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class HelioroomTeacherDashboard extends TabActivity {


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// Setup TabHost
		Resources res = getResources();	// Resource object to get Drawables
		TabHost tabHost = getTabHost();	// The activity TabHost
		TabHost.TabSpec spec;  			// Resusable TabSpec for each tab
		Intent intent;  				// Reusable Intent for each tab
		// Notifications Tab
		intent = new Intent().setClass(this, NotificationTab.class);
		spec = tabHost.newTabSpec("notifications").setIndicator("Notifications",
				res.getDrawable(R.drawable.ic_tab_notifications))
				.setContent(intent);
		tabHost.addTab(spec);
		// Configurations Tab
		intent = new Intent().setClass(this, ConfigurationTab.class);
		spec = tabHost.newTabSpec("configure").setIndicator("Configure",
				res.getDrawable(R.drawable.ic_tab_configuration))
				.setContent(intent);
		tabHost.addTab(spec);
		// Simulation view Tab
		intent = new Intent().setClass(this, SimulationViewTab.class);
		spec = tabHost.newTabSpec("sim").setIndicator("Simulation view",
				res.getDrawable(R.drawable.ic_tab_godview))
				.setContent(intent);
		tabHost.addTab(spec);
		// Set current Tab
		tabHost.setCurrentTab(0);
	}
}
