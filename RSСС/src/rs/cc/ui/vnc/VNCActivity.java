package rs.cc.ui.vnc;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import rs.cc.config.vnc.VNCSessionConfig;
import rs.cc.connection.EventListener;
import rs.cc.connection.KeyboardMapper;
import rs.cc.connection.vnc.VNCSessionState;
import rs.cc.connection.vnc.VNCSessionState.VNCEventListener;
import rs.cc.ui.SessionActivity;
import rs.cc.ui.widgets.SessionView;
import rs.cc.ui.widgets.vnc.VNCSessionView;
import rs.keyboard.FloatingKeyboard;

public class VNCActivity extends SessionActivity implements EventListener, VNCEventListener {

	static final int MOUSE_BUTTON_LEFT = 1;
	static final int MOUSE_BUTTON_RIGHT = 4;

	private  int _mButtons;
	private Point FBSize = new Point();
	public VNCActivity() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(state() == null) return;
		state().setEventListener(this);
		state().setVNCEventListener(this);
	}

	@Override
	protected VNCSessionState state() {
		return (VNCSessionState)super.state();
	}
	@Override
	protected VNCSessionConfig config() {
		return (VNCSessionConfig)super.config();
	}
	@Override
	public void onUnicode(FloatingKeyboard source, int key) {
	}

	@Override
	public void sendKey(int virtualKeyCode, boolean down) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTouchPointerLeftClick(int x, int y, boolean down) {
		Point p = mapScreenCoordToSessionCoord(x, y);
		state().sendMouseButton(MOUSE_BUTTON_LEFT,p,down);
		if(down)
			_mButtons |= MOUSE_BUTTON_LEFT;
		else 
			_mButtons &= ~MOUSE_BUTTON_LEFT;

	}

	@Override
	public void onTouchPointerRightClick(int x, int y, boolean down) {
		Point p = mapScreenCoordToSessionCoord(x, y);
		state().sendMouseButton(MOUSE_BUTTON_RIGHT,p,down);
		if(down)
			_mButtons |= MOUSE_BUTTON_RIGHT;
		else 
			_mButtons &= ~MOUSE_BUTTON_RIGHT;

	}

	@Override
	public void onTouchPointerMove(int x, int y) {
		if(_mButtons != 0 )
			state().sendMouseButton(_mButtons,mapScreenCoordToSessionCoord(x, y),true);

	}

	@Override
	public void onTouchPointerScroll(boolean down) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSessionViewLeftTouch(int x, int y, boolean down) {
		if (pointer().getVisibility() == View.VISIBLE)
			return;
		state().sendMouseButton(MOUSE_BUTTON_LEFT,new Point(x,y),down);
		
	}

	@Override
	public void onSessionViewRightTouch(int x, int y, boolean down) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSessionViewMove(int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSessionViewScroll(boolean down) {
		// TODO Auto-generated method stub

	}

	@Override
	protected Handler createHandler() {
		return new Handler(getMainLooper());
	}

	@Override
	protected SessionView createSessionView() {
		return new VNCSessionView(this);
	}

	@Override
	protected VNCSessionView sessionView() {
		return (VNCSessionView)super.sessionView();
	}
	
	@Override
	protected KeyboardMapper createKBMapper() {
		return new KeyboardMapper(this) {
			@Override
			public int getVirtualKey(int code) {
				return code;
			}
		};
	}

	@Override
	protected int beginConnection() {
		if(state().isConnected())
			return STATE_CONNECTED;
		state().connect(this);
		return STATE_CONNECTING;
	}

	@Override
	protected void endConnection() {
	}

	@Override
	protected int getScreenWidth() {
		return FBSize.x;
	}

	@Override
	protected int getScreenHeight() {
		return FBSize.y;
	}

	@Override
	protected void sendClibpoardContent(String s) {
	}

	@Override
	protected String getLasErrorDescr() {
		return state().getLastError();
	}

	@Override
	public void OnPreConnect(long instance) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnConnectionSuccess(long instance) {
		setConnectionState(STATE_CONNECTED);
		
	}

	@Override
	public void OnConnectionFailure(long instance) {
		setConnectionState(STATE_FAILED);
		
	}

	@Override
	public void OnDisconnecting(long instance) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnDisconnected(long instance) {
		finish();
	}

	@Override
	public void onSizeChanged(int w, int h) {
		ViewGroup.LayoutParams lp = sessionView().getView().getLayoutParams();
		lp.width = w; lp.height = h;
		sessionView().getView().setLayoutParams(lp);
		sessionView().getView().setImageDrawable(state().getSurface());
		requestLayout();
		FBSize.x = w;
		FBSize.y = h;
		
	}

	@Override
	public void onGraphicUpdate() {
		sessionView().getView().invalidate();
	}

	@Override
	protected void sendUnicodeKey(int key, boolean down) {
		// TODO Auto-generated method stub
		
	}

	
}

