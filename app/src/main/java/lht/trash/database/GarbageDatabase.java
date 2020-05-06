package lht.trash.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {GarbageData.class},version=1,exportSchema =false)
//实现的代码都不用写
//单例模式
public abstract class GarbageDatabase extends RoomDatabase {

    //声明为static 保证是唯一单例
    private static GarbageDatabase INSTANCE;
    //synchronized 保证线程安全
    static synchronized GarbageDatabase getDatabase(Context context){
        //懒汉式
        if(INSTANCE==null){
            //context.getApplicationContext()获取全局上下文
            INSTANCE= Room.databaseBuilder(context.getApplicationContext(),GarbageDatabase.class,"garbage_database2")
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }
    //返回一个dao
    public abstract GarbageDataDAO getGarbageDataDao();

    //属性改变 需要版本迁移
    /*private static Migration migration1_2 = new Migration(1,2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("");
        }
    };*/

}


