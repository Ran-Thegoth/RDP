package rs.cc.config.vnc;

import android.content.Context;
import android.content.Intent;
import rs.cc.Const;
import rs.cc.R;
import rs.cc.config.SessionConfig;
import rs.cc.connection.vnc.VNCSessionState;
import rs.cc.ui.SessionActivity;
import rs.cc.ui.vnc.VNCActivity;

public class VNCSessionConfig extends SessionConfig {

	public VNCSessionConfig() {
		TYPE = Const.VNC_SESSION;
		connectionConfig().port = 5900;
	}

	@Override
	public void connect(Context ctx) {
		Intent i = new Intent(ctx,VNCActivity.class);
		if(_connection == null)
			_connection = new VNCSessionState(this);
		else 
			i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		i.putExtra(SessionActivity.SESSION_ID_TAG, _connection.id());
		ctx.startActivity(i);
	}

	@Override
	public void disconnect() {
		if(_connection != null)
			_connection.disconnect();
		_connection = null;
	}
	@Override
	public int getIconId() {
		return R.drawable.vnc;
	}

}
