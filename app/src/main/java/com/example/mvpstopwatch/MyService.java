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


    public static int i = 0; // 스레드 관련 변수

    //BackgroundTask task;
    String result;

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() 호출됨. ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //ask = new BackgroundTask();
        //task.execute();
        Thread timeThread = new Thread(new TimeThread());
        timeThread.start();
        initNotification(); // 포그라운드 생성
        return START_NOT_STICKY;
    }

    public void initNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1");
        builder.setSmallIcon(R.drawable.timer);

        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.bigText("여기에 시간이 표시됩니다. ");
        style.setBigContentTitle(null);
        style.setSummaryText("실행중");

        builder.setContentText(null);
        builder.setContentTitle(null);
        builder.setOngoing(true);
        builder.setStyle(style);
        builder.setWhen(0);
        builder.setShowWhen(false);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        // 자신이 아닌 다른 컴포넌트들이 PendingIntent를 사용하여 다른 컴포넌트에게 작업을 요청시키는 데 사용됨
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
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

                        result = String.format("%02d:%02d:%02d.%02d", hour, min, sec, mSec);
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
    /*
    class BackgroundTask extends AsyncTask<Integer, String, Integer> {

        @Override
        protected Integer doInBackground(Integer... integers) {
            while(isRunning) {
                Message msg = new Message();
                msg.arg1 = i++;

                int mSec = msg.arg1 % 100;
                int sec = (msg.arg1 / 100) % 60;
                int min = (msg.arg1 / 100) / 60 % 60;
                int hour = (msg.arg1 / 100) / 3600 % 24;

                result = String.format("%02d:%02d:%02d.%02d", hour, min, sec, mSec);
                Log.d(TAG, result);

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return i; // onPostExecute()로 전달함
        }

        @Override // doInBackground() 메서드가 종료된 후 호출됨 -> 리턴값을 전달 받음 (작업에 대한 종료 시 처리할 것이 있다면 여기서 처리함)
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            MainActivity.timeTv.setText(result);
        }
    } */

    @Override
    public void onDestroy() {
        super.onDestroy();

        //task.cancel(true); // 태스크 종료
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}