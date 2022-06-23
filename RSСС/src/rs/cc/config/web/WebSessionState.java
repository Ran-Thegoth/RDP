package rs.cc.config.web;


import android.annotation.SuppressLint;
import android.content.Context;
import rs.cc.Core;
import rs.cc.connection.EventListener;
import rs.cc.connection.SessionState;

@SuppressLint("SetJavaScriptEnabled")
public class WebSessionState implements SessionState {

	private WebSessionConfig _cfg;
	
	public WebSessionState(WebSessionConfig config) {
		_cfg = config;
		Core.registerSession(id(), this);
	}

	@Override
	protected void finalize() throws Throwable {
		Core.unregisterSession(id());
		super.finalize();
	}
	@Override
	public WebSessionConfig getConfig() {
		return _cfg;
	}

	@Override
	public void disconnect() {
		_cfg.disconnect();
		Core.unregisterSession(id());
	}

	@Override
	public boolean connect(Context ctx) {
		return true;
	}

	@Override
	public long id() {
		return hashCode();
	}

	@Override
	public EventListener getEventListener() {
		return null;
	}

}
