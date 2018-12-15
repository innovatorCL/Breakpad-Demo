package com.sample.breakpad;


import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class BreakpadInit {

    private static String mPath = "";
    private static String mUrl = "";
    private static final String TAG = "dodoodla_crash";

    static {
        System.loadLibrary("breakpad-core");
    }

    public static void initBreakpad(Context context,String path,String url){
        mPath = path;
        mUrl = url;
        initBreakpadNative(path);
        uploadDumpFile(context);
    }

    public static void uploadDumpFile(String file) {
        Log.d(TAG, "native call java to upload dump file：" + file);
    }

    public static void uploadDumpFile(Context context){
        //遍历 Crash 目录文件
        File path = new File(mPath);

        // 判断SD卡是否存在，并且是否具有读写权限
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File[] files = path.listFiles();// 读取文件夹下文件

            for (File f : files) {
                if (!f.isDirectory()) {
                    String fileName = f.getName();
                    Log.i(TAG, "文件名：" + fileName);

                    //未记录或者未上传完成，直接记录并上传
                    if(!SPUtils.contains(context,fileName) || SPUtils.get(context,fileName,"false").equals("false")){
                        SPUtils.put(context,fileName,"false");
                        boolean isUploadSuccess = false;
                        Log.i(TAG, "上传文件，文件名：" + fileName);
                        //上传该文件
//                        try{
//                            isUploadSuccess = UploadUtils.uploadFile(f,"www.baidu.com");
//                        }catch (Exception e){
//                            e.printStackTrace();
//                            break;
//                        }

                        //Test Code
                        try {
                            Thread.sleep(10000);
                            isUploadSuccess = true;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if(isUploadSuccess){
                            SPUtils.put(context,fileName,"true");
                            Log.i(TAG, "上传完成，文件名：" + fileName);
                        }
                    }else {
                        Log.i(TAG, "文件已上传，删除文件记录：" + fileName);
                        SPUtils.remove(context,fileName);
                        f.delete();
                    }
                }
            }

        }

    }

    private static native void initBreakpadNative(String path);
}
