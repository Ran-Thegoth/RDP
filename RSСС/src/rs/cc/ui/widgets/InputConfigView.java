package rs.cc.ui.widgets;


import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import cs.ui.MainActivity;
import cs.ui.UIView;
import cs.ui.fragments.BaseEditor.OnSaveListener;
import cs.ui.widgets.DialogSpinner;
import rs.cc.Const;
import rs.cc.Core;
import rs.cc.config.SessionConfig.InputConfig;
import rs.cc.config.SessionConfig.Scripting;
import rs.cc.ui.fragments.ConditionEditor;
import rs.cc.ui.fragments.HWKeySetup;
import rs.cc.ui.fragments.KeySetupFragment;
import rs.cc.R;

public class InputConfigView extends LinearLayout
		implements UIView, OnCheckedChangeListener, OnItemSelectedListener, View.OnClickListener, OnSaveListener<String>  {

	private View _keyboardSetup, _serial, _hwSetup;
	private InputConfig _config;
	private Scripting _sConfig;
	private String _bcScript = Const.EMPTY_STRING;
	private JSONObject _kbSettings,_hwSettings;
	public InputConfigView(Context context) {
		super(context);
	}

	public InputConfigView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public InputConfigView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public InputConfigView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		_keyboardSetup = findViewById(R.id.iv_key_setup);
		_keyboardSetup.setOnClickListener(this);
		_hwSetup = findViewById(R.id.iv_hwkey_setup);
		_hwSetup.setOnClickListener(this);
		_serial = findViewById(R.id.v_serial);
		((CompoundButton) findViewById(R.id.sw_hwkeyboard)).setOnCheckedChangeListener(this);
		((CompoundButton) findViewById(R.id.sw_keyboard)).setOnCheckedChangeListener(this);
		DialogSpinner sp = findViewById(R.id.lv_scaner_type);
		sp.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,
				getContext().getResources().getStringArray(R.array.scaner_type_names)));
		sp.setOnItemSelectedListener(this);
		sp = findViewById(R.id.lv_server_type);
//		findViewById(R.id.v_scenario).setOnClickListener(this);
		sp.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,
				getContext().getResources().getStringArray(R.array.scaner_con_types)));
		sp.setOnItemSelectedListener(this);
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View v, int p, long arg3) {
		Core.enableUI(_serial,p == 1);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	@Override
	public void onCheckedChanged(CompoundButton v, boolean on) {
		switch (v.getId()) {
		case R.id.sw_hwkeyboard:
			_hwSetup.setEnabled(on);
			break;
		case R.id.sw_keyboard:
			_keyboardSetup.setEnabled(on);
			break;
		}

	}

	@Override
	public void onBined(Object o) {
		if( o instanceof InputConfig) {
		_config = (InputConfig) o;
		Core.enableUI(_keyboardSetup, _config.keyboard);
		onItemSelected(null, findViewById(R.id.lv_server_type),_config.serverType,0);
		try {
			_kbSettings = new JSONObject(_config.kbSettings.toString());
			_hwSettings = new JSONObject(_config.hwSettings.toString());
		} catch(JSONException jse) {
			_kbSettings = new JSONObject();
			_hwSettings = new JSONObject();
		}
		} else if(o instanceof Scripting) {
			_sConfig = (Scripting)o;
			_bcScript = _sConfig.onBarcode;
		}
	}

	@Override
	public void onObtain(Object o) {
		if(o instanceof InputConfig) {
		_config = (InputConfig)o;
		try {
			_config.kbSettings = new JSONObject(_kbSettings.toString());
			_config.hwSettings = new JSONObject(_hwSettings.toString());
		} catch(JSONException jse) {
			_config.kbSettings = new JSONObject();
		}
		} else if(o instanceof Scripting){
			_sConfig = (Scripting)o;
			_sConfig.onBarcode = _bcScript;
		}
		
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.iv_key_setup:
			((MainActivity)getContext()).showFragment(KeySetupFragment.newInstance(_kbSettings));
			break;
		case R.id.iv_hwkey_setup:
			((MainActivity)getContext()).showFragment(HWKeySetup.newInstance(_hwSettings));
			break;
/*		case R.id.v_scenario:	
			((MainActivity)getContext()).showFragment(new ConditionEditor());
			break; */
		}
	}

	@Override
	public void onSaved(String value) {
		_bcScript = value;
		
	}



}

