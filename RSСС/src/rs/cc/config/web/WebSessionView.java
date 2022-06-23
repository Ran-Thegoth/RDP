package rs.cc.config.web;




import android.content.Context;
import android.view.View;
import rs.cc.ui.widgets.SessionView;

public class WebSessionView extends SessionView {

	public WebSessionView(Context context) {
		super(context);
	}

	@Override
	public void onKeyboardShown(boolean visible) {
	}

	@Override
	public View getView() {
		return null;
	}
	

}
