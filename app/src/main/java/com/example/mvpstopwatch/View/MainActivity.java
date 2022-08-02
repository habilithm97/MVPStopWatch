package com.example.mvpstopwatch.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
    TextView timeTv, recordsTv;
    Button subBtn, mainBtn;
    ScrollView scrollView;

    private Contract.Presenter presenter; // Presenter와 통신하기 위한 객체 생성

    Thread timeThread = null;
    boolean isRunning = true;
    int mainButtonCount = 0;
    int i = 0; // 스레드 관련 변수
    int num = 0; // 기록 번호 관련 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        presenter = new MainPresenter(this);
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

        // 스레드를 생성해서 실행함
        timeThread = new Thread(new TimeThread());
        timeThread.start();

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
        i = 0;
        timeTv.setText("00:00:00.00");
        subBtn.setVisibility(View.GONE);
        mainBtn.setText("시작");
        recordsTv.setText(null);
    }

    // 핸들러를 이용해서 UI를 변경할 수 있음
    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            int mSec = msg.arg1 % 100;
            int sec = (msg.arg1 / 100) % 60;
            int min = (msg.arg1 / 100) / 60 % 60;
            int hour = (msg.arg1 / 100) / 3600 % 24;

            String result = String.format("%02d:%02d:%02d.%02d", hour, min, sec, mSec);
            timeTv.setText(result);
        }
    };

    public class TimeThread implements Runnable {
        @Override
        public void run() {
            while(isRunning) { // 실행중인 상태
                Message msg = new Message();
                msg.arg1 = i++;
                handler.sendMessage(msg);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}