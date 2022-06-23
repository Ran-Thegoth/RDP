package rs.cc.ui.widgets;

import com.developer.filepicker.controller.DialogSelectionListener;
import com.developer.filepicker.model.DialogConfigs;
import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import cs.ui.UIView;
import rs.cc.config.SessionConfig.StorageConfig;
import rs.cc.R;

public class StorageSettingsView extends LinearLayout implements UIView, CompoundButton.OnCheckedChangeListener, View.OnTouchListener, DialogSelectionListener {

	private TextView _path;
	
	public StorageSettingsView(Context context) {
		super(context);
	}

	public StorageSettingsView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public StorageSettingsView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public StorageSettingsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent e) {
		 if(e.getAction() == MotionEvent.ACTION_UP) {
	            if(e.getRawX() >= ((TextView)v).getRight() - ((TextView)v).getTotalPaddingRight()) {
	            	DialogProperties pr = new DialogProperties();
	            	pr.root = Environment.getExternalStorageDirectory();
	            	pr.selection_type = DialogConfigs.DIR_SELECT;
	            	FilePickerDialog dlg = new FilePickerDialog(getContext(), pr);
	            	dlg.setDialogSelectionListener(this);
	            	dlg.show();
	            	return true;
	            }
		 }
		return false;
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean v) {
		_path.setEnabled(v);
	}

	@Override
	public void onBined(Object o) {
		_path.setEnabled(((StorageConfig)o).mount);
	}

	@Override
	public void onObtain(Object o) {
	}
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		_path = findViewById(R.id.ed_root_folder);
		_path.setOnTouchListener(this);
		((CompoundButton)findViewById(R.id.sw_storage)).setOnCheckedChangeListener(this);
	}

	@Override
	public void onSelectedFilePaths(FilePickerDialog dlg, String [] files) {
		if(files != null && files.length > 0)
			_path.setText(files[0]);
		
	}

}
