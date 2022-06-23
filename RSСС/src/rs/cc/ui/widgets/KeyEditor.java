package rs.cc.ui.widgets;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class KeyEditor extends LinearLayout {

	private RecyclerView _list;
	public KeyEditor(Context context) {
		super(context);
	}

	public KeyEditor(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public KeyEditor(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public KeyEditor(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
	}
}
