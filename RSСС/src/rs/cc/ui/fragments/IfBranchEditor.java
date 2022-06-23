package rs.cc.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cs.ui.fragments.BaseEditor;
import rs.cc.api.IfBranch;

public class IfBranchEditor extends BaseEditor<IfBranch> {
	public static IfBranchEditor newInstance(IfBranch value, OnSaveListener<IfBranch> l) {
		IfBranchEditor result = new IfBranchEditor();
		result.setValue(value);
		result.setListener(l);
		return result;
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	@Override
	protected boolean doSave(IfBranch value) {
		return true;
	}

}
