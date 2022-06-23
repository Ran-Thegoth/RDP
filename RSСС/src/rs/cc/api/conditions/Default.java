package rs.cc.api.conditions;

import rs.cc.api.Barcode;

public class Default extends Condition {

	public Default() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean check(Barcode barcode) {
		return true;
	}

}
