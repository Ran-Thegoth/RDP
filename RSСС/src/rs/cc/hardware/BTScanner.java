package rs.cc.hardware;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import rs.cc.tty.FIFOTTY;
import rs.cc.tty.TTY;
import rs.cc.ui.SessionActivity;

public class BTScanner implements BarcodeScaner, Runnable {

	private static final UUID SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private BluetoothDevice _device;
	private BluetoothSocket _socket;
	private InputStream _is;
	private OutputStream _os;
	private BarcodeListener _l;
	private boolean _once;
	private Thread _reader;
	
	private boolean _enabled;
	private class BTTTY extends FIFOTTY {
		@Override
		public void write(byte[] buf, int offset, int size) {
			if(_os != null) try {
				_os.write(buf, offset, size);
				super.write(buf, offset, size);
			} catch(Exception e) { }
		}
	}
	
	public BTScanner(String device) {
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		if (!adapter.isEnabled())
			adapter.enable();
		_device = adapter.getRemoteDevice(device);
	}

	@Override
	public void scanOnce(BarcodeListener l, boolean showRequest) {
		_l = l;
		_once = true;
		_reader = new Thread(this);
		_reader.start();
	}

	@Override
	public void scanOnce(BarcodeListener l) {
		scanOnce(l, false);
	}

	@Override
	public void startScan(BarcodeListener l) {
		_l = l;
		_once = false;
		_reader = new Thread(this);
		_reader.start();
	}

	@Override
	public void doScan() {

	}

	@Override
	public void stopScan() {
		if (_device == null)
			return;
		if (_reader != null)
			_reader.interrupt();
		if (_is != null) {
			try {
				_is.close();
			} catch (Exception e) {
			}
			try {
				_os.close();
			} catch (Exception e) {
			}
			try {
				_socket.close();
			} catch (Exception e) {
			}
			_is = null;
			_os = null;
			_socket = null;
		}
		_reader = null;
	}

	private boolean start() {
		if (_device == null)
			return false;
		try {
			_socket = _device.createRfcommSocketToServiceRecord(SPP);
			if (!_socket.isConnected())
				_socket.connect();
			_is = _socket.getInputStream();
			_os = _socket.getOutputStream();
			return true;
		} catch (Exception e) {
			_is = null;
			_os = null;
			return false;
		}
	}

	@Override
	public boolean isManual() {
		return false;
	}

	@Override
	public TTY getTTY(SessionActivity owner) {
		return new BTTTY();
	}

	@Override
	public void run() {
		byte [] buf = new byte[16535];
		int p = 0;
		long lastRead = System.currentTimeMillis();
		while (!Thread.currentThread().isInterrupted()) {
			if (_is == null)
				start();
			if (_is != null) try {
				int read = _is.available();
				if(read > 0) {
					p+= _is.read(buf,p,read);
					lastRead = System.currentTimeMillis();
				} else {
					if(System.currentTimeMillis()-lastRead > 40 && p > 0) {
						final byte [] b = new byte[p];
						System.arraycopy(buf, 0, b, 0, p);
						p = 0;
						if(_enabled) {
							_l.onBarcode(b);
							if(_once) break;
						}
					}
				}
			} catch(Exception e) { }
			try { Thread.sleep(_is == null ? 200 : 20); } catch(InterruptedException ie) { }
		}
		_l = null;
		stopScan();
	}

	@Override
	public void disable() {
		_enabled = false;
	}

	@Override
	public void enable() {
		_enabled = true;
	}

}
