package rs.cc.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;
import rs.cc.R;
import rs.cc.misc.Utils;

public class KeyInputDialog extends AlertDialog implements View.OnClickListener, DialogInterface.OnShowListener, DialogInterface.OnKeyListener {

	public static interface OnKeySelected {
		public void onKeySelected(int code);
	}
	private  int _key;
	private OnKeySelected _l;
	private View _ok;
	public KeyInputDialog(Context context) {
		super(context);
		
		setupUI();
	}

	public KeyInputDialog(Context context, int themeResId) {
		super(context, themeResId);
		setupUI();
	}

	public KeyInputDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		setupUI();
	}
	
	private void setupUI() {
		setMessage(getContext().getString(R.string.press_key));
		setButton(BUTTON_POSITIVE, getContext().getString(android.R.string.ok), (OnClickListener)null);
		setButton(BUTTON_NEGATIVE,getContext().getString(android.R.string.cancel),(OnClickListener)null);
		setOnShowListener(this);
		setOnKeyListener(this);
	}
	


	@Override
	public void onShow(DialogInterface arg0) {
		_ok = getButton(BUTTON_POSITIVE);
		_ok.setOnClickListener(this);
		_ok.setEnabled(false);
		
	}

	@Override
	public void onClick(View v) {
		dismiss();
		if(_l != null)
			_l.onKeySelected(_key);
	}
	public void show(OnKeySelected l) {
		_l = l;
		show();
	}

	@Override
	public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent arg2) {
		if(keyCode == KeyEvent.KEYCODE_BACK) return false;
		_key = keyCode;
		setMessage(Utils.keyName(keyCode));
		_ok.setEnabled(true);
		return true;
	}

}
