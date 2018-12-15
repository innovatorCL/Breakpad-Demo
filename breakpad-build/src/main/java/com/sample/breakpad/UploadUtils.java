package com.sample.breakpad;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class UploadUtils {

    private static final String TAG = "uploadFile";
    private static final int TIME_OUT = 10*1000*1000;   //超时时间
    private static final String CHARSET = "utf-8"; //设置编码

    /**
     * 上传文件
     * @param file
     * @param netUrl
     * @return
     */
    public static boolean uploadFile(File file,String netUrl) throws IOException {


        String uuid = UUID.randomUUID().toString();
        //用来隔开表单中不同部分数据的，由- -开头，以- -结尾
        String boundary = uuid;
        String newLine = "\r\n";
        FileInputStream fis = null;
        DataOutputStream dos = null;
        DataInputStream dis = null;
        URL url = new URL(netUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        //打开输出
        connection.setDoOutput(true);
        //打开输入
        connection.setDoInput(true);
        //关闭缓存
        connection.setUseCaches(false);
        //读取超时
        connection.setReadTimeout(50 * 1000);
        //连接超时
        connection.setConnectTimeout(5 * 1000);
        //请求方式POST
        connection.setRequestMethod("POST");
        //必须设置，数据类型，编码方式，分界线
        connection.setRequestProperty("Content-Type", "multipart/form-data; charset=utf-8; boundary=" + boundary);
        //设置分块传输，一块最大 1024 * 1024 byte，即 1M
        connection.setChunkedStreamingMode(1024 * 1024);
        dos = new DataOutputStream(connection.getOutputStream());

        if (file.exists()) {
            fis = new FileInputStream(file);
            byte[] buff = new byte[1024];
            dis = new DataInputStream(fis);
            int cnt = 0;
            //数据以--boundary开始
            dos.write(("--" + boundary).getBytes());
            //换行
            dos.write(newLine.getBytes());
            //内容描述信息
            String content = "Content-Disposition: form-data; name=\"" + file.getName() + "\"; filename=\"" + file.getName() + "\"";
            dos.write(content.getBytes());
            dos.write(newLine.getBytes());
            dos.write(newLine.getBytes());
            //空一行后，开始通过流传输文件数据
            while ((cnt = dis.read(buff)) != -1) {
                dos.write(buff, 0, cnt);
            }
            dos.write(newLine.getBytes());
            //结束标志--boundary--
            dos.write(("--" + boundary + "--").getBytes());
            dos.write(newLine.getBytes());
            dos.flush();
        }

        //开始发送请求，获取请求码和请求结果
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try {
                dis.close();
                dos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        } else {
            System.err.println("请求失败" + connection.getResponseMessage() + "code=" + connection.getResponseCode());
            return false;
        }
    }

}
