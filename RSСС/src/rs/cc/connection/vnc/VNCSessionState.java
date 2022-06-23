package rs.cc.connection.vnc;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.zip.Inflater;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.util.Log;
import rs.cc.Const;
import rs.cc.Core;
import rs.cc.api.BarcodeProcessor;
import rs.cc.config.vnc.VNCSessionConfig;
import rs.cc.connection.EventListener;
import rs.cc.connection.SessionState;
import rs.cc.ui.widgets.vnc.RfbProto;

public class VNCSessionState implements SessionState, Runnable {

	public static interface VNCEventListener {
		public void onSizeChanged(int w, int h);

		public void onGraphicUpdate();
	}

	private class VNCMessage {
		int what;
		int evt;
		Object payload;

		VNCMessage(int w, int e, Object o) {
			what = w;
			payload = o;
			this.evt = e;
		}
	}

	private class FrameBuffer extends DrawableContainer {
		private int[] pixels;

		public FrameBuffer() {
			pixels = new int[_rfb.fbWidth() * _rfb.fbHeight()];
		}

		@SuppressWarnings("deprecation")
		@Override
		public void draw(Canvas canvas) {
			canvas.drawBitmap(pixels, 0, _rfb.fbWidth(), 0, 0, _rfb.fbWidth(), _rfb.fbHeight(), false, null);
		}

		public int offset(int x, int y) {
			return x + y * _rfb.fbWidth();
		}

		@SuppressWarnings("unused")
		public void drawRect(int x, int y, int w, int h, int pixel) {
			for (int sy = y; sy < y + h; y++)
				Arrays.fill(pixels, offset(x, sy), offset(x + w, sy), pixel);

		}

		@Override
		public int getIntrinsicHeight() {
			return _rfb.fbHeight();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.graphics.drawable.DrawableContainer#getIntrinsicWidth()
		 */
		@Override
		public int getIntrinsicWidth() {
			return _rfb.fbWidth();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.graphics.drawable.DrawableContainer#getOpacity()
		 */
		@Override
		public int getOpacity() {
			return PixelFormat.OPAQUE;
		}

		@Override
		public boolean isStateful() {
			return false;
		}

	}

	private FrameBuffer _fb;
	private String _error;
	private RfbProto _rfb;
	private VNCSessionConfig _config;
	private EventListener _eventListener;
	private Thread _worker;
	private VNCEventListener _VNClistener;
	private Queue<VNCMessage> _messages = new LinkedList<>();

	private static final int MSG_CONNECT = 1;
	private static final int MSG_DISCONNECT = 2;
	private static final int MSG_MOUSE_BUTTON = 4;

	public Drawable getSurface() {
		return _fb;
	}

	public VNCSessionState(VNCSessionConfig config) {
		_config = config;
		_rfb = new RfbProto(_config.connectionConfig().host, config.connectionConfig().port);
		Core.registerSession(id(), this);
		_worker = new Thread(this);
		_worker.start();
	}

	@Override
	protected void finalize() throws Throwable {
		if (_worker != null)
			_worker.interrupt();
		Log.d("VNC", "Died...");
		Core.unregisterSession(id());
		super.finalize();
	}

	@Override
	public VNCSessionConfig getConfig() {
		return _config;
	}

	@Override
	public void disconnect() {
		if (!_rfb.isClosed()) {
			sendMessage(MSG_DISCONNECT);
			Log.d("VNC", "Closing");
		}
	}

	private void sendMessage(int what) {
		sendMessage(what, 0,null);
	}

	@SuppressWarnings("unused")
	private void sendMessage(int what, Object o) {
		sendMessage(what,0,o);
	}
	private void sendMessage(int what, int e, Object o) {
		synchronized (_messages) {
			_messages.add(new VNCMessage(what, e, o));
		}

	}

	@Override
	public boolean connect(Context ctx) {
		if (_rfb.isClosed())
			sendMessage(MSG_CONNECT);
		return true;
	}

	@Override
	public long id() {
		return _rfb.hashCode();
	}

	@Override
	public EventListener getEventListener() {
		return _eventListener;
	}

	public void setEventListener(EventListener l) {
		_eventListener = l;
	}

	public void setVNCEventListener(VNCEventListener l) {
		_VNClistener = l;
	}

	@Override
	public void run() {
		Log.d("VNC", "Worker online...");
		while(!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException ie) {
				Log.d("VNC", "IE catched");
				break;
			}
			VNCMessage msg = null;
			synchronized (_messages) {
				if (!_messages.isEmpty())
					msg = _messages.poll();
			}
			try {
				if (msg != null) {
					_error = Const.EMPTY_STRING;
					Log.d("VNC", "Handle msg " + msg.what);
					switch (msg.what) {
					case MSG_CONNECT:
						doConnect();
						break;
					case MSG_MOUSE_BUTTON:{
							Point p = (Point)msg.payload;
							_rfb.writePointerEvent(p.x, p.y, 0, msg.evt);
						}
						break;
					case MSG_DISCONNECT:
						synchronized (_messages) {
							_messages.clear();
						}
						encodings = null;
						if (!_rfb.isClosed()) {
							_rfb.close();
							Core.getInstance().post(new Runnable() {
								@Override
								public void run() {
									_eventListener.OnDisconnected(id());
								}
							});
						}
						_fb = null;
						break;
					}
				}
				if (!_rfb.isClosed()) {
					int msgType = _rfb.readServerMessageType();
					switch (msgType) {
					case RfbProto.FramebufferUpdate:
						updateBuffer();
						break;
					case RfbProto.Bell:
						BarcodeProcessor.beepSuccess();
						break;
					case RfbProto.ServerCutText:
						String s = _rfb.readServerCutText();
						if (s != null && s.length() > 0) {
						}
						break;
					case RfbProto.TextChat:
						s = _rfb.readTextChatMsg();
						if (s != null && s.length() > 0) {
						}
						break;
						
					}
				}
			} catch (Exception | Error  ioe) {
				Log.e("VNC", "loop exception", ioe);
				sendMessage(MSG_DISCONNECT);
			}
		}

	}

	private void doConnect() {
		try {
			_rfb.open();
			_rfb.readVersionMsg();
			_rfb.writeVersionMsg();
			int bitPref = 0;
			if (!_config.authData().login.isEmpty())
				bitPref |= 1;
			int secType = _rfb.negotiateSecurity(bitPref);
			int authType;
			if (secType == RfbProto.SecTypeTight) {
				_rfb.initCapabilities();
				_rfb.setupTunneling();
				authType = _rfb.negotiateAuthenticationTight();
			} else if (secType == RfbProto.SecTypeUltra34) {
				_rfb.prepareDH();
				authType = RfbProto.AuthUltra;
			} else
				authType = secType;
			switch (authType) {
			case RfbProto.AuthNone:
				_rfb.authenticateNone();
				break;
			case RfbProto.AuthVNC:
				_rfb.authenticateVNC(_config.authData().password);
				break;
			case RfbProto.AuthUltra:
				_rfb.authenticateDH(_config.authData().login, _config.authData().password);
				break;
			default:
				throw new Exception("Unknown authentication scheme " + authType);
			}
			_rfb.writeClientInit();
			_rfb.readServerInit();
			_rfb.writeSetPixelFormat(32, 24, false, true, 255, 255, 255, 16, 8, 0, false);
			_rfb.writeFramebufferUpdateRequest(0, 0, _rfb.fbWidth(), _rfb.fbHeight(), false);

			_fb = new FrameBuffer();

			Core.getInstance().post(new Runnable() {
				@Override
				public void run() {
					_VNClistener.onSizeChanged(_rfb.fbWidth(), _rfb.fbHeight());
				}
			});
			Core.getInstance().post(new Runnable() {
				@Override
				public void run() {
					_eventListener.OnConnectionSuccess(id());
				}
			});
			return;
		} catch (Exception ioe) {
			Log.e("VNC", "doConnect()", ioe);
			_error = ioe.getLocalizedMessage();
		}
		Core.getInstance().post(new Runnable() {

			@Override
			public void run() {
				_eventListener.OnConnectionFailure(id());
			}

		});
	}

	int[] encodings = null;

	private void updateBuffer() throws Exception {
		_rfb.readFramebufferUpdate();

		for (int i = 0; i < _rfb.updateNRects(); i++) {
			_rfb.readFramebufferUpdateRectHdr();
			int rx = _rfb.updateRectX(), ry = _rfb.updateRectY();
			int rw = _rfb.updateRectW(), rh = _rfb.updateRectH();

			if (_rfb.updateRectEncoding() == RfbProto.EncodingLastRect)
				break;
			if (_rfb.updateRectEncoding() == RfbProto.EncodingNewFBSize) {
				_rfb.setFramebufferSize(rw, rh);
				break;
			}

			if (_rfb.updateRectEncoding() == RfbProto.EncodingXCursor
					|| _rfb.updateRectEncoding() == RfbProto.EncodingRichCursor)
				continue;
			if (_rfb.updateRectEncoding() == RfbProto.EncodingPointerPos) {
				/*
				 * mouseX=rx; mouseY=ry; Log.v(TAG, "rfb.EncodingPointerPos");
				 */
				continue;
			}

			_rfb.startTiming();
			switch (_rfb.updateRectEncoding()) {
			case RfbProto.EncodingRaw:
				handleRawRect(rx, ry, rw, rh);
				break;
			case RfbProto.EncodingCopyRect:
				break;
			case RfbProto.EncodingRRE:
				break;
			case RfbProto.EncodingCoRRE:
				break;
			case RfbProto.EncodingHextile:
				break;
			case RfbProto.EncodingZRLE:
				break;
			case RfbProto.EncodingZlib:
				handleZlibRect(rx, ry, rw, rh);
				break;
			}
			_rfb.stopTiming();
		}
		Core.getInstance().post(new Runnable() {
			@Override
			public void run() {
				_VNClistener.onGraphicUpdate();
			}
		});
		if (encodings == null) {
			encodings = new int[20];
			encodings[0] = RfbProto.EncodingZlib;
			encodings[1] = RfbProto.EncodingCompressLevel0;
			encodings[2] = RfbProto.EncodingQualityLevel0;
			encodings[3] = RfbProto.EncodingLastRect;
			encodings[4] = RfbProto.EncodingNewFBSize;
			_rfb.writeSetEncodings(encodings, 5);
		}
		_rfb.writeFramebufferUpdateRequest(0, 0, _rfb.fbWidth(), _rfb.fbHeight(), true);
	}

	public boolean isConnected() {
		return !_rfb.isClosed();
	}

	public String getLastError() {
		return _error;
	}

	private void handleRawRect(int x, int y, int w, int h) throws IOException {
		int[] pixels = _fb.pixels;
		int bSize = w * 4;
		byte[] handleRawRectBuffer = new byte[bSize];
		int i, offset;
		for (int dy = y; dy < y + h; dy++) {
			_rfb.readFully(handleRawRectBuffer, 0, bSize);
			offset = _fb.offset(x, dy);
			for (i = 0; i < w; i++) {
				final int idx = i * 4;
				pixels[offset + i] = // 0xFF << 24 |
						(handleRawRectBuffer[idx + 2] & 0xff) << 16 | (handleRawRectBuffer[idx + 1] & 0xff) << 8
								| (handleRawRectBuffer[idx] & 0xff);
			}
		}
	}

	private byte[] zlibBuf;
	private Inflater zlibInflater = new Inflater();

	//
	// Handle a Zlib-encoded rectangle.
	//


	private void handleZlibRect(int x, int y, int w, int h) throws Exception {
		int nBytes = _rfb.readInt();

		if (zlibBuf == null || zlibBuf.length < nBytes) {
			zlibBuf = new byte[nBytes * 2];
		}

		_rfb.readFully(zlibBuf, 0, nBytes);

		zlibInflater.setInput(zlibBuf, 0, nBytes);
		int[] pixels = _fb.pixels;
		final int bSize = w * 4;
		byte[] handleZlibRectBuffer = new byte[bSize];
		
		int i, offset;
		for (int dy = y; dy < y + h; dy++) {
			zlibInflater.inflate(handleZlibRectBuffer, 0, bSize);
			offset = _fb.offset(x, dy);
			for (i = 0; i < w; i++) {
				final int idx = i * 4;
				pixels[offset + i] = (handleZlibRectBuffer[idx + 2] & 0xFF) << 16
						| (handleZlibRectBuffer[idx + 1] & 0xFF) << 8 | (handleZlibRectBuffer[idx] & 0xFF);
			}
		}
	}

	public void sendMouseButton(int button, Point p, boolean down) {
		if(down)
			sendMessage(MSG_MOUSE_BUTTON,button,p);
		else 
			sendMessage(MSG_MOUSE_BUTTON,0,p);
		
	}


}
