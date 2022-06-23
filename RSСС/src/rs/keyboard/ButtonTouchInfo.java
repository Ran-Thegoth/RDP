package rs.keyboard;

import android.widget.PopupWindow;

public class ButtonTouchInfo {
    public Long startTouchTime;
    public PopupWindow popupWindow;
    public boolean extraKeyVisible;

    public ButtonTouchInfo(Long startTouchTime, PopupWindow popupWindow) {
        this.startTouchTime = startTouchTime;
        this.popupWindow = popupWindow;
    }
}
