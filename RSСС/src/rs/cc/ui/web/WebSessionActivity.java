package rs.cc.ui.web;

import java.io.ByteArrayInputStream;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import cs.U;
import rs.cc.Core;
import rs.cc.R;
import rs.cc.config.web.WebSessionConfig;
import rs.cc.config.web.WebSessionState;
import rs.cc.connection.SessionState;
import rs.cc.ui.SessionActivity;

public class WebSessionActivity extends Activity implements OnGlobalLayoutListener {

	private WebSessionState _state;
	private WebView _wv;
	private WebViewClient WC = new WebViewClient() {
		private boolean acceptRefer = true;

		public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
			Log.d("WWW", "BaseRQ "+request.getUrl().getHost());
			if (!BASE.matcher(request.getUrl().getHost()).find())
				acceptRefer = false;
			return false;
		};

		@Override
		public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
			if (!config().features().CrossDomain)
				do {
					if (BASE.matcher(request.getUrl().getHost()).find())
						break;
					if (acceptRefer) {
						Uri refer = null;
						for (String s : request.getRequestHeaders().keySet()) {
							if ("Referer".equalsIgnoreCase(s)) {
								refer = Uri.parse(request.getRequestHeaders().get(s));
								break;
							}
						}
						if (refer != null) {
							boolean r = BASE.matcher(refer.getHost()).find();
							Log.d("WWW", "RQ "+request.getUrl().getHost()+" from " + refer.getHost() + " to " + BASE.pattern() + " " + r);
							if (r)
								break;
						}
					}
					return new WebResourceResponse("text/html", "utf-8", 200, "Access denied", null,
							new ByteArrayInputStream("<html><body><h1>Доступ запрещен</h1></body></html>".getBytes()));
				} while (false);
			acceptRefer = true;
			return super.shouldInterceptRequest(view, request);
		};
	};

	private Pattern BASE;

	public WebSessionActivity() {
		// TODO Auto-generated constructor stub
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SessionState s = Core.getSession(getIntent().getLongExtra(SessionActivity.SESSION_ID_TAG, 0));
		if (!(s instanceof WebSessionState)) {
			finish();
			return;
		}
		_state = (WebSessionState) s;
		setContentView(R.layout.web_activity);
		_wv = findViewById(R.id.v_web);
		_wv.setWebViewClient(WC);
		WebSettings settings = _wv.getSettings();
		if (config().features().JS) {
			settings.setJavaScriptEnabled(true);
			settings.setDomStorageEnabled(true);
		}
		Uri base = Uri.parse(config().connectionConfig().host);
		String host = base.getHost();
		host = host.replaceAll("\\.", "\\\\.");
		BASE = Pattern.compile(host + "$");
		_wv.loadUrl(config().connectionConfig().host);
	}

	private WebSessionConfig config() {
		return _state.getConfig();
	}

	@Override
	public void onGlobalLayout() {
	}

	@Override
	public void onBackPressed() {
		if (_wv.canGoBack())
			_wv.goBack();
		else
			switch (config().advConfig().onBackPressedAction) {
			case 0:
				break;
			case 1:
				askExit();
				break;
			case 2:
				long now = System.currentTimeMillis();
				_wv.dispatchKeyEvent(
						new KeyEvent(now, now, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_F4, KeyEvent.META_ALT_ON));
				_wv.dispatchKeyEvent(
						new KeyEvent(now, now, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_F4, KeyEvent.META_ALT_ON));
				break;
			case 3:
				_wv.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ESCAPE));
				_wv.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ESCAPE));
				break;
			}

	}

	private void askExit() {
		U.confirm(this, R.string.confirm_close, new Runnable() {
			@Override
			public void run() {
				_wv.stopLoading();
				_wv.loadUrl("about:blank");
				_state.disconnect();
				finish();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

	}

}
