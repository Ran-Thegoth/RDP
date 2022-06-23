package rs.cc.api.conditions;

import rs.cc.api.Barcode;

public class EndWith extends Condition {

	public EndWith() {
	}

	@Override
	public boolean check(Barcode barcode) {
		if(_what.length == 0) return false;
		byte [] bcode = barcode.get();
		int bI = bcode.length, wI = _what.length;
		for(;;) {
			if(wI < 0) break;
			if(bI < 0) return false;
			if(bcode[bI--] != _what[wI--]) return false;
		}
		return true;
	}

}
