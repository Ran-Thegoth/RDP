package rs.cc.config;

import android.content.Context;
import android.os.Build;
import cs.ui.annotations.BindTo;
import rs.cc.Const;
import rs.cc.hardware.BTScanner;
import rs.cc.hardware.BarcodeScaner;
import rs.cc.hardware.UrovoScaner;
import rs.cc.hardware.ZXingScaner;
import rs.cc.R;

public class ScanerConfig {

	public static final int SCANER_TYPE_NONE = 0;
	public static final int SCANER_TYPE_HW = 1;
	public static final int SCANER_TYPE_BT = 2;
	public static final int SCANER_TYPE_CAM =3;
	
	@BindTo(ui= {R.id.lv_scaner_type})
	public int scanerType = SCANER_TYPE_CAM;
	@BindTo(ui= {R.id.ed_scaner_address})
	public String BTAddress = Const.EMPTY_STRING;

	public ScanerConfig() {
		if("Urovo".equalsIgnoreCase(Build.MANUFACTURER) || "UBX".equalsIgnoreCase(Build.MANUFACTURER)) 
			scanerType = SCANER_TYPE_HW;
	}
	public BarcodeScaner makeScanner(Context ctx) {
		switch(scanerType) {
		case SCANER_TYPE_NONE:
			return null;
		case SCANER_TYPE_CAM:
			return new ZXingScaner(ctx);
		case SCANER_TYPE_HW:
			return new UrovoScaner();
		case SCANER_TYPE_BT:
			if(BTAddress != null && !BTAddress.isEmpty())
				return new BTScanner(BTAddress);
		}
		return null;
	}

}
