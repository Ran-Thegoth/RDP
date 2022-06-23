package rs.cc.hardware;

import android.view.KeyEvent;

public interface Keyboard {

	public KeyEvent translate(KeyEvent e);
}
