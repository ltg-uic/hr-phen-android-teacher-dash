/**
 * 
 */
package ltg.phenomena;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import ltg.SntpClient;
import ltg.phenomena.helioroom.Helioroom;
import ltg.phenomena.helioroom.HelioroomWindow;
import ltg.phenomena.helioroom.Planet;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

/**
 * @author tebemis
 *
 */
public class SimulationView extends SurfaceView implements Observer, SurfaceHolder.Callback {
	
	/*
     * State-tracking constants
     */
	public static final int STATE_SETUP = 1;
    public static final int STATE_PAUSE = 2;
    public static final int STATE_STOP = 3;
    public static final int STATE_RUNNING = 4;
    
    
    /** Handle to the application context, used to e.g. fetch Drawables. */
    private Context mContext;

    /** Pointer to the text view to display "Paused.." etc. */
    private TextView mStatusText;

    /** The thread that actually draws the animation */
    private CanvasThread thread;
    
    /** Handle to the data to be rendered */
	private Helioroom mData;
	
	private float n = -1;
	private final float pl_r = 12;
	private float orb_r = -1;
	
	
	/**
	 * Rendering thread
	 * @author tebemis
	 */
	class CanvasThread extends Thread {
	
        private int mCanvasHeight = 1;
        private int mCanvasWidth = 1;
        /** The state of the game. One of SETUP, RUNNING, PAUSE or STOP*/
        private int mState = STATE_SETUP;
        /** Indicate whether the surface has been created & is ready to draw */
        private boolean mRun = false;
        /** Handle to the surface manager object we interact with */
        private SurfaceHolder mSurfaceHolder;
        /** Time since the beginning of the simulation*/
        private double timeDelta = 0;
        /** Time since the last frame*/
        private long lastFrame = 0;  
        // Variable used to compute frame rate
        private int frames = 0;
        private long time = 0;

        
        public CanvasThread(SurfaceHolder surfaceHolder, Context context) {
        	this.setName("Rendering");
            mSurfaceHolder = surfaceHolder;
            mContext = context;
        }
		
        
		@Override
		public void run() {
			while (mRun) {
                Canvas c = null;
                try {
                    c = mSurfaceHolder.lockCanvas(null);
                    synchronized (mSurfaceHolder) {
                    	if (mState == STATE_RUNNING)
                        	doDraw(c);
                        // Waits for the data to be ready
                        if (mState==STATE_SETUP)
                        	if (mData != null && mData.getInstanceId()!= null)
                        		setState(STATE_RUNNING);
                    }
                } finally {
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
			doStop();
		}
		
		
		private void doStop() {
			Log.e("", "Stopped thread!");
		}


		public void pause() {
			synchronized (mSurfaceHolder) {
                if (mState == STATE_RUNNING) 
                	setState(STATE_PAUSE);
            }
		}
		
		
        public void unpause() {
            synchronized (mSurfaceHolder) {
               if(mState==STATE_PAUSE)
            	   setState(STATE_RUNNING);
            }
        }
		
		
		
        public void setRunning(boolean b) {
        	synchronized (mSurfaceHolder) {
        		mRun = b;
        	}
        }
        
        
        public void setState(int mode) {
            synchronized (mSurfaceHolder) {
                mState = mode;
            }
        }


        
        public void setSurfaceSize(int width, int height) {
            synchronized (mSurfaceHolder) {
                mCanvasWidth = width;
                mCanvasHeight = height;
            }
        }
        
        
        private void doDraw(Canvas canvas) {
        	// Computes the time deltas
        	long currFrame = System.currentTimeMillis() + mData.getNtcf();
        	timeDelta = ((double)(currFrame)) / 1000 - (double) mData.getStartTime();
        	// Clean the background
        	canvas.drawColor(Color.BLACK);
        	// Draw the window wedges
        	Paint pa = new Paint();
        	pa.setAntiAlias(true);
        	RectF bb = new RectF(mCanvasWidth/2 - (float)(2*pl_r+(n+.5)*orb_r),  mCanvasWidth/2 - (float)(2*pl_r+(n+.5)*orb_r), mCanvasWidth/2 + (float)(2*pl_r+(n+.5)*orb_r), mCanvasWidth/2 + (float)(2*pl_r+(n+.5)*orb_r));
        	pa.setColor(Color.argb(255, 40, 40, 40));
        	for (HelioroomWindow w: mData.getWindows()) {
        		canvas.drawArc(bb, w.getViewAngleEnd(), -(w.getViewAngleEnd()-w.getViewAngleBegin()), true, pa);
        	}
        	//Draw the Sun in the middle
        	pa.setColor(Color.WHITE);
        	canvas.drawCircle(mCanvasWidth/2, mCanvasWidth/2, 2*pl_r, pa);
        	// Draw window labels
        	pa.setTextSize(20);
        	pa.setTextAlign(Align.CENTER);
        	for (HelioroomWindow w: mData.getWindows()) {
        		int angle = w.getViewAngleEnd()-(w.getViewAngleEnd()-w.getViewAngleBegin())/2;
        		float x = (float) ((2*pl_r+(n+1)*orb_r)*Math.cos(Math.toRadians(angle)));
        		float y = (float) (5 +(2*pl_r+(n+1)*orb_r)*Math.sin(Math.toRadians(angle)));
        		canvas.drawText(w.getName(), mCanvasWidth/2 + x, mCanvasWidth/2 + y, pa);
        	}
        	// Draw orbits & planets
        	int i = 1;
        	for (Planet p: mData.getPlanets()) {
        		// Planet orbits 
        		bb = new RectF(mCanvasWidth/2 - (2*pl_r+i*orb_r),  mCanvasWidth/2 - (2*pl_r+i*orb_r), mCanvasWidth/2 + (2*pl_r+i*orb_r), mCanvasWidth/2 + (2*pl_r+i*orb_r));
        		pa.setColor(Color.GRAY);
        		pa.setStyle(Style.STROKE);
        		canvas.drawArc(bb, 0, 360, false, pa);
        		// Planet
        		p.computePosition(timeDelta);
        		p.findNextWindow(mData.getWindows());
        		pa.setStyle(Style.FILL);
        		pa.setColor(Color.parseColor(p.getColor()));
        		float scale = 2*pl_r+i*orb_r;
        		canvas.drawCircle(mCanvasWidth/2 + p.getX()*scale, mCanvasWidth/2 + p.getY()*scale, pl_r, pa);
        		// Increase counter (drawing from the inside out)
        		i++;
        	}
        	// Compute frame rate
        	if (lastFrame!=0) {
        		time = time + (currFrame - lastFrame);
        		lastFrame = currFrame;
        		frames++;
        	} else {
        		lastFrame = currFrame;
        	}
        	if (time >= 5000) {
        		Log.d("SimView", "FPS = "+frames/5);
        		time = 0; frames = 0;
        	}
        	// Set helioroom as changed
        	mData.markAsChanged();
        		
        }
		
	}

	
	//---------------------------//
	// END OF THREAD DECLARATION //
	//---------------------------//
	
	
	
	
	
	public SimulationView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        // create thread only; it's started in surfaceCreated()
        thread = new CanvasThread(holder, context);
        setFocusable(true); // make sure we get key events
	}
	
	
	public CanvasThread getThread() {
		return thread;
	}
	
	
	public void setTextView(TextView textView) {
		mStatusText = textView;
	}
	
	
	@Override
	public void update(Observable observable, Object data) {
		mData = ((Helioroom) data);
		n = (float) mData.getPlanets().size();
		orb_r = (thread.mCanvasWidth -4*pl_r) / (2*(n+1));
	}
	

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		thread.setSurfaceSize(width, height); 
	}

	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		thread.setRunning(true);
        thread.start();
	}

	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
	}
}
