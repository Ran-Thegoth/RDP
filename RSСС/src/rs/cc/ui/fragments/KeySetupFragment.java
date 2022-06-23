package rs.cc.ui.fragments;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import cs.ui.fragments.BaseEditor;
import rs.cc.R;
import rs.keyboard.FloatingKeyboard;

public class KeySetupFragment extends BaseEditor<JSONObject> {
	private int [] _locked = {};
	private View _content;
	
	public static KeySetupFragment newInstance(JSONObject o, int...locked) {
		KeySetupFragment result = new KeySetupFragment();
		result._locked = locked;
		result.setValue(o);
		return result;
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(_content == null) {
			_content = inflater.inflate(R.layout.base_keyboard_setup, container,false);
			CompoundButton sw = _content.findViewById(R.id.sw_meta_keys);
			try {
				if(getValue().has(FloatingKeyboard.MOD_KEYS_TAG))
					sw.setChecked(getValue().getBoolean(FloatingKeyboard.MOD_KEYS_TAG));
				sw.setEnabled(isLocked(_locked,sw.getId()));
				sw.setEnabled(isLocked(_locked,sw.getId()));
				sw.setEnabled(isLocked(_locked,sw.getId()));
				JSONArray a = getValue().getJSONArray(FloatingKeyboard.LAYOUTS_TAG);
				for(int i=0;i<a.length();i++) {
					if("en".equals(a.getString(i)))
						((Switch)_content.findViewById(R.id.sw_en)).setChecked(true);
					else if("ru".equals(a.getString(i)))
						((Switch)_content.findViewById(R.id.sw_ru)).setChecked(true);
					else if("fn".equals(a.getString(i)))
						((Switch)_content.findViewById(R.id.sw_func)).setChecked(true);
				}
			} catch(Exception e) {
			}
			
		}
		return _content;
	}

	private boolean isLocked(int [] ids, int id) {
		for(int i=0;i<ids.length;i++)
			if(id == ids[i]) return false;
		return true;
	}
	@Override
	protected boolean doSave(JSONObject value) {
		try {
			CompoundButton sw = _content.findViewById(R.id.sw_meta_keys);
			value.put(FloatingKeyboard.MOD_KEYS_TAG, sw.isChecked());
			JSONArray a = new JSONArray();
			if(((Switch)_content.findViewById(R.id.sw_en)).isChecked())
				a.put("en");
			if(((Switch)_content.findViewById(R.id.sw_ru)).isChecked())
				a.put("ru");
			if(((Switch)_content.findViewById(R.id.sw_func)).isChecked())
				a.put("fn");
			value.put(FloatingKeyboard.LAYOUTS_TAG, a);
			return true;
		} catch(JSONException jse) {
			Log.e("KB", "Ошибка конфигурации",jse);
			return false;
		}
	}
	@Override
	public void onStart() {
		super.onStart();
		getActivity().setTitle(R.string.keyboard);
	}

}
