package rs.cc.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import cs.ui.UIView;
import cs.ui.widgets.DialogSpinner;
import rs.cc.Core;
import rs.cc.R;

public class RDPNetworkConfigView extends LinearLayout  implements CompoundButton.OnCheckedChangeListener,UIView {


	private View _gwSettinsg;
	private CompoundButton _gwSwitch;
	public RDPNetworkConfigView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public RDPNetworkConfigView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public RDPNetworkConfigView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public RDPNetworkConfigView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		_gwSettinsg = findViewById(R.id.v_gw);
		_gwSwitch = findViewById(R.id.sw_use_gw);
		_gwSwitch.setOnCheckedChangeListener(this);
		DialogSpinner sp = findViewById(R.id.lv_enc);
		sp.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,getContext().getResources().getStringArray(R.array.enc_types)));
	}

	@Override
	public void onBined(Object o) {
		Core.enableUI(_gwSettinsg,_gwSwitch.isChecked());
	}

	@Override
	public void onObtain(Object o) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean v) {
		Core.enableUI(_gwSettinsg, v);
		
	}
}
