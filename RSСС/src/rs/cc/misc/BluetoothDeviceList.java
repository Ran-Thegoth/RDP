package rs.cc.misc;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BluetoothDeviceList extends ArrayAdapter<BluetoothDevice>{

	public BluetoothDeviceList(Context ctx) {
		super(ctx,android.R.layout.two_line_list_item);
		addAll(BluetoothAdapter.getDefaultAdapter().getBondedDevices());
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) { 
			convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.two_line_list_item, parent,false);
			convertView.setPadding(6, 3, 6, 3);
		}
		BluetoothDevice d = getItem(position);
		((TextView)convertView.findViewById(android.R.id.text1)).setText(d.getName());
		((TextView)convertView.findViewById(android.R.id.text2)).setText(d.getAddress());
		return convertView;
	}

}
