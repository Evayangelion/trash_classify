package lht.trash.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Entity
public class GarbageData {

    @PrimaryKey(autoGenerate = true)//主键自增
    private int _id;

    @ColumnInfo(name="stuff_name")
    private String stuff;

    @ColumnInfo(name="stuff_category")
    private String category;

    public GarbageData(String stuff, String category) {
        this.stuff = stuff;
        this.category = category;
    }



    public int getId() {
        return _id;
    }

    public void setId(int id) {
        this._id = id;
    }

    public String getStuff() {
        return stuff;
    }

    public void setStuff(String stuff) {
        this.stuff = stuff;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
