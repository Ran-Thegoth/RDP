package rs.cc.api;

import java.util.ArrayList;

import rs.cc.api.actions.IAction;
import rs.cc.api.conditions.ICondition;
import rs.cc.ui.SessionActivity;

public class IfBranch implements ICondition {

	private ICondition _condition;
	private ArrayList<IAction> _before = new ArrayList<>();
	private ArrayList<IAction> _after = new ArrayList<>();
	public IfBranch() {
	}

	@Override
	public boolean check(Barcode barcode) {
		if(_condition != null) return _condition.check(barcode);
		return false;
	}

	public void before(Barcode BARCODE, SessionActivity sa) {
		for(IAction a :_before)
			a.execute(BARCODE, sa);
	}
	public void after(Barcode BARCODE, SessionActivity sa) {
		for(IAction a :_after)
			a.execute(BARCODE, sa);
	}

	public void setCondition(ICondition con) {
		_condition = con;
		
	}

}
