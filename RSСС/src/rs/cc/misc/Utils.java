package rs.cc.misc;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Proxy;
import android.util.ArrayMap;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.webkit.WebView;

public class Utils {

	private static SparseArray<String> _keyNames = new SparseArray<>();
	
	static {
		try {
			for(Field f : KeyEvent.class.getDeclaredFields()) {
				if(f.getName().startsWith("KEYCODE_")) {
					f.setAccessible(true);
					_keyNames.put(f.getInt(null), f.getName().replace("KEYCODE_", ""));
				}
			}
		} catch(Exception e) {
			
		}
	}
	private Utils() {
		// TODO Auto-generated constructor stub
	}
	
	public static String keyName(int code) {
		String s = _keyNames.get(code);
		if(s == null) s = "UNKNOWN ("+code+")";
		return s;
	}

	public static boolean setProxyKKPlus(WebView webView, String host, int port) {
	    Context appContext = webView.getContext().getApplicationContext();
	    System.setProperty("http.proxyHost", host);
	    System.setProperty("http.proxyPort", port + "");
	    System.setProperty("https.proxyHost", host);
	    System.setProperty("https.proxyPort", port + "");
	    try {
	        Class<?>  applictionCls = Application.class;
	        Field loadedApkField = applictionCls.getField("mLoadedApk");
	        loadedApkField.setAccessible(true);
	        Object loadedApk = loadedApkField.get(appContext);
	        Class<?> loadedApkCls = Class.forName("android.app.LoadedApk");
	        Field receiversField = loadedApkCls.getDeclaredField("mReceivers");
	        receiversField.setAccessible(true);
	        ArrayMap<?, ?> receivers = (ArrayMap<?,?>) receiversField.get(loadedApk);
	        for (Object receiverMap : receivers.values()) {
	            for (Object rec : ((ArrayMap<?,?>) receiverMap).keySet()) {
	                Class<?> clazz = rec.getClass();
	                if (clazz.getName().contains("ProxyChangeListener")) {
	                    Method onReceiveMethod = clazz.getDeclaredMethod("onReceive", Context.class, Intent.class);
	                    Intent intent = new Intent(Proxy.PROXY_CHANGE_ACTION);

	                    onReceiveMethod.invoke(rec, appContext, intent);
	                }
	            }
	        }

	        return true;
	    } catch (ClassNotFoundException e) {
	    } catch (NoSuchFieldException e) {
	    } catch (IllegalAccessException e) {
	    } catch (IllegalArgumentException e) {
	    } catch (NoSuchMethodException e) {
	    } catch (InvocationTargetException e) {
	    } 
	    return false;
	}

}
