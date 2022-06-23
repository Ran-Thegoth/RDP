package rs.cc.api.conditions;

import java.nio.ByteBuffer;

public abstract class Condition implements ICondition {

	protected byte [] _what = {};

	public Condition() {
		// TODO Auto-generated constructor stub
	}
	
	
	public void parse(String s) {
		ByteBuffer bb = ByteBuffer.allocate(s.length()*2);
		boolean special = false;
		for(int i=0;i<s.length();i++) {
			char ch = s.charAt(i);
			if(ch == '\\') special = true;
			else {
				if(special) {
					special = false;
					switch(ch) {
					case 'n': bb.put((byte)'\n'); continue; 
					case 'r': bb.put((byte)'\r'); continue;
					case 't': bb.put((byte)'\t'); continue;
					}
				} 
				bb.put((""+ch).getBytes());
			}
		}
		_what = new byte[bb.position()];
		System.arraycopy(_what, 0, bb.array(), 0, bb.position());
		
	}

}
