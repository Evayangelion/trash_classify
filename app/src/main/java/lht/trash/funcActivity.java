package lht.trash;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.room.Room;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

import java.util.ArrayList;
import java.util.List;

public class funcActivity extends AppCompatActivity {

    /*动态权限申请*/
    private void initPermission() {
        String permission[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ArrayList<String> applyList = new ArrayList<>();

        for (String per : permission) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, per)) {
                applyList.add(per);
            }
        }

        String tmpList[] = new String[applyList.size()];
        if (!applyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, applyList.toArray(tmpList), 123);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_func);
        initPermission();
        //语音听写初始化
        SpeechUtility.createUtility(this, SpeechConstant.APPID +"=5e66772c");

        //标题栏变化
        NavController controller= Navigation.findNavController(this,R.id.fragment);
        NavigationUI.setupActionBarWithNavController(this,controller);
        //需要重载onSupportNavigateUp


        //获取底部导航栏对象
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavController navController = Navigation.findNavController(this,R.id.fragment);
        //装配导航栏
        AppBarConfiguration configuration =new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this,navController,configuration);
        NavigationUI.setupWithNavController(bottomNavigationView,navController);
    }
    @Override
    public boolean onSupportNavigateUp(){
        NavController controller = Navigation.findNavController(this,R.id.fragment);
        return controller.navigateUp();
    }



}
