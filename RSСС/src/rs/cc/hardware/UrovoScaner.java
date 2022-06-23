package rs.cc.hardware;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.device.ScanManager;
import rs.cc.Core;
import rs.cc.tty.BarcodeTTY;
import rs.cc.tty.TTY;
import rs.cc.ui.SessionActivity;
import rs.cc.R;

public class UrovoScaner extends BroadcastReceiver implements BarcodeScaner {

	private ScanManager _sm;
	private BarcodeListener _l;
	private boolean _once;
	private AlertDialog _dialog;

	public UrovoScaner() {
		_sm = new ScanManager();
		_sm.openScanner();
		_sm.switchOutputMode(0);
	}

	@Override
	public void scanOnce(BarcodeListener l) {
		scanOnce(l, false);
	}

	@Override
	public void startScan(BarcodeListener l) {
		stopScan();
		_l = l;
		_once = false;
		Core.getInstance().registerReceiver(this, new IntentFilter(ScanManager.ACTION_DECODE));
		

	}

	@Override
	public void stopScan() {
		try {
			Core.getInstance().unregisterReceiver(this);
		} catch (Exception | Error e) {
		}

	}

	@Override
	public boolean isManual() {
		return false;
	}

	@Override
	protected void finalize() throws Throwable {
		stopScan();
		super.finalize();
	}

	@Override
	public void onReceive(Context arg0, Intent e) {
		if (_dialog != null && _dialog.isShowing())
			_dialog.dismiss();
		if (_l != null)
			_l.onBarcode(e.getByteArrayExtra(ScanManager.DECODE_DATA_TAG));
		if (_once)
			Core.getInstance().unregisterReceiver(this);

	}

	@Override
	public void scanOnce(BarcodeListener l, boolean showRequest) {
		stopScan();
		_l = l;
		_once = true;
		if (showRequest) {
			if (_dialog == null && l instanceof ContextOwner) {
				_dialog = new AlertDialog.Builder(((ContextOwner) l).getContext()) .setMessage(R.string.do_scan)
						.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								stopScan();
							}
						}).setCancelable(false).create();
				_dialog.setCanceledOnTouchOutside(false);
			}
			if(_dialog != null)
				_dialog.show();
		}
		Core.getInstance().registerReceiver(this, new IntentFilter(ScanManager.ACTION_DECODE));

	}

	@Override
	public void doScan() {
		_sm.startDecode();
	}

	@Override
	public TTY getTTY(SessionActivity owner) {
		return new BarcodeTTY(owner);
	}

	@Override
	public void disable() {
		_sm.closeScanner();
		
	}

	@Override
	public void enable() {
		_sm.openScanner();
	}

}
