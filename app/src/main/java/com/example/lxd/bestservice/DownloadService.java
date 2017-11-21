package com.example.lxd.bestservice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.lxd.bestservice.DownloadListener;

public class DownloadService extends Service
{
    private DownloadTask downloadTask;
    private String downloadUrl;

    public DownloadService()
    {
    }

    public void onCreate()
    {
        super.onCreate();
        Log.d("DownloadService","onCreate executed");
    }

    private DownloadBinder mBinder = new DownloadBinder();
    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return mBinder;
    }

    private DownloadListener listener = new DownloadListener()
    {
        @Override
        public void onProgress(int progress)
        {
            getNotificationManager().notify(
                                            1,
                                            getNotification("Downloading...", progress));
        }

        @Override
        public void onSuccess()
        {
            //下载成功时将前台服务通知关闭，并创建一个下载成功的通知
            downloadTask = null;
            stopForeground(true);
            getNotificationManager().notify(
                                            1,
                                            getNotification("Download Success",
                                            -1));
            Toast.makeText(
                    DownloadService.this,
                    "Download Success.",
                    Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void onFailed()
        {
            stopForeground(true);
            Toast.makeText(
                    DownloadService.this,
                    "Download Failed.",
                    Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void onPaused()
        {
            stopForeground(true);
            Toast.makeText(
                    DownloadService.this,
                    "Download Paused.",
                    Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void onCancelled()
        {
            stopForeground(true);
            Toast.makeText(
                    DownloadService.this,
                    "Download Cancelled.",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    };

    //通过继承Binder的方式来实现IBinder
    class DownloadBinder extends Binder
    {
        public void test_whether_service_bound()
        {

            Toast.makeText(
                    DownloadService.this,
                    "DownloadService is bound successfully.",
                    Toast.LENGTH_SHORT)
                    .show();
        }
        public void start_download(String url)
        {
            if (downloadTask == null)
            {
                downloadUrl = url;
                downloadTask = new DownloadTask(listener);

                //启动任务
                downloadTask.execute(downloadUrl);

                startForeground(
                                1,
                                 getNotification("Downloading...",
                                0));
                Toast.makeText(
                        DownloadService.this,
                        "Downloading...",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }

        public void pause_download()
        {

        }

        public void cancel_download()
        {

        }
    }

    private NotificationManager getNotificationManager()
    {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title,int progress)
    {
        Intent intent = new Intent(this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle(title);

        if (progress >= 0)
        {
            //当progress大于等于0时才需要显示下载进度
            builder.setContentText(progress + "%");
            //setProgress()方法接受3个参数
            //第一个参数传入通知的最大进度，第二个参数传入通知的当前进度，第三个参数表示是否使用模糊进度条,这里传入false
            builder.setProgress(100,progress,false);
            //设置完setProgress()方法，通知上就会有进度条显示出来了
        }
        return builder.build();
    }


}
