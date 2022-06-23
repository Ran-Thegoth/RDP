package rs.cc;

import cs.ui.fragments.BaseEditor;
import cs.ui.fragments.BaseEditor.OnSaveListener;
import rs.cc.config.SessionConfig;

public class Const {

	public static final String EMPTY_JSON_OBJECT = "{}";
	public static final String EMPTY_STRING  = "";
	public static final int RDP_SESSION = 1;
	public static final int VNC_SESSION = 2;
	public static final int ELM_SESSION = 3;
	public static final int WEB_SESSION = 4;
	
	public static final int SOUND_LOCAL = 0;
	public static final int SOUND_REMOTE = 1;
	public static final int SOUND_DISABLED = 2;
	
	public static final String CODE_FIELD = "CODE";
	public static final String TYPE_FIELD = "TYPE";
	public static final long DAY_MS = 3600*24*1000L;
	
	public static final String IGNORED_KEYS_TAG = "iKeys";
	
	public static abstract class RemoteConnectionType<T extends SessionConfig> {
		public abstract int type();
		public abstract String name();
		public abstract Class<T> configClass();
		public abstract BaseEditor<SessionConfig> editor(SessionConfig config, OnSaveListener<SessionConfig> l);
		public T newInstance() { 
			try {
				T result = configClass().newInstance();
				result.setName("Новое соединение");
				return result;
			} catch(Exception e) { 
				return null;
			}
		}
		@Override
		public String toString() {
			return name();
		}
	}
	
	private Const() {
		// TODO Auto-generated constructor stub
	}
	
	

}
