/*
   Android Keyboard Mapping

   Copyright 2013 Thinstuff Technologies GmbH, Author: Martin Fleisz

   This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
   If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/


package rs.cc.connection.rdp;


import android.view.KeyEvent;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import rs.cc.connection.KeyboardMapper;

public class RDPKeyboardMapper extends KeyboardMapper
{

	public static final int KEYBOARD_TYPE_FUNCTIONKEYS = 1;
	public static final int KEYBOARD_TYPE_NUMPAD = 2;
	public static final int KEYBOARD_TYPE_CURSOR = 3;

	// defines key states for modifier keys - locked means on and no auto-release if an other key is pressed
	public static final int KEYSTATE_ON = 1;
	public static final int KEYSTATE_LOCKED = 2;
	public static final int KEYSTATE_OFF = 3;

	// interface that gets called for input handling



	final static int VK_LBUTTON = 0x01;
	final static int VK_RBUTTON = 0x02;
	final static int VK_CANCEL = 0x03;
	final static int VK_MBUTTON = 0x04;
	final static int VK_XBUTTON1 = 0x05;
	final static int VK_XBUTTON2 = 0x06;
	final static int VK_BACK = 0x08;
	final static int VK_TAB	 = 0x09;
	final static int VK_CLEAR = 0x0C;
	final static int VK_RETURN = 0x0D;
	final static int VK_SHIFT = 0x10;
	final static int VK_CONTROL = 0x11;
	final static int VK_MENU = 0x12;
	final static int VK_PAUSE = 0x13;
	final static int VK_CAPITAL = 0x14;
	final static int VK_KANA = 0x15;
	final static int VK_HANGUEL = 0x15;
	final static int VK_HANGUL = 0x15;
	final static int VK_JUNJA = 0x17;
	final static int VK_FINAL = 0x18;
	final static int VK_HANJA = 0x19;
	final static int VK_KANJI = 0x19;

	final static int VK_CONVERT = 0x1C;
	final static int VK_NONCONVERT = 0x1D;
	final static int VK_ACCEPT = 0x1E;
	final static int VK_MODECHANGE = 0x1F;
	final static int VK_SPACE = 0x20;
	final static int VK_SELECT = 0x29;
	final static int VK_PRINT = 0x2A;
	final static int VK_EXECUTE = 0x2B;
	final static int VK_SNAPSHOT = 0x2C;
	final static int VK_INSERT = 0x2D;
	final static int VK_DELETE = 0x2E;
	final static int VK_HELP = 0x2F;
	final static int VK_KEY_0 = 0x30;
	final static int VK_KEY_1 = 0x31;
	final static int VK_KEY_2 = 0x32;
	final static int VK_KEY_3 = 0x33;
	final static int VK_KEY_4 = 0x34;
	final static int VK_KEY_5 = 0x35;
	final static int VK_KEY_6 = 0x36;
	final static int VK_KEY_7 = 0x37;
	final static int VK_KEY_8 = 0x38;
	final static int VK_KEY_9 = 0x39;
	final static int VK_KEY_A = 0x41;
	final static int VK_KEY_B = 0x42;
	final static int VK_KEY_C = 0x43;
	final static int VK_KEY_D = 0x44;
	final static int VK_KEY_E = 0x45;
	final static int VK_KEY_F = 0x46;
	final static int VK_KEY_G = 0x47;
	final static int VK_KEY_H = 0x48;
	final static int VK_KEY_I = 0x49;
	final static int VK_KEY_J = 0x4A;
	final static int VK_KEY_K = 0x4B;
	final static int VK_KEY_L = 0x4C;
	final static int VK_KEY_M = 0x4D;
	final static int VK_KEY_N = 0x4E;
	final static int VK_KEY_O = 0x4F;
	final static int VK_KEY_P = 0x50;
	final static int VK_KEY_Q = 0x51;
	final static int VK_KEY_R = 0x52;
	final static int VK_KEY_S = 0x53;
	final static int VK_KEY_T = 0x54;
	final static int VK_KEY_U = 0x55;
	final static int VK_KEY_V = 0x56;
	final static int VK_KEY_W = 0x57;
	final static int VK_KEY_X = 0x58;
	final static int VK_KEY_Y = 0x59;
	final static int VK_KEY_Z = 0x5A;
	final static int VK_LWIN = 0x5B;
	final static int VK_RWIN = 0x5C;
	final static int VK_SLEEP = 0x5F;
	final static int VK_NUMPAD0	= 0x60;
	final static int VK_NUMPAD1	= 0x61;
	final static int VK_NUMPAD2	= 0x62;
	final static int VK_NUMPAD3	= 0x63;
	final static int VK_NUMPAD4	= 0x64;
	final static int VK_NUMPAD5	= 0x65;
	final static int VK_NUMPAD6	= 0x66;
	final static int VK_NUMPAD7	= 0x67;
	final static int VK_NUMPAD8	= 0x68;
	final static int VK_NUMPAD9	= 0x69;
	final static int VK_MULTIPLY = 0x6A;
	final static int VK_ADD = 0x6B;
	final static int VK_SEPARATOR = 0x6C;
	final static int VK_SUBTRACT = 0x6D;
	final static int VK_DECIMAL = 0x6E;
	final static int VK_DIVIDE = 0x6F;
	final static int VK_NUMLOCK = 0x90;
	final static int VK_SCROLL = 0x91;
	final static int VK_LSHIFT = 0xA0;
	final static int VK_RSHIFT = 0xA1;
	final static int VK_LCONTROL = 0xA2;
	final static int VK_RCONTROL = 0xA3;
	final static int VK_LMENU = 0xA4;
	final static int VK_RMENU = 0xA5;
	final static int VK_BROWSER_BACK = 0xA6;
	final static int VK_BROWSER_FORWARD = 0xA7;
	final static int VK_BROWSER_REFRESH = 0xA8;
	final static int VK_BROWSER_STOP = 0xA9;
	final static int VK_BROWSER_SEARCH = 0xAA;
	final static int VK_BROWSER_FAVORITES = 0xAB;
	final static int VK_BROWSER_HOME = 0xAC;
	final static int VK_VOLUME_MUTE = 0xAD;
	final static int VK_VOLUME_DOWN = 0xAE;
	final static int VK_VOLUME_UP = 0xAF;
	final static int VK_MEDIA_NEXT_TRACK = 0xB0;
	final static int VK_MEDIA_PREV_TRACK = 0xB1;
	final static int VK_MEDIA_STOP = 0xB2;
	final static int VK_MEDIA_PLAY_PAUSE = 0xB3;
	final static int VK_LAUNCH_MAIL = 0xB4;
	final static int VK_LAUNCH_MEDIA_SELECT = 0xB5;
	final static int VK_LAUNCH_APP1 = 0xB6;
	final static int VK_LAUNCH_APP2 = 0xB7;
	final static int VK_OEM_1 = 0xBA;
	final static int VK_OEM_PLUS = 0xBB;
	final static int VK_OEM_COMMA = 0xBC;
	final static int VK_OEM_MINUS = 0xBD;
	final static int VK_OEM_PERIOD = 0xBE;
	final static int VK_OEM_2 = 0xBF;
	final static int VK_OEM_3 = 0xC0;
	final static int VK_ABNT_C1 = 0xC1;
	final static int VK_ABNT_C2 = 0xC2;
	final static int VK_OEM_4 = 0xDB;
	final static int VK_OEM_5 = 0xDC;
	final static int VK_OEM_6 = 0xDD;
	final static int VK_OEM_7 = 0xDE;
	final static int VK_OEM_8 = 0xDF;
	final static int VK_OEM_102 = 0xE2;
	final static int VK_PROCESSKEY = 0xE5;
	final static int VK_PACKET = 0xE7;
	final static int VK_ATTN = 0xF6;
	final static int VK_CRSEL = 0xF7;
	final static int VK_EXSEL = 0xF8;
	final static int VK_EREOF = 0xF9;
	final static int VK_PLAY = 0xFA;
	final static int VK_ZOOM = 0xFB;
	final static int VK_NONAME = 0xFC;
	final static int VK_PA1	= 0xFD;
	final static int VK_OEM_CLEAR = 0xFE;
	final static int VK_UNICODE = 0x80000000;


	// key codes to switch between custom keyboard 
/*	private final static int EXTKEY_KBFUNCTIONKEYS = 0x1100; 
	private final static int EXTKEY_KBNUMPAD = 0x1101; 
	private final static int EXTKEY_KBCURSOR = 0x1102; */ 

	// this flag indicates if we got a VK or a unicode character in our translation map 

	// this flag indicates if the key is a toggle key (remains down when pressed and goes up if pressed again)
	private static final int KEY_FLAG_TOGGLE = 0x40000000;

	
	private TIntIntMap _keymap = new TIntIntHashMap();
	public RDPKeyboardMapper(KeyProcessor processor) {
		super(processor);
		
		_keymap.put(KeyEvent.KEYCODE_0, VK_KEY_0);
		_keymap.put(KeyEvent.KEYCODE_1, VK_KEY_1);
		_keymap.put(KeyEvent.KEYCODE_2, VK_KEY_2);
		_keymap.put(KeyEvent.KEYCODE_3, VK_KEY_3);
		_keymap.put(KeyEvent.KEYCODE_4, VK_KEY_4);
		_keymap.put(KeyEvent.KEYCODE_5, VK_KEY_5);
		_keymap.put(KeyEvent.KEYCODE_6, VK_KEY_6);
		_keymap.put(KeyEvent.KEYCODE_7, VK_KEY_7);
		_keymap.put(KeyEvent.KEYCODE_8, VK_KEY_8);
		_keymap.put(KeyEvent.KEYCODE_9, VK_KEY_9);

		_keymap.put(KeyEvent.KEYCODE_A, VK_KEY_A);
		_keymap.put(KeyEvent.KEYCODE_B, VK_KEY_B);
		_keymap.put(KeyEvent.KEYCODE_C, VK_KEY_C);
		_keymap.put(KeyEvent.KEYCODE_D, VK_KEY_D);
		_keymap.put(KeyEvent.KEYCODE_E, VK_KEY_E);
		_keymap.put(KeyEvent.KEYCODE_F, VK_KEY_F);
		_keymap.put(KeyEvent.KEYCODE_G, VK_KEY_G);
		_keymap.put(KeyEvent.KEYCODE_H, VK_KEY_H);
		_keymap.put(KeyEvent.KEYCODE_I, VK_KEY_I);
		_keymap.put(KeyEvent.KEYCODE_J, VK_KEY_J);
		_keymap.put(KeyEvent.KEYCODE_K, VK_KEY_K);
		_keymap.put(KeyEvent.KEYCODE_L, VK_KEY_L);
		_keymap.put(KeyEvent.KEYCODE_M, VK_KEY_M);
		_keymap.put(KeyEvent.KEYCODE_N, VK_KEY_N);
		_keymap.put(KeyEvent.KEYCODE_O, VK_KEY_O);
		_keymap.put(KeyEvent.KEYCODE_P, VK_KEY_P);
		_keymap.put(KeyEvent.KEYCODE_Q, VK_KEY_Q);
		_keymap.put(KeyEvent.KEYCODE_R, VK_KEY_R);
		_keymap.put(KeyEvent.KEYCODE_S, VK_KEY_S);
		_keymap.put(KeyEvent.KEYCODE_T, VK_KEY_T);
		_keymap.put(KeyEvent.KEYCODE_U, VK_KEY_U);
		_keymap.put(KeyEvent.KEYCODE_V, VK_KEY_V);
		_keymap.put(KeyEvent.KEYCODE_W, VK_KEY_W);
		_keymap.put(KeyEvent.KEYCODE_X, VK_KEY_X);
		_keymap.put(KeyEvent.KEYCODE_Y, VK_KEY_Y);
		_keymap.put(KeyEvent.KEYCODE_Z, VK_KEY_Z);
		
		_keymap.put(KeyEvent.KEYCODE_SHIFT_LEFT, (KEY_FLAG_TOGGLE | VK_LSHIFT));
		_keymap.put(KeyEvent.KEYCODE_SYM,(VK_EXT_KEY | VK_LWIN));
		_keymap.put(KeyEvent.KEYCODE_SHIFT_RIGHT,VK_RSHIFT);
		_keymap.put(KeyEvent.KEYCODE_ESCAPE,VK_ESCAPE);
		_keymap.put(KeyEvent.KEYCODE_ALT_LEFT,(KEY_FLAG_TOGGLE | VK_LMENU));
		_keymap.put(KeyEvent.KEYCODE_CTRL_LEFT,(KEY_FLAG_TOGGLE | VK_LCONTROL));
		_keymap.put(KeyEvent.KEYCODE_DEL,VK_BACK);
		_keymap.put(KeyEvent.KEYCODE_ENTER,VK_RETURN);
		_keymap.put(KeyEvent.KEYCODE_HOME,VK_HOME | VK_EXT_KEY);
		_keymap.put(KeyEvent.KEYCODE_MOVE_END,VK_END | VK_EXT_KEY);
		_keymap.put(KeyEvent.KEYCODE_DPAD_DOWN,VK_DOWN | VK_EXT_KEY);
		_keymap.put(KeyEvent.KEYCODE_DPAD_UP,VK_UP | VK_EXT_KEY);
		_keymap.put(KeyEvent.KEYCODE_DPAD_LEFT,VK_LEFT | VK_EXT_KEY);
		_keymap.put(KeyEvent.KEYCODE_DPAD_RIGHT,VK_RIGHT | VK_EXT_KEY);
		_keymap.put(KeyEvent.KEYCODE_INSERT,VK_INSERT | VK_EXT_KEY);
		_keymap.put(KeyEvent.KEYCODE_PAGE_DOWN, VK_NEXT | VK_EXT_KEY);
		_keymap.put(KeyEvent.KEYCODE_PAGE_UP, VK_NEXT | VK_EXT_KEY);
		_keymap.put(KeyEvent.KEYCODE_FORWARD_DEL,VK_DELETE | VK_EXT_KEY);
		_keymap.put(KeyEvent.KEYCODE_SYSRQ, VK_PRINT);
		for(int i=0;i<12;i++)
			_keymap.put(KeyEvent.KEYCODE_F1+i,VK_F1+i);
	}

	@Override
	public int getVirtualKey(int code) {
		int vkey = _keymap.get(code);
		return vkey == 0 ? code : vkey;
	}
}

