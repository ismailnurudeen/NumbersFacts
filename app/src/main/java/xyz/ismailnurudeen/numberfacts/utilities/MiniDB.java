package xyz.ismailnurudeen.numberfacts.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.util.List;
import java.util.Set;

public class MiniDB {
    private Context mContext;
    private String mName;
    private Gson mGson;
    private SharedPreferences mSharedPref;

    private MiniDB(Context context, String name) {
        mContext = context;
        mName = name;
        mGson = new Gson();
        mSharedPref = mContext.getSharedPreferences(mName, Context.MODE_PRIVATE);
    }

    @NonNull
    public static MiniDB open(Context context, String dbName) {
        return new MiniDB(context, dbName);
    }

    public boolean insertString(String key, String value) {
        return mSharedPref.edit().putString(key, value).commit();
    }

    public boolean insertBoolean(String key, Boolean value) {
        return mSharedPref.edit().putBoolean(key, value).commit();
    }

    public boolean insertStringSet(String key, Set<String> value) {
        return mSharedPref.edit().putStringSet(key, value).commit();
    }

    public boolean insertStringArray(String key, String[] value) {
        return mSharedPref.edit().putString(key, mGson.toJson(value)).commit();
    }

    public boolean insertIntArray(String key, Integer[] value) {
        return mSharedPref.edit().putString(key, mGson.toJson(value)).commit();
    }

    public boolean insertList(String key, List<Object> value) {
        return mSharedPref.edit().putString(key, mGson.toJson(value)).commit();
    }

    public boolean insertObject(String key, Object value) {
        return mSharedPref.edit().putString(key, mGson.toJson(value)).commit();
    }

    public String readString(String key, String defaultValue) {
        return mSharedPref.getString(key, defaultValue);
    }

    public int readInt(String key, int defaultValue) {
        return mSharedPref.getInt(key, defaultValue);
    }

    public boolean readBoolean(String key, Boolean defaultValue) {
        return mSharedPref.getBoolean(key, defaultValue);
    }

    public Object readObject(String key, Object defaultValue) {
        String objectStr = mSharedPref.getString(key, "");
        if (!objectStr.equalsIgnoreCase("")) {
            return mGson.fromJson(objectStr, defaultValue.getClass());
        }
        return defaultValue;
    }

    public List readList(String key, List<Object> defaultValue) {
        String objectStr = mSharedPref.getString(key, "");
        if (!objectStr.equalsIgnoreCase("")) {
            return mGson.fromJson(objectStr, defaultValue.getClass());
        }
        return defaultValue;
    }

    public String[] readStringArray(String key, String[] defaultValue) {
        String objectStr = mSharedPref.getString(key, "");
        if (!objectStr.equalsIgnoreCase("")) {
            return mGson.fromJson(objectStr, defaultValue.getClass());
        }
        return defaultValue;
    }

    public boolean deleteAll() {
        return mSharedPref.edit().clear().commit();
    }

    public void deleteValue(String key) {
        mSharedPref.edit().remove(key).apply();
    }

}
