package com.slair.quickclicksparenting;

import java.io.File;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.slair.quickclicksparenting.MainActivity.ThrowListener;
import com.slair.quickclicksparenting.MainActivity.Thrower;

import android.content.Context;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class PlayButton extends Button implements ThrowListener {
	private MainActivity parent;
	private PlayButton self = this;
    String mFileName = null;
    private File f = null;
    boolean mPressed = false;
    boolean mRecorded = false;
    
    String mMessageName;    
    
    ScheduledExecutorService t = Executors.newScheduledThreadPool(1);
    ScheduledFuture<?> scheduled = null;
    
    public Boolean hasSound()
    {
    	return (f.exists() && !f.isDirectory());
    }
    
	TimerTask task = new TimerTask(){
		public void run()
		{
			if(mPressed)
			{
				mRecorded = true;
				parent.onRecord(true, self);
			}
		}
	};
	
    private OnClickListener clicker = new OnClickListener() {
        public void onClick(View v) {	
        	if(hasSound() && !mRecorded)
        	{            	
        		parent.onPlay(self);
        	}
        }
    };
    
    private OnTouchListener toucher = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Start
            	mPressed = true;
				mRecorded = false;
				scheduled = t.schedule(task, 2, TimeUnit.SECONDS);
                break;
            case MotionEvent.ACTION_UP:
            	try {
					if(scheduled != null)
					{
						scheduled.cancel(true);
					}
				} catch (Exception e) {
					return false;
				}
            	mPressed = false;
            	if(mRecorded)
            	{
            		parent.onRecord(false, self);
            	}
                // End
                break;
            }
            return false;
        }
    };

    public PlayButton(Context ctx) {
        this(ctx, "item");
    }
    
    public PlayButton(Context ctx, String buttonName) {
        super(ctx);
        parent = (MainActivity) ctx;
        mFileName = String.format("%s/%s_%s.3gp", Environment.getExternalStorageDirectory().getAbsolutePath(), "qcp", buttonName);
        
        f = new File(mFileName);
        if(hasSound()) 
        { setText("PLAY"); }
        else
        { setText("NEW SOUND"); }

        setOnClickListener(clicker);
        setOnTouchListener(toucher);
    }

	@Override
	public void Catch(int event, PlayButton button) {
		if(self != button)
		{
			switch(event)
			{
				case Thrower.RECORDING_STOPPED:  
				case Thrower.PLAYING_STOPPED:  
			        if(f.exists() && !f.isDirectory()) 
			        { setText("PLAY"); }
			        else
			        { setText("NEW SOUND"); }
                    break;  
				case Thrower.PLAYING_STARTED:
				case Thrower.RECORDING_STARTED:  
                    setText("<<BUSY>>");
                    break;
                default: 
                    setText("Unknown state!");
                    break;
			}					
		}
		else
		{
			switch(event)
			{
				case Thrower.RECORDING_STOPPED:  
				case Thrower.PLAYING_STOPPED: 
			        if(f.exists() && !f.isDirectory()) 
			        { setText("PLAY"); }
			        else
			        { setText("NEW SOUND"); }
                    break;
				case Thrower.PLAYING_STARTED:  
                    setText("PAUSE");
                    break;
				case Thrower.RECORDING_STARTED:  
                    setText("<<Recording>>");
                    break;
                default: 
                    setText("Unknown state!");
                    break;
			}
		}
	}
}

