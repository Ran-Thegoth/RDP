package rs.cc.tty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;

public class BTTTY extends TTY {

	private static final UUID SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private BluetoothDevice _device;
	private BluetoothSocket _socket;
	private InputStream _is;
	private OutputStream _os;
	
	private Handler _handler;

	private Runnable CLOSER = new Runnable() {
		@Override
		public void run() {
			close();
			
		}
	};
	
	public BTTTY(Context ctx, String device) {
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		if (!adapter.isEnabled())
			adapter.enable();
		_handler = new Handler(ctx.getMainLooper());
		_device = adapter.getRemoteDevice(device);
	}

	private boolean open() {
		if(_device == null) return false;
		try {
		if(_is != null) return true;
		try {
			
			_socket = _device.createRfcommSocketToServiceRecord(SPP);
			if (!_socket.isConnected())
				_socket.connect();
			_is = _socket.getInputStream();
			_os = _socket.getOutputStream();
			return true;
		} catch(IOException ioe) {
			_is = null;
			return false;
		}
		} finally {
			if(_is != null) {
				_handler.removeCallbacks(CLOSER);
				_handler.postDelayed(CLOSER, 60000);
			}
		}
	}
	private void close() {
		if(_is != null) {
			try { _is.close(); } catch(Exception ioe) {}
			try { _os.close(); } catch(Exception ioe) {}
			try { _socket.close(); } catch(Exception ioe) {}
		}
		_is = null; _os =null; _socket = null;
		_handler.removeCallbacks(CLOSER);
	}
	
	@Override
	public int available() {
		if(!open())	return 0;
		try {
			return _is.available();
		} catch(IOException ioe) {
			close();
			return 0;
		}
	}

	@Override
	public int read(byte[] buf, int offset, int size) {
		if(!open()) return 0;
		try {
			return _is.read(buf, offset, size);
		} catch(IOException ioe) {
			close();
			return 0;
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	@Override
	public void write(byte[] buf, int offset, int size) {
		if(!open()) return;
		try {
			_os.write(buf,offset,size);
		} catch(IOException ioe) {
			close();
		}
	}

	@Override
	public void store(byte[] b) {
	}

}
