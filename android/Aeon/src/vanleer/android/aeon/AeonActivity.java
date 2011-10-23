package vanleer.android.aeon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AeonActivity extends Activity implements OnClickListener {

	private Button planButton;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        planButton = (Button) findViewById(R.id.button_plan);
        planButton.setOnClickListener(this);
    }

	public void onClick(View v)  {
		switch(v.getId()) {
		case R.id.button_plan:
			Intent startItineraryOpen = new Intent(AeonActivity.this, Itinerary.class);
			startActivity(startItineraryOpen);
			break;
		}
		
		// TODO Auto-generated method stub		
	}
}