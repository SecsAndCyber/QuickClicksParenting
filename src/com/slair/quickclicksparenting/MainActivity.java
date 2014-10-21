package com.slair.quickclicksparenting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.Toast;

public class MainActivity extends Activity  {
	 static final String LOG_TAG = "AudioRecordTest";
	    private MediaRecorder mRecorder = null;

	    private List<PlayButton> mPlayButtons = new ArrayList<PlayButton>();
	    private MediaPlayer	mPlayer = null;
	    private Thrower		mEventThrower = new Thrower();

	    private int ButtonCount = 20;
	    private boolean mRecording = false;
	    private boolean mPlaying = false;
	    
	    ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);

	    void onRecord(boolean start, PlayButton button) {
	        if (start) {
	            startRecording(button);
	        } else {
	            stopRecording(button);
	        }
	    }

	    void onPlay(PlayButton button) {
	        if (mPlaying) {
	            stopPlaying(button);
	        } else {
	            startPlaying(button);
	        }
	    }

	    private void startPlaying(PlayButton button) {
	        mPlayer = new MediaPlayer();
	        try {
	            mPlayer.setDataSource(button.mFileName);
	            mPlayer.setOnCompletionListener(new OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mp) {
				        mEventThrower.Throw(Thrower.PLAYING_STOPPED, null);	
				        mPlaying = false;
					}
				});
	            mPlayer.prepare();
	            mPlayer.start();
	            mPlaying = true;
		        mEventThrower.Throw(Thrower.PLAYING_STARTED, button);
	        } catch (IOException e) {
	            Log.e(LOG_TAG, "startPlaying prepare() failed");
	        }
	    }

	    private void stopPlaying(PlayButton button) {
            mPlaying = false;
            if(mPlayer != null)
            {
		        mPlayer.release();
		        mEventThrower.Throw(Thrower.PLAYING_STOPPED, button);	
		        mPlayer = null;
            }
	    }

	    private void startRecording(PlayButton button) {
	    	if(mPlayer != null)
	    	{
	    		mPlayer.stop();
	    		mPlayer.release();
		        mPlayer = null;
	    	}
	        mRecorder = new MediaRecorder();
	        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
	        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
	        mRecorder.setOutputFile(button.mFileName);
	        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

	        try {
	            mRecorder.prepare();
	        } catch (IOException e) {
	            Log.e(LOG_TAG, "startRecording prepare() failed");
	        }
	        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 20);
	        mRecorder.start(); 
	        mRecording = true;
	        mEventThrower.Throw(Thrower.RECORDING_STARTED, button);	
	    }

	    private void stopRecording(PlayButton button) {
	    	if(mRecording)
	    	{
		        try {
			        mRecorder.stop();
			        mRecording = false;
			        mEventThrower.Throw(Thrower.RECORDING_STOPPED, button);	
			        mRecorder.release();
			        mRecorder = null;
			        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 20);
			        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 20);
		        } catch (Exception e) {
		            Log.e(LOG_TAG, "stopRecording prepare() failed");
		        }
	    	}
	    }

	    //a declaration of the events that can caught by a catcher
	    interface ThrowListener {
	        public void Catch(int event, PlayButton button);
	    }

	    class Thrower {
	    	public static final int PLAYING_STARTED = 0;
	    	public static final int PLAYING_STOPPED = 1;
	    	public static final int RECORDING_STARTED = 2;
	    	public static final int RECORDING_STOPPED = 3;
	    	
	        //a list of catchers
	        List<ThrowListener> listeners = new ArrayList<ThrowListener>();
	        //a way to add someone to the list of catchers
	        public void addThrowListener(ThrowListener toAdd){
	            listeners.add(toAdd);
	        }

	        public void Throw(int event, PlayButton button) {
	            //1 or more times, a Notification that an event happened is thrown.
	            for (ThrowListener hl : listeners) hl.Catch(event, button);
	        }
	    }
	    
	    public MainActivity() {
	    }
	    
	    @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        MenuInflater inflater = getMenuInflater();
	        inflater.inflate(R.menu.main, menu);
	        return super.onCreateOptionsMenu(menu);
	    }
	    @Override	    
	    public boolean onOptionsItemSelected(MenuItem item) {
	    	int itemId = item.getItemId();
		    switch (itemId) {
		    case R.id.action_settings:
		    	showToast("Clicked Settings");
		    	break;
		    }
		    Log.i(LOG_TAG, String.format("Item %d clicked",itemId));
		    return super.onOptionsItemSelected(item);
	    }
	    @Override
	    public void onCreate(Bundle icicle) {
	        super.onCreate(icicle);
	        setContentView(R.layout.activity_main);
	        
	        ActionBar actionBar = getActionBar();
	        actionBar.show();

	        TableLayout ll = (TableLayout)findViewById(R.id.container);	     
	        
	        PlayButton tmpButton; 
	        
	        for(Integer x = 0; x < ButtonCount; x++)
	        {
		        tmpButton = new PlayButton(this, x.toString() );
		        
		        ll.addView(tmpButton);
		        
		        mPlayButtons.add(tmpButton);
		        mEventThrower.addThrowListener(tmpButton);
	        }	        
	    }

	    @Override
	    public void onPause() {
	        super.onPause();
	        if (mRecorder != null) {
	            mRecorder.release();
	            mRecorder = null;
	        }

	        if (mPlayer != null) {
	            mPlayer.release();
	            mPlayer = null;
	        }
	    }

	    public void showToast(String message)	    
	    {	     
		    Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);  
		    toast.show();	     
	    }
}
