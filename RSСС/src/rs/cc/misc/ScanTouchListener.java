package rs.cc.misc;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import rs.cc.Core;
import rs.cc.hardware.BarcodeScaner.BarcodeListener;

public class ScanTouchListener implements OnTouchListener {

	private BarcodeListener _listener;
	public ScanTouchListener(BarcodeListener l) {
		_listener = l;
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent e) {
		 if(e.getAction() == MotionEvent.ACTION_UP) {
	            if(e.getRawX() >= ((TextView)v).getRight() - ((TextView)v).getTotalPaddingRight()) {
	            	Core.getInstance().barcodeScaner(v.getContext()).scanOnce(_listener,true);
	            	return true;
	            }
		 }
		return false;
	}


}
