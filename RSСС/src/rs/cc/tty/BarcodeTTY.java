package rs.cc.tty;

import java.util.LinkedList;
import java.util.Queue;

import rs.cc.api.Barcode;
import rs.cc.ui.SessionActivity;

public class BarcodeTTY extends TTY {

	private SessionActivity _owner;
	private Barcode _current;
	private byte [] _cBtyes;
	private int _pos;
	
	private Queue<Barcode> _barcodes = new LinkedList<>();
	public BarcodeTTY(SessionActivity owner) {
		_owner = owner;
		_owner.barcodeScaner().disable();
	}

	@Override
	public int available() {
		int size = 0;
		synchronized(_barcodes) {
			for(Barcode bc : _barcodes) 
				size += bc.get().length;
		}
		return size;
	}

	@Override
	public int read(byte[] buf, int offset, int size) {
		if(_current == null) {
			synchronized (_barcodes) {
				if(_barcodes.isEmpty()) return 0;
				_current = _barcodes.poll();
			}
			_pos = 0;
			_cBtyes = _current.get();
			_current.before(_owner); 
			
		}
		
		int bs = _cBtyes.length - _pos > size ? size : _cBtyes.length - _pos;
		System.arraycopy(_cBtyes, _pos, buf, offset, bs);
		_pos += bs;
		if(_pos >= _cBtyes.length) {
			_current.after(_owner);
			_current = null;
			_cBtyes = null;
		}
		return bs;
	}

	@Override
	public void write(byte[] buf, int offset, int size) {
	}

	@Override
	public void store(byte[] b) {
	}
	
	public void store(Barcode b) {
		synchronized (_barcodes) {
			if(_barcodes.size() > 10)
				_barcodes.clear();
			_barcodes.add(b);
		}
	}
	@Override
	public void onEnabled() {
		_owner.barcodeScaner().enable();
	}
	@Override
	public void onDisabled() {
		_owner.barcodeScaner().disable();
	}

}
