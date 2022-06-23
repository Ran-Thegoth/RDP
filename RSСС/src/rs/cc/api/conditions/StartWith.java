package rs.cc.api.conditions;

import rs.cc.api.Barcode;

public class StartWith extends Condition {

	public StartWith() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean check(Barcode barcode) {
		if(_what.length == 0) return false;
		byte [] bcode = barcode.get();
		for(int i=0;i<_what.length;i++) {
			if(bcode.length == i) return false;
			if(bcode[i] != _what[i])
				return false;
		}
		return true;
	}

}
