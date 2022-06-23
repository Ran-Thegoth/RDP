package rs.cc.misc;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import cs.ui.fragments.BaseEditor.OnSaveListener;
import cs.ui.widgets.DialogSpinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemSelectedListener;
import rs.cc.Core;
import rs.cc.config.ScanerConfig;
import rs.cc.config.SessionConfig.Scripting;
import rs.cc.R;

public class ScannerConfigHelper implements View.OnClickListener,OnTouchListener, OnSaveListener<String>,OnItemSelectedListener {

	private View _scaner, _scanerAddress;
	private TextView _btAddress,_scenario;
	private String _script;
	public ScannerConfigHelper(ScanerConfig config, Scripting scripts, View v) {
		_scaner = v.findViewById(R.id.v_scaner);
		_scanerAddress = v.findViewById(R.id.v_bt_scaner);
		_btAddress = v.findViewById(R.id.ed_scaner_address);
		_btAddress.setInputType(InputType.TYPE_NULL);
		_btAddress.setOnTouchListener(this);
//		_scenario = v.findViewById(R.id.v_scenario);
		if(_scenario != null && scripts!= null) {
			_scenario.setOnClickListener(this);
			_script = scripts.onBarcode;
			if(_script != null || !_script.isEmpty())
				_scenario.setTypeface(Typeface.DEFAULT_BOLD);
			else 
				_scenario.setTypeface(Typeface.DEFAULT);
		}
		DialogSpinner sp = v.findViewById(R.id.lv_scaner_type);
		sp.setAdapter(new ArrayAdapter<String>(v.getContext(), android.R.layout.simple_list_item_1,
				v.getContext().getResources().getStringArray(R.array.scaner_type_names)));
		sp.setSelection(config.scanerType);
		sp.setOnItemSelectedListener(this);
		onItemSelected(null, sp, config.scanerType,0);
		
	}
	
	@Override
	public void onClick(View v) {
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
							_btAddress.setText(d.getAddress());
						}
	            	});
	            	b.show();
	            	return true;
	            }
		 }
		return false;
	}
	@Override
	public void onSaved(String value) {
		_script = value;
		if(_script != null || !_script.isEmpty())
			_scenario.setTypeface(Typeface.DEFAULT_BOLD);
		else 
			_scenario.setTypeface(Typeface.DEFAULT);
	}
	public void disableScenario() {
		if(_scenario !=null)
			_scenario.setVisibility(View.GONE);
	}
	public String script() { return _script; }
	
	@Override
	public void onItemSelected(AdapterView<?> arg0, View v, int p, long arg3) {
		Core.enableUI(_scaner, p != 0);
		switch (p) {
		case 1:
			_scanerAddress.setVisibility(View.GONE);
			break;
		case 3:	
			_scanerAddress.setVisibility(View.GONE);
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if(v.getContext().checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
					((Activity)v.getContext()).requestPermissions(new String [] {android.Manifest.permission.CAMERA},9000);
			}
			break;
		case 2:
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				ArrayList<String> request = new ArrayList<>();
				if(v.getContext().checkSelfPermission(android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED)
					request.add(android.Manifest.permission.BLUETOOTH);
				if(v.getContext().checkSelfPermission(android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED)
					request.add(android.Manifest.permission.BLUETOOTH_ADMIN);
				if(!request.isEmpty())
					((Activity)v.getContext()).requestPermissions(request.toArray(new String[request.size()]), 9000);
			}
			_scanerAddress.setVisibility(View.VISIBLE);
			break;
		}
		
	}
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

}
