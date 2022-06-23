package rs.cc.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import cs.U;
import cs.orm.ORMHelper;
import cs.ui.UIBinder;
import cs.ui.fragments.BaseFragment;
import rs.cc.Const;
import rs.cc.Core;
import rs.cc.config.SessionConfig;
import rs.cc.hardware.ContextOwner;
import rs.cc.hardware.BarcodeScaner.BarcodeListener;
import rs.cc.misc.ScanTouchListener;
import rs.cc.misc.ScannerConfigHelper;
import rs.cc.R;

public class SysSettingsFragment extends BaseFragment implements TextWatcher, View.OnClickListener, BarcodeListener, ContextOwner{

	private View _content;
	private TextView _pin;
	private View _pinMode;
	private ScannerConfigHelper _sHelper;
	public SysSettingsFragment() {
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(_content == null) {
			_content = inflater.inflate(R.layout.system_config, container,false);
			_pinMode = _content.findViewById(R.id.sw_pinmode);
			_pin = _content.findViewById(R.id.ed_a_pin);
			_pin.addTextChangedListener(this);
			_pin.setOnTouchListener(new ScanTouchListener(this));
			_sHelper = new ScannerConfigHelper(Core.getInstance().sysConfig().systemScanner, null, _content);
			UIBinder.bind(Core.getInstance().sysConfig(), _content);
			_pinMode.setEnabled(_pin.getText().length() > 0);
		}
		return _content;
	}
	
	
	@Override
	public void onStart() {
		super.onStart();
		setupButtons(this, R.id.iv_save);
	}

	@Override
	public void onClick(View arg0) {
		if(_pin.getText().length() > 0 && _pin.getText().length() < 4) {
			Toast.makeText(getContext(), R.string.pin_to_short, Toast.LENGTH_LONG).show();
			_pin.requestFocus();
			return;
		}
		if(UIBinder.obtain(getView(), Core.getInstance().sysConfig())&&
		   UIBinder.obtain(getView(), Core.getInstance().sysConfig().systemScanner) &&	 	
		   Core.getInstance().sysConfig().store()) {
			Core.getInstance().rebuildScaner();
			getFragmentManager().popBackStack();
		}
		
	}

	@Override
	public void afterTextChanged(Editable arg0) {
		_pinMode.setEnabled(_pin.getText().length() > 3);
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBarcode(byte[] barcode) {
		try {
			if(ORMHelper.load(SessionConfig.class, U.pair(Const.CODE_FIELD, new String(barcode)))!=null) {
				Toast.makeText(getContext(), R.string.code_already_use, Toast.LENGTH_LONG).show();
				return;
			}
		} catch(Exception e) {
			
		}
		_pin.setText(new String(barcode));
	}

}
