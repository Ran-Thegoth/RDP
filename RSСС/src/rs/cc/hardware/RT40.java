package rs.cc.hardware;

import android.view.KeyEvent;

public class RT40 implements Keyboard {

	@Override
	public KeyEvent translate(KeyEvent e) {
		return e;
	}

}
