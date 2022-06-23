package rs.cc.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import cs.ui.UIBinder;
import cs.ui.fragments.BaseEditor;
import rs.cc.config.SessionConfig;
import rs.cc.config.vnc.VNCSessionConfig;
import rs.cc.R;

public class VNCSessionEdit extends BaseEditor<SessionConfig> {
	private View _content;
	public static VNCSessionEdit newInstance(VNCSessionConfig config, OnSaveListener<SessionConfig> l) {
		VNCSessionEdit result = new VNCSessionEdit();
		result.setListener(l);
		result.setValue(config);
		return result;
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(_content == null) {
			_content = inflater.inflate(R.layout.elm_session, container,false);
			UIBinder.bind(getValue(), _content);
			UIBinder.bind(getValue().connectionConfig(),_content);
		}
		return _content;
	}
	@Override
	public void onStart() {
		super.onStart();
		getActivity().setTitle(R.string.vnc_session);
	}
	@Override
	protected boolean doSave(SessionConfig value) {
		if(UIBinder.obtain(_content, getValue()) && UIBinder.obtain(_content, getValue().connectionConfig())) {
			if(!value.store()) {
				Toast.makeText(getContext(), "Соединение с таким имененм уже существует", Toast.LENGTH_LONG).show();
				return false;
			}
			return true;
		}
		return false;
					
	}
}
