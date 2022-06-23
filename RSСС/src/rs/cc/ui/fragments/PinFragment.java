package rs.cc.ui.fragments;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cs.ui.fragments.BaseFragment;
import cs.ui.widgets.PinPad;
import rs.cc.Core;
import rs.cc.hardware.BarcodeScaner.BarcodeListener;
import rs.cc.misc.ScanTouchListener;
import rs.cc.R;

public class PinFragment extends BaseFragment implements PinPad.OnPinChangedListener, BarcodeListener {

	
	public static interface PINLocked {
		public boolean checkPin(String pin);
		public String getTitle();
	}
	
	private PINLocked _locked;
	private PinPad _ppad;
	private LinearLayout _content;
	private TextView _pin;
	public static PinFragment newInstance(PINLocked l) {
		PinFragment result = new PinFragment();
		result._locked = l;
		return result;
	}
	public PinFragment() {
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(_content == null) {
			_content = new LinearLayout(getContext());
			_content.setPadding(0, 6, 0, 0);
			_content.setGravity(Gravity.CENTER_HORIZONTAL);
			_ppad = (PinPad)inflater.inflate(R.layout.pinpad, container,false);
			_ppad.setOnPinChangedListener(this);
			_pin = _ppad.findViewById(R.id.lbl_pin);
			if(Core.getInstance().barcodeScaner(getContext()).isManual())
				_pin.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_menu_qr, 0);
			_pin.setOnTouchListener(new ScanTouchListener(this));
			_content.addView(_ppad);
		}
		return _content;
	}
	@Override
	public void onStart() {
		super.onStart();
		getActivity().setTitle(_locked.getTitle());
		if(!Core.getInstance().barcodeScaner(getContext()).isManual()) {
			Core.getInstance().barcodeScaner(getContext()).startScan(this);
			Toast.makeText(getContext(), R.string.you_can_scan_code, Toast.LENGTH_LONG).show();
		}
	}
	@Override
	public void onPinChanged(String pin) {
		if(_locked.checkPin(pin)) 
			getFragmentManager().popBackStack();
	}
	@Override
	public void onBarcode(byte[] barcode) {
		_ppad.setPin(new String(barcode));
		
	}
	@Override
	public void onStop() {
		if(!Core.getInstance().barcodeScaner(getContext()).isManual())
			Core.getInstance().barcodeScaner(getContext()).stopScan();
		super.onStop();
	}

	
}
