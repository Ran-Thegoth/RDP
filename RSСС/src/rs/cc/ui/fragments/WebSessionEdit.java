package rs.cc.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import cs.ui.UIBinder;
import cs.ui.fragments.BaseEditor;
import rs.cc.R;
import rs.cc.config.SessionConfig;
import rs.cc.config.web.WebSessionConfig;

public class WebSessionEdit extends BaseEditor<SessionConfig> {

	private View _content, _input;
	public static WebSessionEdit newInstance(WebSessionConfig cfg, OnSaveListener<SessionConfig> l) {
		WebSessionEdit result = new WebSessionEdit();
		result.setListener(l);
		result.setValue(cfg);
		return result;
	}
	public WebSessionEdit() {
	}

	@Override
	protected WebSessionConfig getValue() {
		return (WebSessionConfig)super.getValue();
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(_content == null) {
			_content = inflater.inflate(R.layout.web_session, container,false);
			UIBinder.bind(getValue(), _content);
			UIBinder.bind(getValue().connectionConfig(), _content);
			UIBinder.bind(getValue().features(),_content);
			_input = _content.findViewById(R.id.v_input);
			UIBinder.bind(getValue().inputConfig(),_input);
			
		}
		return _content;
	}
	@Override
	protected boolean doSave(SessionConfig value) {
		if(UIBinder.obtain(_content,((WebSessionConfig)value).features()) &&  UIBinder.obtain(_input, value.inputConfig()) && UIBinder.obtain(_content, value.connectionConfig())
				&& UIBinder.obtain(_content, value)) {
			if(!value.store()) {
				Toast.makeText(getContext(), "Соединение с таким имененм уже существует", Toast.LENGTH_LONG).show();
				return false;
			}
			return true;

		}
		return false;
	}

}
