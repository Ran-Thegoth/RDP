package rs.cc.config.rdp;

import java.lang.reflect.Field;

import android.content.Context;
import android.content.Intent;
import cs.ui.annotations.BindTo;
import cs.ui.annotations.Validator;
import rs.cc.Const;
import rs.cc.config.SessionConfig;
import rs.cc.connection.rdp.RDPSessionState;
import rs.cc.ui.SessionActivity;
import rs.cc.ui.rdp.RDPActivity;
import rs.cc.R;

public class RDPSessionConfig extends SessionConfig {
	
	/**
	 * Сетевой конфиг для RDP
	 * @author nick
	 *
	 */
	public class RDPConnectionConfig extends ConnectionConfig {
		@BindTo(ui= {R.id.ed_srv_domain})
		public String domain;
		@BindTo(ui= {R.id.lv_enc})
		public int security = 1;
		
	}
	
	public class RDPPrinterConfig extends PrinterConfig {
	}
	/**
	 * Всякая ерунда
	 * @author nick
	 *
	 */
	public class RDPAdvancedConfig extends AdvancedConfig {
		@BindTo(ui= {80002},title="Консольный режим",UIOrder=1)
		public boolean consoleMode = false;
		@BindTo(ui= {80004},title="Выполнить при подключении",UIOrder=2)
		public String remoteProgram = Const.EMPTY_STRING;
		@BindTo(ui= {80005},title="Рабочий каталог на сервере",UIOrder=3)
		public String workDir = Const.EMPTY_STRING;
	}
	
	public class RDPPerformanceConfig {
		@BindTo(ui= {60001},title="Эффекты рабочего стола")
		public boolean rfx = true;
		@BindTo(ui= {60002},title="Графические эффекты")
		public boolean gfx = true;
		@BindTo(ui= {60003},title="Обои")
		public boolean wallpaper = true;
		@BindTo(ui= {60004},title="Перетягивание окон")
		public boolean fullWindowDrag = true;
		@BindTo(ui= {60005},title="Эффекты Aero") 
		public boolean aero = true; 
		@BindTo(ui= {60006},title="Анимация меню")
		public boolean menuAnimation = true;
		@BindTo(ui= {60007},title="Сглаживание шрифтов")
		public boolean fontSmoothing = true;
		@BindTo(ui= {60008},title="Темы")
		public boolean themes = true;
	}

	public static class GWValidator implements Validator {
		@Override
		public boolean isCorrect(String value, Field field, Context context,Object owner) {
			GWConfig gwconfig = (GWConfig)owner;
			if(!gwconfig.enabled) return true;
			return !value.isEmpty();
		}
		
	}
	
	public class GWConfig {
		@BindTo(ui= {R.id.sw_use_gw})
		public boolean enabled = false;
		@BindTo(ui= {R.id.ed_gw_name},validate=GWValidator.class)
		public String host = Const.EMPTY_STRING;
		@BindTo(ui= {R.id.ed_gw_port},validate=GWValidator.class)
		public int port = 3389;
		@BindTo(ui= {R.id.ed_gw_usr_name})
		public String username = Const.EMPTY_STRING;
		@BindTo(ui= {R.id.ed_gw_usr_psk})
		public String password = Const.EMPTY_STRING;
		@BindTo(ui= {R.id.ed_gw_domain})
		public String domain= Const.EMPTY_STRING;
	}
	
	private class RDPConfigs extends Configs {
		
		private RDPPerformanceConfig PREFCONFIG = new RDPPerformanceConfig();
		private GWConfig GW = new GWConfig();
		private RDPConfigs() {
			CONNECTION = new RDPConnectionConfig();
			CONNECTION.port = 3389;
			ADVCONFIG = new RDPAdvancedConfig();
			PRINTER = new RDPPrinterConfig();
		}
	};
	
	public RDPSessionConfig() {
		NAME = "Новый RDP сеанс";
		CONFIGS = new RDPConfigs();
		TYPE = Const.RDP_SESSION;
	}
	@Override
	public RDPConnectionConfig connectionConfig() {
		
		return (RDPConnectionConfig)super.connectionConfig();
	}
	@Override
	public RDPAdvancedConfig advConfig() { return (RDPAdvancedConfig)super.advConfig();}
	public RDPPerformanceConfig prefConfig() { return ((RDPConfigs)CONFIGS).PREFCONFIG; }
	public GWConfig gateway() { return ((RDPConfigs)CONFIGS).GW; }
	@Override
	public RDPPrinterConfig prnConfig() {
		return (RDPPrinterConfig)super.prnConfig();
	}
	@Override
	public int getIconId() {
		return R.drawable.rdp;
	}
	@Override
	public void connect(Context ctx) {
		Intent i = new Intent(ctx,RDPActivity.class);
		if(_connection == null) 
			_connection = new RDPSessionState(this);
		else 
			i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		i.putExtra(SessionActivity.SESSION_ID_TAG, _connection.id());
		ctx.startActivity(i);
	}
	@Override
	public void disconnect() {
		if(_connection == null) return;
		_connection.disconnect();
		_connection = null;
	}
}
