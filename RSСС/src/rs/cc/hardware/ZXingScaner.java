package rs.cc.hardware;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoderFactory;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import rs.cc.api.BarcodeProcessor;
import rs.cc.tty.BarcodeTTY;
import rs.cc.tty.TTY;
import rs.cc.ui.SessionActivity;
import rs.cc.R;

public class ZXingScaner implements BarcodeScaner, View.OnClickListener, BarcodeCallback, DialogInterface.OnDismissListener, DialogInterface.OnShowListener {

	private AlertDialog _scanDialog;
	private DecoratedBarcodeView _bv;
	private BarcodeListener _l;
	private boolean _once;
	private int _scanType = 0;
	private View _content;
	
	public ZXingScaner(Context ctx) {
		AlertDialog.Builder b = new AlertDialog.Builder(ctx);
		_content = LayoutInflater.from(ctx).inflate(R.layout.zxing, new LinearLayout(ctx),false);
		View v = _content.findViewById(R.id.iv_bw);
		v.setOnClickListener(this);
		v.setSelected(true);
		_content.findViewById(R.id.iv_wb).setOnClickListener(this);
		_content.findViewById(R.id.iv_spot).setOnClickListener(this);
		_bv = _content.findViewById(R.id.zxing_barcode_scanner);
		_bv.setDecoderFactory(createFactory(0));
		_bv.decodeContinuous(this);
		b.setView(_content);
		_scanDialog = b.create();
		_scanDialog.setOnDismissListener(this);
		_scanDialog.setOnShowListener(this);
		
	}

	@Override
	public void scanOnce(BarcodeListener l, boolean showRequest) {
		_once = true;
		_l = l;
		_scanDialog.show();
	}

	@Override
	public void scanOnce(BarcodeListener l) {
		scanOnce(l,true);		
	}

	@Override
	public void startScan(BarcodeListener l) {
		_once = false;
		_l = l;
		_scanDialog.show();
	}

	@Override
	public void doScan() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopScan() {
		if(_scanDialog.isShowing())
			_scanDialog.dismiss();

	}

	@Override
	public boolean isManual() {
		return true;
	}

	@Override
	public TTY getTTY(SessionActivity owner) {
		return new BarcodeTTY(owner);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.iv_spot:
			if(v.isSelected())
				_bv.setTorchOff();
			else 
				_bv.setTorchOn();
			v.setSelected(!v.isSelected());
			break;
		case R.id.iv_bw:
			if(_scanType == 1) {
				v.setSelected(true);
				_content.findViewById(R.id.iv_wb).setSelected(false);
				_bv.pause();
				_scanType = 0;
				_bv.setDecoderFactory(createFactory(_scanType));
				_bv.decodeContinuous(this);
				_bv.resume();
			}
			break;
		case R.id.iv_wb:
			if(_scanType == 0) {
				v.setSelected(true);
				_content.findViewById(R.id.iv_bw).setSelected(false);
				_bv.pause();
				_scanType = 1;
				_bv.setDecoderFactory(createFactory(_scanType));
				_bv.decodeContinuous(this);
				_bv.resume();
			}
			break;
			
		}
	}
	private DecoderFactory createFactory(int scanType) {
		List<BarcodeFormat> formats = new ArrayList<>();
		formats.add(BarcodeFormat.CODE_128);
		formats.add(BarcodeFormat.EAN_13);
		formats.add(BarcodeFormat.EAN_8);
		formats.add(BarcodeFormat.QR_CODE);
		formats.add(BarcodeFormat.CODE_39);
		formats.add(BarcodeFormat.CODE_93);
		formats.add(BarcodeFormat.PDF_417);
		formats.add(BarcodeFormat.DATA_MATRIX);
		formats.add(BarcodeFormat.UPC_A);
		Map<DecodeHintType, Object> hints = new HashMap<>();
		hints.put(DecodeHintType.TRY_HARDER, true);
		DefaultDecoderFactory result = new DefaultDecoderFactory(formats, hints, "utf8", scanType);
		return result;
	}

	private String _last;
	private long _lastTime;
	@Override
	public void barcodeResult(BarcodeResult result) {
		if(result == null) return;
		if(result.getText().equals(_last)) 
			if(System.currentTimeMillis() - _lastTime < 1500)
				return;
		_last = result.getText();
		_lastTime = System.currentTimeMillis();
		BarcodeProcessor.beepSuccess();
		_l.onBarcode(_last.getBytes());
		if(_once) _scanDialog.dismiss();
	}

	@Override
	public void possibleResultPoints(List<ResultPoint> resultPoints) {
		
	}

	@Override
	public void onDismiss(DialogInterface arg0) {
		_bv.pauseAndWait();
	}

	@Override
	public void onShow(DialogInterface arg0) {
		_last = null;
		_bv.resume();
		
	}

	@Override
	public void disable() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enable() {
		// TODO Auto-generated method stub
		
	}

}
