/*
   Android FreeRDP JNI Wrapper

   Copyright 2013 Thincast Technologies GmbH, Author: Martin Fleisz

   This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
   If a copy of the MPL was not distributed with this file, You can obtain one at
   http://mozilla.org/MPL/2.0/.
*/

package com.freerdp.freerdpcore.services;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LongSparseArray;
import android.util.Log;
import rs.cc.Core;
import rs.cc.config.ScanerConfig;
import rs.cc.config.SessionConfig.InputConfig;
import rs.cc.config.rdp.RDPSessionConfig;
import rs.cc.connection.SessionState;
import rs.cc.connection.UIEventListener;
import rs.cc.connection.rdp.RDPSessionState;
import rs.cc.tty.TTYMap;

// TODO Auto-generated method stub
import java.util.ArrayList;

public class LibFreeRDP
{
	private static final String TAG = "RDP";
//	private static EventListener listener;
	private static boolean mHasH264 = false;
	private static boolean DEBUG = true;

	private static final LongSparseArray<Boolean> mInstanceState = new LongSparseArray<>();

	static
	{
		final String h264 = "openh264";
		final String[] libraries = {
				
                					 "crypto",
                					 "ssl",
			                         "jpeg",
			                         "winpr3",
			                         "freerdp3",
			                         "freerdp-client3",
			                         "freerdp-android3" };
		final String LD_PATH = System.getProperty("java.library.path");

		for (String lib : libraries)
		{
			try
			{
				Log.i(TAG, "Trying to load library " + lib + " from LD_PATH: " + LD_PATH);
				System.loadLibrary(lib);
			}
			catch (UnsatisfiedLinkError e)
			{
				Log.e(TAG, "Failed to load library " + lib + ": " + e.toString());
				if (lib.equals(h264))
				{
					mHasH264 = false;
				}
			}
		}
		Log.e(TAG, "RDP --------------------------------------------------");
		freerdp_set_serial_class(TTYMap.class);
	}

	public static boolean hasH264Support()
	{
		return mHasH264;
	}

	private static native String freerdp_get_jni_version();

	private static native String freerdp_get_version();

	private static native String freerdp_get_build_revision();

	private static native String freerdp_get_build_config();

	private static native long freerdp_new(Context context);

	private static native void freerdp_set_serial_class(Class<?> clazz);
	
	private static native void freerdp_free(long inst);
	private static native boolean freerdp_parse_arguments(long inst, String[] args);
	private static native boolean freerdp_connect(long inst);
	private static native boolean freerdp_disconnect(long inst);
	private static native String freerdp_get_error(long inst);

	private static native boolean freerdp_update_graphics(long inst, Bitmap bitmap, int x, int y,
	                                                      int width, int height);

	private static native boolean freerdp_send_cursor_event(long inst, int x, int y, int flags);

	private static native boolean freerdp_send_key_event(long inst, int keycode, boolean down);

	private static native boolean freerdp_send_unicodekey_event(long inst, int keycode,
	                                                            boolean down);

	private static native boolean freerdp_send_clipboard_data(long inst, String data);

	private static native String freerdp_get_last_error_string(long inst);

	public static long newInstance(Context context)
	{
		return freerdp_new(context);
	}

	public static void freeInstance(long inst)
	{
		synchronized (mInstanceState)
		{
			if (mInstanceState.get(inst, false))
			{
				freerdp_disconnect(inst);
			}
			while (mInstanceState.get(inst, false))
			{
				try
				{
					mInstanceState.wait();
				}
				catch (InterruptedException e)
				{
					throw new RuntimeException();
				}
			}
		}
		freerdp_free(inst);
	}

	public static boolean isConnected(long inst) {
		synchronized (mInstanceState) {
			return mInstanceState.get(inst, false); 
		}
	}
	public static boolean connect(long inst)
	{
		synchronized (mInstanceState)
		{
			if (mInstanceState.get(inst, false))
			{
				return true;
			}
		}
		return freerdp_connect(inst);
	}

	public static boolean disconnect(long inst)
	{
		synchronized (mInstanceState)
		{
			if (mInstanceState.get(inst, false))
			{
				return freerdp_disconnect(inst);
			}
			return true;
		}
	}

	public static boolean cancelConnection(long inst)
	{
		synchronized (mInstanceState)
		{
			if (mInstanceState.get(inst, false))
			{
				return freerdp_disconnect(inst);
			}
			return true;
		}
	}

	private static String addFlag(String name, boolean enabled)
	{
		if (enabled)
		{
			return "+" + name;
		}
		return "-" + name;
	}

	public static boolean setConnectionInfo(Context context, long inst, RDPSessionConfig config)
	{
		

		String arg;
		ArrayList<String> args = new ArrayList<>();

		args.add(TAG);
		args.add("/gdi:sw");

		args.add("/client-hostname:" + Core.getHostname());

		
		int port = config.connectionConfig().port;
		String hostname = config.connectionConfig().host;

		args.add("/v:" + hostname);
		args.add("/port:" + String.valueOf(port));

		arg = config.authData().login;
		if (!arg.isEmpty())
		{
			args.add("/u:" + arg);
		}
		arg = config.connectionConfig().domain;
		if (arg !=null && !arg.isEmpty())
		{
			args.add("/d:" + arg);
		}
		arg = config.authData().password;
		if (!arg.isEmpty())
		{
			args.add("/p:" + arg);
		}

		if(config.screenConfig().fullScreen)
			args.add(
				    String.format("/size:%dx%d",Core.getInstance().getResources().getDisplayMetrics().widthPixels, 
				    		Core.getInstance().getResources().getDisplayMetrics().heightPixels));
		else
			args.add(
					String.format("/size:%dx%d", config.screenConfig().width, config.screenConfig().height));
		args.add("/bpp:" + String.valueOf(config.screenConfig().bpp));

		if (config.advConfig().consoleMode)
		{
			args.add("/admin");
		}

		switch (config.connectionConfig().security)
		{
			case 3: // NLA
				args.add("/sec-nla");
				break;
			case 2: // TLS
				args.add("/sec-tls");
				break;
			case 1: // RDP
				args.add("/sec-rdp");
				break;
			default:
				break;
		}

/*		if (!certName.isEmpty())
		{
			args.add("/cert-name:" + certName);
		} */

/*		if (config.prefConfig().rfx)
		{
			args.add("/rfx");
		}

		if (config.prefConfig().gfx)
		{
			args.add("/gfx");
		} 

		if(hasH264Support())
			args.add("/gfx:AVC444"); */

		args.add(addFlag("wallpaper", config.prefConfig().wallpaper));
		args.add(addFlag("window-drag", config.prefConfig().fullWindowDrag));
		args.add(addFlag("menu-anims", config.prefConfig().menuAnimation));
		args.add(addFlag("themes", config.prefConfig().themes));
		args.add(addFlag("fonts", config.prefConfig().fontSmoothing));
		args.add(addFlag("aero", config.prefConfig().aero));
		args.add(addFlag("glyph-cache", false));

		if (!config.advConfig().remoteProgram.isEmpty())
		{
			args.add("/shell:" + config.advConfig().remoteProgram);
		}

		if (!config.advConfig().workDir.isEmpty())
		{
			args.add("/shell-dir:" + config.advConfig().workDir);
		}

/*		args.add(addFlag("async-channels", debug.getAsyncChannel()));
		args.add(addFlag("async-input", debug.getAsyncInput()));
		args.add(addFlag("async-update", debug.getAsyncUpdate())); */

		if (config.storageConfig().mount) {
			args.add("/drive:sdcard," + config.storageConfig().folder);
		}

		args.add("/clipboard");

		// Gateway enabled?
		if (config.gateway().enabled)
		{

			args.add(String.format("/g:%s:%d", config.gateway().host, config.gateway().port));

			arg = config.gateway().username;
			if (!arg.isEmpty())
			{
				args.add("/gu:" + arg);
			}
			arg = config.gateway().domain;
			if (!arg.isEmpty())
			{
				args.add("/gd:" + arg);
			}
			arg = config.gateway().password;
			if (!arg.isEmpty())
			{
				args.add("/gp:" + arg);
			}
		}

		/* 0 ... local
		   1 ... remote
		   2 ... disable */
		args.add("/audio-mode:" + String.valueOf(config.audioConfig().soundMode));
		if (config.audioConfig().soundMode == 0)
		{
			args.add("/sound");
			if(config.audioConfig().microphone)
				args.add("/microphone");
		}


		args.add("/cert-ignore");
		if(DEBUG)
			args.add("/log-level:DEBUG");
		else 
			args.add("/log-level:INFO");
		if(config.inputConfig().scanerType != ScanerConfig.SCANER_TYPE_NONE) {
			if(config.inputConfig().serverType == InputConfig.STYPE_SERIAL) {
				args.add("/serial:COM"+config.inputConfig().serialPortNumber);
			}
		}
		if(config.prnConfig().enabled) 
			args.add("/serial:COM"+config.prnConfig().portNumber);
		String[] arrayArgs = args.toArray(new String[0]);
		Log.d("RDP", " "+args.toString());
		return freerdp_parse_arguments(inst, arrayArgs);
	}

	public static boolean updateGraphics(long inst, Bitmap bitmap, int x, int y, int width,
	                                     int height)
	{
		return freerdp_update_graphics(inst, bitmap, x, y, width, height);
	}

	public static boolean sendCursorEvent(long inst, int x, int y, int flags)
	{
		return freerdp_send_cursor_event(inst, x, y, flags);
	}

	public static boolean sendKeyEvent(long inst, int keycode, boolean down)
	{
		return freerdp_send_key_event(inst, keycode, down);
	}

	public static boolean sendUnicodeKeyEvent(long inst, int keycode, boolean down)
	{
		return freerdp_send_unicodekey_event(inst, keycode, down);
	}

	public static boolean sendClipboardData(long inst, String data)
	{
		return freerdp_send_clipboard_data(inst, data);
	}

	private static void OnConnectionSuccess(long inst)
	{
		SessionState s = Core.getSession(inst);
		if(s == null) return;
		s.getEventListener().OnConnectionSuccess(inst);
		synchronized (mInstanceState)
		{
			mInstanceState.append(inst, true);
			mInstanceState.notifyAll();
		}
	}

	private static void OnConnectionFailure(long inst)
	{
		SessionState s = Core.getSession(inst);
		if(s == null) return;
		s.getEventListener().OnConnectionFailure(inst);
		synchronized (mInstanceState)
		{
			mInstanceState.remove(inst);
			mInstanceState.notifyAll();
		}
	}

	private static void OnPreConnect(long inst)
	{
		SessionState s = Core.getSession(inst);
		if(s == null) return;
		s.getEventListener().OnPreConnect(inst);
	}

	private static void OnDisconnecting(long inst)
	{
		SessionState s = Core.getSession(inst);
		if(s == null) return;
		s.getEventListener().OnDisconnecting(inst);
	}

	private static void OnDisconnected(long inst)
	{
		SessionState s = Core.getSession(inst);
		if(s == null) return;
		s.getEventListener().OnDisconnected(inst);
		synchronized (mInstanceState)
		{
			mInstanceState.remove(inst);
			mInstanceState.notifyAll();
		}
	}

	private static void OnSettingsChanged(long inst, int width, int height, int bpp)
	{
		RDPSessionState s = (RDPSessionState)Core.getSession(inst);
		if (s == null)
			return;
		UIEventListener uiEventListener = s.getUIEventListener();
		if (uiEventListener != null)
			uiEventListener.OnSettingsChanged(width, height, bpp);
	}

	private static boolean OnAuthenticate(long inst, StringBuilder username, StringBuilder domain,
	                                      StringBuilder password)
	{
		RDPSessionState s = (RDPSessionState)Core.getSession(inst);
		if (s == null)
			return false;
		UIEventListener uiEventListener = s.getUIEventListener();
		if (uiEventListener != null)
			return uiEventListener.OnAuthenticate(username, domain, password);
		return false;
	}

	private static boolean OnGatewayAuthenticate(long inst, StringBuilder username,
	                                             StringBuilder domain, StringBuilder password)
	{
		RDPSessionState s = (RDPSessionState)Core.getSession(inst);
		if (s == null)
			return false;
		UIEventListener uiEventListener = s.getUIEventListener();
		if (uiEventListener != null)
			return uiEventListener.OnGatewayAuthenticate(username, domain, password);
		return false;
	}

	private static int OnVerifyCertificate(long inst, String commonName, String subject,
	                                       String issuer, String fingerprint, boolean hostMismatch)
	{
		RDPSessionState s = (RDPSessionState)Core.getSession(inst);
		if (s == null)
			return 0;
		UIEventListener uiEventListener = s.getUIEventListener();
		if (uiEventListener != null)
			return uiEventListener.OnVerifiyCertificate(commonName, subject, issuer, fingerprint,
			                                            hostMismatch);
		return 0;
	}

	private static int OnVerifyChangedCertificate(long inst, String commonName, String subject,
	                                              String issuer, String fingerprint,
	                                              String oldSubject, String oldIssuer,
	                                              String oldFingerprint)
	{
		RDPSessionState s = (RDPSessionState)Core.getSession(inst);
		if (s == null)
			return 0;
		UIEventListener uiEventListener = s.getUIEventListener();
		if (uiEventListener != null)
			return uiEventListener.OnVerifyChangedCertificate(
			    commonName, subject, issuer, fingerprint, oldSubject, oldIssuer, oldFingerprint);
		return 0;
	}

	private static void OnGraphicsUpdate(long inst, int x, int y, int width, int height)
	{
		RDPSessionState s = (RDPSessionState)Core.getSession(inst);
		if (s == null)
			return;
		UIEventListener uiEventListener = s.getUIEventListener();
		if (uiEventListener != null)
			uiEventListener.OnGraphicsUpdate(x, y, width, height);
	}

	private static void OnGraphicsResize(long inst, int width, int height, int bpp)
	{
		RDPSessionState s = (RDPSessionState)Core.getSession(inst);
		if (s == null)
			return;
		UIEventListener uiEventListener = s.getUIEventListener();
		if (uiEventListener != null)
			uiEventListener.OnGraphicsResize(width, height, bpp);
	}

	private static void OnRemoteClipboardChanged(long inst, String data)
	{
		RDPSessionState s = (RDPSessionState)Core.getSession(inst);
		if (s == null)
			return;
		UIEventListener uiEventListener = s.getUIEventListener();
		if (uiEventListener != null)
			uiEventListener.OnRemoteClipboardChanged(data);
	}

	public static String getError(long inst) {
		return freerdp_get_error(inst);
	}
	public static String getVersion()
	{
		return freerdp_get_version();
	}


}
