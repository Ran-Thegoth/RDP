/*
   Android Touch Pointer view

   Copyright 2013 Thinstuff Technologies GmbH, Author: Martin Fleisz

   This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
   If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/

package rs.cc.ui.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import rs.cc.Core;
import rs.cc.R;


@SuppressLint("HandlerLeak")
public class TouchPointerView extends ImageView {

	private View _session2d, _sessionRoot;
	// touch pointer listener - is triggered if an action field is 
	public interface TouchPointerListener {
		abstract void onTouchPointerClose();
		abstract void onTouchPointerLeftClick(int x, int y, boolean down);
		abstract void onTouchPointerRightClick(int x, int y, boolean down);
		abstract void onTouchPointerMove(int x, int y);
		abstract void onTouchPointerScroll(boolean down);
		abstract void onTouchPointerResetScrollZoom();
		abstract void onTouchPointerToggleKeyboard();
		abstract void onTouchPointerToggleExtKeyboard();
	}
	
	
	private class UIHandler extends Handler {
				
		UIHandler() {
			super(Core.getInstance().getMainLooper());
		}
		
		@Override
		public void handleMessage(Message msg) {
			setPointerImage(R.drawable.touch_pointer_default);
		}
	}	
	
	
	
	// the touch pointer consists of 9 quadrants with the following functionality:
	//
	// -------------
	// | 0 | 1 | 2 | 
	// -------------
	// | 3 | 4 | 5 | 
	// -------------
	// | 6 | 7 | 8 | 
	// -------------
	//
	// 0 ... contains the actual pointer (the tip must be centered in the quadrant)
	// 1 ... is left empty
	// 2, 3, 5, 6, 7, 8 ... function quadrants that issue a callback
	// 4 ... pointer center used for left clicks and to drag the pointer 
		
	private static final int POINTER_ACTION_CURSOR = 0;	
	private static final int POINTER_ACTION_CLOSE = 3;	
	private static final int POINTER_ACTION_RCLICK = 2;	
	private static final int POINTER_ACTION_LCLICK = 4;	
	private static final int POINTER_ACTION_MOVE = 4;	
	private static final int POINTER_ACTION_SCROLL = 5;	
	private static final int POINTER_ACTION_RESET = 6;	
	private static final int POINTER_ACTION_KEYBOARD = 7;	
	private static final int POINTER_ACTION_EXTKEYBOARD = 8;	

	private static final float SCROLL_DELTA = 10.0f;

	private static final int DEFAULT_TOUCH_POINTER_RESTORE_DELAY = 150; 
	
	private RectF pointerRect;
	private RectF pointerAreaRects[] = new RectF[9];
	private Matrix translationMatrix;
	private Matrix m = new Matrix();
	private boolean pointerMoving = false;
	private boolean pointerScrolling = false;
	private TouchPointerListener listener = null;
	private UIHandler uiHandler = new UIHandler();

	// gesture detection
	private GestureDetector gestureDetector;

	private class TouchPointerGestureListener extends GestureDetector.SimpleOnGestureListener {
		
		private MotionEvent prevEvent = null;
		
		public boolean onDown(MotionEvent e) {
			if(pointerAreaTouched(e, POINTER_ACTION_MOVE))
			{
				prevEvent = MotionEvent.obtain(e);
				pointerMoving = true;
			}
			else if(pointerAreaTouched(e, POINTER_ACTION_SCROLL))
        	{
        		prevEvent = MotionEvent.obtain(e);
        		pointerScrolling = true;
				setPointerImage(R.drawable.touch_pointer_scroll);				
        	}

			return true;
		}
		
		public boolean onUp(MotionEvent e) {
			if(prevEvent != null)
			{
				prevEvent.recycle();
				prevEvent = null;
			}
			
			if(pointerScrolling)
				setPointerImage(R.drawable.touch_pointer_default);				

			pointerMoving = false;
			pointerScrolling = false;
			return true;
		}
		
		public void onLongPress(MotionEvent e) {
			if(pointerAreaTouched(e, POINTER_ACTION_LCLICK))
			{
				setPointerImage(R.drawable.touch_pointer_active);				
				pointerMoving = true;
	        	RectF rect = getCurrentPointerArea(POINTER_ACTION_CURSOR);
				listener.onTouchPointerLeftClick((int)rect.centerX(), (int)rect.centerY(), true);
			}
        }

        public void onLongPressUp(MotionEvent e) {
        	if(pointerMoving)
        	{
				setPointerImage(R.drawable.touch_pointer_default);				
				pointerMoving = false;
	        	RectF rect = getCurrentPointerArea(POINTER_ACTION_CURSOR);
				listener.onTouchPointerLeftClick((int)rect.centerX(), (int)rect.centerY(), false);        		
        	}
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			if(pointerMoving) {
				float deltaX = (int)(e2.getX() - prevEvent.getX());
				float deltaY = (int)(e2.getY() - prevEvent.getY());

				// move pointer graphics
	        	movePointer(deltaX, deltaY);
	        	prevEvent.recycle();
	        	prevEvent = MotionEvent.obtain(e2);				

	        	// send move notification
	        	RectF rect = getCurrentPointerArea(POINTER_ACTION_CURSOR);
	        	listener.onTouchPointerMove((int)rect.centerX(), (int)rect.centerY());

	            return true;
			}
			else if(pointerScrolling)
			{
				// calc if user scrolled up or down (or if any scrolling happened at all) 
				float deltaY = e2.getY() - prevEvent.getY();
				if(deltaY > SCROLL_DELTA)
				{
					listener.onTouchPointerScroll(true);
					prevEvent.recycle();
					prevEvent = MotionEvent.obtain(e2);
				}
				else if(deltaY < -SCROLL_DELTA)
				{
					listener.onTouchPointerScroll(false);
					prevEvent.recycle();
					prevEvent = MotionEvent.obtain(e2);
				}
	            return true;
			}
            return false;
        }

        public boolean onSingleTapUp(MotionEvent e) {
        	// look what area got touched and fire actions accordingly
           	if(pointerAreaTouched(e, POINTER_ACTION_CLOSE))
           		listener.onTouchPointerClose();
          	else if(pointerAreaTouched(e, POINTER_ACTION_LCLICK))
          	{
          		displayPointerImageAction(R.drawable.touch_pointer_lclick);
          		RectF rect = getCurrentPointerArea(POINTER_ACTION_CURSOR);
          		listener.onTouchPointerLeftClick((int)rect.centerX(), (int)rect.centerY(), true);
          		listener.onTouchPointerLeftClick((int)rect.centerX(), (int)rect.centerY(), false);
          	}
           	else if(pointerAreaTouched(e, POINTER_ACTION_RCLICK))
           	{
          		displayPointerImageAction(R.drawable.touch_pointer_rclick);
          		RectF rect = getCurrentPointerArea(POINTER_ACTION_CURSOR);
          		listener.onTouchPointerRightClick((int)rect.centerX(), (int)rect.centerY(), true);
          		listener.onTouchPointerRightClick((int)rect.centerX(), (int)rect.centerY(), false);
           	}
           	else if(pointerAreaTouched(e, POINTER_ACTION_KEYBOARD))
           	{
          		displayPointerImageAction(R.drawable.touch_pointer_keyboard);           		
           		listener.onTouchPointerToggleKeyboard();
           	}
           	else if(pointerAreaTouched(e, POINTER_ACTION_EXTKEYBOARD))
           	{
          		displayPointerImageAction(R.drawable.touch_pointer_extkeyboard);
           		listener.onTouchPointerToggleExtKeyboard();
           	}
           	else if(pointerAreaTouched(e, POINTER_ACTION_RESET))
           	{
          		displayPointerImageAction(R.drawable.touch_pointer_reset);
           		listener.onTouchPointerResetScrollZoom();
           	}

           	return true;
        }		
        
        public boolean onDoubleTap(MotionEvent e) {
        	// issue a double click notification if performed in center quadrant
          	if(pointerAreaTouched(e, POINTER_ACTION_LCLICK))
          	{
          		RectF rect = getCurrentPointerArea(POINTER_ACTION_CURSOR);
          		listener.onTouchPointerLeftClick((int)rect.centerX(), (int)rect.centerY(), true);
          		listener.onTouchPointerLeftClick((int)rect.centerX(), (int)rect.centerY(), false);
          	}
          	return true;
        }
	}	

	public TouchPointerView(Context context) {
		super(context);
		initTouchPointer(context);
	}

	public TouchPointerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initTouchPointer(context);
	}

	public TouchPointerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initTouchPointer(context);
	}

	private void initTouchPointer(Context context) {
		if(getDrawable() == null)
			setImageResource(R.drawable.touch_pointer_default);
		gestureDetector = new GestureDetector(context, new TouchPointerGestureListener(), null, true);
		gestureDetector.setLongPressTimeout(500);
		translationMatrix = new Matrix();
		setScaleType(ScaleType.MATRIX);
		setImageMatrix(translationMatrix);
				
		// init rects
		final float rectSizeWidth = (float)getDrawable().getIntrinsicWidth() / 3.0f;
		final float rectSizeHeight = (float)getDrawable().getIntrinsicWidth() / 3.0f;
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 3; j++)
			{
				int left = (int)(j * rectSizeWidth);
				int top = (int)(i * rectSizeHeight);
				int right = left + (int)rectSizeWidth;
				int bottom = top + (int)rectSizeHeight;
				pointerAreaRects[i * 3 + j] = new RectF(left, top, right, bottom);
			}
		}		
		pointerRect = new RectF(0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
	}

	public void setTouchPointerListener(TouchPointerListener listener) {
		this.listener = listener;
	}
	
	public int getPointerWidth() {
		return getDrawable().getIntrinsicWidth();
	}

	public int getPointerHeight() {
		return getDrawable().getIntrinsicHeight();
	}	
	
	public float[] getPointerPosition() {
		float []curPos = new float[2];
		translationMatrix.mapPoints(curPos);
		return curPos;
	}

	public void movePointer(float deltaX, float deltaY) {
		m.set(translationMatrix);
		m.postTranslate(deltaX, deltaY);

		if(_session2d == null) {
			_session2d = ((Activity)getContext()).findViewById(R.id.v_scroll_view);
			_sessionRoot = ((Activity)getContext()).findViewById(R.id.session_root_view);
		}

		RectF rect = new RectF(0,0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
		m.mapRect(rect);

		if(rect.right > _sessionRoot.getWidth()) {
			_session2d.scrollTo((int) (_session2d.getScrollX() + rect.right - _sessionRoot.getWidth()), _session2d.getScrollY());
		} else if(rect.left < 0) {
			_session2d.scrollTo((int) (_session2d.getScrollX() + rect.left), _session2d.getScrollY());
		}

		if(rect.bottom > _sessionRoot.getHeight()) {
			_session2d.scrollTo(_session2d.getScrollX(), (int)(_session2d.getScrollY() + rect.bottom - _sessionRoot.getHeight()));
		} else if (rect.top < 0) {
			_session2d.scrollTo(_session2d.getScrollX(), (int)(_session2d.getScrollY() + rect.top));
		}


		if(rect.bottom > getHeight()) {
			deltaY -= (rect.bottom - getHeight());
		}
		if(rect.right > getWidth()) {
			deltaX -= (rect.right- getWidth());
		}
		if(rect.left < 0) {
			deltaX -= rect.left; 
		}
		if(rect.top < 0) {
			deltaY -= rect.top; 
		}
		
		/*
		if(rect.bottom > _sessionRoot.getHeight() - rect.top - _session2d.getScrollY() + _session2d.getHeight()) {
			deltaY -= (_session2d.getHeight() - (rect.top + _session2d.getScrollY()));
			if(deltaY > 0) {
				deltaY = 0;
			}
		}
		*/
		translationMatrix.postTranslate(deltaX, deltaY);
		setImageMatrix(translationMatrix);
	}
	
	private void ensureVisibility(int screen_width, int screen_height)
	{
		float []curPos = new float[2];
		translationMatrix.mapPoints(curPos);
		
	    if (curPos[0] > (screen_width - pointerRect.width()))
	    	curPos[0] = screen_width - pointerRect.width();
	    if (curPos[0] < 0)
	    	curPos[0] = 0;
	    if (curPos[1] > (screen_height - pointerRect.height()))
	    	curPos[1] = screen_height - pointerRect.height();
	    if (curPos[1] < 0)
	    	curPos[1] = 0;

	    translationMatrix.setTranslate(curPos[0], curPos[1]);
	    setImageMatrix(translationMatrix);	
	}
	
	private void displayPointerImageAction(int resId)
	{
		setPointerImage(resId);
		uiHandler.sendEmptyMessageDelayed(0, DEFAULT_TOUCH_POINTER_RESTORE_DELAY);
	}
	
	private void setPointerImage(int resId)
	{
		setImageResource(resId);
	}
	
	// returns the pointer area with the current translation matrix applied
	private RectF getCurrentPointerArea(int area) {
		RectF transRect = new RectF(pointerAreaRects[area]);
		translationMatrix.mapRect(transRect);
		return transRect;						
	}

	private boolean pointerAreaTouched(MotionEvent event, int area) {				
		RectF transRect = new RectF(pointerAreaRects[area]);
		translationMatrix.mapRect(transRect);
		if(transRect.contains(event.getX(), event.getY()))
			return true;			
		return false;
	}	
	
	private boolean pointerTouched(MotionEvent event) {				
		RectF transRect = new RectF(pointerRect);
		translationMatrix.mapRect(transRect);
		if(transRect.contains(event.getX(), event.getY()))
			return true;			
		return false;
	}			

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// check if pointer is being moved or if we are in scroll mode or if the pointer is touched
		if(!pointerMoving && !pointerScrolling && !pointerTouched(event))
			return false;
		return gestureDetector.onTouchEvent(event);
	}		

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom)
	{
		
		// ensure touch pointer is visible
		if (changed)
			ensureVisibility(right - left, bottom - top);		
	}
}
