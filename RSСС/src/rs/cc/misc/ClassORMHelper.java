package rs.cc.misc;

import java.lang.reflect.Field;

import org.json.JSONArray;
import org.json.JSONObject;

import cs.orm.DBObject;
import cs.orm.FieldHelper;

public class ClassORMHelper implements FieldHelper {

		private static boolean isDirectStorable(Object fo) {
			return fo == null || (fo instanceof JSONObject) || (fo instanceof JSONArray) ||
					   (fo instanceof String) || (fo instanceof Number) || (fo instanceof Boolean);
		}
		public static void storeFields(JSONObject result, Object o) throws Exception {
			Class<?> c = o.getClass();
			
			while (c != Object.class) {
				if(c == DBObject.class) break;
				for(Field f : c.getDeclaredFields()) {
					if(f.getName().endsWith("$0")) continue;
					if(f.isAnnotationPresent(NoJSON.class)) continue;

					f.setAccessible(true);
					Object fo = f.get(o);
					if(isDirectStorable(fo))		
						result.put(f.getName(), fo);
					else {
						JSONObject js = new JSONObject();
						storeFields(js, fo);
						result.put(f.getName(),js);
					}
				}
				c = c.getSuperclass();
			}
		}
		public static void restoreFields(JSONObject json, Object o) throws Exception {
			Class<?> c = o.getClass();
			while (c != Object.class) {
				for(Field f : c.getDeclaredFields()) {
					if(f.getName().endsWith("$0")) continue;
					f.setAccessible(true);
					if(json.has(f.getName())) {
						if(f.getType() == JSONObject.class)
							f.set(o, json.getJSONObject(f.getName()));
						else if(f.getType() == JSONArray.class)
							f.set(o, json.getJSONArray(f.getName()));
						else if(Object.class.isAssignableFrom(f.getType())) { 
							Object fo = f.get(o);
							if(isDirectStorable(fo))
								f.set(o, json.get(f.getName()));
							else
								if(fo != null) restoreFields(json.getJSONObject(f.getName()), fo);
						} else
							f.set(o, json.get(f.getName()));
					}
				}
				c = c.getSuperclass();
			}
		}
		
		@Override
		public Object getFieldValue(Object owner, Field field){
			JSONObject result = new JSONObject();
			try {
			Object o = field.get(owner);
			Class<?> c = o.getClass();
			while (c != Object.class) {
				for(Field f : c.getDeclaredFields()) {
					if(f.getName().endsWith("$0")) continue;
					f.setAccessible(true);
					Object fo = f.get(o);
					if(isDirectStorable(fo))
						result.put(f.getName(), fo);
					else {
						JSONObject sub = new JSONObject();
						storeFields(sub, f.get(o));
						result.put(f.getName(), sub);
					}
					
				}
				c = c.getSuperclass();
			}
			} catch(Exception e) {
			}
			
			return result.toString();
		}

		@Override
		public void setFieldValue(Object owner, Field field, Object value)  {
			try {
			JSONObject json = new JSONObject(value.toString());
			Object o = field.get(owner);
			Class<?> c = o.getClass();
			while (c != Object.class) {
				for(Field f : c.getDeclaredFields()) {
					if(f.getName().endsWith("$0")) continue;
					f.setAccessible(true);
					if(json.has(f.getName())) {
						Object fo =  f.get(o);
						if(isDirectStorable(fo))
							f.set(o,json.get(f.getName()));
						else	
							restoreFields(json.getJSONObject(f.getName()), fo);
					}
				}
				c = c.getSuperclass();
			}
			} catch(Exception e) {
			}
		}
		

}
