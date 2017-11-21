package com.example.lxd.bestservice;

import android.os.AsyncTask;
import android.os.Environment;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by lxd on 2017/11/20.
 */

//第一个泛型参数指定为String,表示在执行AsyncTask的时候需要传入一个字符串参数给后台任务
//第二个泛型参数指定为Integer,表示使用整型数据来作为进度显示单位
//第三个泛型参数指定为Integer，表示使用整型数据来反馈执行结果
public class DownloadTask extends AsyncTask <String,Integer,Integer>
{
    private DownloadListener listener;

    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_FAILED  = 1;
    public static final int TYPE_PAUSED  = 2;
    public static final int TYPE_CANCELLED = 3;

    private boolean isCancelled = false;
    private boolean isPaused = false;

    private int last_progress;

    public DownloadTask(DownloadListener listener)
    {
        this.listener = listener;
    }

    private long getContentLength(String download_URL) throws IOException
    {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(download_URL)
                .build();
        Response response = client.newCall(request).execute();
        if (response != null && response.isSuccessful())
        {
            long content_length = response.body().contentLength();
            response.body().close();
            return content_length;
        }
        return 0;
    }

    @Override
    protected Integer doInBackground(String... params)
    {
        File file = null;
        InputStream inputStream = null;
        RandomAccessFile saved_file = null;
        try
        {
            long downloaded_length = 0;//记录已经下载的文件长度
            String download_URL = params[0];
            //String url = "https://raw.githubusercontent.com/guolindev/eclipse/master/eclipse-inst-win64.exe";
            //String file_name = eclipse-inst-win64.exe
            String file_name = download_URL.substring(download_URL.lastIndexOf("/"));
            String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            file = new File(directory + file_name);

            if (file.exists())
            {
                downloaded_length = file.length();
            }

            //获取文件总字节数getContentLength(download_URL)
            long content_length = getContentLength(download_URL);
            if (content_length == 0)
            {
                return TYPE_FAILED;
            }
            else if (content_length == downloaded_length)
            {
                //已经下载字节和文件总字节相等，说明已经下载完成
                return TYPE_SUCCESS;
            }

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    //断点下载，指定从哪个字节开始下载,downloaded_length一开始是0
                    .addHeader("RANGE","bytes=" + downloaded_length + "-" )
                    .url(download_URL)
                    .build();
            Response response = client.newCall(request).execute();
            if (response != null)
            {
                inputStream = response.body().byteStream();
                saved_file  = new RandomAccessFile(file,"rw");
                saved_file.seek(downloaded_length);//跳过已下载的字节
                byte[] bytes = new byte[1024];
                int total=0;
                int length;
                while ( (length = inputStream.read(bytes)) != -1)
                {
                    if (isCancelled)
                    {
                        return TYPE_CANCELLED;
                    }
                    else if (isPaused)
                    {
                        return TYPE_PAUSED;
                    }
                    else
                    {
                        total += length;
                        saved_file.write(
                                bytes,
                                0,
                                length);
                        //计算已下载的百分比
                        int progress = (int)((total + downloaded_length) * 100/content_length);
                        //调用publishProgress()方法将当前下载速度传进来，这样onProgressUpdate()方法就会很快被调用
                        publishProgress(progress);
                    }
                }
                response.body().close();
                return TYPE_SUCCESS;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (inputStream != null)
                {
                    inputStream.close();
                }
                if (saved_file != null)
                {
                    saved_file.close();
                }
                if (isCancelled && file != null)
                {
                    file.delete();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return TYPE_FAILED;
    }

    protected void onProgressUpdate(Integer... values)
    {
        int progress = values[0];
        if (progress > last_progress)
        {
            listener.onProgress(progress);
            last_progress = progress;
        }
    }

    protected void onPostExecute(Integer status)
    {
        switch (status)
        {
            case TYPE_SUCCESS:
                listener.onSuccess();
                break;
            case TYPE_FAILED:
                listener.onFailed();
                break;
            case TYPE_PAUSED:
                listener.onPaused();
                break;
            case TYPE_CANCELLED:
                listener.onCancelled();
                break;
            default:
                break;
        }
    }

    public void pause_download()
    {
        isPaused = true;
    }

    public void cancel_download()
    {
        isCancelled = true;
    }
}
