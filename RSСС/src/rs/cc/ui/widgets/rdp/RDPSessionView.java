/*
   Android Session view

   Copyright 2013 Thinstuff Technologies GmbH, Author: Martin Fleisz

   This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
   If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/

package rs.cc.ui.widgets.rdp;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import rs.cc.Core;
import rs.cc.connection.rdp.RDPSessionState;
import rs.cc.ui.widgets.SessionView;
import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;

import java.util.*;


public class RDPSessionView extends SessionView {

	private int width;
	private int height;
	private BitmapDrawable surface;
	private Stack<Rect> invalidRegions;

	private int touchPointerPaddingWidth = 0;
	private int touchPointerPaddingHeight = 0;

	public static final float MAX_SCALE_FACTOR = 3.0f;
	public static final float MIN_SCALE_FACTOR = 1.0f;
	private static final float SCALE_FACTOR_DELTA = 0.0001f;

	// helpers for scaling gesture handling
	private float scaleFactorX = 1.0f, scaleFactorY = 1.0f;
	private Matrix scaleMatrix;

	private RectF invalidRegionF;

	// private static final String TAG = "FreeRDP.SessionView";

	@Override
	protected void initSessionView(Context context) {
		_view = new RdpView(context);
		super.initSessionView(context);
		invalidRegions = new Stack<Rect>();

		scaleMatrix = new Matrix();

		invalidRegionF = new RectF();
	}

	public RDPSessionView(Context context) {
		super(context);

	}

	public void addInvalidRegion(Rect invalidRegion) {
		invalidRegionF.set(invalidRegion);
		scaleMatrix.mapRect(invalidRegionF);
		invalidRegionF.roundOut(invalidRegion);
		invalidRegions.add(invalidRegion);
	}

	@SuppressWarnings("deprecation")
	public void invalidateRegion() {
		getView().invalidate(invalidRegions.pop());
	}

	public void onSurfaceChange(RDPSessionState session) {
		surface = session.getSurface();
		Bitmap bitmap = surface.getBitmap();
		if(session.getConfig().screenConfig().fullScreen ) {
			width = Core.getInstance().getResources().getDisplayMetrics().widthPixels;
			height = Core.getInstance().getResources().getDisplayMetrics().heightPixels;
		} else {
			width = bitmap.getWidth();
			height = bitmap.getHeight();
		}
		//width = Globals.getInstance().getResources().getDisplayMetrics().widthPixels;
		//height = Globals.getInstance().getResources().getDisplayMetrics().heightPixels;
		/*
		 * float scale = 1f; int pw = ((View)getParent()).getWidth(); int ph =
		 * ((View)getParent()).getHeight(); scale = pw / (float)width; while(height *
		 * scale > ph) scale -= 0.05f; setScaleX(scale); setScaleY(scale);
		 */
		surface.setBounds(0, 0, width, height);
		getView().setLeft(0);
		getView().setTop(0);
		getView().setMinimumWidth(width);
		getView().setMinimumHeight(height);

		getView().requestLayout();
	}

	public void setZoom(float factor) {
		setZoom(factor,factor);
	}
	public void setZoom(float factorX, float factorY) {
		// calc scale matrix and inverse scale matrix (to correctly transform the view
		// and moues coordinates)
		scaleFactorX = factorX;
		scaleFactorY = factorY;
		scaleMatrix.setScale(scaleFactorX, scaleFactorY);
		invScaleMatrix.setScale(1.0f / scaleFactorX, 1.0f / scaleFactorY);

		// update layout
		getView().requestLayout();
	}

	@Override
	public float getZoom() {
		return scaleFactorX;
	}

	public boolean isAtMaxZoom() {
		return (scaleFactorX > (MAX_SCALE_FACTOR - SCALE_FACTOR_DELTA));
	}

	public boolean isAtMinZoom() {
		return (scaleFactorX < (MIN_SCALE_FACTOR + SCALE_FACTOR_DELTA));
	}

	public boolean zoomIn(float factor) {
		boolean res = true;
		scaleFactorX += factor;
		scaleFactorY += factor;
		if (scaleFactorX > (MAX_SCALE_FACTOR - SCALE_FACTOR_DELTA)) {
			scaleFactorX = MAX_SCALE_FACTOR;
			res = false;
		}
		if (scaleFactorY > (MAX_SCALE_FACTOR - SCALE_FACTOR_DELTA)) {
			scaleFactorY = MAX_SCALE_FACTOR;
			res = false;
		}
		
		setZoom(scaleFactorX,scaleFactorY);
		return res;
	}

	public boolean zoomOut(float factor) {
		boolean res = true;
		scaleFactorX -= factor;
		scaleFactorY -= factor;
		if (scaleFactorX < (MIN_SCALE_FACTOR + SCALE_FACTOR_DELTA)) {
			scaleFactorX = MIN_SCALE_FACTOR;
			res = false;
		}
		if (scaleFactorY < (MIN_SCALE_FACTOR + SCALE_FACTOR_DELTA)) {
			scaleFactorY = MIN_SCALE_FACTOR;
			res = false;
		}
		
		setZoom(scaleFactorX,scaleFactorY);
		return res;
	}

	public void setTouchPointerPadding(int widht, int height) {
		touchPointerPaddingWidth = widht;
		touchPointerPaddingHeight = height;
		getView().requestLayout();
	}

	public int getTouchPointerPaddingWidth() {
		return touchPointerPaddingWidth;
	}

	public int getTouchPointerPaddingHeight() {
		return touchPointerPaddingHeight;
	}

	private class RdpView extends View {

		public RdpView(Context context) {
			super(context);
		}

		@Override
		public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			Log.v("SessionView", width + "x" + height);
			this.setMeasuredDimension((int) (width * scaleFactorX) + touchPointerPaddingWidth,
					(int) (height * scaleFactorY) + touchPointerPaddingHeight);
		}

		@Override
		public void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			canvas.save();
			canvas.concat(scaleMatrix);
			surface.draw(canvas);
			canvas.restore();
		}

		// dirty hack: we call back to our activity and call onBackPressed as this
		// doesn't reach us when the soft keyboard is shown ...
		@Override
		public boolean dispatchKeyEventPreIme(KeyEvent event) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
				((Activity) this.getContext()).onBackPressed();
			return super.dispatchKeyEventPreIme(event);
		}

	}

	public Bitmap getScreenshot() {
		return surface.getBitmap();

	}

	private RdpView _view;

	@Override
	public View getView() {
		return _view;
	}

	@Override
	public void onKeyboardShown(boolean visible) {
	}

	@Override
	public float getZoomX() {
		return scaleFactorX;
	}
	@Override
	public float getZoomY() {
		return scaleFactorY;
	}
}
