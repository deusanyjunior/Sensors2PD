package br.usp.ime.compmus.dj.sensors2pd;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.io.PdAudio;
import org.puredata.android.utils.PdUiDispatcher;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Sensors2PDActivity extends Activity implements SensorEventListener, OnTouchListener {

	private static final String TAG = "SensorToPD";
	private boolean debug = false;
	
	private PdUiDispatcher dispatcher;
	private SensorManager mSensorManager;
	public View multiTouchView;
	
	private int maxSensorId = 1;
	
	File pdFile = null;
	ArrayList<String> str = new ArrayList<String>();
	private Boolean firstLvl = true;
	private Item[] fileList;
	private File path = new File(Environment.getExternalStorageDirectory() + "");
	private String chosenFile;
	private static final int BROWSE_DIALOG = 0xFFFFFFFF;
	private static final int ERROR_DIALOG = 0xFFFFFFF0;
	ListAdapter adapter;
	protected Handler handler = new Handler();
	String errorMessage;
	private int touchIds[] = new int[20];
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensor2_pd);
		
		// View
		View view = findViewById(R.id.scrollView1);
		view.setOnTouchListener(this);
		
		// Sensors and Touch
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		List<Sensor> listSensor = mSensorManager.getSensorList(Sensor.TYPE_ALL);
		for(Sensor sensor : listSensor){
			// "maxSensorId" is used on touch methods to set touch textView initial id
			if(sensor.getType()>=maxSensorId) maxSensorId = sensor.getType()+1;
		}
				
		// PD
		try {
        	initPd();
        	loadPatch();
        } catch (IOException e) {
        	Log.e(TAG, e.toString());
        	finish();
        }
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sensor2_pd, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    { 
        switch (item.getItemId())
        {
	        case R.id.action_browse:
	        	if( isSDCardUnlocked(Sensors2PDActivity.this) ) {
	        		loadFileList();
	        		onCreateDialog(BROWSE_DIALOG).show();	        		
	        	}
	        	return true;
	        case R.id.action_debug:
	        	if(debug) {
	        		TextView textIntro = (TextView) findViewById(R.id.textViewIntro);
	        		textIntro.setVisibility(TextView.VISIBLE);
	        		debug = false;
	        		for(int i = 0; i <= 30 || i<=maxSensorId+10; i++) {
	        			setTextView(i, "", TextView.GONE);
	        		}
	        	} else {
	        		TextView textIntro = (TextView) findViewById(R.id.textViewIntro);
	        		textIntro.setVisibility(TextView.GONE);
	        		debug = true;
	        	}
	        	return true;
	        case R.id.action_guide:
	        	Intent intent = new Intent(this, GuideActivity.class);
	        	startActivity(intent);
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }    
 
	
	
	@Override
    protected void onResume() {
    	super.onResume();
    	PdAudio.startAudio(this);
    	
    	// TODO: Sensor delay can be an option changed from the menu
    	for(Sensor sensor: mSensorManager.getSensorList(Sensor.TYPE_ALL)) {
    		mSensorManager.registerListener(this,sensor, SensorManager.SENSOR_DELAY_FASTEST);
    	}
    }
	
	@Override
    protected void onPause() {
    	super.onPause();
    	PdAudio.stopAudio();
    }
    
    @Override
    protected void onStop() {
        mSensorManager.unregisterListener(this);
        super.onStop();
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	PdAudio.release();
    	PdBase.release();
    }	
    
    
    
// Pure Data
	
    private void initPd() throws IOException {
    	// Configure the audio glue
    	int sampleRate = AudioParameters.suggestSampleRate();
    	PdAudio.initAudio(sampleRate, 0, 2, 8, true);
    	// Create and install the dispatcher
    	dispatcher = new PdUiDispatcher();
    	PdBase.setReceiver(dispatcher);
    }
    
    private void loadPatch() throws IOException {
    	// Hear the sound
    	if (pdFile != null) {
    		Log.i(TAG, pdFile.getParentFile()+" <"+pdFile.getName().replace(".zip", ".pd")+">");

    		if(pdFile.getAbsolutePath().endsWith(".pd")) {
    			PdBase.openPatch(pdFile.getAbsolutePath()); 
    			
    		} else if(pdFile.getAbsolutePath().endsWith(".zip")) {
	    		InputStream in = null;
	    		in = new BufferedInputStream(new FileInputStream(pdFile));
	    		IoUtils.extractZipResource(in, pdFile.getParentFile(), true);
	    		File patchFile = new File(pdFile.getParentFile()+"/"+pdFile.getName().replace(".zip", ""), pdFile.getName().replace(".zip", ".pd"));
	    		try {
	    			PdBase.openPatch(patchFile.getAbsolutePath());
	    			Log.e(TAG, "File "+pdFile.getAbsolutePath()+" "+patchFile.getAbsolutePath());
	    		} catch (IOException e) {
	    			Toast.makeText(this, "The zip file needs a file with the same name inside: patch.zip -> patch.pd", Toast.LENGTH_SHORT).show();
	    		}
	    		
    		} else {
    			Toast.makeText(this, "Invalid file format. Please try .pd or .zip", Toast.LENGTH_SHORT).show();
    		}
    	}
    }
	
    
    
    // Android Touch
	protected int getTouchIdAssignment() {
		for(int i = 0; i < touchIds.length; i++) {
			if(touchIds[i] == -1) {
				return i;
			}
		}
		return -1;
	}
	
	protected int getTouchId(int touchId) {
		for(int i = 0; i < touchIds.length; i++) {
			if(touchIds[i] == touchId) {
				return i;
			}
		}
		return -1;
	}
    
	@Override
	// TODO: Change the touches IDs correctly
	public boolean onTouch(View v, MotionEvent event) {
		final int action = event.getAction() & MotionEvent.ACTION_MASK;
		switch(action) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN:
			
			for(int i = 0; i < event.getPointerCount(); i++) {
				int id = event.getPointerId(i);
				PdBase.sendFloat("sensorT"+id+"vx", event.getX());
				PdBase.sendFloat("sensorT"+id+"vy", event.getY());

				if(debug) {
					StringBuilder textView = new StringBuilder("sensorT"+id+":\n vx: "+event.getX()+"\n vy: "+event.getY());
					setTextView(maxSensorId+id, textView.toString(), TextView.VISIBLE);
				}
//				Log.w(TAG, "Pointer Down: "+id);						
			}
			break;
			
		case MotionEvent.ACTION_MOVE:
			
			for(int i = 0; i < event.getPointerCount(); i++) {
				int id = event.getPointerId(i);
				PdBase.sendFloat("sensorT"+id+"vx", event.getX());
				PdBase.sendFloat("sensorT"+id+"vy", event.getY());
				if(debug) {
					StringBuilder textView = new StringBuilder("sensorT"+id+":\n vx: "+event.getX()+"\n vy: "+event.getY());
					setTextView(maxSensorId+id, textView.toString(), TextView.VISIBLE);						
//					Log.w(TAG, "Pointer Move: "+event.getPointerId(i));					
				}
			}
			break;
			
		case MotionEvent.ACTION_POINTER_UP:
		case MotionEvent.ACTION_UP:		
			int id = event.getActionIndex();
			PdBase.sendFloat("sensorT"+id+"vx", -1);
			PdBase.sendFloat("sensorT"+id+"vy", -1);
			if(debug) {
				StringBuilder textView = new StringBuilder("sensorT"+id+":\n vx: -1\n vy: -1");
				// TODO: set the visibility to GONE someday..
				setTextView(maxSensorId+id, textView.toString(), TextView.VISIBLE);
//				Log.w(TAG, "Pointer UP: "+id+" "+event.getActionIndex()+" "+event.getPointerId(event.getActionIndex()));
				for(int i = 0; i < event.getPointerCount(); i++) {
//					Log.e(TAG, "Pointer UP: "+event.getPointerId(i));
				}					
			}

			break;
		case MotionEvent.ACTION_CANCEL:
			Log.e(TAG, "MotionEvent.ACTION_CANCEL");
			break;
		case MotionEvent.ACTION_SCROLL:
			Log.e(TAG, "MotionEvent.ACTION_SCROLL");
			break;
		case MotionEvent.ACTION_POINTER_INDEX_MASK:
			Log.e(TAG, "MotionEvent.ACTION_POINTER_INDEX_MASK");
			break;
		}
		return false;
	}

    
	
	 
    // Android Sensors
    
	/* Sensor ID	 
	 * Number   Type of Sensor              Description
	 * 	 1	int	TYPE_ACCELEROMETER			A constant describing an accelerometer sensor type.
	 * 	 2	int	TYPE_MAGNETIC_FIELD			A constant describing a magnetic field sensor type.
	 * 	 3	int	TYPE_ORIENTATION	 		This constant was deprecated in API level 8. use SensorManager.getOrientation() instead.
	 * 	 4	int	TYPE_GYROSCOPE				A constant describing a gyroscope sensor type
	 * 	 5	int	TYPE_LIGHT					A constant describing a light sensor type.
	 * 	 6	int	TYPE_PRESSURE				A constant describing a pressure sensor type
	 * 	 7	int	TYPE_TEMPERATURE	 		This constant was deprecated in API level 14. use Sensor.TYPE_AMBIENT_TEMPERATURE instead.
	 * 	 8	int	TYPE_PROXIMITY				A constant describing a proximity sensor type.
	 * 	 9	int	TYPE_GRAVITY				A constant describing a gravity sensor type.
	 * 	10	int	TYPE_LINEAR_ACCELERATION	A constant describing a linear acceleration sensor type.
	 * 	11	int	TYPE_ROTATION_VECTOR		A constant describing a rotation vector sensor type.
	 * 	12	int	TYPE_RELATIVE_HUMIDITY		A constant describing a relative humidity sensor type.
	 * 	13	int	TYPE_AMBIENT_TEMPERATURE	A constant describing an ambient temperature sensor type
	 *  14	int TYPE_MAGNETIC_FIELD_UNCALIBRATED	 A constant describing an uncalibrated magnetic field sensor type.
	 *  15	int TYPE_GAME_ROTATION_VECTOR	A constant describing an uncalibrated rotation vector sensor type.
	 *  16	int TYPE_GYROSCOPE_UNCALIBRATED	A constant describing an uncalibrated gyroscope sensor type.
	 *  17	int TYPE_SIGNIFICANT_MOTION		A constant describing the significant motion trigger sensor.
	 */
	
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		for(int i = 0; i < event.values.length; i++) {
			PdBase.sendFloat("sensor"+event.sensor.getType()+"v"+i, event.values[i]);
		}
		if(debug) {
			StringBuilder textView = new StringBuilder("sensor"+event.sensor.getType()+":"+event.sensor.getName());
			for(int i = 0; i < event.values.length; i++) {
				textView.append("\n v"+i+": "+event.values[i]);				
			}
			setTextView(event.sensor.getType(), textView.toString(), TextView.VISIBLE);			
		}
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
	
	// View
	
	private void setTextView(int id, String text, int visibility) {
		TextView tv;
		switch (id) {
		case 1:
			tv = (TextView) findViewById(R.id.textView1);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		case 2:
			tv = (TextView) findViewById(R.id.textView2);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		case 3:
			tv = (TextView) findViewById(R.id.textView3);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		case 4:
			tv = (TextView) findViewById(R.id.textView4);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		case 5:
			tv = (TextView) findViewById(R.id.textView5);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		case 6:
			tv = (TextView) findViewById(R.id.textView6);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		case 7:
			tv = (TextView) findViewById(R.id.textView7);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		case 8:
			tv = (TextView) findViewById(R.id.textView8);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		case 9:
			tv = (TextView) findViewById(R.id.textView9);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		case 10:
			tv = (TextView) findViewById(R.id.textView10);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		case 11:
			tv = (TextView) findViewById(R.id.textView11);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		case 12:
			tv = (TextView) findViewById(R.id.textView12);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		case 13:
			tv = (TextView) findViewById(R.id.textView13);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		case 14:
			tv = (TextView) findViewById(R.id.textView14);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		case 15:
			tv = (TextView) findViewById(R.id.textView15);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		case 16:
			tv = (TextView) findViewById(R.id.textView16);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		case 17:
			tv = (TextView) findViewById(R.id.textView17);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		case 18:
			tv = (TextView) findViewById(R.id.textView18);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		case 19:
			tv = (TextView) findViewById(R.id.textView19);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		case 20:
			tv = (TextView) findViewById(R.id.textView20);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		case 21:
			tv = (TextView) findViewById(R.id.textView21);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		case 22:
			tv = (TextView) findViewById(R.id.textView22);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		case 23:
			tv = (TextView) findViewById(R.id.textView23);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		case 24:
			tv = (TextView) findViewById(R.id.textView24);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		case 25:
			tv = (TextView) findViewById(R.id.textView25);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		case 26:
			tv = (TextView) findViewById(R.id.textView26);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		case 27:
			tv = (TextView) findViewById(R.id.textView27);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		case 28:
			tv = (TextView) findViewById(R.id.textView28);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		case 29:
			tv = (TextView) findViewById(R.id.textView29);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		case 30:
			tv = (TextView) findViewById(R.id.textView30);
			tv.setText(text);
			tv.setVisibility(visibility);
			break;
		default:
			break;
		}
	}
	
	
    
		
    
    // Menu
	
	/**
	 * Code based on CsoundApp:
	 * http://sourceforge.net/projects/csound/files/csound5/Android/
	 */
    
	private void loadFileList() {
		try {
			path.mkdirs();
		} catch (SecurityException e) {
			Toast.makeText(this, "Unable to read SD Card!", Toast.LENGTH_SHORT).show();
			Log.e("error", "unable to write on the sd card ");
		}

		
		if (path.exists()) {
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					File sel = new File(dir, filename);
					return (sel.isFile() || sel.isDirectory())
							&& !sel.isHidden();
				}
			};

			String[] fList = path.list(filter);
			fileList = new Item[fList.length];
			for (int i = 0; i < fList.length; i++) {
				fileList[i] = new Item(fList[i], R.drawable.file_icon);
				File sel = new File(path, fList[i]);
				if (sel.isDirectory()) {
					fileList[i].icon = R.drawable.directory_icon;
				} 
			}
			if (!firstLvl) {
				Item temp[] = new Item[fileList.length + 1];
				for (int i = 0; i < fileList.length; i++) {
					temp[i + 1] = fileList[i];
				}
				temp[0] = new Item("Up", R.drawable.directory_up);
				fileList = temp;
			}
		} 
		adapter = new ArrayAdapter<Item>(this,
				android.R.layout.select_dialog_item, android.R.id.text1,
				fileList) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				TextView textView = (TextView) view
						.findViewById(android.R.id.text1);
				textView.setCompoundDrawablesWithIntrinsicBounds(
						fileList[position].icon, 0, 0, 0);
				int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
				textView.setCompoundDrawablePadding(dp5);
				return view;
			}
		};
	}

	private class Item {
		public String file;
		public int icon;
		public Item(String file, Integer icon) {
			this.file = file;
			this.icon = icon;
		}

		@Override
		public String toString() {
			return file;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		AlertDialog.Builder builder = new Builder(this);
		if (fileList == null) {
			dialog = builder.create();
			return dialog;
		}

		switch (id) {
		case BROWSE_DIALOG:
			builder.setTitle("Choose your file");
			builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					chosenFile = fileList[which].file;
					File sel = new File(path + "/" + chosenFile);
					if (sel.isDirectory()) {
						firstLvl = false;
						str.add(chosenFile);
						fileList = null;
						path = new File(sel + "");
						loadFileList();
						
						// TODO 
						// update based on:
						// http://stackoverflow.com/questions/10285047/showdialog-deprecated-whats-the-alternative
						
						removeDialog(BROWSE_DIALOG);
						showDialog(BROWSE_DIALOG);
					}
					else if (chosenFile.equalsIgnoreCase("up") && !sel.exists()) {
						String s = str.remove(str.size() - 1);
						path = new File(path.toString().substring(0,
								path.toString().lastIndexOf(s)));
						fileList = null;
						if (str.isEmpty()) {
							firstLvl = true;
						}
						loadFileList();
						removeDialog(BROWSE_DIALOG);
						showDialog(BROWSE_DIALOG);
					}
					else OnFileChosen(sel);
				}
			});
			break;
		  case  ERROR_DIALOG:
			  builder.setTitle(errorMessage);
			  break;
		}
		dialog = builder.show();
		return dialog;
	}
	
	private void OnFileChosen(File file){
		Log.d("FILE CHOSEN", file.getAbsolutePath());
		pdFile = file;		
		try {
			if(pdFile != null) {
				loadPatch();    				
			}
		} catch (IOException e) {
			Log.e(TAG, e.toString());
        	finish();
		}   
	}
	
	
	
	
	
	
	
	
	// SD Card
	
	/**
	 * Verify external memory access
	 * 
	 * Original code:
	 * http://stackoverflow.com/questions/4580683/writing-text-file-to-sd-card-fails
	 * 
	 * @param mContext
	 * @return boolean	Return true if we can use the sd card
	 * 					Return false if we had some problem, and show the reason
	 */
	public static Boolean isSDCardUnlocked(Context mContext) {
	    String auxSDCardStatus = Environment.getExternalStorageState();

	    if ( auxSDCardStatus.equals(Environment.MEDIA_MOUNTED) )
	        return true;
	    else if ( auxSDCardStatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY) ) {
	        Toast.makeText(
	                mContext,
	                "Warning, the SDCard it's only in read mode.\nthis does not result in malfunction"
	                        + " of the read aplication", Toast.LENGTH_LONG)
	                .show();
	        return true;
	    } else if ( auxSDCardStatus.equals(Environment.MEDIA_NOFS) ) {
	        Toast.makeText(
	                mContext,
	                "Error, the SDCard can be used, it has not a corret format or "
	                        + "is not formated.", Toast.LENGTH_LONG)
	                .show();
	        return false;
	    } else if ( auxSDCardStatus.equals(Environment.MEDIA_REMOVED) ) {
	        Toast.makeText(
	                mContext,
	                "Error, the SDCard is not found, to use the reader you need "
	                        + "insert a SDCard on the device.",
	                Toast.LENGTH_LONG).show();
	        return false;
	    } else if ( auxSDCardStatus.equals(Environment.MEDIA_SHARED) ) {
	        Toast.makeText(
	                mContext,
	                "Error, the SDCard is not mounted beacuse is using "
	                        + "connected by USB. Plug out and try again.",
	                Toast.LENGTH_LONG).show();
	        return false;
	    } else if ( auxSDCardStatus.equals(Environment.MEDIA_UNMOUNTABLE) ) {
	        Toast.makeText(
	                mContext,
	                "Error, the SDCard cant be mounted.\nThe may be happend when the SDCard is corrupted "
	                        + "or crashed.", Toast.LENGTH_LONG).show();
	        return false;
	    } else if ( auxSDCardStatus.equals(Environment.MEDIA_UNMOUNTED) ) {
	        Toast.makeText(
	                mContext,
	                "Error, the SDCArd is on the device but is not mounted."
	                        + "Mount it before use the app.",
	                Toast.LENGTH_LONG).show();
	        return false;
	    }

	    return true;
	}


}
