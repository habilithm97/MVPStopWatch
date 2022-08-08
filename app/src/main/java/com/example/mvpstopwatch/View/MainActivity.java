package com.example.mvpstopwatch.View;

import androidx.annotation.NonNull;
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

import com.example.mvpstopwatch.MyService;
import com.example.mvpstopwatch.Presenter.Contract;
import com.example.mvpstopwatch.Presenter.MainPresenter;
import com.example.mvpstopwatch.R;

import org.w3c.dom.Text;

//View
public class MainActivity extends AppCompatActivity implements Contract.View {
    private static final String TAG = "MainActivity";

    public static TextView timeTv;
    TextView recordsTv;
    Button subBtn, mainBtn;
    ScrollView scrollView;

    private Contract.Presenter presenter; // Presenter와 통신하기 위한 객체 생성
    public static boolean isRunning = false;
    int mainButtonCount = 0;
    int num = 0; // 기록 번호 관련 변수
    //int num2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        presenter = new MainPresenter(this);
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "액티비티가 종료됨. ");

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
    public void mainResult() {
        if(mainButtonCount == 0) {
            startTimer();
        } else {
            PauseAndRestartTimer();
       }
    }

    @Override
    public void subResult() {
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

        Intent intent = new Intent(MainActivity.this, MyService.class);
        startService(intent);

        isRunning = true; // 실행중인가? -> Yes
        mainButtonCount++; // 버튼을 누른 상태 1
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
    }

    protected void saveRecord() {
        SharedPreferences sharedPreferences = getSharedPreferences("pref", Activity.MODE_PRIVATE); // "pref"는 저장소 이름
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // 저장하려는 데이터 설정
        editor.putString("record", recordsTv.getText().toString());
        editor.putInt("num", num);
        editor.commit(); // 실제로 저장함
    }

    protected void restoreRecord() {
        SharedPreferences sharedPreferences = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        if((sharedPreferences != null) && (sharedPreferences.contains("record"))) {
            String record = sharedPreferences.getString("record", "");
            recordsTv.setText(record);
            num = sharedPreferences.getInt("num", 1);
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
        }
    }
}

/*
1. 시간이 기록된 상태에서 앱을 종료하고 다시 실행하면 기록 카운트가 다시 1부터 시작하는 버그
2. 알림에 스레드 표시
 */