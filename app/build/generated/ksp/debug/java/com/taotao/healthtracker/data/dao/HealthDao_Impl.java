package com.taotao.healthtracker.data.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.taotao.healthtracker.data.entity.AlmanacData;
import com.taotao.healthtracker.data.entity.HealthRecord;
import com.taotao.healthtracker.data.entity.UserProfile;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Float;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class HealthDao_Impl implements HealthDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<HealthRecord> __insertionAdapterOfHealthRecord;

  private final EntityInsertionAdapter<UserProfile> __insertionAdapterOfUserProfile;

  private final EntityInsertionAdapter<AlmanacData> __insertionAdapterOfAlmanacData;

  private final SharedSQLiteStatement __preparedStmtOfDeleteProfile;

  public HealthDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfHealthRecord = new EntityInsertionAdapter<HealthRecord>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `health_records` (`id`,`userId`,`date`,`sbp`,`dbp`,`hr`,`weight`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final HealthRecord entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getUserId());
        statement.bindString(3, entity.getDate());
        if (entity.getSbp() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getSbp());
        }
        if (entity.getDbp() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getDbp());
        }
        if (entity.getHr() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getHr());
        }
        if (entity.getWeight() == null) {
          statement.bindNull(7);
        } else {
          statement.bindDouble(7, entity.getWeight());
        }
      }
    };
    this.__insertionAdapterOfUserProfile = new EntityInsertionAdapter<UserProfile>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `user_profiles` (`id`,`name`,`birthYear`,`birthMonth`,`birthDay`,`height`,`language`,`insightLanguage`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UserProfile entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getBirthYear());
        statement.bindLong(4, entity.getBirthMonth());
        statement.bindLong(5, entity.getBirthDay());
        statement.bindDouble(6, entity.getHeight());
        statement.bindString(7, entity.getLanguage());
        statement.bindString(8, entity.getInsightLanguage());
      }
    };
    this.__insertionAdapterOfAlmanacData = new EntityInsertionAdapter<AlmanacData>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `almanac_data` (`date`,`yi`,`ji`,`lunarDate`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AlmanacData entity) {
        statement.bindString(1, entity.getDate());
        statement.bindString(2, entity.getYi());
        statement.bindString(3, entity.getJi());
        statement.bindString(4, entity.getLunarDate());
      }
    };
    this.__preparedStmtOfDeleteProfile = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM user_profiles WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertRecord(final HealthRecord record,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfHealthRecord.insert(record);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAll(final List<HealthRecord> records,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfHealthRecord.insert(records);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertProfile(final UserProfile profile,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfUserProfile.insert(profile);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAlmanac(final AlmanacData almanac,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfAlmanacData.insert(almanac);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteProfile(final int id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteProfile.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteProfile.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<HealthRecord>> getRecordsByUser(final int userId) {
    final String _sql = "SELECT * FROM health_records WHERE userId = ? ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"health_records"}, new Callable<List<HealthRecord>>() {
      @Override
      @NonNull
      public List<HealthRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfSbp = CursorUtil.getColumnIndexOrThrow(_cursor, "sbp");
          final int _cursorIndexOfDbp = CursorUtil.getColumnIndexOrThrow(_cursor, "dbp");
          final int _cursorIndexOfHr = CursorUtil.getColumnIndexOrThrow(_cursor, "hr");
          final int _cursorIndexOfWeight = CursorUtil.getColumnIndexOrThrow(_cursor, "weight");
          final List<HealthRecord> _result = new ArrayList<HealthRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final HealthRecord _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final int _tmpUserId;
            _tmpUserId = _cursor.getInt(_cursorIndexOfUserId);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final Integer _tmpSbp;
            if (_cursor.isNull(_cursorIndexOfSbp)) {
              _tmpSbp = null;
            } else {
              _tmpSbp = _cursor.getInt(_cursorIndexOfSbp);
            }
            final Integer _tmpDbp;
            if (_cursor.isNull(_cursorIndexOfDbp)) {
              _tmpDbp = null;
            } else {
              _tmpDbp = _cursor.getInt(_cursorIndexOfDbp);
            }
            final Integer _tmpHr;
            if (_cursor.isNull(_cursorIndexOfHr)) {
              _tmpHr = null;
            } else {
              _tmpHr = _cursor.getInt(_cursorIndexOfHr);
            }
            final Float _tmpWeight;
            if (_cursor.isNull(_cursorIndexOfWeight)) {
              _tmpWeight = null;
            } else {
              _tmpWeight = _cursor.getFloat(_cursorIndexOfWeight);
            }
            _item = new HealthRecord(_tmpId,_tmpUserId,_tmpDate,_tmpSbp,_tmpDbp,_tmpHr,_tmpWeight);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<UserProfile>> getAllProfiles() {
    final String _sql = "SELECT * FROM user_profiles ORDER BY id ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"user_profiles"}, new Callable<List<UserProfile>>() {
      @Override
      @NonNull
      public List<UserProfile> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfBirthYear = CursorUtil.getColumnIndexOrThrow(_cursor, "birthYear");
          final int _cursorIndexOfBirthMonth = CursorUtil.getColumnIndexOrThrow(_cursor, "birthMonth");
          final int _cursorIndexOfBirthDay = CursorUtil.getColumnIndexOrThrow(_cursor, "birthDay");
          final int _cursorIndexOfHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "height");
          final int _cursorIndexOfLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "language");
          final int _cursorIndexOfInsightLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "insightLanguage");
          final List<UserProfile> _result = new ArrayList<UserProfile>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final UserProfile _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpBirthYear;
            _tmpBirthYear = _cursor.getInt(_cursorIndexOfBirthYear);
            final int _tmpBirthMonth;
            _tmpBirthMonth = _cursor.getInt(_cursorIndexOfBirthMonth);
            final int _tmpBirthDay;
            _tmpBirthDay = _cursor.getInt(_cursorIndexOfBirthDay);
            final float _tmpHeight;
            _tmpHeight = _cursor.getFloat(_cursorIndexOfHeight);
            final String _tmpLanguage;
            _tmpLanguage = _cursor.getString(_cursorIndexOfLanguage);
            final String _tmpInsightLanguage;
            _tmpInsightLanguage = _cursor.getString(_cursorIndexOfInsightLanguage);
            _item = new UserProfile(_tmpId,_tmpName,_tmpBirthYear,_tmpBirthMonth,_tmpBirthDay,_tmpHeight,_tmpLanguage,_tmpInsightLanguage);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<AlmanacData> getAlmanacByDate(final String date) {
    final String _sql = "SELECT * FROM almanac_data WHERE date = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, date);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"almanac_data"}, new Callable<AlmanacData>() {
      @Override
      @Nullable
      public AlmanacData call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfYi = CursorUtil.getColumnIndexOrThrow(_cursor, "yi");
          final int _cursorIndexOfJi = CursorUtil.getColumnIndexOrThrow(_cursor, "ji");
          final int _cursorIndexOfLunarDate = CursorUtil.getColumnIndexOrThrow(_cursor, "lunarDate");
          final AlmanacData _result;
          if (_cursor.moveToFirst()) {
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final String _tmpYi;
            _tmpYi = _cursor.getString(_cursorIndexOfYi);
            final String _tmpJi;
            _tmpJi = _cursor.getString(_cursorIndexOfJi);
            final String _tmpLunarDate;
            _tmpLunarDate = _cursor.getString(_cursorIndexOfLunarDate);
            _result = new AlmanacData(_tmpDate,_tmpYi,_tmpJi,_tmpLunarDate);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
