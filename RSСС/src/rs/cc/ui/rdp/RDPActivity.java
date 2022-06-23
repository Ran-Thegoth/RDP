package rs.cc.ui.rdp;

import com.freerdp.freerdpcore.services.LibFreeRDP;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;
import rs.cc.config.rdp.RDPSessionConfig;
import rs.cc.connection.EventListener;
import rs.cc.connection.KeyboardMapper;
import rs.cc.connection.UIEventListener;
import rs.cc.connection.rdp.RDPKeyboardMapper;
import rs.cc.connection.rdp.RDPSessionState;
import rs.cc.ui.SessionActivity;
import rs.cc.ui.widgets.SessionView;
import rs.cc.ui.widgets.rdp.Mouse;
import rs.cc.ui.widgets.rdp.RDPSessionView;
import rs.keyboard.FloatingKeyboard;

@SuppressLint("HandlerLeak")
@SuppressWarnings("deprecation")
public class RDPActivity extends SessionActivity implements UIEventListener,EventListener {

	private class UIHandler extends Handler {
		public static final int REFRESH_SESSIONVIEW = 1;
		public static final int DISPLAY_TOAST = 2;
		public static final int SEND_MOVE_EVENT = 4;
		public static final int SHOW_DIALOG = 5;
		public static final int GRAPHICS_CHANGED = 6;
		public static final int BIND_SESSION = 7;

		UIHandler() {
			super(getMainLooper());

		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BIND_SESSION:
				sessionView().onSurfaceChange(state());
				requestLayout();
				break;
			case GRAPHICS_CHANGED: {
				sessionView().onSurfaceChange(state());
				break;
			}
			case REFRESH_SESSIONVIEW: {
				sessionView().invalidateRegion();
				break;
			}
			case DISPLAY_TOAST: {
				Toast errorToast = Toast.makeText(RDPActivity.this, msg.obj.toString(), Toast.LENGTH_LONG);
				errorToast.show();
				break;
			}
			case SEND_MOVE_EVENT: {
				LibFreeRDP.sendCursorEvent(state().id(), msg.arg1, msg.arg2,
						rs.cc.ui.widgets.rdp.Mouse.getMoveEvent());
				break;
			}
			case SHOW_DIALOG: {
				((Dialog) msg.obj).show();
				break;
			}
			}
		}
	}

	private Bitmap _surface;
	private static final int MAX_DISCARDED_MOVE_EVENTS = 3;
	private static final int SEND_MOVE_EVENT_TIMEOUT = 150;
	private int _discardedMoveEvents = 0;
	private boolean toggleMouseButtons;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(state() == null) return;
		state().setEventListener(this);
		state().setUIListener(this);
		_surface = Bitmap.createBitmap(config().screenConfig().width, config().screenConfig().height, Config.ARGB_8888);
		state().setSurface(new BitmapDrawable(_surface));
		
	}

	@Override
	public void onUnicode(FloatingKeyboard source, int key) {
		int metas = source.getMetaState();
		if((metas & KeyEvent.META_ALT_ON) != 0)
			LibFreeRDP.sendKeyEvent(state().id(),KBMapper().getVirtualKey(KeyEvent.KEYCODE_ALT_LEFT),true);
		if((metas & KeyEvent.META_CTRL_ON) != 0)
			LibFreeRDP.sendKeyEvent(state().id(),KBMapper().getVirtualKey(KeyEvent.KEYCODE_CTRL_LEFT),true);
		if((metas & KeyEvent.META_SYM_ON) != 0)
			LibFreeRDP.sendKeyEvent(state().id(),KBMapper().getVirtualKey(KeyEvent.KEYCODE_SYM),true);
		if((metas & KeyEvent.META_SHIFT_ON) != 0)
			LibFreeRDP.sendKeyEvent(state().id(),KBMapper().getVirtualKey(KeyEvent.KEYCODE_SHIFT_LEFT),true);
		
		LibFreeRDP.sendUnicodeKeyEvent(state().id(), key, true);
		LibFreeRDP.sendUnicodeKeyEvent(state().id(), key, false);
		if((metas & KeyEvent.META_ALT_ON) != 0)
			LibFreeRDP.sendKeyEvent(state().id(),KBMapper().getVirtualKey(KeyEvent.KEYCODE_ALT_LEFT),false);
		if((metas & KeyEvent.META_CTRL_ON) != 0)
			LibFreeRDP.sendKeyEvent(state().id(),KBMapper().getVirtualKey(KeyEvent.KEYCODE_CTRL_LEFT),false);
		if((metas & KeyEvent.META_SYM_ON) != 0)
			LibFreeRDP.sendKeyEvent(state().id(),KBMapper().getVirtualKey(KeyEvent.KEYCODE_SYM),false);
		if((metas & KeyEvent.META_SHIFT_ON) != 0)
			LibFreeRDP.sendKeyEvent(state().id(),KBMapper().getVirtualKey(KeyEvent.KEYCODE_SHIFT_LEFT),false);
		
	}

	@Override
	public void sendKey(int code, boolean down) {
		LibFreeRDP.sendKeyEvent(state().id(), code, down);
	}


	@Override
	public void onTouchPointerLeftClick(int x, int y, boolean down) {
		Point p = mapScreenCoordToSessionCoord(x, y);
		LibFreeRDP.sendCursorEvent(state().id(), p.x, p.y, Mouse.getLeftButtonEvent(down));
	}

	@Override
	public void onTouchPointerRightClick(int x, int y, boolean down) {
		Point p = mapScreenCoordToSessionCoord(x, y);
		LibFreeRDP.sendCursorEvent(state().id(), p.x, p.y, Mouse.getRightButtonEvent(down));
	}

	@Override
	public void onTouchPointerMove(int x, int y) {
		Point p = mapScreenCoordToSessionCoord(x, y);
		LibFreeRDP.sendCursorEvent(state().id(), p.x, p.y, Mouse.getMoveEvent());
	}

	@Override
	public void onTouchPointerScroll(boolean down) {
		LibFreeRDP.sendCursorEvent(state().id(), 0, 0, Mouse.getScrollEvent(down));

	}

	@Override
	protected Handler createHandler() {
		return new UIHandler();
	}

	@Override
	protected SessionView createSessionView() {
		return new RDPSessionView(this);
	}

	@Override
	protected int beginConnection() {
		if(LibFreeRDP.isConnected(state().id())) return STATE_CONNECTED;
		if(state().connect(this)) return STATE_CONNECTING;
		return STATE_FAILED;
	}

	@Override
	protected void endConnection() {
		if(LibFreeRDP.isConnected(state().id())) 
			state().disconnect();
	}

	@Override
	protected RDPSessionState state() {
		return (RDPSessionState)super.state();
	}
	@Override
	protected RDPSessionConfig config() {
		return (RDPSessionConfig)super.config();
	}
	@Override
	protected RDPSessionView sessionView() {
		return (RDPSessionView)super.sessionView();
	}
	
	@Override
	public void onSessionViewLeftTouch(int x, int y, boolean down) {
		if (pointer().getVisibility() == View.VISIBLE)
			return;
		if (!down)
			cancelDelayedMoveEvent();
		LibFreeRDP.sendCursorEvent(state().id(), x, y,
				toggleMouseButtons ? Mouse.getRightButtonEvent(down) : Mouse.getLeftButtonEvent(down));

		if (!down)
			toggleMouseButtons = false;

		
	}
	@Override
	public void onSessionViewRightTouch(int x, int y, boolean down) {
		if (!down)
			toggleMouseButtons = !toggleMouseButtons;
	}
	@Override
	public void onSessionViewMove(int x, int y) {
		sendDelayedMoveEvent(x, y);
		
	}
	@Override
	public void onSessionViewScroll(boolean down) {
		LibFreeRDP.sendCursorEvent(state().id(), 0, 0, Mouse.getScrollEvent(down));
		
	}
	@Override
	public void OnSettingsChanged(int width, int height, int bpp) {
		if (bpp > 16)
			_surface = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		else
			_surface = Bitmap.createBitmap(width, height, Config.RGB_565);
		state().setSurface(new BitmapDrawable(_surface));
		rescaleIfNeeded();
	}
	
	@Override
	public boolean OnAuthenticate(StringBuilder username, StringBuilder domain, StringBuilder password) {
		return true;
	}
	@Override
	public boolean OnGatewayAuthenticate(StringBuilder username, StringBuilder domain, StringBuilder password) {
		return true;
	}
	@Override
	public int OnVerifiyCertificate(String commonName, String subject, String issuer, String fingerprint,
			boolean mismatch) {
		return 0;
	}
	@Override
	public int OnVerifyChangedCertificate(String commonName, String subject, String issuer, String fingerprint,
			String oldSubject, String oldIssuer, String oldFingerprint) {
		return 0;
	}
	@Override
	public void OnGraphicsUpdate(int x, int y, int width, int height) {
		LibFreeRDP.updateGraphics(state().id(), _surface, x, y, width, height);
		sessionView().addInvalidRegion(new Rect(x, y, x + width, y + height));
		handler().sendEmptyMessage(UIHandler.REFRESH_SESSIONVIEW);
		
	}
	@Override
	public void OnGraphicsResize(int width, int height, int bpp) {
		OnSettingsChanged(width, height, bpp);
	}
	@Override
	public void OnRemoteClipboardChanged(String data) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void OnPreConnect(long instance) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void OnConnectionSuccess(long instance) {
		setConnectionState(STATE_CONNECTED);
		handler().sendEmptyMessage(UIHandler.BIND_SESSION);
	}
	@Override
	public void OnConnectionFailure(long instance) {
		setConnectionState(STATE_FAILED);
	}
	@Override
	public void OnDisconnecting(long instance) {
		
	}
	@Override
	public void OnDisconnected(long instance) {
		finish();
	}
	

	private void sendDelayedMoveEvent(int x, int y) {
		if (handler().hasMessages(UIHandler.SEND_MOVE_EVENT)) {
			handler().removeMessages(UIHandler.SEND_MOVE_EVENT);
			_discardedMoveEvents++;
		} else
			_discardedMoveEvents = 0;
		if (_discardedMoveEvents > MAX_DISCARDED_MOVE_EVENTS)
			LibFreeRDP.sendCursorEvent(state().id(), x, y, Mouse.getMoveEvent());
		else
			handler().sendMessageDelayed(Message.obtain(null, UIHandler.SEND_MOVE_EVENT, x, y), SEND_MOVE_EVENT_TIMEOUT);
	}
	private void cancelDelayedMoveEvent() {
		handler().removeMessages(UIHandler.SEND_MOVE_EVENT);
	}

	@Override
	protected KeyboardMapper createKBMapper() {
		return new RDPKeyboardMapper(this);
	}


	@Override
	protected int getScreenWidth() {
		return _surface.getWidth();
	}

	@Override
	protected int getScreenHeight() {
		return _surface.getHeight();
	}

	@Override
	protected void sendClibpoardContent(String s) {
		LibFreeRDP.sendClipboardData(state().id(), s);
		
	}

	@Override
	protected String getLasErrorDescr() {
		return LibFreeRDP.getError(state().id());
	}

	@Override
	protected void sendUnicodeKey(int key, boolean down) {
		LibFreeRDP.sendUnicodeKeyEvent(state().id(), key, down);
	}
	@Override
	protected void onBeginDisconnection() {
		
	}
	
}
