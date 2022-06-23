package rs.cc.ui.widgets.vnc;

import android.content.Context;
import android.widget.ImageView;
import rs.cc.ui.widgets.SessionView;

public class VNCSessionView extends SessionView {

	private ImageView _view;
	public VNCSessionView(Context context) {
		super(context);
		
	}
	@Override
	protected void initSessionView(Context context) {
		_view = new ImageView(context);
		super.initSessionView(context);
	}

	@Override
	public void onKeyboardShown(boolean visible) {
	}

	@Override
	public ImageView getView() {
		return _view;
	}

}
