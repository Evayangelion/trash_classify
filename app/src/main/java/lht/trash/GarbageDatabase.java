package lht.trash;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {GarbageData.class},version=1,exportSchema =false)
//实现的代码都不用写
public abstract class GarbageDatabase extends RoomDatabase {
    //返回一个dao
    public abstract GarbageDataDao getGarbageDataDao();
}
