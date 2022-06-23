package rs.cc.connection.rdp;

import com.freerdp.freerdpcore.services.LibFreeRDP;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import rs.cc.Core;
import rs.cc.config.rdp.RDPSessionConfig;
import rs.cc.connection.EventListener;
import rs.cc.connection.SessionState;
import rs.cc.connection.UIEventListener;

public class RDPSessionState implements SessionState {

	private long _handle;
	private RDPSessionConfig _config;
	private UIEventListener _uiListener;
	private EventListener _eListener;
	private BitmapDrawable _surface;
	public RDPSessionState( RDPSessionConfig config) {
		_handle = LibFreeRDP.newInstance(Core.getInstance());
		_config = config;
		Core.registerSession(_handle, this);
	}

	public void setUIListener(UIEventListener l) {
		_uiListener = l;
	}
	public void setEventListener(EventListener l) {
		_eListener = l;
	}

	public UIEventListener getUIEventListener() {
		return _uiListener;
	}

	@Override
	public RDPSessionConfig getConfig() {
		return _config;
	}

	@Override
	public void disconnect() {
		LibFreeRDP.disconnect(_handle);
	}

	@Override
	public boolean connect(Context ctx) {
		LibFreeRDP.setConnectionInfo(ctx, _handle, _config);
		return LibFreeRDP.connect(_handle);
	}
	@Override
	protected void finalize() throws Throwable {
		Core.unregisterSession(_handle);
		LibFreeRDP.freeInstance(_handle);
		_uiListener = null;
		_eListener = null;
		super.finalize();
	}

	@Override
	public long id() {
		return _handle;
	}
	public void setSurface(BitmapDrawable surface) {
		_surface = surface;
	}
	public BitmapDrawable getSurface() {
		return _surface;
	}

	@Override
	public EventListener getEventListener() {
		// TODO Auto-generated method stub
		return _eListener;
	}
}
