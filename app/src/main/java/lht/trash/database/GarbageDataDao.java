package lht.trash.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao //database access object
public interface GarbageDataDao {
    @Insert
    void insertData(GarbageData... garbageData);//可以传递多个参数
    @Update
    int updateData(GarbageData... garbageData);
    @Delete
    void deleteData(GarbageData... garbageData);

    @Query("SELECT * FROM GarbageData WHERE stuff_name=:gname")
    GarbageData searchGarbage(String gname);

    @Query("SELECT * FROM GarbageData ")
    List<GarbageData> getALL();
}
