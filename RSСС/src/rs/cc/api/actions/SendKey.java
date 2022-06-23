package rs.cc.api.actions;

import rs.cc.api.Barcode;
import rs.cc.ui.SessionActivity;

public class SendKey implements IAction {

	private int _key;
	private int _meta;
	public SendKey() {
	}

	@Override
	public boolean execute(Barcode BARCODE, SessionActivity a) {
		a.processKey(_key, _meta);
		return true;
	}

}
