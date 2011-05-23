/**
 * 
 */
package ltg.phenomena;

import java.util.Observable;
import java.util.Observer;

import ltg.phenomena.helioroom.Degree;
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
    public static final int STATE_RUNNING = 3;
    

    /** The thread that actually draws the animation */
    private CanvasThread thread;
    
    /** Handle to the data to be rendered */
	private Helioroom mData;
	
	private float n = -1;
	private final float pl_r = 12;
	private float orb_r = -1;
	private float[] planetXs = null;
	private float[] planetYs = null;
    private int grabbedPlanet = -1;
	
	
	/**
	 * Rendering thread
	 * @author tebemis
	 */
	class CanvasThread extends Thread {
	
        private int mCanvasWidth = 1;
        /** The state of the simulation. One of SETUP, RUNNING, or PAUSE*/
        private int mState = STATE_SETUP;
        /** Indicate whether the surface has been created & is ready to draw */
        private boolean mRun = false;
        /** Handle to the surface manager object we interact with */
        private SurfaceHolder mSurfaceHolder;
        /** Time since the beginning of the simulation*/
        private double timeDelta = 0;
        /** Time since the last frame*/
        //private long lastFrame = 0;  
        private long currFrame = 0;
        // Variable used to compute frame rate
        //private int frames = 0;
        //private long time = 0;

        
        public CanvasThread(SurfaceHolder surfaceHolder, Context context) {
        	this.setName("Rendering");
            mSurfaceHolder = surfaceHolder;
        }
		
        
        @Override
        public void run() {
        	while (mRun) {
        		Canvas c = null;
        		if (mState == STATE_RUNNING) {
        			try {
        				c = mSurfaceHolder.lockCanvas(null);
        				synchronized (mSurfaceHolder) {
        					// Computes the time deltas
        		        	currFrame = System.currentTimeMillis() + mData.getNtcf();
        		        	timeDelta = ((double)(currFrame)) / 1000 - (double) mData.getStartTime();
        		        	// Draws
        					doDraw(c);
        				}
        			} finally {
        				if (c != null) {
        					mSurfaceHolder.unlockCanvasAndPost(c);
        				}
        			}
        		}
        		// Waits for the data to be ready
        		if (mState==STATE_SETUP)
        			if (mData != null && mData.getInstanceId()!= null) {
        				// Once data is received decide if the simulation is paused or not
        				if (mData.getState().equals(Helioroom.RUNNING))
        					// simutation is not paused so we just regularly start the rendering
        					setState(STATE_RUNNING);
        				else {
        					// simulation is paused so we refresh the screen and 
        					// put the thread in pause 
        					refreshCanvas();
        					pause();
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
            }
        }
        
        
        private void doDraw(Canvas canvas) {
        	// Draw background
        	drawBackground(canvas);
        	// Draw orbits & planets
        	Paint pa = new Paint();
        	pa.setAntiAlias(true);
        	RectF bb = null;
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
        		planetXs[i-1]=mCanvasWidth/2 + p.getX()*scale;
        		planetYs[i-1]=mCanvasWidth/2 + p.getY()*scale;
        		canvas.drawCircle(planetXs[i-1], planetYs[i-1], pl_r, pa);
        		// Draw the touch area
//        		pa.setStyle(Style.STROKE);
//        		canvas.drawRect(planetXs[i-1]-orb_r/2, planetYs[i-1]-orb_r/2, planetXs[i-1]+orb_r/2, planetYs[i-1]+orb_r/2, pa);
        		// Increase counter (drawing from the inside out)
        		i++;
        	}
        	// Compute frame rate
//        	if (lastFrame!=0) {
//        		time = time + (currFrame - lastFrame);
//        		lastFrame = currFrame;
//        		frames++;
//        	} else {
//        		lastFrame = currFrame;
//        	}
//        	if (time >= 5000) {
//        		Log.d("SimView", "FPS = "+frames/5);
//        		time = 0; frames = 0;
//        	}
        	// Set helioroom as changed
        	mData.markAsChanged();
        		
        }
        
        private void drawBackground(Canvas canvas) {
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

        }
        
        
        public void dragDraw(float x, float y) {
        	Canvas canvas = null;
    		try {
    			canvas = mSurfaceHolder.lockCanvas(null);
    			synchronized (mSurfaceHolder) {
    	    		// Find planet position
    				Planet p = mData.getPlanets().get(grabbedPlanet);
    				p.findDragPosition(x-mCanvasWidth/2, y-mCanvasWidth/2);
    				// Draw background
    				drawBackground(canvas);
    				// Draw orbit
    	    		Paint pa = new Paint();
    	    		pa.setAntiAlias(true);
    	    		RectF bb = new RectF(mCanvasWidth/2 - (2*pl_r+(grabbedPlanet+1)*orb_r),  mCanvasWidth/2 - (2*pl_r+(grabbedPlanet+1)*orb_r), mCanvasWidth/2 + (2*pl_r+(grabbedPlanet+1)*orb_r), mCanvasWidth/2 + (2*pl_r+(grabbedPlanet+1)*orb_r));
            		pa.setColor(Color.GRAY);
            		pa.setStyle(Style.STROKE);
            		canvas.drawArc(bb, 0, 360, false, pa);
    				// Draw planet
    	    		pa.setStyle(Style.FILL);
    	    		pa.setColor(Color.parseColor(p.getColor()));
    	    		float scale = 2*pl_r+(grabbedPlanet+1)*orb_r;
    	    		planetXs[grabbedPlanet] = mCanvasWidth/2 + p.getX()*scale;
    	    		planetYs[grabbedPlanet] = mCanvasWidth/2 + p.getY()*scale;
    	    		canvas.drawCircle(planetXs[grabbedPlanet], planetYs[grabbedPlanet], pl_r, pa);
    			}
    		} finally {
    			if (canvas != null) {
    				mSurfaceHolder.unlockCanvasAndPost(canvas);
    			}
    		}
        }


		public void refreshCanvas() {
			Canvas c = null;
    			try {
    				c = mSurfaceHolder.lockCanvas(null);
    				synchronized (mSurfaceHolder) {
    					currFrame = mData.getStartOfLastPauseTime();
    					timeDelta = ((double)(currFrame)) / 1000 - (double) mData.getStartTime();
    					doDraw(c);
    				}
    			} finally {
    				if (c != null) {
    					mSurfaceHolder.unlockCanvasAndPost(c);
    				}
    			}
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
	
	
	@Override
	public void update(Observable observable, Object data) {
		mData = ((Helioroom) data);
		if (n!=mData.getPlanets().size()) {
			n = (float) mData.getPlanets().size();
			planetXs = new float[(int)n];
			planetYs = new float[(int)n];
			orb_r = (thread.mCanvasWidth -4*pl_r) / (2*(n+1));
		}
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
	
	
	
    public void touch_start(float x, float y) {
        for (int i=0; i<planetXs.length; i++) {
        	if(x<planetXs[i]+orb_r/2 && x>planetXs[i]-orb_r/2 && y<planetYs[i]+orb_r/2 && y>planetYs[i]-orb_r/2) {
        		grabbedPlanet = i;
        		thread.pause();
        	}
        }
    }
    
    
    public void touch_move(float x, float y) {
    	if (grabbedPlanet != -1) 
    		thread.dragDraw(x, y);        
    }
    
    
    public void releaseGrabbedPlanet() {
    	grabbedPlanet = -1;
    	if (mData.getState().equals(Helioroom.RUNNING))
    		thread.unpause();
    	else {
    		// Release with paused system
    		Log.e("Resume", "Release with paused system");
    		thread.refreshCanvas();
    	}
    }

       
    public String getGrabbedPlanet() {
    	if (grabbedPlanet!=-1)
    		return mData.getPlanets().get(grabbedPlanet).getName();
    	return null;
    }
    
    
    public String moveGrabbedPlanet() {
    	Planet p = mData.getPlanets().get(grabbedPlanet);
    	Degree currentPosition = p.getCurrentPositionDegree();
    	// Computes the time deltas
    	long currFrame = System.currentTimeMillis() + mData.getNtcf();
    	double timeDelta = ((double)(currFrame)) / 1000 - (double) mData.getStartTime();
    	p.computePosition(timeDelta);
    	Degree computedPosition = p.getCurrentPositionDegree();
    	Degree newStartPosition = p.getStartPosition().sub(computedPosition.sub(currentPosition));
    	p.setStartPosition(newStartPosition);
    	return String.format("%.3f",newStartPosition.getValue());
    }  
	
}
