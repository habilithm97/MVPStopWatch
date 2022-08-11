package com.example.mvpstopwatch;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.mvpstopwatch.View.MainActivity;

public class MyService extends Service {
    private static final String TAG = "MyService";

    public static int i = 0; // 스레드 값

    public MyService() {}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Thread timeThread = new Thread(new TimeThread());
        timeThread.start();

        initNotification(); // 알림 객체를 통한 포그라운드 서비스 실행(액티비티가 onDestroy()되어도 알림을 통해 실행 유지됨)

        return START_NOT_STICKY; // 시스템에 의해 강제 종료되더라도 서비스가 재시작되지 않음
    }

    public void initNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1");
        builder.setSmallIcon(R.drawable.timer);

        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.setSummaryText("실행중"); // 앱 이름 우측의 텍스트
        style.setBigContentTitle(null); // 제목
        style.bigText("스톱워치가 실행중입니다. "); // 내용

        builder.setContentTitle(null); // 제목
        builder.setContentText(null); // 내용
        builder.setOngoing(true); // 사용자가 알림을 지우지 못하도록 유지
        builder.setStyle(style);
        builder.setWhen(0); // 알람 시간(miliSecond 단위로 넣어주면 내부적으로 자동 파싱함)
        builder.setShowWhen(false); // setWhen()을 감춤

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // 자신이 아닌 다른 컴포넌트들이 PendingIntent를 사용하여 다른 컴포넌트에게 작업을 요청시키는 데 사용됨
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE |
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // 오레오 버전 이상 Notification 알림 설정
            manager.createNotificationChannel(new NotificationChannel("1", "포그라운드 서비스", NotificationManager.IMPORTANCE_NONE));
        }
        Notification notification = builder.build(); // builder의 build()를 통해 Notification 객체 생성
        startForeground(1, notification); // 생성한 Notification 객체로 포그라운드 서비스 실행
    }

    private class TimeThread implements Runnable {
        private Handler handler = new Handler();

        @Override
        public void run() {
            while(MainActivity.isRunning) {
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
                        MainActivity.timeTv.setText(result);
                    }
                });
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
