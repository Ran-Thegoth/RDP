package rs.keyboard;

import android.view.KeyEvent;

public interface KeyboardHandler {
    public void onUnicode(FloatingKeyboard source, int key);
    public void onKeyPressed(FloatingKeyboard source, KeyEvent []events);
}
