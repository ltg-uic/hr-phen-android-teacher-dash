package ltg.phenomena;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ConfigurationTab extends Activity {
	 public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        TextView textview = new TextView(this);
	        textview.setText("This is the CONFIGURATION tab");
	        setContentView(textview);
	    }
}
