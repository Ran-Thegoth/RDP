package rs.cc.config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cs.orm.DBField;
import cs.orm.DBObject;
import cs.orm.FieldHelper;

@SuppressWarnings("unchecked")
public class Shortcut extends DBObject {

	public static class ActionHelper implements FieldHelper {

		@Override
		public Object getFieldValue(Object owner, Field field) throws Exception {
			List<ShortcutAction> list = (List<ShortcutAction>)field.get(owner);
			JSONArray result = new JSONArray();
			for(ShortcutAction a : list)
				result.put(a.toJSON());
			return result.toString();
		}

		
		@Override
		public void setFieldValue(Object owner, Field field, Object value) throws Exception {
			List<ShortcutAction> list = (List<ShortcutAction>)field.get(owner);
			list.clear();
			JSONArray a = new JSONArray(value.toString());
			for(int i=0;i<a.length();i++)
				list.add(new ShortcutAction(a.getJSONObject(i)));
			
		}
		
	}
	
	public static enum ShortcutActionType { KEY, LOCAL_ACTION,SERVER_ACTION}; 
	public static class ShortcutAction {
		ShortcutActionType action;
		String arg;
		public ShortcutAction() {
			action = ShortcutActionType.KEY;
		}
		public ShortcutAction(JSONObject o) throws JSONException {
			action = ShortcutActionType.values()[o.getInt("action")];
			arg = o.getString("arg");
		}
		
		public JSONObject toJSON() throws JSONException {
			JSONObject result = new JSONObject();
			result.put("action", action.ordinal());
			result.put("arg",arg);
			return result;
		}
	}
	
	@DBField(name="KEYCODE")
	private int KEYCODE;
	@DBField(name="ACTIONS",type="TEXT",getter=ActionHelper.class,setter=ActionHelper.class)
	private List<ShortcutAction> ACTIONS = new ArrayList<>();
	@DBField(name="SESSION_ID")
	private long _session;
	
	public Shortcut(SessionConfig owner) {
		_session = owner.id();
	}


}
