package rs.cc.api.actions;

import rs.cc.api.Barcode;
import rs.cc.ui.SessionActivity;

public interface IAction {
	public boolean execute(Barcode BARCODE,SessionActivity a);
}
