package rs.cc.ui.fragments;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import cs.ui.fragments.BaseEditor;
import rs.cc.Const;
import rs.cc.R;
import rs.cc.misc.Utils;
import rs.cc.ui.dialogs.KeyInputDialog;
import rs.cc.ui.dialogs.KeyInputDialog.OnKeySelected;

public class HWKeySetup extends BaseEditor<JSONObject> implements View.OnClickListener, OnKeySelected {
	private View _content;

	private class KeyAdapter extends BaseAdapter implements View.OnClickListener {
		private JSONArray _keys = new JSONArray();
		private int _selected = -1;

		private KeyAdapter() {
			try {
				if (getValue().has(Const.IGNORED_KEYS_TAG))
					_keys = getValue().getJSONArray(Const.IGNORED_KEYS_TAG);
			} catch (JSONException jse) {

			}
			_selected = _keys.length() > 0 ? 0 :-1; 
		}

		@Override
		public int getCount() {
			return _keys.length();
		}

		@Override
		public Integer getItem(int p) {
			try {
				return _keys.getInt(p);
			} catch (JSONException jse) {
				return 0;
			}
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@SuppressWarnings("deprecation")
		@Override
		public View getView(int p, View v, ViewGroup vg) {
			if (v == null) {
				v = getActivity().getLayoutInflater().inflate(android.R.layout.simple_list_item_1, vg, false);
				v.setOnClickListener(this);
			}
			if (p == _selected) {
				v.setBackgroundColor(getContext().getResources().getColor(R.color.blue));
				((TextView)v).setTextColor(Color.WHITE);
			} else {
				((TextView)v).setTextColor(Color.BLACK);
				if (p % 2 == 0)
					v.setBackgroundColor(getContext().getResources().getColor(R.color.odd_color));
				else
					v.setBackgroundColor(Color.TRANSPARENT);
			}
			try {
				((TextView)v).setText(Utils.keyName(_keys.getInt(p)));
			} catch(JSONException jse) { }
			v.setTag(p);
			return v;

		}

		@Override
		public void onClick(View v) {
			_selected = ((Number) v.getTag()).intValue();
			notifyDataSetInvalidated();
		}

	}

	private KeyAdapter _adapter;

	public static HWKeySetup newInstance(JSONObject o) {
		HWKeySetup result = new HWKeySetup();
		result.setValue(o);
		return result;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (_content == null) {
			_content = inflater.inflate(R.layout.hw_keyboard_setup, container, false);
			_content.findViewById(R.id.iv_add).setOnClickListener(this);
			_content.findViewById(R.id.iv_del).setOnClickListener(this);
			_adapter = new KeyAdapter();
			((ListView) _content.findViewById(R.id.lv_ignored)).setAdapter(_adapter);
		}
		return _content;
	}

	@Override
	public void onStart() {
		super.onStart();
		getActivity().setTitle(R.string.hw_keyboard);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_add:
			new KeyInputDialog(getContext()).show(this);
			break;
		case R.id.iv_del:
			if(_adapter._selected != -1) try {
				_adapter._keys.remove(_adapter._selected);
				if(--_adapter._selected < 0 && _adapter._keys.length() > 0 )
					_adapter._selected = 0;
				_adapter.notifyDataSetChanged();	
			} catch(Exception e) {
				
			}
			break;
		}
	}

	@Override
	protected boolean doSave(JSONObject value) {
		try {
			value.put(Const.IGNORED_KEYS_TAG, _adapter._keys);
			return true;
		} catch (JSONException jse) {
			return false;
		}
	}

	@Override
	public void onKeySelected(int code) {
		_adapter._keys.put(code);
		_adapter._selected = _adapter._keys.length()-1; 
		_adapter.notifyDataSetChanged();
		
	}
}
