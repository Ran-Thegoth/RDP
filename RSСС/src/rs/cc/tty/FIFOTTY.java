package rs.cc.tty;

import java.util.LinkedList;
import java.util.Queue;

public class FIFOTTY extends TTY {

	private Queue<byte []> _bytes = new LinkedList<>();
	private  int _readPTR = 0;
	private  boolean _accessed = false;
	public FIFOTTY() {
	}

	@Override
	public int available() {
		int result = 0;
		synchronized (_bytes) {
			for(byte [] b : _bytes) 
				result += b.length;
		}
		return result;
	}

	@Override
	public int read(byte[] buf, int offset, int size) {
		int read = 0;
		while(size > 0) {
			synchronized (_bytes) {
				_accessed = true;
				if(_bytes.isEmpty()) return read;
				byte [] b =_bytes.peek();
				int rs = Math.min(b.length-_readPTR,size);
				System.arraycopy(b, _readPTR, buf, offset+read, rs);
				_readPTR += rs;
				if(_readPTR == b.length) {
					_bytes.remove();
					_readPTR = 0;
				}
				read += rs;
				size -= rs;
			}
		}
		return read;
	}

	@Override
	public void write(byte[] buf, int offset, int size) {
		synchronized (_bytes) {
			_accessed = true;
		}
	}

	@Override
	public void store(byte[] b) {
		synchronized (_bytes) {
			if(!_accessed) return;
			_bytes.add(b);
		}
	}
	

}
