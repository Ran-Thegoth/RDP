package rs.cc.ui.fragments;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import cs.U;
import cs.ui.BackHandler;
import cs.ui.UIBinder;
import cs.ui.fragments.BaseEditor;
import cs.ui.widgets.DialogSpinner;
import rs.cc.config.SessionConfig;
import rs.cc.config.rdp.RDPSessionConfig;
import rs.cc.hardware.ContextOwner;
import rs.cc.hardware.BarcodeScaner.BarcodeListener;
import rs.cc.misc.ScanTouchListener;
import rs.cc.R;

@SuppressLint("ClickableViewAccessibility")
public class RDPSessionEditor extends BaseEditor<SessionConfig> implements OnItemSelectedListener, View.OnClickListener, BarcodeListener,ContextOwner, BackHandler {
	
	private DialogSpinner _pageHeader;
	private ViewPager _pages;
	private View _content, _rightArrow,_leftArrow;
	private TextView _code;
	
	private class SettingsPageAdapater extends PagerAdapter {
		private class ViewPage {
			Object[] o;
			ScrollView page;
			View content;
			public ViewPage(Object ...o) {
				this.o = o;
				page = new ScrollView(getContext());
				content = new LinearLayout(getContext());
				content.setPadding(0, 6, 0, 6);
				page.addView(content,new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
				((LinearLayout)content).setOrientation(LinearLayout.VERTICAL);
				for(Object a : o)
					UIBinder.buildUI(a, (ViewGroup)content);
			}
			public boolean obtain() {
				for(Object a : o)
					if(!UIBinder.obtain(content, a)) return false;
				return true;
			}
			
			public ViewPage( View v, Object...o) {
				this.o = o;
				this.content = v;
				page = new ScrollView(getContext());
				page.addView(content,new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
				for(Object a : o)
					UIBinder.bind(a, v);
			}
		}
		
		private List<ViewPage> _pages = new ArrayList<>();
		public SettingsPageAdapater() {
			_pages.add(new ViewPage(getActivity().getLayoutInflater().inflate(R.layout.rdp_network_config ,RDPSessionEditor.this._pages,false),
						getValue().connectionConfig(), ((RDPSessionConfig)getValue()).gateway()));
			_pages.add(new ViewPage(getActivity().getLayoutInflater().inflate(R.layout.screen_config,RDPSessionEditor.this._pages,false), getValue().screenConfig()));
			_pages.add(new ViewPage(
					getActivity().getLayoutInflater().inflate(R.layout.storage_config,RDPSessionEditor.this._pages,false),
					getValue().storageConfig()));
			_pages.add(new ViewPage(getValue().audioConfig()));
			_pages.add(new ViewPage(((RDPSessionConfig)getValue()).prefConfig()));
			_pages.add(new ViewPage(getValue().advConfig()));
			_pages.add(new ViewPage(
					getActivity().getLayoutInflater().inflate(R.layout.input_config,RDPSessionEditor.this._pages,false),
					getValue().inputConfig(),getValue().scripts()));
	/*		_pages.add(new ViewPage(getActivity().getLayoutInflater().inflate(R.layout.printer_config,RDPSessionEditor.this._pages,false),
					getValue().prnConfig())); */
		}
		@Override
		public int getCount() {
			return _pages.size();
		}

		@Override
		public boolean isViewFromObject(View v, Object o) {
			return v == o;
		}
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(_pages.get(position).page);
		}
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			ViewPage vp = _pages.get(position);
			container.addView(vp.page);
			return vp.page;
		}
		
	}
	
	public static RDPSessionEditor newInstance(RDPSessionConfig value, OnSaveListener<SessionConfig> l) {
		RDPSessionEditor result = new RDPSessionEditor();
		result.setValue(value);
		result.setListener(l);
		return result;
	}
	
	private List<rs.cc.ui.fragments.RDPSessionEditor.SettingsPageAdapater.ViewPage> getPages() { return ((SettingsPageAdapater)_pages.getAdapter())._pages; }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(_content == null) {
			_content = inflater.inflate(R.layout.rdp_session,container,false);
			_pages = _content.findViewById(R.id.v_pages);
			_code = _content.findViewById(R.id.ed_code);
			_code.setOnTouchListener(new ScanTouchListener(this));
			_pages.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View arg0, MotionEvent arg1) {
					return true;
				}
			});
			_pageHeader = _content.findViewById(R.id.lbl_page_name);
			_pageHeader.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.rdp_config_pages)));
			_pages.setAdapter(new SettingsPageAdapater());
			_pageHeader.setOnItemSelectedListener(this);
			_rightArrow = _content.findViewById(R.id.iv_right);
			_rightArrow.setOnClickListener(this);
			_leftArrow = _content.findViewById(R.id.iv_left);
			_leftArrow.setOnClickListener(this);
			_pageHeader.setSelection(0);
			UIBinder.bind(getValue(), _content);
		}
		return _content;
	}
	
	@Override
	public void onStart() {
		getActivity().setTitle(R.string.rdp_session);
		super.onStart();
	}

	@Override
	public void onItemSelected(AdapterView<?> av, View v, int p, long arg3) {
		_leftArrow.setEnabled(p > 0);
		_rightArrow.setEnabled(p < _pageHeader.getAdapter().getCount()-1);
		_pages.setCurrentItem(p);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.iv_left:
			_pageHeader.setSelection(_pageHeader.getSelectedItemPosition()-1);
			break;
		case R.id.iv_right:
			_pageHeader.setSelection(_pageHeader.getSelectedItemPosition()+1);
			break;
			
		}
	}
	@Override
	protected boolean doSave(SessionConfig value) {
		if(!UIBinder.obtain(_content, getValue())) return false;
		for (int i=0;i<getPages().size();i++) {
			if(!getPages().get(i).obtain()) {
				_pageHeader.setSelection(i);
				return false;
			}
			
		}
		if(!value.store()) {
			Toast.makeText(getContext(), "Соединение с таким имененм уже существует", Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}

	@Override
	public void onBarcode(byte[] barcode) {
		_code.setText(new String(barcode));
		
	}

	@Override
	public boolean onBackPressed() {
		U.confirm(getContext(), "Сохранить изменения?", new Runnable() {
			@Override
			public void run() {
				if(doSave(getValue()))
					getFragmentManager().popBackStack();
			}
		}, new Runnable() {
			@Override
			public void run() {
				getFragmentManager().popBackStack();
			}
		});	
		return false;
	}

}

