package com.taotao.healthtracker.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.taotao.healthtracker.data.dao.HealthDao;
import com.taotao.healthtracker.data.dao.HealthDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile HealthDao _healthDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(8) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `health_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userId` INTEGER NOT NULL, `date` TEXT NOT NULL, `sbp` INTEGER, `dbp` INTEGER, `hr` INTEGER, `weight` REAL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `user_profiles` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `birthYear` INTEGER NOT NULL, `birthMonth` INTEGER NOT NULL, `birthDay` INTEGER NOT NULL, `height` REAL NOT NULL, `language` TEXT NOT NULL, `insightLanguage` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `almanac_data` (`date` TEXT NOT NULL, `yi` TEXT NOT NULL, `ji` TEXT NOT NULL, `lunarDate` TEXT NOT NULL, PRIMARY KEY(`date`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '8f2ced5f529c201493c1b3bf3a3d6730')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `health_records`");
        db.execSQL("DROP TABLE IF EXISTS `user_profiles`");
        db.execSQL("DROP TABLE IF EXISTS `almanac_data`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsHealthRecords = new HashMap<String, TableInfo.Column>(7);
        _columnsHealthRecords.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHealthRecords.put("userId", new TableInfo.Column("userId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHealthRecords.put("date", new TableInfo.Column("date", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHealthRecords.put("sbp", new TableInfo.Column("sbp", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHealthRecords.put("dbp", new TableInfo.Column("dbp", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHealthRecords.put("hr", new TableInfo.Column("hr", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHealthRecords.put("weight", new TableInfo.Column("weight", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysHealthRecords = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesHealthRecords = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoHealthRecords = new TableInfo("health_records", _columnsHealthRecords, _foreignKeysHealthRecords, _indicesHealthRecords);
        final TableInfo _existingHealthRecords = TableInfo.read(db, "health_records");
        if (!_infoHealthRecords.equals(_existingHealthRecords)) {
          return new RoomOpenHelper.ValidationResult(false, "health_records(com.taotao.healthtracker.data.entity.HealthRecord).\n"
                  + " Expected:\n" + _infoHealthRecords + "\n"
                  + " Found:\n" + _existingHealthRecords);
        }
        final HashMap<String, TableInfo.Column> _columnsUserProfiles = new HashMap<String, TableInfo.Column>(8);
        _columnsUserProfiles.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProfiles.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProfiles.put("birthYear", new TableInfo.Column("birthYear", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProfiles.put("birthMonth", new TableInfo.Column("birthMonth", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProfiles.put("birthDay", new TableInfo.Column("birthDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProfiles.put("height", new TableInfo.Column("height", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProfiles.put("language", new TableInfo.Column("language", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProfiles.put("insightLanguage", new TableInfo.Column("insightLanguage", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUserProfiles = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUserProfiles = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUserProfiles = new TableInfo("user_profiles", _columnsUserProfiles, _foreignKeysUserProfiles, _indicesUserProfiles);
        final TableInfo _existingUserProfiles = TableInfo.read(db, "user_profiles");
        if (!_infoUserProfiles.equals(_existingUserProfiles)) {
          return new RoomOpenHelper.ValidationResult(false, "user_profiles(com.taotao.healthtracker.data.entity.UserProfile).\n"
                  + " Expected:\n" + _infoUserProfiles + "\n"
                  + " Found:\n" + _existingUserProfiles);
        }
        final HashMap<String, TableInfo.Column> _columnsAlmanacData = new HashMap<String, TableInfo.Column>(4);
        _columnsAlmanacData.put("date", new TableInfo.Column("date", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlmanacData.put("yi", new TableInfo.Column("yi", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlmanacData.put("ji", new TableInfo.Column("ji", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlmanacData.put("lunarDate", new TableInfo.Column("lunarDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAlmanacData = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAlmanacData = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAlmanacData = new TableInfo("almanac_data", _columnsAlmanacData, _foreignKeysAlmanacData, _indicesAlmanacData);
        final TableInfo _existingAlmanacData = TableInfo.read(db, "almanac_data");
        if (!_infoAlmanacData.equals(_existingAlmanacData)) {
          return new RoomOpenHelper.ValidationResult(false, "almanac_data(com.taotao.healthtracker.data.entity.AlmanacData).\n"
                  + " Expected:\n" + _infoAlmanacData + "\n"
                  + " Found:\n" + _existingAlmanacData);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "8f2ced5f529c201493c1b3bf3a3d6730", "bb37632b5a9bac10ba0ce7dd2d626f2c");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "health_records","user_profiles","almanac_data");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `health_records`");
      _db.execSQL("DELETE FROM `user_profiles`");
      _db.execSQL("DELETE FROM `almanac_data`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(HealthDao.class, HealthDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public HealthDao healthDao() {
    if (_healthDao != null) {
      return _healthDao;
    } else {
      synchronized(this) {
        if(_healthDao == null) {
          _healthDao = new HealthDao_Impl(this);
        }
        return _healthDao;
      }
    }
  }
}
