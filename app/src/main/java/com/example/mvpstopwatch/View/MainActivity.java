package com.example.mvpstopwatch.View;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mvpstopwatch.MyService;
import com.example.mvpstopwatch.Presenter.Contract;
import com.example.mvpstopwatch.Presenter.MainPresenter;
import com.example.mvpstopwatch.R;

import org.w3c.dom.Text;

//View
public class MainActivity extends AppCompatActivity implements Contract.View {

    public static TextView timeTv;
    TextView recordsTv;
    Button subBtn, mainBtn;
    ScrollView scrollView;

    private Contract.Presenter presenter;
    public static boolean isRunning = false;
    int mainButtonCount = 0;
    int num = 0; // 기록 번호 관련 변수

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        presenter = new MainPresenter(this); // Presenter와 통신하기 위한 객체 생성
        init();
    }

    public void init(){
        subBtn = (Button)findViewById(R.id.subBtn);
        subBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.subAction();
            }
        });

        mainBtn = (Button)findViewById(R.id.mainBtn);
        mainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.mainAction();
            }
        });

        timeTv = (TextView)findViewById(R.id.timeTv);
        recordsTv = (TextView)findViewById(R.id.recordsTv);
        scrollView = (ScrollView)findViewById(R.id.scrollView);
    }

    @Override
    public void mainResult() { // 메인 버튼 액션
        if(mainButtonCount == 0) {
            startTimer();
        } else {
            PauseAndRestartTimer();
       }
    }

    @Override
    public void subResult() { // 서브 버튼 액션
        if (subBtn.getText().toString().equals("기록")) {
            recordingTime();
        } else if (subBtn.getText().toString().equals("초기화")) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("초기화");
            builder.setMessage("시간을 초기화하시겠습니까?");
            builder.setIcon(R.drawable.timer);
            builder.setPositiveButton("초기화", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    clearTime();
                }
            });
            builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }
    }

    public void startTimer() {
        subBtn.setVisibility(View.VISIBLE);
        subBtn.setText("기록");
        mainBtn.setText("일시정지");

        isRunning = true; // 실행중인가? -> Yes
        mainButtonCount++; // 버튼을 누른 상태 1

        intent = new Intent(MainActivity.this, MyService.class);
        startService(intent);
    }

    public void PauseAndRestartTimer() {
        subBtn.setText("초기화");
        mainBtn.setText("계속");

        isRunning = !isRunning; // 스레드를 멈춤
        mainButtonCount--; // 버튼을 누르지 않은 상태 0
    }

    public void recordingTime() {
        String currentTime = timeTv.getText().toString();
        num++;
        recordsTv.append(num + ") " + currentTime + "\n");
        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }

    public void clearTime() {
        MyService.i = 0;
        timeTv.setText("00:00:00.00");
        subBtn.setVisibility(View.GONE);
        mainBtn.setText("시작");
        recordsTv.setText(null);

        num = 0; // 0으로 초기화하지 않으면 기록 초기화 버튼을 눌러도 저장된 num 값부터 기록됨

        intent = new Intent(MainActivity.this, MyService.class);
        stopService(intent);
    }

    protected void saveRecord() {
        SharedPreferences sharedPreferences = getSharedPreferences("pref", Activity.MODE_PRIVATE); // "pref"는 저장소 이름
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // 저장하려는 데이터 설정
        editor.putString("record", recordsTv.getText().toString());
        editor.putInt("num", num);
        editor.putString("time", timeTv.getText().toString());
        editor.commit(); // 실제로 저장함
    }

    protected void restoreRecord() {
        SharedPreferences sharedPreferences = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        if((sharedPreferences != null) && (sharedPreferences.contains("record"))) {
            String record = sharedPreferences.getString("record", "");
            recordsTv.setText(record);
            num = sharedPreferences.getInt("num", 1);
            String time = sharedPreferences.getString("time", "");
            timeTv.setText(time);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        saveRecord();
    }

    @Override
    protected void onResume() {
        super.onResume();

        restoreRecord();

        if(isRunning == true) {
            mainBtn.setText("일시정지");
            subBtn.setVisibility(View.VISIBLE);
        } else {
            mainBtn.setText("시작");
            subBtn.setVisibility(View.GONE);

            if(!timeTv.getText().toString().equals("00:00:00.00")) {
                mainBtn.setText("계속");
                subBtn.setVisibility(View.VISIBLE);
                subBtn.setText("초기화");
            }
        }
    }
}