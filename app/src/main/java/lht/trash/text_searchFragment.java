package lht.trash;

import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import lht.trash.database.GarbageData;
import lht.trash.database.GarbageDataDAO;
import lht.trash.database.GarbageDatabase;


/**
 * A simple {@link Fragment} subclass.
 */
public class text_searchFragment extends Fragment {

    // Required empty public constructor
    GarbageDatabase garbageDatabase;
    GarbageDataDAO garbageDataDao;
    //Button buttonQuery;
    TextView textView;
    //输入框对象
    //EditText searchContent;

    private SearchView searchView;
    private ListView listView;
    private SimpleCursorAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view=inflater.inflate(R.layout.fragment_text_search,container,false);
        setHasOptionsMenu(true);
        return view;
    }

    private void setAdapter(Cursor cursor) {
        if (listView.getAdapter() == null) {
            adapter = new SimpleCursorAdapter(getActivity(), R.layout.list, cursor, new String[]{"stuff_name"}, new int[]{R.id.text1});
            listView.setAdapter(adapter);
        } else {
            ((SimpleCursorAdapter) listView.getAdapter()).changeCursor(cursor);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //数据库操作
        garbageDatabase = Room.databaseBuilder(this.getContext(), GarbageDatabase.class,"garbage_database")
                .allowMainThreadQueries()
                .build();
        garbageDataDao=garbageDatabase.getGarbageDataDao();


        //资源绑定
        textView=getView().findViewById(R.id.text_result);
        //searchContent=getView().findViewById(R.id.search_content);
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

        //配置搜索框内容监听器
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            //点击提交按钮后才触发
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(getActivity(),"you choose:" + query,Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            //发生内容改变时触发
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {//如果这个文字等于空
                    return false;
                }else {
                    Cursor cursor= TextUtils.isEmpty(newText)?null:garbageDataDao.rawSearchGarbage(newText);
                    int t=cursor.getCount();
                    Toast.makeText(getActivity(), ""+t, Toast.LENGTH_SHORT).show();
                    setAdapter(cursor);
                }
                return false;
            }
        });



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //String t=adapter.getCursor().getColumnName((int)id);
                Toast.makeText(getActivity(),"第几个啊到底"+position,Toast.LENGTH_LONG).show();
                Cursor cur=adapter.getCursor();
                cur.moveToPosition(position);
                String tap=cur.getString(cur.getColumnIndex("stuff_name"));
                Toast.makeText(getActivity(),"这个指向的是"+tap,Toast.LENGTH_LONG).show();

                //Toast.makeText(getActivity(), tap, Toast.LENGTH_SHORT).show();
                GarbageData result = garbageDataDao.searchGarbage(tap);
                try{

                    String catg="";
                    switch (result.getCategory()){
                        case "1":catg+="可回收垃圾";break;
                        case "2":catg+="有害垃圾";break;
                        case "4":catg+="厨余垃圾";break;
                        case "8":catg+="其他垃圾";break;
                        case "16":catg+="其他垃圾";break;
                    }
                    String showResult=result.getId()+":"+result.getStuff()+"属于"+catg;
                    textView.setText(showResult);
                    //点击后取消焦点收起键盘
                    searchView.clearFocus();
                    searchView.onActionViewCollapsed();
                    listView.setAdapter(null);
                }catch (Exception e){
                    //Toast.makeText(getActivity(),"没有这种垃圾",Toast.LENGTH_SHORT).show();
                    //String showResult="这东西不用回收，扔了吧";
                    //textView.setText(showResult);
                }


            }
        });

        //实现点击按钮查询输入框中的内容
        //已废弃 改用searchview+listview实现
        /*buttonQuery=getView().findViewById(R.id.bt_query);
        buttonQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
            }
        });*/

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

}
