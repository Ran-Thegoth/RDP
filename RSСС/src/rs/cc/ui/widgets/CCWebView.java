package rs.cc.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.WebView;

public class CCWebView extends WebView {

	public CCWebView(Context context) {
		super(context);
	}

	public CCWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CCWebView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public CCWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}
	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
		outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE;
		outAttrs.inputType = EditorInfo.TYPE_NULL;
		return null;
	}
	@Override
	public boolean onCheckIsTextEditor() {
		return false;
	}

}
