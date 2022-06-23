package rs.cc.ui.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnTouchListener;

public abstract class SessionView  implements OnTouchListener {

	private static final float TOUCH_SCROLL_DELTA = 10.0f;
	
	private Context _context;
	
	public interface SessionViewListener {
		abstract void onSessionViewBeginTouch(int x, int y);
		abstract void onSessionViewEndTouch(int x,int y);
		abstract void onSessionViewLeftTouch(int x, int y, boolean down);
		abstract void onSessionViewRightTouch(int x, int y, boolean down);
		abstract void onSessionViewMove(int x, int y);
		abstract void onSessionViewScroll(boolean down);
	}

	private class SessionGestureListener extends GestureDetector.SimpleOnGestureListener {
		boolean longPressInProgress = false;
		
		public boolean onDown(MotionEvent e) {
			return true;
		}
		
		public boolean onUp(MotionEvent e) {
        	sessionViewListener.onSessionViewEndTouch((int)e.getX(),(int)e.getY());
			return true;
		}
		
        public void onLongPress(MotionEvent e) {
        	MotionEvent mappedEvent = mapTouchEvent(e);
			sessionViewListener.onSessionViewBeginTouch((int)e.getX(),(int)e.getY());
        	sessionViewListener.onSessionViewLeftTouch((int)mappedEvent.getX(), (int)mappedEvent.getY(), true); 
        	longPressInProgress = true;
        }

        public void onLongPressUp(MotionEvent e) {
        	MotionEvent mappedEvent = mapTouchEvent(e);
        	sessionViewListener.onSessionViewLeftTouch((int)mappedEvent.getX(), (int)mappedEvent.getY(), false);
        	longPressInProgress = false; 
        	sessionViewListener.onSessionViewEndTouch((int)e.getX(),(int)e.getY());
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        	if(longPressInProgress)
        	{
            	MotionEvent mappedEvent = mapTouchEvent(e2);
            	sessionViewListener.onSessionViewMove((int)mappedEvent.getX(), (int)mappedEvent.getY());
                return true;
        	}
        	
        	return false;
        }

        public boolean onDoubleTap(MotionEvent e) {
        	// send 2nd click for double click
        	MotionEvent mappedEvent = mapTouchEvent(e);
        	sessionViewListener.onSessionViewLeftTouch((int)mappedEvent.getX(), (int)mappedEvent.getY(), true);
        	sessionViewListener.onSessionViewLeftTouch((int)mappedEvent.getX(), (int)mappedEvent.getY(), false);
            return true;
        }

        public boolean onSingleTapUp(MotionEvent e) {
        	// send single click
        	MotionEvent mappedEvent = mapTouchEvent(e);
			sessionViewListener.onSessionViewBeginTouch((int)e.getX(),(int)e.getY());
        	sessionViewListener.onSessionViewLeftTouch((int)mappedEvent.getX(), (int)mappedEvent.getY(), true);
        	sessionViewListener.onSessionViewLeftTouch((int)mappedEvent.getX(), (int)mappedEvent.getY(), false);
        	sessionViewListener.onSessionViewEndTouch((int)e.getX(),(int)e.getY());
            return true;
        }		
	}

	private class SessionDoubleGestureListener implements DoubleGestureDetector.OnDoubleGestureListener {
		private MotionEvent prevEvent = null;

		public boolean onDoubleTouchDown(MotionEvent e) {
			sessionViewListener.onSessionViewBeginTouch((int)e.getX(),(int)e.getY());
			prevEvent = MotionEvent.obtain(e);
			return true;
		}
		
		public boolean onDoubleTouchUp(MotionEvent e) {
			if(prevEvent != null)
			{
				prevEvent.recycle();
				prevEvent = null;
			}        	
			sessionViewListener.onSessionViewEndTouch((int)e.getX(),(int)e.getY());
			return true;
		}

		public boolean onDoubleTouchScroll(MotionEvent e1, MotionEvent e2) {
  			// calc if user scrolled up or down (or if any scrolling happened at all)
			float deltaY = e2.getY() - prevEvent.getY();
			if(deltaY > TOUCH_SCROLL_DELTA)
			{
				sessionViewListener.onSessionViewScroll(true);
				prevEvent.recycle();
				prevEvent = MotionEvent.obtain(e2);
			}
			else if(deltaY < -TOUCH_SCROLL_DELTA)
			{
				sessionViewListener.onSessionViewScroll(false);
				prevEvent.recycle();
				prevEvent = MotionEvent.obtain(e2);
			}
            return true;
        }

        public boolean onDoubleTouchSingleTap(MotionEvent e) {
        	// send single click
        	MotionEvent mappedEvent = mapDoubleTouchEvent(e);
        	sessionViewListener.onSessionViewRightTouch((int)mappedEvent.getX(), (int)mappedEvent.getY(), true);
        	sessionViewListener.onSessionViewRightTouch((int)mappedEvent.getX(), (int)mappedEvent.getY(), false);
            return true;
        }		
	}

	private SessionViewListener sessionViewListener = null;
	private GestureDetector gestureDetector;
	private DoubleGestureDetector doubleGestureDetector;
	protected Matrix invScaleMatrix;
	
	public SessionView(Context context) {
		_context = context;
		initSessionView(context);
	}

	protected Context getContext() { return _context; }

	public void setScaleGestureDetector(ScaleGestureDetector scaleGestureDetector) {
		doubleGestureDetector.setScaleGestureDetector(scaleGestureDetector);
	}

	protected void initSessionView(Context context) {
		gestureDetector = new GestureDetector(context, new SessionGestureListener(), null, true);
		doubleGestureDetector = new DoubleGestureDetector(context, null, new SessionDoubleGestureListener());
		invScaleMatrix = new Matrix();
		getView().setOnTouchListener(this);
	}

	public void setSessionViewListener(SessionViewListener sessionViewListener) {
		this.sessionViewListener = sessionViewListener;
	}
	// perform mapping on the touch event's coordinates according to the current scaling

	private MotionEvent mapTouchEvent(MotionEvent event) {
		MotionEvent mappedEvent = MotionEvent.obtain(event);
		float[] coordinates = { mappedEvent.getX(), mappedEvent.getY() };
		invScaleMatrix.mapPoints(coordinates);
		mappedEvent.setLocation(coordinates[0], coordinates[1]);
		return mappedEvent;
	}

	// perform mapping on the double touch event's coordinates according to the current scaling
	private MotionEvent mapDoubleTouchEvent(MotionEvent event) {
		MotionEvent mappedEvent = MotionEvent.obtain(event);		
		float[] coordinates = { (mappedEvent.getX(0) + mappedEvent.getX(1)) / 2, (mappedEvent.getY(0) + mappedEvent.getY(1)) / 2 };
		invScaleMatrix.mapPoints(coordinates);
		mappedEvent.setLocation(coordinates[0], coordinates[1]);
		return mappedEvent;
	}

	
	public float getZoom() {
		return 1.0f;
	}
	public float getZoomX() {
		return 1.0f;
	}
	public float getZoomY() {
		return 1.0f;
	}
	

	public abstract void onKeyboardShown(boolean visible);
	public abstract View getView(); 
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		boolean res = gestureDetector.onTouchEvent(event);
		if(doubleGestureDetector != null)
			res |= doubleGestureDetector.onTouchEvent(event);
		return res;
	}
	public void setZoom(float zoomX, float zoom) { }
	public void setZoom(float zoom) { }

}
