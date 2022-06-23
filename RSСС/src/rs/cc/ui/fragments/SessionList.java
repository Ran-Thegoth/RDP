package rs.cc.ui.fragments;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.developer.filepicker.controller.DialogSelectionListener;
import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cs.U;
import cs.ui.UIBinder;
import cs.ui.fragments.BaseEditor;
import cs.ui.fragments.BaseFragment;
import rs.cc.Const;
import rs.cc.Core;
import rs.cc.config.SessionConfig;
import rs.cc.misc.ClassORMHelper;
import rs.cc.R;

public class SessionList extends BaseFragment implements OnClickListener, BaseEditor.OnSaveListener<SessionConfig>,
		DialogInterface.OnClickListener, DialogSelectionListener {

	private RecyclerView _list;
	private AlertDialog _menu;
	private boolean _doSave;

	public SessionList() {
	}

	private class SessionConfigHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		private SessionConfig _config;
		private Const.RemoteConnectionType<?> _type;
		private ImageView _icon, _edit, _delete;
		private TextView _last;

		public SessionConfigHolder(View itemView) {
			super(itemView);
			itemView.setOnClickListener(this);
			_icon = itemView.findViewById(R.id.iv_icon);
			_edit = itemView.findViewById(R.id.iv_edit);
			_last = itemView.findViewById(R.id.lbl_last_con);
			_delete = itemView.findViewById(R.id.iv_delete);
			_edit.setOnClickListener(this);
			_delete.setOnClickListener(this);

		}

		public void update(SessionConfig config) {
			UIBinder.bind(config, itemView);
			_type = Core.getKnownTypes().get(config.getClass());
			_config = config;
			_icon.setImageResource(_config.getIconId());
			_edit.setVisibility(Core.getInstance().sysConfig().isAdminMode() ? View.VISIBLE : View.GONE);
			_delete.setVisibility(_edit.getVisibility());
			_last.setText(getString(R.string.last_connection) + "\n" + _config.getLastSuccessSession());
		}

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.v_session_card:
				if (_config.isConnected())
					_config.disconnect();
				else
					_config.connect(getContext());
				break;
			case R.id.iv_edit:
				showFragment(_type.editor(_config, SessionList.this));
				break;
			case R.id.iv_delete:
				U.confirm(getContext(), R.string.sure_to_delete, new Runnable() {
					@Override
					public void run() {
						_config.disconnect();
						if (_config.delete()) {
							configs().remove(_config);
							_list.getAdapter().notifyDataSetChanged();
						}

					}
				});
				break;
			}
		}
	}

	private class SessionConfigAdapter extends RecyclerView.Adapter<SessionConfigHolder> {
		private List<SessionConfig> _configs = Core.getKnownConfig();

		@Override
		public int getItemCount() {
			return _configs.size();
		}

		@Override
		public void onBindViewHolder(SessionConfigHolder vh, int p) {
			vh.update(_configs.get(p));
		}

		@Override
		public SessionConfigHolder onCreateViewHolder(ViewGroup vg, int arg1) {
			return new SessionConfigHolder(getActivity().getLayoutInflater().inflate(R.layout.session_card, vg, false));
		}
		public void refresh() {
			notifyDataSetChanged();
		}

	}

	private List<SessionConfig> configs() {
		return _adapter._configs;
	}

	private SessionConfigAdapter _adapter;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (_list == null) {
			_list = new RecyclerView(getContext());
			_list.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
			_adapter = new SessionConfigAdapter();
			_list.setAdapter(_adapter);
			AlertDialog.Builder b = new AlertDialog.Builder(getContext());
			b.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,
					getResources().getStringArray(R.array.main_menu)), this);
			_menu = b.create();
		}
		return _list;
	}

	@Override
	public void onStart() {
		super.onStart();
		getActivity().setTitle(R.string.app_name);
		setCustomButtom(Core.getInstance().sysConfig().isAdminMode() ? R.drawable.ic_menu_settings_w
				: R.drawable.ic_menu_admin_mode, this);
		if (Core.getInstance().sysConfig().isAdminMode())
			setupButtons(this, R.id.iv_add, R.id.iv_more);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_add:
			if (Core.getKnownTypes().size() > 1) {
				AlertDialog.Builder b = new AlertDialog.Builder(getContext());
				final ArrayAdapter<Const.RemoteConnectionType<?>> adapter = new ArrayAdapter<>(getContext(),
						android.R.layout.simple_list_item_1);
				adapter.addAll(Core.getKnownTypes().values());
				b.setAdapter(adapter, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int p) {
						Const.RemoteConnectionType<?> t = adapter.getItem(p);
						showFragment(t.editor(t.newInstance(), SessionList.this));
					}
				});
				b.show();
			} else {
				Const.RemoteConnectionType<?> t = Core.getKnownTypes().values().iterator().next();
				showFragment(t.editor(t.newInstance(), this));
			}
			break;
		case R.id.iv_more:
			_menu.show();
			break;
		case R.id.iv_custom:
			if (!Core.getInstance().sysConfig().isAdminMode()) {
				showFragment(PinFragment.newInstance(new PinFragment.PINLocked() {
					@Override
					public String getTitle() {
						return getString(R.string.enter_admin_pin);
					}

					@Override
					public boolean checkPin(String pin) {
						if (pin.equals(Core.getInstance().sysConfig().PIN)) {
							Core.getInstance().sysConfig().setAuthed();
							_list.getAdapter().notifyDataSetChanged();
							return true;
						}
						return false;
					}
				}));
			}
			break;
		}
	}

	@Override
	public void onSaved(SessionConfig value) {
		if (!configs().contains(value))
			configs().add(value);
		_adapter.refresh();

	}

	@Override
	public void onResume() {
		super.onResume();
		_adapter.refresh();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(DialogInterface arg0, int p) {
		switch (p) {
		case 0:
			showFragment(new SysSettingsFragment());
			break;
		case 1:
		case 3:
			_doSave = p == 1;
			DialogProperties props = new DialogProperties();
			props.save_mode = _doSave;
			props.root = Environment.getExternalStorageDirectory();
			FilePickerDialog dlg = new FilePickerDialog(getContext(), props);
			dlg.setDialogSelectionListener(this);
			dlg.show();
			break;
		case 2:
			final SessionConfigAdapter a = (SessionConfigAdapter) _list.getAdapter();
			if (!a._configs.isEmpty())
				U.confirm(getContext(), R.string.clear_all_sessions, new Runnable() {
					@Override
					public void run() {
						while (!a._configs.isEmpty()) {
							if (a._configs.get(0).delete())
								a._configs.remove(0);
						}
						_adapter.refresh();
					}
				});
			break;
		}

	}

	@Override
	public void onSelectedFilePaths(FilePickerDialog dlg, String [] files) {
		try {
			SessionConfigAdapter a = (SessionConfigAdapter) _list.getAdapter();
			if (_doSave) {
				JSONObject result = new JSONObject();
				JSONObject o = new JSONObject();
				ClassORMHelper.storeFields(o, Core.getInstance().sysConfig());
				result.put("system", o);
				
				JSONArray sessions = new JSONArray();
				for (SessionConfig cfg : a._configs) {
					o = new JSONObject();
					ClassORMHelper.storeFields(o, cfg);
					sessions.put(o);
				}
				result.put("sessions", sessions);
				FileOutputStream fos = new FileOutputStream(files[0]);
				fos.write(result.toString(0).getBytes());
				fos.close();
				Toast.makeText(getContext(), R.string.file_saved, Toast.LENGTH_SHORT).show();
			} else {
				FileInputStream fis = new FileInputStream(files[0]);
				byte [] b = new byte[fis.available()];
				fis.read(b);
				fis.close();
				JSONObject json = new JSONObject(new String(b));
				if(json.has("system")) {
					ClassORMHelper.restoreFields(json.getJSONObject("system"), Core.getInstance().sysConfig());
					Core.getInstance().sysConfig().store();
					Core.getInstance().rebuildScaner();
				}
				while (!a._configs.isEmpty()) {
					if (a._configs.get(0).delete())
						a._configs.remove(0);
				}
				if(json.has("sessions")) {
					JSONArray sessions = json.getJSONArray("sessions");
					for(int i=0;i<sessions.length();i++) {
						JSONObject session = sessions.getJSONObject(i);
						if(session.has(Const.TYPE_FIELD)) {
							SessionConfig cfg = Core.getClassForType(session.getInt(Const.TYPE_FIELD));
							if(cfg != null) {
								ClassORMHelper.restoreFields(session, cfg);
								if(cfg.store())
									a._configs.add(cfg);
							}
								
						}
					}
				}
				_adapter.refresh();
					
			}
		} catch (Exception e) {
			Log.e("RDP", "File error ", e);
			Toast.makeText(getContext(), R.string.file_error, Toast.LENGTH_LONG).show();
		}

	}
}
