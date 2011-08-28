package vanleer.android.aeon;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AeonActivity extends Activity {
	final Itinerary aeonItinerary = new Itinerary(this);
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);        
        
        final Button planButton = (Button) findViewById(R.id.button_plan);
        planButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {            	
            	setContentView(R.layout.itinerary);
            }
            
                // Perform action on click
        });    
    }
}