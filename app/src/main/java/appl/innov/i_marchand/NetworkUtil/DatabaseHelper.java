package appl.innov.i_marchand.NetworkUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
private static final String DATABASE_NAME = "trader.db";
    private static final String TABLE_NAME = "TRD_table";

    private static final String COL_1 = "ID";
    private static final String COL_2 = "IDSESSION";
    private static final String COL_3 = "COMMISSION";
    private static final String COL_4 = "IDCARD";
    private static final String COL_5 = "TELEPHONE";
    private static final String COL_6 = "IDUSER";
    private static final String COL_7 = "NUMEROFROMCARD";
    private static final String COL_8 = "PRENOMFROMCARD";
    private static final String COL_9 = "NOMFROMCARD";
    private static final String COL_10 = "FIXEDAMOUNT";


    public DatabaseHelper(Context context){

    super(context, DATABASE_NAME, null, 7);
}

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE " + TABLE_NAME +"(" + COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, IDSESSION TEXT, COMMISSION TEXT, IDCARD TEXT, " +
                        "TELEPHONE TEXT, IDUSER TEXT, NUMEROFROMCARD TEXT, PRENOMFROMCARD TEXT, NOMFROMCARD TEXT, FIXEDAMOUNT TEXT)"
        );
    }

    public boolean insertData(
            String toke, String commission, String idCard,String telephone,
            String idUser, String numeroFromCard,String prenomFromCard,
            String nomFromCard, String fixedAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COL_2, toke);
        contentValues.put(COL_3, commission);
        contentValues.put(COL_4, idCard);
        contentValues.put(COL_5, telephone);
        contentValues.put(COL_6, idUser);
        contentValues.put(COL_7, numeroFromCard);
        contentValues.put(COL_8, prenomFromCard);
        contentValues.put(COL_9, nomFromCard);
        contentValues.put(COL_10, fixedAmount);

        long result  = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }
public Cursor getAllData(){
    SQLiteDatabase db = this.getWritableDatabase();
    String query = "SELECT rowid from your_table_name order by ROWID DESC limit 1";
    Cursor res = db.rawQuery("SELECT  * FROM " + TABLE_NAME , null);

    return res;
}

public boolean updateData(){
    // TO DO
    return true;
}
public Integer deleteData(String id){
    SQLiteDatabase db = this.getWritableDatabase();
    return  db.delete(TABLE_NAME, "ID=?",new String[]{id});
}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }
}
