package lht.trash;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import lht.trash.database.GarbageData;
import lht.trash.database.GarbageDataDao;
import lht.trash.database.GarbageDatabase;


/**
 * A simple {@link Fragment} subclass.
 */
public class text_searchFragment extends Fragment {

    // Required empty public constructor
    GarbageDatabase garbageDatabase;
    GarbageDataDao garbageDataDao;
    Button buttonQuery;
    TextView textView;
    //输入框对象
    EditText searchContent;


    public text_searchFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        return inflater.inflate(R.layout.fragment_text_search, container, false);
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
        searchContent=getView().findViewById(R.id.search_content);
        //updateView();

        //TODO 实现点击按钮查询输入框中的内容
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

}
