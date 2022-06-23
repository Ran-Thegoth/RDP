package rs.cc.ui.widgets;

import java.util.Arrays;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import cs.ui.UIView;
import cs.ui.widgets.DialogSpinner;
import rs.cc.Core;
import rs.cc.config.SessionConfig.ScreenConfig;
import rs.cc.R;

public class ScreenSettingsView extends LinearLayout implements UIView,CompoundButton.OnCheckedChangeListener {

	private int [] BPP_VAL = {8,16,32};
	private DialogSpinner _rotation, _bpp;
	private View _dimensions;
	public ScreenSettingsView(Context context) {
		super(context);
	}

	public ScreenSettingsView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ScreenSettingsView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public ScreenSettingsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		_dimensions = findViewById(R.id.v_dimen);
		((CompoundButton)findViewById(R.id.sw_fullscreen)).setOnCheckedChangeListener(this);
		_bpp = findViewById(R.id.lv_bpp);
		_bpp.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,getContext().getResources().getStringArray(R.array.bpp_names)));
		_rotation = findViewById(R.id.lv_rotation);
		_rotation.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,getContext().getResources().getStringArray(R.array.rotation_names)));
	}

	@Override
	public void onBined(Object o) {
		Core.enableUI(_dimensions, !((ScreenConfig)o).fullScreen);
		_bpp.setSelection(Arrays.binarySearch(BPP_VAL, ((ScreenConfig)o).bpp));
	}

	@Override
	public void onObtain(Object o) {
		((ScreenConfig)o).bpp = BPP_VAL[_bpp.getSelectedItemPosition()];
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean v) {
		Core.enableUI(_dimensions, !v);
		
	}
}
