package rs.cc.connection;

import android.view.KeyEvent;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

public abstract class KeyboardMapper {

	public static final int KEY_ALT 	= 1;
	public static final int KEY_CONTROL = 2;
	public static final int KEY_SHIFT 	= 4;
	public static final int KEY_SYM 	= 8;
	public final static int VK_F1 = 0x70;
	public final static int VK_F2 = 0x71;
	public final static int VK_F3 = 0x72;
	public final static int VK_F4 = 0x73;
	public final static int VK_F5 = 0x74;
	public final static int VK_F6 = 0x75;
	public final static int VK_F7 = 0x76;
	public final static int VK_F8 = 0x77;
	public final static int VK_F9 = 0x78;
	public final static int VK_F10 = 0x79;
	public final static int VK_F11 = 0x7A;
	public final static int VK_F12 = 0x7B;
	public final static int VK_F13 = 0x7C;
	public final static int VK_F14 = 0x7D;
	public final static int VK_F15 = 0x7E;
	public final static int VK_F16 = 0x7F;
	public final static int VK_F17 = 0x80;
	public final static int VK_F18 = 0x81;
	public final static int VK_F19 = 0x82;
	public final static int VK_F20 = 0x83;
	public final static int VK_F21 = 0x84;
	public final static int VK_F22 = 0x85;
	public final static int VK_F23 = 0x86;
	public final static int VK_F24 = 0x87;
	public final static int VK_ESCAPE = 0x1B;
	public final static int VK_LEFT = 0x25;
	public final static int VK_UP = 0x26;
	public final static int VK_RIGHT = 0x27;
	public final static int VK_DOWN = 0x28;
	public final static int VK_APPS = 0x5D;
	public final static int VK_HOME = 0x24;
	public final static int VK_PRIOR = 0x21;
	public final static int VK_NEXT = 0x22;
	public final static int VK_END	 = 0x23;
	public final static int VK_EXT_KEY = 0x00000100;
	public static final int KEY_FLAG_UNICODE = 0x80000000;
	
	
	
	public static interface KeyProcessor {
		void sendKey(int virtualKeyCode, boolean down);
	}
	
	
	
	protected KeyProcessor _processor;
	public KeyboardMapper(KeyProcessor processor) {
		_processor = processor;
	}
	
	public abstract int getVirtualKey(int code);

	private TIntSet _modifierList = new TIntHashSet();
	public synchronized void processKey(KeyEvent [] e, int meta ) {
		if(e == null || e.length == 0) return;
		_modifierList.clear();
		if(( meta & KeyEvent.META_ALT_ON) != 0) {
			_modifierList.add(KeyEvent.KEYCODE_ALT_LEFT);
			_processor.sendKey(getVirtualKey(KeyEvent.KEYCODE_ALT_LEFT), true);
		}
		if(( meta & KeyEvent.META_CTRL_ON) != 0) {
			_modifierList.add(KeyEvent.KEYCODE_CTRL_LEFT);
			_processor.sendKey(getVirtualKey(KeyEvent.KEYCODE_CTRL_LEFT), true);
		}
		if(( meta & KeyEvent.META_SHIFT_ON) != 0) {
			_modifierList.add(KeyEvent.KEYCODE_SHIFT_LEFT);
			_processor.sendKey(getVirtualKey(KeyEvent.KEYCODE_SHIFT_LEFT), true);
		}
		if(( meta & KeyEvent.META_SYM_ON) != 0) {
			_modifierList.add(KeyEvent.KEYCODE_SYM);
			_processor.sendKey(getVirtualKey(KeyEvent.KEYCODE_SYM), true);
		}
		for(KeyEvent ev : e) {
			if(_modifierList.contains(ev.getKeyCode())) continue;
			_processor.sendKey(getVirtualKey(ev.getKeyCode()),ev.getAction() == KeyEvent.ACTION_DOWN);
		}
		_modifierList.forEach(new TIntProcedure() {
			@Override
			public boolean execute(int k) {
				_processor.sendKey(getVirtualKey(k),false);
				return true;
			}
			
		});
		
	}
	
	
}
