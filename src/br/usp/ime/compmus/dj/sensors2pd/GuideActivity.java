package br.usp.ime.compmus.dj.sensors2pd;

import java.util.List;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

public class GuideActivity extends Activity {
	

	private SensorManager mSensorManager;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide);
		
		// Sensors
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);		
		List<Sensor> listSensor = mSensorManager.getSensorList(Sensor.TYPE_ALL);
		TextView textViewAvailableSensors = (TextView) findViewById(R.id.textViewAvailableSensors);
		StringBuilder availableSensors = new StringBuilder();
		availableSensors.append(listSensor.size()+" "+textViewAvailableSensors.getText());
		for(Sensor sensor : listSensor){
			int sensorId = sensor.getType();
			availableSensors.append("\n"+sensor.getName()+
					"\n max range: "+sensor.getMaximumRange()+
					"\n resolution: "+sensor.getResolution()+
					"\n send: sensor"+sensorId+"v0; sensor"+sensorId+"v1; sensor"+sensorId+"v2;\n");
		}
		textViewAvailableSensors.setText(availableSensors);
	}
	
}
