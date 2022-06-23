package rs.cc.ui;


import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import bsh.EvalError;
import bsh.Interpreter;
import cs.U;
import rs.cc.Core;
import rs.cc.api.Barcode;
import rs.cc.api.BarcodeProcessor;
import rs.cc.config.SessionConfig;
import rs.cc.config.SessionConfig.InputConfig;
import rs.cc.connection.KeyboardMapper;
import rs.cc.connection.SessionState;
import rs.cc.connection.KeyboardMapper.KeyProcessor;
import rs.cc.hardware.BarcodeScaner;
import rs.cc.hardware.BarcodeScaner.BarcodeListener;
import rs.cc.tty.BarcodeTTY;
import rs.cc.tty.TTY;
import rs.cc.tty.TTYMap;
import rs.cc.ui.dialogs.AuthDialog;
import rs.cc.ui.widgets.ScrollView2D;
import rs.cc.ui.widgets.SessionView;
import rs.cc.ui.widgets.TouchPointerView;
import rs.cc.ui.widgets.UpMenu;
import rs.cc.ui.widgets.SessionView.SessionViewListener;
import rs.cc.ui.widgets.TouchPointerView.TouchPointerListener;
import rs.cc.ui.widgets.UpMenu.UpMenuListener;
import rs.keyboard.FloatingKeyboard;
import rs.keyboard.KeyboardHandler;
import rs.keyboard.KeyboardLayouts;
import rs.cc.R;

@SuppressWarnings("deprecation")
public abstract class SessionActivity extends Activity implements KeyboardHandler, KeyProcessor, TouchPointerListener,
		SessionViewListener, UpMenuListener, BarcodeListener {

	protected static final int STATE_CONNECTING = 0;
	protected static final int STATE_CONNECTED = 1;
	protected static final int STATE_FAILED = 2;

	public static final String SESSION_ID_TAG = "sId";
	private SessionConfig _config;
	private WakeLock _wl;
	private SessionState _state;
	private KeyboardMapper _keyMapper;
	private View _screen, _locker;
	private TextView _status;
	private RelativeLayout _screenHolder;
	protected FloatingKeyboard _kbd;
	private TouchPointerView _pointer;
	private Handler _handler;
	private SessionView _sessionView;
	private ProgressBar _progress;
	private UpMenu _um;
	private ScrollView2D _scroller;
	private volatile int _cState;
	private BarcodeScaner _scaner;
	private long _startCon;
	private int LEFT_TOP_SIZE = 17;
	private TTY _tty;
	private JSONArray _ignoredKeys = null;
	private Interpreter _bsh = new Interpreter();
	private PointF _baseZoom = new PointF(1.0f, 1.0f);
	private BarcodeProcessor _bProcessor = new BarcodeProcessor();

	private class Closer implements Runnable  {
		
		private int _countdown = 0;
		public Runnable start() {
			_status.setText(R.string.disconnecting);
			_locker.setVisibility(View.VISIBLE);
			_progress.setProgress(0);
			return this; 
		}
		@Override
		public void run() {
			_progress.setProgress(++_countdown);
			if(_countdown == 100)
				finish();
			else
				handler().postDelayed(this, 30);
			
		}
	};
	private Closer CLOSER = new Closer(); 
	private Runnable START_CONNECTION = new Runnable() {
		@Override
		public void run() {
			_startCon = System.currentTimeMillis();
			_cState = beginConnection();
			switch (_cState) {
			case STATE_CONNECTING:
				_status.setText(R.string.connecting);
				_locker.setVisibility(View.VISIBLE);
				PROGRESS.run();
				break;
			case STATE_CONNECTED:
				_locker.setVisibility(View.GONE);
				break;
			}
		}
	};

	private Runnable PROGRESS = new Runnable() {
		@Override
		public void run() {

			if (System.currentTimeMillis() - _startCon > 30000) {
				_cState = STATE_FAILED;
			}
			if (_cState != STATE_CONNECTING) {
				
				if (_cState == STATE_FAILED) {
					U.confirm(SessionActivity.this, getString(R.string.connection_error) + "\n" + getLasErrorDescr(),
							new Runnable() {
								@Override
								public void run() {
									START_CONNECTION.run();
								}
							}, new Runnable() {
								@Override
								public void run() {
									finish();
								}
							});
				} else {
					_locker.setVisibility(View.GONE);
					_config.setSuccessConnected();
				}
					
				return;
			}
			if (_progress.getProgress() == 100)
				_progress.setProgress(0);
			else
				_progress.setProgress(_progress.getProgress() + 1);
			_handler.postDelayed(this, 150);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			_state = Core.getSession(savedInstanceState.getLong(SESSION_ID_TAG));
		} else if (getIntent() != null)
			_state = Core.getSession(getIntent().getLongExtra(SESSION_ID_TAG, 0));
		if (_state == null) {
			U.notify(this, getString(R.string.session_error), new Runnable() {
				@Override
				public void run() {
					finish();
				}
			});
			return;
		}
		_config = _state.getConfig();
		switch (config().screenConfig().rotation) {
		case 0:
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
			break;
		case 1:
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			break;
		case 2:
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			break;
		}

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		_wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
		LEFT_TOP_SIZE = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, LEFT_TOP_SIZE,
				getResources().getDisplayMetrics());
		setContentView(R.layout.session_activity);
		_progress = findViewById(R.id.pb_progress);
		_status = findViewById(R.id.lbl_status);
		_locker = findViewById(R.id.v_ui_locker);
		_locker.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				MarginLayoutParams lp = (MarginLayoutParams)_progress.getLayoutParams();
				int h = bottom-top;
				int w = right - left;
				int s = h > w ? w : h;
				lp.width = lp.height =  (int)(s * 0.83);
				_progress.setLayoutParams(lp);
				lp = (MarginLayoutParams)_status.getLayoutParams();
				lp.leftMargin = lp.rightMargin = (int)(s - (s * 0.83));
				_status.setLayoutParams(lp);
			}
		});
		_kbd = findViewById(R.id.v_keyboard);
		_um = findViewById(R.id.v_upmenu);
		_um.setUpMenuListener(this);

		_scroller = findViewById(R.id.v_scroll_view);
//		_kbd.setHolderPosition(Gravity.TOP);
		if (config().inputConfig().keyboard) {
			_kbd.setHandler(this);
			_kbd.setup(config().inputConfig().kbSettings);
		}
		if(config().inputConfig().hwKeyboard) {
			if(config().inputConfig().hwSettings.has(rs.cc.Const.IGNORED_KEYS_TAG)) try {
				_ignoredKeys =  config().inputConfig().hwSettings.getJSONArray(rs.cc.Const.IGNORED_KEYS_TAG);
			} catch(JSONException jse) { }
		}
		_kbd.setVisibility(View.GONE);

		_pointer = findViewById(R.id.v_pointer);
		if (config().inputConfig().pointer)
			_pointer.setTouchPointerListener(this);
		_keyMapper = createKBMapper();
		_handler = createHandler();
		_sessionView = createSessionView();
		_sessionView.setSessionViewListener(this);
		_screen = _sessionView.getView();
		_screen.setFocusable(false);
		_screen.setFocusableInTouchMode(false);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		_screenHolder = findViewById(R.id.v_session_holder);
		_screenHolder.addView(_screen, lp);
		_scaner = config().inputConfig().makeScanner(this);
		setupBSHEnviroment(_bsh);

		if (_scaner != null) {
			if (_scaner.isManual())
				_um.enableBarcodeReader();
			if (config().inputConfig().serverType == InputConfig.STYPE_SERIAL) {
				_tty = _scaner.getTTY(this);
				if (_tty != null)
					TTYMap.registerTTY("COM" + config().inputConfig().serialPortNumber, _tty);
			}
			
			if (!config().scripts().onBarcode.isEmpty()) try {
				_bProcessor = new BarcodeProcessor(config().scripts().onBarcode);
			} catch(Exception e) {
				_bProcessor = new BarcodeProcessor();
			}
		}
/*		if (config().prnConfig().enabled) {
			_printer = new BTTTY(this, config().prnConfig().deviceAddress);
			TTYMap.registerTTY("COM" + config().prnConfig().portNumber, _printer);
		} */

	}

	protected SessionConfig config() {
		return _config;
	}

	protected SessionState state() {
		return _state;
	}

	protected SessionView sessionView() {
		return _sessionView;
	}

	protected Handler handler() {
		return _handler;
	}

	protected ScrollView2D scroller() {
		return _scroller;
	}

	protected TouchPointerView pointer() {
		return _pointer;
	}

	protected KeyboardMapper KBMapper() {
		return _keyMapper;
	}

	public BarcodeScaner barcodeScaner() {
		return _scaner;
	}

	protected Interpreter beanShell() {
		return _bsh;
	}

	protected void rescaleIfNeeded() {
		handler().post(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				int sw = getScreenWidth();
				int sh = getScreenHeight();
				int dw = getResources().getDisplayMetrics().widthPixels;
				int dh = getResources().getDisplayMetrics().heightPixels;
				if (sw < dw || sh < dh) {
					ViewGroup.LayoutParams lp = sessionView().getView().getLayoutParams();
					int vw = dw, vh = dh;
					if (config().screenConfig().zoom) {
						if (config().screenConfig().keepRatio) {
							float scale = 1.0f;
							if (sw > sh) {
								scale = sw / (float) dw;
								vh = (int) (vw * scale);
								_baseZoom.x = _baseZoom.y = dw / (float) sw;
							} else {
								scale = sh / (float) dh;
								vw = (int) (vh * scale);
								_baseZoom.x = _baseZoom.y = dh / (float) sh;
							}
						} else {
							_baseZoom.x = dw / (float)sw;
							_baseZoom.y = dh / (float)sh;
						}
						lp.width = vw;
						lp.height = vh;
						sessionView().setZoom(_baseZoom.x, _baseZoom.y);
					}
					if (config().screenConfig().center) {
						int lm = (dw - lp.width) / 2;
						int tm = (dh - lp.height) / 2;
						if (lm < 0)
							lm = 0;
						if (tm < 0)
							tm = 0;
						((MarginLayoutParams) lp).topMargin = tm;
						((MarginLayoutParams) lp).leftMargin = lm;
					}
					sessionView().getView().setLayoutParams(lp);

				}
			}
		});

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(SESSION_ID_TAG, _state.id());

	}

	@Override
	public void onKeyPressed(FloatingKeyboard source, KeyEvent[] e) {
		if (_keyMapper != null)
			_keyMapper.processKey(e, _kbd.getMetaState());
	}

	@Override
	protected void onStart() {
		super.onStart();
		_wl.acquire();
		if (config().authData().login.isEmpty() || config().authData().password.isEmpty()) {
			new AuthDialog(this, config().authData()).show(START_CONNECTION, new Runnable() {
				@Override
				public void run() {
					finish();
				}
			});
		} else
			_handler.postDelayed(START_CONNECTION, 200);
		if (_scaner != null && !_scaner.isManual())
			_scaner.startScan(this);
	}

	@Override
	protected void onStop() {
		_wl.release();
		if (_scaner != null)
			_scaner.stopScan();
		_handler.removeCallbacks(START_CONNECTION);
		_handler.removeCallbacks(PROGRESS);
		super.onStop();
	}

	protected void requestLayout() {
		findViewById(R.id.session_root_view).requestLayout();
	}

	protected abstract Handler createHandler();

	protected abstract SessionView createSessionView();

	protected abstract KeyboardMapper createKBMapper();

	protected abstract int beginConnection();

	protected abstract void endConnection();

	protected abstract int getScreenWidth();

	protected abstract int getScreenHeight();

	protected abstract void sendClibpoardContent(String s);

	protected abstract String getLasErrorDescr();
	protected abstract void sendUnicodeKey(int key, boolean down);

	protected void setupBSHEnviroment(Interpreter bsh) {
		try {
			bsh.set("Keys", _keyMapper);
		} catch (EvalError err) {

		}
	}

	protected void setConnectionState(int state) {
		_cState = state;
	}

	@Override
	public void onTouchPointerToggleKeyboard() {
		if (config().inputConfig().keyboard) {
			if (_kbd.getVisibility() != View.VISIBLE) {
				_kbd.showSelectedKeyboard(KeyboardLayouts.Language);
				_kbd.setVisibility(View.VISIBLE);
			} else {
				if(_kbd.getCurrentLayout() != KeyboardLayouts.Language)
					_kbd.showSelectedKeyboard(KeyboardLayouts.Language);
				else 
					_kbd.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onTouchPointerToggleExtKeyboard() {
		if (config().inputConfig().keyboard) {
			if (_kbd.getVisibility() != View.VISIBLE) {
				_kbd.showSelectedKeyboard(KeyboardLayouts.ExtraMeta);
				_kbd.setVisibility(View.VISIBLE);
			} else {
				if(_kbd.getCurrentLayout() != KeyboardLayouts.ExtraMeta)
					_kbd.showSelectedKeyboard(KeyboardLayouts.ExtraMeta);
				else
					_kbd.setVisibility(View.GONE);
			}
		}
	}

	protected void onBeginDisconnection() {
		
	}
	
	private void askExit() {
		U.confirm(this, R.string.confirm_close, new Runnable() {
			@Override
			public void run() {
				CLOSER.start().run();
				onBeginDisconnection();
				_config.disconnect();
			}
		});
	}
	@Override
	public void onBackPressed() {
		if (_kbd != null && _kbd.getVisibility() == View.VISIBLE) {
			_kbd.setVisibility(View.GONE);
			return;
		}
		if(mKeyPress) {
			askExit();
			return;
		}
		switch (_config.advConfig().onBackPressedAction) {
		case 0:
			break;
		case 1:
			askExit();
			break;
		case 2:
			_keyMapper.processKey(new KeyEvent[] { new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_F4),
					new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_F4) }, KeyEvent.META_ALT_ON);
			break;
		case 3:
			_keyMapper.processKey(new KeyEvent[] { new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ESCAPE) }, 0);
			break;
		}
	}

	private Point _TP = new Point();
	private long _touchTime;

	@Override
	public void onSessionViewBeginTouch(int x, int y) {
		_TP.set(x, y);
		_touchTime = System.currentTimeMillis();
	}

	@Override
	public void onSessionViewEndTouch(int x, int y) {
		if (System.currentTimeMillis() - _touchTime > 400) {
			if ((Math.abs(x - _TP.x) < 10) && (Math.abs(y - _TP.y) < 10)) {
				if (config().inputConfig().keyboard || config().inputConfig().pointer) {
					if (_um.getVisibility() != View.VISIBLE)
						_um.setVisibility(View.VISIBLE);
				}
			}
		} else if (_um.getVisibility() == View.VISIBLE)
			_um.setVisibility(View.GONE);
	}

	@Override
	public void onPonter() {
		_um.setVisibility(View.GONE);
		if (config().inputConfig().pointer) {
			MarginLayoutParams lp = (MarginLayoutParams) _screenHolder.getLayoutParams();
			_screenHolder.setPadding(LEFT_TOP_SIZE, LEFT_TOP_SIZE, 0, 0);
			lp.width = getScreenWidth() + _pointer.getPointerWidth();
			lp.height = getScreenHeight() + _pointer.getPointerHeight();
			_screenHolder.setLayoutParams(lp);
			_pointer.setVisibility(View.VISIBLE);
			handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					_scroller.scrollBy(LEFT_TOP_SIZE, LEFT_TOP_SIZE);
				}
			}, 10);
		}
	}

	@Override
	public void onKeyboard() {
		_um.setVisibility(View.GONE);
		onTouchPointerToggleKeyboard();
	}

	@Override
	public void onNumPad() {
		_um.setVisibility(View.GONE);
		onTouchPointerToggleExtKeyboard();
	}

	@Override
	public void onBarcode() {
		_um.setVisibility(View.GONE);
		if (_scaner != null && _scaner.isManual())
			_scaner.scanOnce(this);
	}

	@Override
	public void onTouchPointerClose() {
		_pointer.setVisibility(View.GONE);
		MarginLayoutParams lp = (MarginLayoutParams) _screenHolder.getLayoutParams();
		_screenHolder.setPadding(0, 0, 0, 0);
		lp.width = getScreenWidth();
		lp.height = getScreenHeight();
		_screenHolder.setLayoutParams(lp);
	}

	@Override
	public void onTouchPointerResetScrollZoom() {
		sessionView().setZoom(_baseZoom.x, _baseZoom.y);
		scroller().scrollTo(LEFT_TOP_SIZE, LEFT_TOP_SIZE);
	}

	@Override
	protected void onDestroy() {
		handler().removeCallbacks(CLOSER);
		if (_tty != null)
			TTYMap.unregisterTTY("COM" + config().inputConfig().serialPortNumber);
/*		if (_printer != null)
			TTYMap.unregisterTTY("COM" + config().prnConfig().portNumber); */
		super.onDestroy();
	}

	protected Point mapScreenCoordToSessionCoord(int x, int y) {
		int mappedX = (int) ((float) (x + scroller().getScrollX()) / sessionView().getZoomX()) - LEFT_TOP_SIZE;
		int mappedY = (int) ((float) (y + scroller().getScrollY()) / sessionView().getZoomY()) - LEFT_TOP_SIZE;
		if (mappedX > getScreenWidth())
			mappedX = getScreenWidth();
		if (mappedY > getScreenHeight())
			mappedY = getScreenHeight();
		return new Point(mappedX, mappedY);
	}

	private KeyCharacterMap _charmap = KeyCharacterMap.load(KeyCharacterMap.BUILT_IN_KEYBOARD);

	@Override
	public void onBarcode(byte[] barcode) {
		Barcode bc = new Barcode(barcode);
		_bProcessor.process(bc);
		switch (config().inputConfig().serverType) {
		case InputConfig.STYPE_KEYBOARD: {
			String s = bc.asString();
			Log.d("BC", "Barcode is '"+s+"'");
			bc.before(this);
			for (int i = 0; i < s.length(); i++)
					_keyMapper.processKey(_charmap.getEvents(new char[] { s.charAt(i) }), 0);
			bc.after(this);
		}
			break;
		case InputConfig.STYPE_CLIPBOARD:
			bc.before(this);
			sendClibpoardContent(new String(bc.asString()));
			_keyMapper.processKey(FloatingKeyboard.makeEvent(KeyEvent.KEYCODE_INSERT), KeyEvent.META_SHIFT_ON);
			bc.after(this);
			break;
		case InputConfig.STYPE_SERIAL:
			if (_tty != null) {
				if(_tty instanceof BarcodeTTY)
					((BarcodeTTY)_tty).store(bc);
				else 
					_tty.store(bc.get());
			}
			break;
		}
	}
	
	public void processKey(int keycode, int meta) {
		_keyMapper.processKey(FloatingKeyboard.makeEvent(keycode), meta);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	private boolean mKeyPress = false;
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK)
			return false;
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
			mKeyPress = true;
		if (!config().inputConfig().hwKeyboard)
			return false;
		if(_ignoredKeys != null) try {
			for(int i=0;i<_ignoredKeys.length();i++) 
				if(keyCode == _ignoredKeys.getInt(i)) return false;
		} catch(JSONException jse) { }
		event = Core.getInstance().HWKeyboard().translate(event);
		if (event.getUnicodeChar() != 0 && event.getKeyCode() != KeyEvent.KEYCODE_ENTER)
			sendUnicodeKey(event.getUnicodeChar(), true);
		else
			_keyMapper.processKey(new KeyEvent[] { event }, event.getMetaState());
		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			onBackPressed();
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
			mKeyPress = false;
		if (!config().inputConfig().hwKeyboard)
			return false;
		if(_ignoredKeys != null) try {
			for(int i=0;i<_ignoredKeys.length();i++) 
				if(keyCode == _ignoredKeys.getInt(i)) return false;
		} catch(JSONException jse) { }
		event = Core.getInstance().HWKeyboard().translate(event);
		if (event.getUnicodeChar() != 0 && event.getKeyCode() != KeyEvent.KEYCODE_ENTER)
			sendUnicodeKey(event.getUnicodeChar(), false);
		else
			_keyMapper.processKey(new KeyEvent[] { event }, event.getMetaState());
		return true;
	}
	
}
