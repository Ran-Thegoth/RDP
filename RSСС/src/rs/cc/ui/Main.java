package rs.cc.ui;

import android.os.Bundle;
import cs.U;
import cs.orm.ORMHelper;
import cs.ui.MainActivity;
import rs.cc.Const;
import rs.cc.Core;
import rs.cc.api.BarcodeProcessor;
import rs.cc.config.SessionConfig;
import rs.cc.ui.fragments.PinFragment;
import rs.cc.ui.fragments.SessionList;
import rs.cc.ui.fragments.PinFragment.PINLocked;
import rs.cc.R;

public class Main extends MainActivity  {

	private static final String [] PERMISSIONS = { android.Manifest.permission.WAKE_LOCK,
			android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.INTERNET,
			android.Manifest.permission.BLUETOOTH,android.Manifest.permission.BLUETOOTH_ADMIN,
			android.Manifest.permission.ACCESS_NETWORK_STATE};
	public Main() {
	}
	private PINLocked UNLOCKER = new PINLocked() {
		@Override
		public String getTitle() {
			return getString(R.string.enter_pin);
		}
		
		@Override
		public boolean checkPin(String pin) {
			if(pin.length() < 4) return false;
			if(pin.equals(Core.getInstance().sysConfig().PIN)) {
				Core.getInstance().sysConfig().setAuthed();
				setFragment(new SessionList());
				return false;
			} else try {
				SessionConfig cfg = ORMHelper.load(SessionConfig.class, U.pair(Const.CODE_FIELD, pin));
				if(cfg != null) {
					cfg.connect(Main.this);
					finish();
					return false;
				}
			} catch(Exception e) {
				
			}
			BarcodeProcessor.beepError();
			return false;
		}
	};
	@Override
	protected void onCreate(Bundle saved) {
		setRequiredPermissions(PERMISSIONS);
		super.onCreate(saved);
		disableSideMenu();
		
	}
	@Override
	protected void doExit() {
		U.confirm(this, R.string.confirm_exit, new Runnable() {
			@Override
			public void run() {
				finish();
				Runtime.getRuntime().exit(0);
			}
		});
	}
	
	@Override
	protected void onNewInstance() {
		if(!Core.getInstance().sysConfig().isAdminMode() &&
				Core.getInstance().sysConfig().PINMode)
			setFragment(PinFragment.newInstance(UNLOCKER));
		else
			setFragment(new SessionList());
	}
}
