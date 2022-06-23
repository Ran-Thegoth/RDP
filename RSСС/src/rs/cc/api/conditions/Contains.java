package rs.cc.api.conditions;


import rs.cc.api.Barcode;

public class Contains extends Condition {

	public Contains() {
	}

	@Override
	public boolean check(Barcode barcode) {
		return barcode.contains(_what);
	}


}
