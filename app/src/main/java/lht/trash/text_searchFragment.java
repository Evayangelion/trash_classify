package lht.trash;

import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import lht.trash.R;

import java.util.List;

import lht.trash.database.GarbageData;
import lht.trash.database.GarbageDataDao;
import lht.trash.database.GarbageDatabase;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class text_searchFragment extends Fragment {

    GarbageDatabase garbageDatabase;
    GarbageDataDao garbageDataDao;
    Button buttonQuery;
    TextView textView;
    //输入框对象
    EditText searchContent;

    private SearchView searchView;
    private ListView listView;





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        return inflater.inflate(R.layout.fragment_text_search, container, false);
    }
    /*//属性改变 需要版本迁移
    private static Migration migration1_2 = new Migration(1,2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

        }
    };*/
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //数据库操作
        garbageDatabase = Room.databaseBuilder(this.getContext(), GarbageDatabase.class,"garbage_database")
                //允许主线程运行
                .allowMainThreadQueries()
                .build();

        garbageDataDao=garbageDatabase.getGarbageDataDao();
        if(garbageDataDao==null)
            System.out.println("noooooooooooooo");

        /*List<GarbageData> result=garbageDataDao.getALL();
        String[] gstring=new String[result.size()];
        int i=0;
        for(GarbageData g:result){
            gstring[i]=g.getStuff();
            i++;
        }*/


        //资源绑定
        textView=getView().findViewById(R.id.text_result);
        searchContent=getView().findViewById(R.id.search_content);
        //updateView();

        listView = getView().findViewById(R.id.lv);

        //为ListView启动过滤
        listView.setTextFilterEnabled(true);
        searchView = getView().findViewById(R.id.sv);
        //设置SearchView自动缩小为图标
        searchView.setIconifiedByDefault(false);//设为true则搜索栏 缩小成俄日一个图标点击展开
        //设置该SearchView显示搜索按钮
        searchView.setSubmitButtonEnabled(true);
        //设置默认提示文字
        searchView.setQueryHint("输入您想查找的内容");
        //配置监听器
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //点击搜索按钮时触发
            @Override
            public boolean onQueryTextSubmit(String query) {
                //此处添加查询开始后的具体时间和方法
                Toast.makeText(getActivity(),"you choose:" + query,Toast.LENGTH_SHORT).show();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                /*if (TextUtils.isEmpty(newText)) {//如果这个文字等于空
                    //清除listview的过滤
                    listView.clearTextFilter();
                }else {
                    listView.setFilterText(newText);
                }
*/
                Cursor cursor=garbageDataDao.rawSearchGarbage(newText);
                if(searchView.getSuggestionsAdapter()==null){
                    searchView.setSuggestionsAdapter(new SimpleCursorAdapter(getContext(),R.layout.list,cursor,new String[]{"stuff_name"}, new int[]{R.id.text1}, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER));
                }
                else{
                    searchView.getSuggestionsAdapter().changeCursor(cursor);
                }
                return true;
            }
        });
        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Object string = adapter.getItem(position);
                //searchView.setQuery(string.toString(),true);

            }
        });


        //实现点击按钮查询输入框中的内容
        buttonQuery=getView().findViewById(R.id.bt_query);
        buttonQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //GarbageData data1=new GarbageData("阿司匹林","2");
                //GarbageData data2=new GarbageData("阿尔卑斯糖","4");
                //garbageDataDao.insertData(data1,data2);
                String search=searchContent.getText().toString();

                GarbageData result = garbageDataDao.searchGarbage(search);
                try{

                    String catg="";
                    switch (result.getCategory()){
                        case "1":catg+="可回收垃圾";break;
                        case "2":catg+="有害垃圾";break;
                        case "4":catg+="湿垃圾";break;
                        case "8":catg+="干垃圾";break;
                        case "16":catg+="大件垃圾";break;
                    }
                    String showResult=result.getId()+":"+result.getStuff()+"属于"+catg;
                    textView.setText(showResult);
                }catch (Exception e){
                    Toast.makeText(getActivity(),"没有这种垃圾",Toast.LENGTH_SHORT).show();
                    //String showResult="这东西不用回收，扔了吧";
                    //textView.setText(showResult);
                }






                //garbageDataDao.getALL();
                //updateView();
            }
        });

        //切换fragment的工作
        /*Button bt_toVoice;
        bt_toVoice=getView().findViewById(R.id.bt_text_to_voice);
        bt_toVoice.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                NavController controller = Navigation.findNavController(view);
                //查找action的id
                controller.navigate(R.id.action_text_searchFragment_to_voice_searchFragment);
            }
        });
        getView().findViewById(R.id.bt_text_to_pic).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_text_searchFragment_to_pic_searchFragment));
*/
    }

    //实现搜索联想

}
