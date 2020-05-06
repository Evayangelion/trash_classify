package lht.trash;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

import lht.trash.database.GarbageData;
import lht.trash.database.GarbageDataDAO;
import lht.trash.database.GarbageDatabase;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 */

public class voice_searchFragment extends Fragment {

    Button speak;
    TextView resulttext;
    EditText voice_search_content;
    GarbageDatabase garbageDatabase;
    GarbageDataDAO garbageDataDao;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String , String>();
    public voice_searchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_voice_search, container, false);
    }







    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        speak=getView().findViewById(R.id.bt_speak);
        resulttext=getView().findViewById(R.id.text_result);
        //voice_search_content=getView().findViewById(R.id.voice_search_content);

        //数据库对象初始化
        garbageDatabase = Room.databaseBuilder(this.getContext(),GarbageDatabase.class,"garbage_database")
                .allowMainThreadQueries()
                .build();
        garbageDataDao=garbageDatabase.getGarbageDataDao();

        //说话按钮点击事件注册
        speak = (Button) getView().findViewById(R.id.bt_speak );
        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeechDialog();
            }
        });

        //getView().findViewById(R.id.bt_voice_to_text).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_voice_searchFragment_to_text_searchFragment));
        //getView().findViewById(R.id.bt_voice_to_pic).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_voice_searchFragment_to_pic_searchFragment));
    }





    private void startSpeechDialog() {
        //1. 创建RecognizerDialog对象
        RecognizerDialog mDialog = new RecognizerDialog(this.getActivity(), new MyInitListener()) ;
        //2. 设置accent、 language等参数
        mDialog.setParameter(SpeechConstant. LANGUAGE, "zh_cn" );// 设置中文
        mDialog.setParameter(SpeechConstant. ACCENT, "mandarin" );
        mDialog.setParameter(SpeechConstant.ASR_PTT, "0");//取消标点
        // 若要将UI控件用于语义理解，必须添加以下参数设置，设置之后 onResult回调返回将是语义理解
        // 结果
        // mDialog.setParameter("asr_sch", "1");
        // mDialog.setParameter("nlp_version", "2.0");
        //3.设置回调接口
        mDialog.setListener( new MyRecognizerDialogListener()) ;
        //4. 显示dialog，接收语音输入
        mDialog.show() ;
    }

    class MyRecognizerDialogListener implements RecognizerDialogListener {

        /**
         * @param results
         * @param isLast  是否说完了
         */
        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            String result = results.getResultString(); //为解析的
            showTip(result) ;
            System. out.println(" 没有解析的 :" + result);

            String text = JsonParser.parseIatResult(result) ;//解析过后的
            System. out.println(" 解析后的 :" + text);

            String sn = null;
            // 读取json结果中的 sn字段
            try {
                JSONObject resultJson = new JSONObject(results.getResultString()) ;
                sn = resultJson.optString("sn" );
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mIatResults .put(sn, text) ;//没有得到一句，添加到

            StringBuffer resultBuffer = new StringBuffer();
            for (String key : mIatResults.keySet()) {
                resultBuffer.append(mIatResults .get(key));
            }

            //voice_search_content.setText(resultBuffer.toString());// 设置输入框的文本

            //获取输入框结果
            //String search=voice_search_content.getText().toString();
            //获取语音识别结果
            String search = resultBuffer.toString();

            //从数据库查询
            GarbageData voice_search_result = garbageDataDao.searchGarbage(search);
            try{
                String catg="";
                switch (voice_search_result.getCategory()){
                    case "1":catg+="可回收垃圾";break;
                    case "2":catg+="有害垃圾";break;
                    case "4":catg+="厨余垃圾";break;
                    case "8":catg+="其他垃圾";break;
                    case "16":catg+="其他垃圾";break;
                }
                String showResult=voice_search_result.getId()+":"+voice_search_result.getStuff()+"属于"+catg;
                resulttext.setText(showResult);
            }catch (Exception e){
                Toast.makeText(getActivity(),"没有这种垃圾",Toast.LENGTH_SHORT).show();
                //String showResult="这东西不用回收，扔了吧";
                //textView.setText(showResult);
            }
        }

        @Override
        public void onError(SpeechError speechError) {

        }
    }

    class MyInitListener implements InitListener {

        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败 ");
            }

        }
    }




    // 听写监听器
    private RecognizerListener mRecoListener = new RecognizerListener() {
        // 听写结果回调接口 (返回Json 格式结果，用户可参见附录 13.1)；
//一般情况下会通过onResults接口多次返回结果，完整的识别内容是多次结果的累加；
//关于解析Json的代码可参见 Demo中JsonParser 类；
//isLast等于true 时会话结束。
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.e (TAG, results.getResultString());
            System.out.println(results.getResultString()) ;
            showTip(results.getResultString()) ;
        }

        // 会话发生错误回调接口
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true)) ;
            // 获取错误码描述
            Log. e(TAG, "error.getPlainDescription(true)==" + error.getPlainDescription(true ));
        }

        // 开始录音
        public void onBeginOfSpeech() {
            showTip(" 开始录音 ");
        }

        //volume 音量值0~30， data音频数据
        public void onVolumeChanged(int volume, byte[] data) {
            showTip(" 声音改变了 ");
        }

        // 结束录音
        public void onEndOfSpeech() {
            showTip(" 结束录音 ");
        }

        // 扩展用接口
        public void onEvent(int eventType, int arg1 , int arg2, Bundle obj) {
        }
    };

    private void showTip (String data) {
        Toast.makeText( getActivity(), data, Toast.LENGTH_SHORT).show() ;
    }
}

