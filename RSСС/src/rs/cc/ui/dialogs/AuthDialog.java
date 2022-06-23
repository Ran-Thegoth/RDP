package rs.cc.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import cs.ui.UIBinder;
import rs.cc.config.SessionConfig.AuthData;
import rs.cc.R;

public class AuthDialog implements OnClickListener, OnShowListener, DialogInterface.OnClickListener{

	private AuthData _auth;
	private AlertDialog _dialog;
	private View _v;
	private Runnable _ok, _cancel;
	public AuthDialog(Context ctx, AuthData auth) {
		AlertDialog.Builder b = new AlertDialog.Builder(ctx);
		_auth = auth;
		_v = LayoutInflater.from(ctx).inflate(R.layout.auth_dialog, new LinearLayout(ctx),false);
		UIBinder.bind(_auth, _v);
		b.setView(_v);
		b.setPositiveButton(android.R.string.ok, null);
		b.setNegativeButton(android.R.string.cancel,this);
		_dialog = b.create();
		_dialog.setCancelable(false);
		_dialog.setCanceledOnTouchOutside(false);
		_dialog.setOnShowListener(this);
	}
	@Override
	public void onShow(DialogInterface arg0) {
		_dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(this);
		
	}
	@Override
	public void onClick(View arg0) {
		if(UIBinder.obtain(_v, _auth)) {
			_dialog.dismiss();
			if(_ok != null)
				_ok.run();
		}
	}
	
	public void show(Runnable ok, Runnable cancel) {
		_ok = ok; _cancel = cancel;
		_dialog.show();
	}
	
	@Override
	public void onClick(DialogInterface arg0, int arg1) {
		if(_cancel != null)
			_cancel.run();
		
	}
	

}
