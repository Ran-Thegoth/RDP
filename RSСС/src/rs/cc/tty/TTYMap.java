package rs.cc.tty;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;
import rs.cc.Core;

public class TTYMap {

	private TTYMap() { }
	private static Map<String, TTY> _ttys = new HashMap<>();
	public static void registerTTY(String tty, TTY handler) {
		Log.d("RDP", "Register TTY "+handler.getClass().getName()+" as "+tty); 
		_ttys.put(tty, handler);
	}
	public static void unregisterTTY(String tty) {
		_ttys.remove(tty);
	}
	public static TTY get(String tty) {
		return _ttys.get(tty);
	}
	public static int available(String tty) {
		TTY h = _ttys.get(tty);
		return h == null ? 0 : h.available();
	}
	public static void  write(String tty, byte [] buf, int offset, int size) {
		TTY h = _ttys.get(tty);
		if(h != null) h.write(buf, offset, size);
	}
	public static int read(String tty, byte [] buf, int offset, int size) {
		TTY h = _ttys.get(tty);
		return h == null ? 0 : h.read(buf, offset, size);
	}
	public static void notifyTTY(String tty, int state) {
		TTY h = _ttys.get(tty);
		if(h != null) {
			if(state != 0) h.onEnabled(); else h.onDisabled();
		}
	}
}
