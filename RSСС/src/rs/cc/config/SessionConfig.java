package rs.cc.config;


import java.text.SimpleDateFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import cs.orm.ClassCreator;
import cs.orm.DBField;
import cs.orm.DBObject;
import cs.orm.DBTable;
import cs.ui.annotations.BindTo;
import rs.cc.Const;
import rs.cc.Core;
import rs.cc.connection.SessionState;
import rs.cc.misc.ClassORMHelper;
import rs.cc.misc.NoJSON;
import rs.keyboard.FloatingKeyboard;
import rs.cc.R;

@DBTable(name="SESSIONS",unique= {"NAME"},indeces =  {})
public abstract class SessionConfig extends DBObject  {

	
	
	/**
	 * Базовые параметры соединения  
	 * @author nick
	 *
	 */
	public class ConnectionConfig {
		@BindTo(ui= {R.id.ed_srv_name},required=true)
		public String host = Const.EMPTY_STRING;
		@BindTo(ui= {R.id.ed_srv_port},required=true)
		public int port;
		@BindTo(ui= {R.id.ed_usr_name})
		public String username = Const.EMPTY_STRING;
		@BindTo(ui= {R.id.ed_usr_psk})
		public String password = Const.EMPTY_STRING;
		public boolean noAUTH = false;
	}
	/**
	 * Параметры экрана сервера
	 * @author nick
	 *
	 */
	public class ScreenConfig {
		@BindTo(ui= {R.id.sw_fullscreen})
		public boolean fullScreen = true;
		@BindTo(ui= {R.id.lv_rotation})
		public int rotation = 0;
		@BindTo(ui= {R.id.ed_width})
		public int width = Core.getInstance().getResources().getDisplayMetrics().widthPixels;
		@BindTo(ui= {R.id.ed_height})
		public int height = Core.getInstance().getResources().getDisplayMetrics().heightPixels;
		// Не биндится, биндинг в коде ScreenSettingsView
		public int bpp = 32;
		@BindTo(ui= {R.id.sw_zoom})
		public boolean zoom = true;
		@BindTo(ui= {R.id.sw_center})
		public boolean center = true;
		@BindTo(ui= {R.id.sw_ratio})
		public boolean keepRatio = true;
		
	}
	
	/**
	 * Параметры локального хранилища
	 * @author nick
	 *
	 */
	public class StorageConfig {
		@BindTo(ui= {R.id.sw_storage})
		public boolean mount = true;
		@SuppressWarnings("deprecation")
		@BindTo(ui= {R.id.ed_root_folder})
		public String folder = Environment.getExternalStorageDirectory().getAbsolutePath();
	}
	public class InputConfig extends ScanerConfig {

		@BindTo(ui= {R.id.sw_keyboard})
		public boolean keyboard = true;
		public JSONObject kbSettings = new JSONObject();
		
		@BindTo(ui= {R.id.sw_pointer})
		public boolean pointer = true;
		
		@BindTo(ui= {R.id.sw_hwkeyboard})
		public boolean hwKeyboard = true;
		public JSONObject hwSettings = new JSONObject();
		
		
		public static final int STYPE_KEYBOARD = 0;
		public static final int STYPE_SERIAL = 1;
		public static final int STYPE_CLIPBOARD = 2;
		@BindTo(ui= {R.id.lv_server_type})
		public int serverType = STYPE_SERIAL;
		
		
		@BindTo(ui= {R.id.ed_scaner_port})
		public int serialPortNumber = 16;
		InputConfig() {
			try {
				kbSettings.put(FloatingKeyboard.MOD_KEYS_TAG, true);
				kbSettings.put(FloatingKeyboard.LAYOUTS_TAG,new JSONArray("[\"en\",\"ru\",\"fn\"]"));
			} catch(JSONException jse) {
				
			}
		}
		
	}
	
	public class AdvancedConfig {
		@BindTo(ui= {80001},title="При нажатии \"Назад\"",choice="Ничего не делать;Завершить сессию;Отправить ALT+F4;Отправить ESC;",UIOrder=100)
		public int onBackPressedAction = 1;
	}
	/**
	 * Звук
	 * @author nick
	 *
	 */
	public class AudioConfig {
		@BindTo(ui= {40001},title="Режим звука",UIOrder=1,choice="На устройстве;На сервере;Отключен")
		public int soundMode = Const.SOUND_LOCAL;
		@BindTo(ui= {40002},title="Микрофон",UIOrder=2)
		public boolean microphone = false;
	}
	
	public class PrinterConfig {
		@BindTo(ui= {R.id.sw_printer})
		public boolean enabled;
		@BindTo(ui= {R.id.ed_printer_address})
		public String deviceAddress;
		@BindTo(ui= {R.id.ed_printer_port})
		public int portNumber = 17;
	}
	
	public class Scripting {
		public String onBarcode = Const.EMPTY_STRING;
		public String onUI = Const.EMPTY_STRING;
	}
	
	@DBField(name=Const.TYPE_FIELD)
	protected int TYPE;

	@DBField(name="NAME")
	@BindTo(ui= {R.id.lbl_name})
	protected String NAME;
	
	@DBField(name=Const.CODE_FIELD)
	@BindTo(ui= {R.id.ed_code})
	protected String CODE;

	@NoJSON
	@DBField(name="LAST_SUCCESS")
	protected long _lastSuccess;
	/**
	 * Группировка конфигураций для удобного хранения в БД
	 * @author nick
	 *
	 */
	protected class Configs {
		protected ConnectionConfig CONNECTION = new ConnectionConfig();
		protected ScreenConfig SCREEN = new ScreenConfig();
		protected StorageConfig STORAGE = new StorageConfig();
		protected AudioConfig AUDIO = new AudioConfig();
		protected InputConfig INPUT = new InputConfig();
		protected AdvancedConfig ADVCONFIG = new AdvancedConfig();
		protected PrinterConfig PRINTER = new PrinterConfig();
		protected Scripting SCRIPTS = new Scripting();
	}
	
	@NoJSON
	protected SessionState _connection;
	
	@DBField(name="CONFIG",type="TEXT",getter=ClassORMHelper.class,setter=ClassORMHelper.class)
	protected Configs CONFIGS = new Configs();
	
	public SessionConfig() {
	}

	public String getName() { return NAME; }
	
	public ConnectionConfig connectionConfig() { return CONFIGS.CONNECTION; }
	public ScreenConfig screenConfig() { return CONFIGS.SCREEN; }
	public StorageConfig storageConfig() { return CONFIGS.STORAGE; }
	public AudioConfig audioConfig() { return CONFIGS.AUDIO; }
	public InputConfig inputConfig() { return CONFIGS.INPUT; }
	public AdvancedConfig advConfig() { return CONFIGS.ADVCONFIG; }
	public PrinterConfig prnConfig() { return CONFIGS.PRINTER; }
	public Scripting scripts() { return CONFIGS.SCRIPTS; }
	public int getIconId() { return 0; }
	public boolean isConnected() { return _connection != null; }
	public abstract void connect(Context ctx);
	public abstract void disconnect();

	public class AuthData {
		@BindTo(ui= {R.id.ed_login},required=true)
		public String login;
		@BindTo(ui= {R.id.ed_passwd})
		public String password;
		public void clear() {
			login = password = null;
		}
	}
	@NoJSON
	protected AuthData AUTH_DATA = new AuthData();
	
	
	
	@NoJSON
	public static final ClassCreator<SessionConfig> CREATOR = new ClassCreator<SessionConfig>() {
		@Override
		public SessionConfig newInstance(Cursor c) {
			try {
				return Core.getClassForType(c.getInt(c.getColumnIndex(Const.TYPE_FIELD)));
			} catch(Exception e) {
				return null;
			}
		}
		
	};
	
	public AuthData authData() {
		if(AUTH_DATA.login == null) {
			AUTH_DATA.login = CONFIGS.CONNECTION.username;
			AUTH_DATA.password = CONFIGS.CONNECTION.password;
		}
		return AUTH_DATA;
	}
	@Override
	public boolean store() {
		AUTH_DATA.clear();
		return super.store();
	}
	@SuppressLint("SimpleDateFormat")
	public String getLastSuccessSession() {
		if(_lastSuccess == 0) return Core.getInstance().getString(R.string.never);
		if(System.currentTimeMillis() - _lastSuccess > Const.DAY_MS) {
			return new SimpleDateFormat("dd.MM HH:mm").format(_lastSuccess);
		}
		else 
			return  Core.getInstance().getString(R.string.today)+", "+new SimpleDateFormat("HH:mm:ss").format(_lastSuccess);
	}
	public void setSuccessConnected() {
		_lastSuccess = System.currentTimeMillis();
		store();
	}

	public void setName(String value) {
		NAME = value;
		
	}
}
