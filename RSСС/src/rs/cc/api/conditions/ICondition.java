package rs.cc.api.conditions;

import rs.cc.api.Barcode;

public interface ICondition {
	public boolean check(Barcode barcode);
}
