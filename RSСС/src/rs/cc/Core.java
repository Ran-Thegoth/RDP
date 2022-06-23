package rs.cc;

import java.util.HashMap;
import java.util.List;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.v4.util.LongSparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import bsh.Interpreter;
import cs.orm.ORMHelper;
import cs.ui.fragments.BaseEditor;
import cs.ui.fragments.BaseEditor.OnSaveListener;
import rs.cc.api.BarcodeProcessor;
import rs.cc.config.SessionConfig;
import rs.cc.config.SystemConfig;
import rs.cc.config.rdp.RDPSessionConfig;
import rs.cc.config.vnc.VNCSessionConfig;
import rs.cc.config.web.WebSessionConfig;
import rs.cc.connection.SessionState;
import rs.cc.hardware.BarcodeScaner;
import rs.cc.hardware.Keyboard;
import rs.cc.hardware.RT40;
import rs.cc.ui.fragments.RDPSessionEditor;
import rs.cc.ui.fragments.VNCSessionEdit;
import rs.cc.ui.fragments.WebSessionEdit;

public class Core extends Application {

	private static LongSparseArray<SessionState> SESSIONS = new LongSparseArray<>(); 
	private static String HOSTNAME;
	private static Core _instance;

	
	private static HashMap<Class<? extends SessionConfig>, Const.RemoteConnectionType<?>> KNOWN_TYPES = new HashMap<>();
	
	public static Core getInstance() { return _instance; }
	
	private DB _db;
	private SystemConfig _sysConfig;
	
	private BarcodeScaner _barcodeScaner;
	private Keyboard _HWKeyboard;
	private Handler _handler;
	
	static {
		KNOWN_TYPES.put(RDPSessionConfig.class, new Const.RemoteConnectionType<RDPSessionConfig>() {
			@Override
			public int type() {
				return Const.RDP_SESSION;
			}
			@Override
			public String name() {
				return Core.getInstance().getString(R.string.rdp_session);
			}

			@Override
			public Class<RDPSessionConfig> configClass() {
				return RDPSessionConfig.class;
			}

			@Override
			public BaseEditor<SessionConfig> editor(SessionConfig config,OnSaveListener<SessionConfig> l) {
				return RDPSessionEditor.newInstance((RDPSessionConfig)config, l);
			}
			
		});
		KNOWN_TYPES.put(VNCSessionConfig.class,new Const.RemoteConnectionType<VNCSessionConfig>() {

			@Override
			public int type() {
				return Const.VNC_SESSION;
			}

			@Override
			public String name() {
				return "VNC";
			}

			@Override
			public Class<VNCSessionConfig> configClass() {
				return VNCSessionConfig.class;
			}

			@Override
			public BaseEditor<SessionConfig> editor(SessionConfig config, OnSaveListener<SessionConfig> l) {
				return VNCSessionEdit.newInstance((VNCSessionConfig)config, l);
			}
		});
		KNOWN_TYPES.put(WebSessionConfig.class,new Const.RemoteConnectionType<WebSessionConfig> () {

			@Override
			public int type() {
				return Const.WEB_SESSION;
			}

			@Override
			public String name() {
				return Core.getInstance().getString(R.string.web_session);
			}

			@Override
			public Class<WebSessionConfig> configClass() {
				return WebSessionConfig.class;
			}

			@Override
			public BaseEditor<SessionConfig> editor(SessionConfig config, OnSaveListener<SessionConfig> l) {
				return WebSessionEdit.newInstance((WebSessionConfig)config, l);
			}
		});  
	}
	public Core() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		_handler = new Handler(getMainLooper());
		_db = new DB(this);
		_instance = this;
		_HWKeyboard = new Keyboard() {
			@Override
			public KeyEvent translate(KeyEvent e) {
				return e;
			}
		};
		Interpreter.init(this);
		HOSTNAME = Build.MODEL;
		if("UBX".equalsIgnoreCase(Build.MANUFACTURER)|| "Urovo".equalsIgnoreCase(Build.MANUFACTURER)) {
			if("RT40".contains(Build.MODEL))
				_HWKeyboard = new RT40();
		}
		
		try {
			_sysConfig = ORMHelper.load(SystemConfig.class);
		} catch(Exception e) {
			_sysConfig = null;
		}
		if(_sysConfig == null) {
			_sysConfig = new SystemConfig();
			_sysConfig.store();
		}
		BarcodeProcessor.init();
	}
	
	public static void unregisterSession(long handle) {
		synchronized(SESSIONS) {
			SESSIONS.remove(handle);
		}
	}
	public static void registerSession(long handle, SessionState state) {
		synchronized(SESSIONS) {
			SESSIONS.put(handle, state);
		}
	}
	public static SessionState getSession(long inst) {
		synchronized(SESSIONS) {
			return SESSIONS.get(inst);
		}
		
	}
	public static String getHostname() {
		return HOSTNAME;
	}

	private static List<SessionConfig> _knownSessions;
	public static List<SessionConfig> getKnownConfig() {
		if(_knownSessions == null)
			_knownSessions = ORMHelper.loadAll(SessionConfig.class);
		return _knownSessions;
	}
	
	public static HashMap<Class<? extends SessionConfig>, Const.RemoteConnectionType<?>> getKnownTypes() { return KNOWN_TYPES; }
	
	public static SessionConfig getClassForType(int type) throws Exception{
		for(Const.RemoteConnectionType<?> clazz : Core.getKnownTypes().values()) {
			if(clazz.type() == type) 
				return clazz.newInstance();
		}
		return null;
	}
	public DB db() { return _db;}

	public static void enableUI(View v, boolean enable) {
		if(v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup)v;
			for(int i=0;i<vg.getChildCount();i++)
				enableUI(vg.getChildAt(i), enable);
		} else
			v.setEnabled(enable);
	}
	
	public SystemConfig sysConfig() { return _sysConfig; }
	public void rebuildScaner() {
		_barcodeScaner = null;
	}
	public BarcodeScaner barcodeScaner(Context ctx) {
		if(_barcodeScaner == null)
			_barcodeScaner = _sysConfig.systemScanner.makeScanner(ctx);
		return _barcodeScaner;
	}
	public Keyboard HWKeyboard() { return _HWKeyboard; }
	public void post(Runnable r) {
		_handler.post(r);
	}
}
