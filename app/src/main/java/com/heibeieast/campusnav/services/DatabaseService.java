package com.heibeieast.campusnav.services;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.heibeieast.campusnav.models.CampusLocation;
import com.heibeieast.campusnav.models.Route;

import java.util.ArrayList;
import java.util.List;

public class DatabaseService extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "campus_nav.db";
    private static final int DATABASE_VERSION = 1;

    // Campus locations table
    private static final String TABLE_LOCATIONS = "campus_locations";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_LATITUDE = "latitude";
    private static final String COL_LONGITUDE = "longitude";
    private static final String COL_CATEGORY = "category";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_CREATED_AT = "created_at";

    // Campus routes table
    private static final String TABLE_ROUTES = "campus_routes";
    private static final String COL_FROM_ID = "from_location_id";
    private static final String COL_TO_ID = "to_location_id";
    private static final String COL_DISTANCE = "distance_meters";
    private static final String COL_ROUTE_DESC = "route_description";
    private static final String COL_ESTIMATED_STEPS = "estimated_steps";

    // User preferences table
    private static final String TABLE_PREFERENCES = "user_preferences";
    private static final String COL_AVG_STEP_LENGTH = "average_step_length";
    private static final String COL_VOICE_SPEED = "voice_speed";
    private static final String COL_VOICE_VOLUME = "voice_volume";
    private static final String COL_ACCESSIBILITY_MODE = "accessibility_mode";

    // Navigation history table
    private static final String TABLE_HISTORY = "user_navigation_history";
    private static final String COL_START_TIME = "start_time";
    private static final String COL_END_TIME = "end_time";
    private static final String COL_DISTANCE_TRAVELED = "distance_traveled";

    private static DatabaseService instance;

    private DatabaseService(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseService getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseService(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create campus_locations table
        String createLocationsTable = "CREATE TABLE " + TABLE_LOCATIONS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT NOT NULL, " +
                COL_LATITUDE + " REAL NOT NULL, " +
                COL_LONGITUDE + " REAL NOT NULL, " +
                COL_CATEGORY + " TEXT, " +
                COL_DESCRIPTION + " TEXT, " +
                COL_CREATED_AT + " TEXT)";
        db.execSQL(createLocationsTable);

        // Create campus_routes table
        String createRoutesTable = "CREATE TABLE " + TABLE_ROUTES + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_FROM_ID + " INTEGER NOT NULL, " +
                COL_TO_ID + " INTEGER NOT NULL, " +
                COL_DISTANCE + " REAL, " +
                COL_ROUTE_DESC + " TEXT, " +
                COL_ESTIMATED_STEPS + " INTEGER, " +
                "FOREIGN KEY (" + COL_FROM_ID + ") REFERENCES " + TABLE_LOCATIONS + "(" + COL_ID + "), " +
                "FOREIGN KEY (" + COL_TO_ID + ") REFERENCES " + TABLE_LOCATIONS + "(" + COL_ID + "))";
        db.execSQL(createRoutesTable);

        // Create user_preferences table
        String createPreferencesTable = "CREATE TABLE " + TABLE_PREFERENCES + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_AVG_STEP_LENGTH + " REAL DEFAULT 0.7, " +
                COL_VOICE_SPEED + " REAL DEFAULT 0.8, " +
                COL_VOICE_VOLUME + " INTEGER DEFAULT 100, " +
                COL_ACCESSIBILITY_MODE + " INTEGER DEFAULT 1)";
        db.execSQL(createPreferencesTable);

        // Create navigation_history table
        String createHistoryTable = "CREATE TABLE " + TABLE_HISTORY + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_FROM_ID + " INTEGER NOT NULL, " +
                COL_TO_ID + " INTEGER NOT NULL, " +
                COL_START_TIME + " TEXT, " +
                COL_END_TIME + " TEXT, " +
                COL_DISTANCE_TRAVELED + " REAL, " +
                "FOREIGN KEY (" + COL_FROM_ID + ") REFERENCES " + TABLE_LOCATIONS + "(" + COL_ID + "), " +
                "FOREIGN KEY (" + COL_TO_ID + ") REFERENCES " + TABLE_LOCATIONS + "(" + COL_ID + "))";
        db.execSQL(createHistoryTable);

        // Initialize default user preferences
        initializeDefaultPreferences(db);

        // Initialize default campus locations
        initializeDefaultLocations(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PREFERENCES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        onCreate(db);
    }

    private void initializeDefaultPreferences(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COL_AVG_STEP_LENGTH, 0.7);
        values.put(COL_VOICE_SPEED, 0.8);
        values.put(COL_VOICE_VOLUME, 100);
        values.put(COL_ACCESSIBILITY_MODE, 1);
        db.insert(TABLE_PREFERENCES, null, values);
    }

    private void initializeDefaultLocations(SQLiteDatabase db) {
        // Heilongjiang Oriental College - estimated coordinates
        // These are approximate coordinates for Harbin area
        List<CampusLocation> defaultLocations = new ArrayList<>();
        defaultLocations.add(new CampusLocation("图书馆", 45.7535, 126.6485, "library", "校园图书馆，提供学习资料和自习环境"));
        defaultLocations.add(new CampusLocation("主楼", 45.7540, 126.6490, "building", "行政主楼，行政办公区域"));
        defaultLocations.add(new CampusLocation("食堂", 45.7530, 126.6470, "canteen", "学生食堂，提供餐饮服务"));
        defaultLocations.add(new CampusLocation("教学楼", 45.7538, 126.6480, "building", "主要教学楼，上课场所"));
        defaultLocations.add(new CampusLocation("体育馆", 45.7545, 126.6475, "gym", "体育活动中心和体育馆"));
        defaultLocations.add(new CampusLocation("南门", 45.7525, 126.6485, "gate", "校园南门入口"));
        defaultLocations.add(new CampusLocation("北门", 45.7550, 126.6495, "gate", "校园北门入口"));
        defaultLocations.add(new CampusLocation("宿舍区", 45.7542, 126.6470, "building", "学生宿舍区域"));

        for (CampusLocation location : defaultLocations) {
            ContentValues values = new ContentValues();
            values.put(COL_NAME, location.getName());
            values.put(COL_LATITUDE, location.getLatitude());
            values.put(COL_LONGITUDE, location.getLongitude());
            values.put(COL_CATEGORY, location.getCategory());
            values.put(COL_DESCRIPTION, location.getDescription());
            values.put(COL_CREATED_AT, location.getCreatedAt());
            db.insert(TABLE_LOCATIONS, null, values);
        }

        Log.d("DatabaseService", "Initialized " + defaultLocations.size() + " default campus locations");
    }

    // CampusLocation CRUD operations
    public long addLocation(CampusLocation location) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, location.getName());
        values.put(COL_LATITUDE, location.getLatitude());
        values.put(COL_LONGITUDE, location.getLongitude());
        values.put(COL_CATEGORY, location.getCategory());
        values.put(COL_DESCRIPTION, location.getDescription());
        values.put(COL_CREATED_AT, location.getCreatedAt());
        long id = db.insert(TABLE_LOCATIONS, null, values);
        db.close();
        return id;
    }

    public boolean updateLocation(CampusLocation location) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, location.getName());
        values.put(COL_LATITUDE, location.getLatitude());
        values.put(COL_LONGITUDE, location.getLongitude());
        values.put(COL_CATEGORY, location.getCategory());
        values.put(COL_DESCRIPTION, location.getDescription());
        int rowsAffected = db.update(TABLE_LOCATIONS, values, COL_ID + " = ?",
                new String[]{String.valueOf(location.getId())});
        db.close();
        return rowsAffected > 0;
    }

    public boolean deleteLocation(long locationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_LOCATIONS, COL_ID + " = ?",
                new String[]{String.valueOf(locationId)});
        db.close();
        return rowsAffected > 0;
    }

    public CampusLocation getLocationById(long locationId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_LOCATIONS, null, COL_ID + " = ?",
                new String[]{String.valueOf(locationId)}, null, null, null);
        CampusLocation location = null;
        if (cursor != null && cursor.moveToFirst()) {
            location = cursorToLocation(cursor);
            cursor.close();
        }
        db.close();
        return location;
    }

    public List<CampusLocation> getAllLocations() {
        List<CampusLocation> locations = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_LOCATIONS, null, null, null, null, null, COL_NAME + " ASC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                locations.add(cursorToLocation(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return locations;
    }

    public List<CampusLocation> getLocationsByCategory(String category) {
        List<CampusLocation> locations = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_LOCATIONS, null, COL_CATEGORY + " = ?",
                new String[]{category}, null, null, COL_NAME + " ASC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                locations.add(cursorToLocation(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return locations;
    }

    public long getLocationIdByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_LOCATIONS, new String[]{COL_ID}, COL_NAME + " = ?",
                new String[]{name}, null, null, null);
        long id = -1;
        if (cursor != null && cursor.moveToFirst()) {
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
            cursor.close();
        }
        db.close();
        return id;
    }

    private CampusLocation cursorToLocation(Cursor cursor) {
        CampusLocation location = new CampusLocation();
        location.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)));
        location.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)));
        location.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LATITUDE)));
        location.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LONGITUDE)));
        location.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)));
        location.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)));
        location.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COL_CREATED_AT)));
        return location;
    }

    // Route operations
    public long addRoute(Route route) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FROM_ID, route.getFromLocationId());
        values.put(COL_TO_ID, route.getToLocationId());
        values.put(COL_DISTANCE, route.getDistanceMeters());
        values.put(COL_ROUTE_DESC, route.getRouteDescription());
        values.put(COL_ESTIMATED_STEPS, route.getEstimatedSteps());
        long id = db.insert(TABLE_ROUTES, null, values);
        db.close();
        return id;
    }

    public List<Route> getRoutesFromLocation(long fromLocationId) {
        List<Route> routes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ROUTES, null, COL_FROM_ID + " = ?",
                new String[]{String.valueOf(fromLocationId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                routes.add(cursorToRoute(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return routes;
    }

    private Route cursorToRoute(Cursor cursor) {
        Route route = new Route();
        route.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)));
        route.setFromLocationId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_FROM_ID)));
        route.setToLocationId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_TO_ID)));
        route.setDistanceMeters(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_DISTANCE)));
        route.setRouteDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_ROUTE_DESC)));
        route.setEstimatedSteps(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ESTIMATED_STEPS)));
        return route;
    }

    // User preferences operations
    public double getAverageStepLength() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PREFERENCES, new String[]{COL_AVG_STEP_LENGTH},
                COL_ID + " = 1", null, null, null, null);
        double stepLength = 0.7;
        if (cursor != null && cursor.moveToFirst()) {
            stepLength = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_AVG_STEP_LENGTH));
            cursor.close();
        }
        db.close();
        return stepLength;
    }

    public boolean updateAverageStepLength(double stepLength) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_AVG_STEP_LENGTH, stepLength);
        int rowsAffected = db.update(TABLE_PREFERENCES, values, COL_ID + " = 1", null);
        db.close();
        return rowsAffected > 0;
    }

    public double getVoiceSpeed() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PREFERENCES, new String[]{COL_VOICE_SPEED},
                COL_ID + " = 1", null, null, null, null);
        double voiceSpeed = 0.8;
        if (cursor != null && cursor.moveToFirst()) {
            voiceSpeed = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_VOICE_SPEED));
            cursor.close();
        }
        db.close();
        return voiceSpeed;
    }

    public boolean updateVoiceSpeed(double voiceSpeed) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_VOICE_SPEED, voiceSpeed);
        int rowsAffected = db.update(TABLE_PREFERENCES, values, COL_ID + " = 1", null);
        db.close();
        return rowsAffected > 0;
    }

    public int getVoiceVolume() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PREFERENCES, new String[]{COL_VOICE_VOLUME},
                COL_ID + " = 1", null, null, null, null);
        int voiceVolume = 100;
        if (cursor != null && cursor.moveToFirst()) {
            voiceVolume = cursor.getInt(cursor.getColumnIndexOrThrow(COL_VOICE_VOLUME));
            cursor.close();
        }
        db.close();
        return voiceVolume;
    }

    public boolean updateVoiceVolume(int voiceVolume) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_VOICE_VOLUME, voiceVolume);
        int rowsAffected = db.update(TABLE_PREFERENCES, values, COL_ID + " = 1", null);
        db.close();
        return rowsAffected > 0;
    }

    public boolean getAccessibilityMode() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PREFERENCES, new String[]{COL_ACCESSIBILITY_MODE},
                COL_ID + " = 1", null, null, null, null);
        boolean accessibilityMode = true;
        if (cursor != null && cursor.moveToFirst()) {
            accessibilityMode = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ACCESSIBILITY_MODE)) == 1;
            cursor.close();
        }
        db.close();
        return accessibilityMode;
    }

    public boolean updateAccessibilityMode(boolean accessibilityMode) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ACCESSIBILITY_MODE, accessibilityMode ? 1 : 0);
        int rowsAffected = db.update(TABLE_PREFERENCES, values, COL_ID + " = 1", null);
        db.close();
        return rowsAffected > 0;
    }
}
