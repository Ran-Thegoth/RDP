package rs.cc;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import cs.orm.ORMHelper;
import android.database.sqlite.SQLiteOpenHelper;

public class DB extends SQLiteOpenHelper {

	public DB(Context context) {
		super(context, "data.db", null, 100);
		ORMHelper.setSQLiteHelper(this);
	}

	@Override
	public void onCreate(SQLiteDatabase arg0) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
	}

}
