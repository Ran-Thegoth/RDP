package rs.cc.config.web;



import org.json.JSONArray;

import android.content.Context;
import android.content.Intent;
import cs.ui.annotations.BindTo;
import rs.cc.Const;
import rs.cc.R;
import rs.cc.config.SessionConfig;
import rs.cc.config.rdp.RDPSessionConfig.GWValidator;
import rs.cc.ui.SessionActivity;
import rs.cc.ui.web.WebSessionActivity;

public class WebSessionConfig extends SessionConfig {

	public class WebConnectionConfig extends ConnectionConfig {
		@BindTo(ui= {R.id.sw_use_gw})
		public boolean gw_enabled = false;
		@BindTo(ui= {R.id.ed_gw_name},validate=GWValidator.class)
		public String gw_host = Const.EMPTY_STRING;
		@BindTo(ui= {R.id.ed_gw_port},validate=GWValidator.class)
		public int gw_port = 8080;
	}
	

	public class WebFeaturesConfig {
		@BindTo(ui = {R.id.sw_js})
		public boolean JS = true;
		@BindTo(ui = { R.id.sw_cross_domains})
		public boolean CrossDomain = true;
		public JSONArray domains = new JSONArray();
	}
	private class WebConfigs extends Configs {
		WebFeaturesConfig FEATURES = new WebFeaturesConfig();
		WebConfigs() {
			super();
			CONNECTION = new WebConnectionConfig();
		}
	}
	public WebSessionConfig() {
		TYPE = Const.WEB_SESSION;
		CONFIGS = new WebConfigs();
		connectionConfig().host = "https://";
	}

	public WebFeaturesConfig features() { return ((WebConfigs)CONFIGS).FEATURES; }
	@Override
	public WebConnectionConfig connectionConfig() {
		return (WebConnectionConfig)super.connectionConfig();
	}
	
	@Override
	public void connect(Context ctx) {
		Intent i = new Intent(ctx,WebSessionActivity.class);
		if(_connection == null) 
			_connection = new WebSessionState(this);
		else
			i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		i.putExtra(SessionActivity.SESSION_ID_TAG, _connection.id());
		ctx.startActivity(i);
	}

	@Override
	public void disconnect() {
		_connection = null;
	}
	@Override
	public int getIconId() {
		return R.drawable.www;
	}

}
