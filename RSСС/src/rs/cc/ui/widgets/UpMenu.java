package rs.cc.ui.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import rs.cc.R;

public class UpMenu extends LinearLayout implements OnTouchListener, View.OnClickListener {

	public static interface UpMenuListener {
		public void onPonter();
		public void onKeyboard();
		public void onNumPad();
		public void onBarcode();
	}
	private UpMenuListener _listener;
	
	public UpMenu(Context context,AttributeSet attrs) {
		super(context,attrs);
		setOrientation(HORIZONTAL);
		ImageView base = new ImageView(context);
		base.setImageResource(R.drawable.up_menu_base);
		base.setOnTouchListener(this);
		addView(base,new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));base = new ImageView(context);
		base = new ImageView(context);
		base.setImageResource(R.drawable.up_menu_end);
		addView(base,new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));base = new ImageView(context);
	}

	public void setUpMenuListener(UpMenuListener l) {
		_listener = l;
	}
	public void enableBarcodeReader() {
		ImageView base = new ImageView(getContext());
		base.setImageResource(R.drawable.up_menu_qr);
		base.setOnClickListener(this);
		addView(base,1,new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_UP) {
			if(event.getX() < v.getMeasuredWidth() / 3) {
				if(_listener != null)
					_listener.onPonter();
			} else if(event.getX() < v.getMeasuredWidth() - (v.getMeasuredWidth() / 3)) {
				if(_listener != null)
					_listener.onKeyboard();
			} else 
				if(_listener != null)
					_listener.onNumPad();
		}
		return true;
	}

	@Override
	public void onClick(View arg0) {
		if(_listener !=null)
			_listener.onBarcode();
	}

}
