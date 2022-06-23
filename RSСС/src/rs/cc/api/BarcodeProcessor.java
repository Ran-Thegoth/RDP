package rs.cc.api;

import java.io.IOException;
import java.util.ArrayList;

import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import rs.cc.Core;

public  class BarcodeProcessor {
	private ArrayList<IfBranch> _conditions = new ArrayList<>();
	@SuppressWarnings("deprecation")
	private static SoundPool _soundPool = new SoundPool(10, AudioManager.STREAM_ALARM, 100);
	private static int _successID, _failID;
	
	public BarcodeProcessor() {
		
	}
	public BarcodeProcessor(String defines) {
		
	}
	
	public static void init() {
		try {
			_successID = _soundPool.load(Core.getInstance().getAssets().openFd("success.wav"), 1);
			_failID = _soundPool.load(Core.getInstance().getAssets().openFd("fail.wav"), 1);
		} catch (IOException ioe) {
			Log.d("RDP", "Sound ",ioe);
		}
		
	}
	public void process(Barcode BARCODE) {
		for(IfBranch b : _conditions) 
			if(b.check(BARCODE))  {
				BARCODE.setActions(b);
				break;
			}
	}
	/** 
	 * Издать звуковой сигнал успеха
	 */
	public static void beepSuccess() {
		_soundPool.play(_successID, 1,1, 1, 0, 1);
	}
	/**
	 * Издать звуковой сигнал ошибки
	 */
	public static void beepError() {
		_soundPool.play(_failID, 1,1, 1, 0, 1);
	}
}
