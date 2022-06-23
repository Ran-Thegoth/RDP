package rs.cc.hardware;

import rs.cc.tty.TTY;
import rs.cc.ui.SessionActivity;

public interface BarcodeScaner {
	public static interface BarcodeListener {
		public void onBarcode(byte [] barcode);
	}
	public void scanOnce(BarcodeListener l, boolean showRequest);
	public void scanOnce(BarcodeListener l);
	public void startScan(BarcodeListener l);
	public void doScan();
	public void stopScan();
	public boolean isManual();
	public TTY getTTY(SessionActivity owner);
	public void disable();
	public void enable();
}
