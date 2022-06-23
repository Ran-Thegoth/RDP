package rs.cc.config;

import cs.orm.DBField;
import cs.orm.DBObject;
import cs.orm.DBTable;
import cs.ui.annotations.BindTo;
import rs.cc.Const;
import rs.cc.misc.ClassORMHelper;
import rs.cc.misc.NoJSON;
import rs.cc.R;


@DBTable(name="SYSCONFIG",unique= {},indeces = {})
public class SystemConfig extends DBObject {

	@DBField(name="PINMODE")
	@BindTo(ui= {R.id.sw_pinmode})
	public boolean PINMode = false;
	@DBField(name="PIN")
	@BindTo(ui= {R.id.ed_a_pin})
	public String PIN = Const.EMPTY_STRING;
	
	@NoJSON
	private boolean _authed;
	
	@DBField(name="SCANER",type="TEXT",getter=ClassORMHelper.class,setter=ClassORMHelper.class)
	public ScanerConfig systemScanner = new ScanerConfig();
	public SystemConfig() {
	}
	public boolean isAdminMode() { return PIN.isEmpty() || _authed; }
	public void setAuthed() { _authed = true; }


}
