package rs.cc.ui.fragments;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.view.View.OnClickListener;
import cs.ui.fragments.BaseFragment;
import cs.ui.fragments.BaseEditor.OnSaveListener;
import rs.cc.R;
import rs.cc.api.IfBranch;
import rs.cc.api.conditions.Contains;
import rs.cc.api.conditions.Default;
import rs.cc.api.conditions.EndWith;
import rs.cc.api.conditions.ICondition;
import rs.cc.api.conditions.StartWith;

public class ConditionEditor extends BaseFragment implements OnClickListener, DialogInterface.OnClickListener, OnSaveListener<IfBranch> {

	
	private class IfBranchHolder extends ViewHolder {

		public IfBranchHolder(View itemView) {
			super(itemView);
			// TODO Auto-generated constructor stub
		}
		
	}
	private class IfBranchAdapter extends Adapter<IfBranchHolder> {

		private ArrayList<IfBranch> _branches = new ArrayList<>();
		@Override
		public int getItemCount() {
			return _branches.size();
		}

		@Override
		public void onBindViewHolder(IfBranchHolder arg0, int arg1) {
		}

		@Override
		public IfBranchHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
			return null;
		}

		public void update(IfBranch value) {
			if(!_branches.contains(value))
				_branches.add(value);
			notifyDataSetChanged();
		}
		
		
	}
	private IfBranchAdapter _adapter;
	private RecyclerView _list;
	private String [] _cTypes;
	public ConditionEditor() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(_list == null) {
			_list = new RecyclerView(getContext());
			_list.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
			_adapter = new IfBranchAdapter();
			_list.setAdapter(_adapter);
			_cTypes = getResources().getStringArray(R.array.conditions);
		}
		return _list;
	}
	@Override
	public void onStart() {
		super.onStart();
		getActivity().setTitle(R.string.barcode_scenario);
		setupButtons(this, R.id.iv_add);
	}

	
	@Override
	public void onClick(View v) {
		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
		ArrayAdapter<String> a = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
		int end = _cTypes.length;
		for(int i=0;i<end;i++)
			a.add(_cTypes[i]);
		b.setAdapter(a, this);
		b.setTitle(R.string.cond_type);
		b.show();
	}

	@Override
	public void onClick(DialogInterface arg0, int p) {
		IfBranch b = new IfBranch();
		ICondition con = null;
		switch(p) {
		case 0: con = new StartWith(); break;
		case 1: con = new EndWith(); break;
		case 2: con = new Contains(); break;
		case 3: con = new Default(); break;
		}
		if(con != null) {
			b.setCondition(con);
			showFragment(IfBranchEditor.newInstance(b, this));
		}
	}

	@Override
	public void onSaved(IfBranch value) {
		_adapter.update(value);
		
	}
	
}
