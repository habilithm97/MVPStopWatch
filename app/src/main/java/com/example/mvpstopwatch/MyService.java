package com.example.mvpstopwatch;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.mvpstopwatch.View.MainActivity;

public class MyService extends Service {
    private static final String TAG = "MyService";

    boolean isRunning = true;
    int i = 0; // 스레드 관련 변수

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() 호출됨. ");

        Thread timeThread = new Thread(new TimeThread());
        timeThread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = true;
    }

    private class TimeThread implements Runnable {
        private Handler handler = new Handler();

        @Override
        public void run() {
            while(isRunning) {
                Message msg = new Message();
                msg.arg1 = i++;

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        int mSec = msg.arg1 % 100;
                        int sec = (msg.arg1 / 100) % 60;
                        int min = (msg.arg1 / 100) / 60 % 60;
                        int hour = (msg.arg1 / 100) / 3600 % 24;

                        String result = String.format("%02d:%02d:%02d.%02d", hour, min, sec, mSec);
                        Log.d(TAG, result);
                    }
                });

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "서비스 종료", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /*
    @Override // Intent는 여기서 처리함
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() 호출됨. ");

        if(intent == null) { // null이면 처리하지 않음
            return Service.START_STICKY; // 서비스가 강제 종료되었을 경우 시스템이 서비스의 Intent 값을 null로 초기화해서 재시작시킴
        } else {
            processCommand(intent);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void processCommand(Intent intent) {

    } */

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}