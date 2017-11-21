package com.example.lxd.bestservice;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity
{
    Button start_download;
    TextView file_address;
    private DownloadService.DownloadBinder downloadBinder;

    private ServiceConnection serviceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            downloadBinder = (DownloadService.DownloadBinder) iBinder;
            downloadBinder.test_whether_service_bound();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            //开启了服务就没想过要让它断了
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        file_address = findViewById(R.id.file_address);
        //启动服务
        Intent intent = new Intent(this, DownloadService.class);
        startService(intent);

        //Bind Service
        bindService(intent, serviceConnection,BIND_AUTO_CREATE);

        //申请可以下载文件到SD卡的权限
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1
            );
        }

        start_download = findViewById(R.id.start_download);
        start_download.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String url = "https://raw.githubusercontent.com/guolindev/eclipse/master/eclipse-inst-win64.exe";
                downloadBinder.start_download(url);
                //downloadBinder.test_whether_service_binded();

                /*
                String file_name = url.substring(url.lastIndexOf("/"));
                String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();

                File file = new File(directory + file_name);

                if (file.exists())
                {
                    String name = file.getName();
                    long length = file.length()/1024/1024;

                    file_address.setText(directory);
                }
                */
            }
        });

    }

    protected void onDestroy()
    {
        super.onDestroy();
        unbindService(serviceConnection);
    }
}
