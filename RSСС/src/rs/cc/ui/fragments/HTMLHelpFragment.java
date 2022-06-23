package rs.cc.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import cs.ui.BackHandler;
import cs.ui.fragments.BaseFragment;
import rs.cc.R;

@SuppressLint("SetJavaScriptEnabled")
public class HTMLHelpFragment extends BaseFragment implements BackHandler, View.OnClickListener {

	public static HTMLHelpFragment newInstance(String url) {
		HTMLHelpFragment result = new HTMLHelpFragment();
		result._url = url;
		return result;
	}
	private String _url;
	private WebView _vw;
	public HTMLHelpFragment() {
		// TODO Auto-generated constructor stub
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(_vw == null) {
			_vw = new WebView(getContext());
			_vw.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
					return false;
				}
				
			});
			
			_vw.getSettings().setJavaScriptEnabled(true);
			_vw.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
			_vw.loadUrl(_url);
		}
		return _vw;
	}
	
	@Override
	public boolean onBackPressed() {
		if(_vw.canGoBack()) {
			_vw.goBack();
			return false;
		}
		return true;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		getActivity().setTitle(R.string.help);
		setCustomButtom(R.drawable.ic_menu_close, this);
	}
	
	@Override
	public void onClick(View arg0) {
		getFragmentManager().popBackStack();
		
	}

}
