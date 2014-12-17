package com.spotz.database;

import java.util.LinkedList;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SpotsHelper extends SQLiteOpenHelper {
	
	// Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "SpotsDB";
   
	public SpotsHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);	
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// SQL statement to create book table
		String CREATE_SPOT_TABLE = "CREATE TABLE spots ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " + 
				"name TEXT, "+
				"description TEXT, "+
				"type TEXT, "+
				"typeid INTEGER, "+
				"latitude TEXT, "+
				"longitude TEXT, "+
				"userid INTEGER, "+
				"imagepath TEXT )";
		
		// create books table
		db.execSQL(CREATE_SPOT_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older books table if existed
        db.execSQL("DROP TABLE IF EXISTS spots");
        
        // create fresh books table
        this.onCreate(db);
	}
	//---------------------------------------------------------------------
   
	/**
     * CRUD operations (create "add", read "get", update, delete) book + get all books + delete all books
     */
	
	// Books table name
    private static final String TABLE_SPOTS = "spots";
    
    // Books Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_TYPE = "type";
    private static final String KEY_TYPEID = "typeid";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_USERID = "userid";
    private static final String KEY_IMAGEPATH = "imagepath";
    
    private static final String[] COLUMNS = {KEY_ID, KEY_NAME, KEY_DESCRIPTION,
    										KEY_TYPE, KEY_TYPEID, KEY_LATITUDE, KEY_LONGITUDE,
    										KEY_USERID, KEY_IMAGEPATH};
    
	public void addSpot(Spot spot){
		Log.d("addBook", spot.toString());
		// 1. get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();
		 
		// 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, spot.getName()); // get title 
        values.put(KEY_DESCRIPTION, spot.getDescription()); // get author
        values.put(KEY_TYPE, spot.getType()); // get title 
        values.put(KEY_TYPEID, spot.getTypeId()); // get title 
        values.put(KEY_LATITUDE, spot.getLatitude()); // get author
        values.put(KEY_LONGITUDE, spot.getLongitude()); // get title 
        values.put(KEY_USERID, spot.getUserid()); // get title 
        values.put(KEY_IMAGEPATH, spot.getImagepath()); // get title 
        
        // 3. insert
        db.insert(TABLE_SPOTS, // table
        		null, //nullColumnHack
        		values); // key/value -> keys = column names/ values = column values
        
        // 4. close
        db.close(); 
	}
	
	public Spot getSpot(int id){

		// 1. get reference to readable DB
		SQLiteDatabase db = this.getReadableDatabase();
		 
		// 2. build query
        Cursor cursor = 
        		db.query(TABLE_SPOTS, // a. table
        		COLUMNS, // b. column names
        		" id = ?", // c. selections 
                new String[] { String.valueOf(id) }, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit
        
        // 3. if we got results get the first one
        if (cursor != null)
            cursor.moveToFirst();
 
        // 4. build book object
        Spot spot = new Spot();
        spot.setId(Integer.parseInt(cursor.getString(0)));
        
        spot.setName(cursor.getString(1));
        spot.setDescription(cursor.getString(2));
        spot.setType(cursor.getString(3));
        spot.setTypeId(cursor.getString(4));
        spot.setLatitude(cursor.getString(5));
        spot.setLongitude(cursor.getString(6));
        spot.setUserid(cursor.getString(7));
        spot.setImagepath(cursor.getString(8));
        
		Log.d("getSpot("+id+")", spot.toString());

        // 5. return book
        return spot;
	}
	
	// Get All Books
    public List<Spot> getAllSpots() {
        List<Spot> spots = new LinkedList<Spot>();

        // 1. build the query
        String query = "SELECT * FROM " + TABLE_SPOTS +" ORDER BY id DESC";
 
    	// 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
 
        // 3. go over each row, build book and add it to list
        Spot spot = null;
        if (cursor.moveToFirst()) {
            do {
            	spot = new Spot();
            	spot.setId(Integer.parseInt(cursor.getString(0)));
            	spot.setName(cursor.getString(1));
            	spot.setDescription(cursor.getString(2));
            	spot.setType(cursor.getString(3));
            	spot.setTypeId(cursor.getString(4));
            	spot.setLatitude(cursor.getString(5));
            	spot.setLongitude(cursor.getString(6));
            	spot.setUserid(cursor.getString(7));
            	spot.setImagepath(cursor.getString(8));
            	// Add book to books
            	spots.add(spot);
            } while (cursor.moveToNext());
        }
        
		Log.d("getAllSpots()", spots.toString());

        // return books
        return spots;
    }
	
	 // Updating single book
    public int updateSpot(Spot spot) {
    	SQLiteDatabase db = this.getWritableDatabase();
 
		ContentValues values = new ContentValues();
        values.put("name", spot.getName()); // get title 
        values.put("description", spot.getDescription()); // get author
        values.put("type", spot.getType()); // get title 
        values.put("typeid", spot.getTypeId()); // get title 
        values.put("latitude", spot.getLatitude()); // get author
        values.put("longitude", spot.getLongitude()); // get author
        values.put("userid", spot.getUserid()); // get author
        values.put("imagepath", spot.getImagepath()); // get author
        
        int i = db.update(TABLE_SPOTS, //table
        		values, // column/value
        		KEY_ID+" = ?", // selections
                new String[] { String.valueOf(spot.getId()) }); //selection args
        
        db.close();
        return i;
    }

    // Deleting single book
    public void deleteSpot(Spot spot) {
    	// 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        // 2. delete
        db.delete(TABLE_SPOTS,
        		KEY_ID+" = ?",
                new String[] { String.valueOf(spot.getId()) });
        // 3. close
        db.close();
		Log.d("deleteBook", spot.toString());
    }
    
    public int getLastSpotId() {
        String query = "SELECT * FROM " + TABLE_SPOTS +" ORDER BY id DESC LIMIT 1";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
            	Log.d("getLastSpotId()", cursor.getString(0));
            	return Integer.parseInt(cursor.getString(0));
            } while (cursor.moveToNext());
        }
		return 0;
    }
    
}