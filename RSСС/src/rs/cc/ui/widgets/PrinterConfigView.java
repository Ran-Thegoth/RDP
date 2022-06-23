package rs.cc.ui.widgets;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import cs.ui.UIView;
import rs.cc.Core;
import rs.cc.config.SessionConfig.PrinterConfig;
import rs.cc.misc.BluetoothDeviceList;
import rs.cc.R;

public class PrinterConfigView extends LinearLayout implements UIView,OnCheckedChangeListener, View.OnTouchListener {

	private View _pInfo;
	private TextView _address;
	public PrinterConfigView(Context context) {
		super(context);
	}

	public PrinterConfigView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PrinterConfigView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public PrinterConfigView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		_pInfo = findViewById(R.id.v_printer);
		_address = findViewById(R.id.ed_printer_address);
		_address.setOnTouchListener(this);
		((Switch)findViewById(R.id.sw_printer)).setOnCheckedChangeListener(this);
	}
	
	@Override
	public void onBined(Object o) {
		onCheckedChanged(null,((PrinterConfig)o).enabled);
	}

	@Override
	public void onObtain(Object o) throws Exception {
		PrinterConfig c = (PrinterConfig)o;
		if(c.enabled  && ((c.deviceAddress == null) || c.deviceAddress.isEmpty())) {
			Toast.makeText(getContext(), R.string.field_not_set, Toast.LENGTH_LONG).show();
			_address.requestFocus();
			throw new Exception();
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean on) {
		Core.enableUI(_pInfo, on);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent e) {
		 if(e.getAction() == MotionEvent.ACTION_UP) {
	            if(e.getRawX() >= ((TextView)v).getRight() - ((TextView)v).getTotalPaddingRight()) {
	            	AlertDialog.Builder b = new AlertDialog.Builder(v.getContext());
	            	b.setAdapter(new BluetoothDeviceList(v.getContext()), new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dlg, int p) {
							BluetoothDevice d = (BluetoothDevice)((AlertDialog)dlg).getListView().getAdapter().getItem(p);
							_address.setText(d.getAddress());
						}
	            	});
	            	b.show();
	            	return true;
	            }
		 }
		return false;
	}

}
