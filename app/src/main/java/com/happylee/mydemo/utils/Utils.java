package com.happylee.mydemo.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.happylee.mydemo.activity.node.NodeTreeUseActivity;
import com.happylee.mydemo.entity.node.tree.SecondLayerNode;

import org.w3c.dom.Node;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

import okhttp3.Headers;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * <pre>
 *     author: Blankj
 *     blog  : http://blankj.com
 *     time  : 16/12/08
 *     desc  : Utils初始化相关
 * </pre>
 */
public class Utils {

    private static Context context;

    private Utils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 初始化工具类
     *
     * @param context 上下文
     */
    public static void init(Context context) {
        Utils.context = context.getApplicationContext();
    }

    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext
     */
    public static Context getContext() {
        if (context != null) {
            return context;
        }
        throw new NullPointerException("u should init first");
    }

    /**
     * 判断用户输入的特性StringSet是否符合转义语法
     * 例如[],["a"],["a","b"],[ " a" , "b " , " c "]都是合法输入
     */
    public static boolean isIllegalStringSet(String string) {
        //可匹配[]和["任意字符"]，如有不合法的["任意字符""任意字符"]，也能正确识别
        String stringSetRegex1 = "\\[\\s*(\"[^\"]*\")?\\s*\\]";
        //要匹配["任意字符" (,"任意字符")+ ]
        String stringSetRegex2 = "\\[\\s*(\"[^\"]*\")(\\s*\\,\\s*\"[^\"]*\")+\\s*\\]";

        if (string.matches(stringSetRegex1)) {
            return true;
        } else if (string.matches(stringSetRegex2)) {
            return true;
        } else {
            return false;
        }
    }

    /**解析，例如用户输入的特性StringSet字符串为[ " a" , "b " , " c "]，则解析成Set<String>类型的[a, b, c]*/
    public static Set<String> parseStringSet(String string) {
        char[] charArray = string.toCharArray();
        Set<String> stringSet = new HashSet<>();
        StringBuffer tempStringBuffer = new StringBuffer("");
        //是否是一对双引号的左边那个双引号
        //boolean isBeginningQuotationMark = true;
        for (int i = 0; i < charArray.length; i++) {
            if (charArray[i] == '\"') {
                //每次准备读入新的字符串时，清空StringBuffer中的内容
                tempStringBuffer.setLength(0);
                do {
                    i++;
                    //如果该字符不是结尾的双引号，则写入tempStringBuffer
                    if (charArray[i] != '\"') {
                        tempStringBuffer.append(charArray[i]);
                    }
                } while (charArray[i] != '\"');
                //把一对双引号中完整的字符串写入stringSet
                stringSet.add(tempStringBuffer.toString());
                //通过i++跳过一对双引号的右边那个双引号
                i++;
            }
        }
        return stringSet;
    }

    /**当用户第一次启动APP时对SP初始化，否则不初始化*/
    public static void initSharedPreferences() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("app_sharedpreferences", MODE_PRIVATE);
        boolean isFirstRun = sharedPreferences.getBoolean("isFirstRun", true);
        if (isFirstRun) {
            Log.d("TAG", "第一次运行该APP");
            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
            sharedPreferencesEditor.putBoolean("isFirstRun", false);
            //必须commit()提交，不然第一次运行APP可能导致SP没有及时加载完全
            sharedPreferencesEditor.commit();
            configSharedPreferencesData();
        } else {
            Log.e("TAG", "不是第一次运行该APP");
        }
    }

    /**将SharedPreference数据写入SP*/
    private static void configSharedPreferencesData() {
        SharedPreferences.Editor sharedPreferencesEditor = context.getSharedPreferences("book", MODE_PRIVATE).edit();
        sharedPreferencesEditor.putBoolean("domestic", false);
        sharedPreferencesEditor.putFloat("width", 17.6F);
        sharedPreferencesEditor.putLong("height", 24);
        Set<String> stringSet=new HashSet<>();
        stringSet.add("novel");
        stringSet.add("romance");
        sharedPreferencesEditor.putStringSet("tag",stringSet);
        sharedPreferencesEditor.putString("name", "Gone with the wind");
        sharedPreferencesEditor.putInt("thickness", 5);
        sharedPreferencesEditor.apply();

        sharedPreferencesEditor = context.getSharedPreferences("user", MODE_PRIVATE).edit();
        sharedPreferencesEditor.putBoolean("male", true);
        sharedPreferencesEditor.putFloat("height", 170.6F);
        sharedPreferencesEditor.putLong("weight", 60);
        stringSet=new HashSet<>();
        stringSet.add("hello");
        stringSet.add("world");
        sharedPreferencesEditor.putStringSet("alias",stringSet);
        sharedPreferencesEditor.putString("name", "Tom");
        sharedPreferencesEditor.putInt("age", 28);
        //必须commit()提交，不然第一次运行APP可能导致SP没有及时加载完全
        sharedPreferencesEditor.commit();
    }

    public static String[] getFileNames(String filepath) {
        //需要获取的文件路径,File可指向文件或文件夹
        File file = new File(filepath);
        //存储文件名的String数组
        String[] fileNamesArray = file.list();
        return fileNamesArray;
    }

    /**
     * 一个输入框的 dialog
     */
    public static void showDialog(Context context, Handler handler, int messageWhat) {
        final EditText editText = new EditText(context);
        //用于获得ENtity再获得sp_value的类型
        NodeTreeUseActivity nodeTreeUseActivity = null;
        SecondLayerNode secondLayerNode = null;
        if (context instanceof NodeTreeUseActivity) {
            nodeTreeUseActivity = (NodeTreeUseActivity) context;
            Object clickedNodeEntity = nodeTreeUseActivity.getClickedNodeEntity();
            if (clickedNodeEntity instanceof SecondLayerNode) {
                secondLayerNode = (SecondLayerNode) clickedNodeEntity;
            }
        }
        String alertText = "输入修改后的内容：";
        if (messageWhat==NodeTreeUseActivity.UPDATE_SP_VALUE && secondLayerNode != null) {
            alertText = "当前spValue类型为：" + secondLayerNode.getSpValue().getClass().getSimpleName() + "，" + alertText;
        }
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context).setTitle(alertText).setView(editText)
                .setPositiveButton("确认修改", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Toast.makeText(context, "修改后的内容为：" + editText.getText().toString(), Toast.LENGTH_LONG).show();
                        //changeFileName(context.getApplicationInfo().dataDir + "/shared_prefs/" + originalFileName, editText.getText().toString());
                        Message message = handler.obtainMessage();
                        message.what = messageWhat;
                        message.obj = editText.getText().toString();
                        //message.arg1 = clickedPosition;
                        //message.obj = new Object[]{view, editText.getText().toString()};
                        handler.sendMessage(message);
                    }
                });
        alertDialogBuilder.create().show();
    }

    /**
     * 通过文件路径直接修改文件名
     *
     * @param filePath    需要修改的文件的完整路径
     * @param newFileName 需要修改的文件的名称
     * @return
     */
    public static String changeFileName(String filePath, String newFileName) {
        File f = new File(filePath);
        // 判断原文件是否存在（防止文件名冲突）
        if (!f.exists()) {
            return null;
        }
        newFileName = newFileName.trim();
        // 文件名不能为空
        if ("".equals(newFileName)) {
            return null;
        }
        String newFilePath = null;
        // 判断是否为文件夹
        if (f.isDirectory()) {
            newFilePath = filePath.substring(0, filePath.lastIndexOf("/")) + "/" + newFileName;
        } else {
            newFilePath = filePath.substring(0, filePath.lastIndexOf("/")) + "/" + newFileName
                    + filePath.substring(filePath.lastIndexOf("."));
        }
        File nf = new File(newFilePath);
        try {
            // 修改文件名
            f.renameTo(nf);
        } catch (Exception err) {
            err.printStackTrace();
            return null;
        }
        return newFilePath;
    }

    /**
     * 关闭流
     *
     * @param closeable
     */
    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 是否支持断点续传
     *
     * @param response
     * @return
     */
    public static boolean isSupportRange(Response response) {
        Headers headers = response.headers();
        return !TextUtils.isEmpty(headers.get("Content-Range"))
                || stringToLong(headers.get("Content-Length")) != -1;
    }

    private static long stringToLong(String s) {
        if (s == null) {
            return -1;
        }
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * 服务器文件是否已更改
     *
     * @param response
     * @return
     */
    public static boolean isNotServerFileChanged(Response response) {
        return response.code() == 206;
    }

    /**
     * 文件最后修改时间
     *
     * @param response
     * @return
     */
    public static String getLastModify(Response response) {
        return response.headers().get("Last-Modified");
    }

    /**
     * 删除文件
     *
     * @param file
     * @return
     */
    public static boolean deleteFile(File file) {
        if (file != null && file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 批量删除文件
     *
     * @param files
     */
    public static void deleteFile(File... files) {
        for (File file : files) {
            deleteFile(file);
        }
    }

    /**
     * 删除文件
     *
     * @param path
     * @param name
     */
    public static void deleteFile(String path, String name) {
        deleteFile(new File(path, name));
    }

    /**
     * 创建文件
     *
     * @param path
     * @param name
     * @return
     */
    public static synchronized File createFile(String path, String name) {
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(name)) {
            return null;
        }

        File parentFile = new File(path);
        if (!parentFile.exists()) {
            parentFile.mkdir();
        }

        File file = new File(parentFile, name);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return file;
    }

    public static boolean isFileExists(File file) {
        if (file != null && file.exists()) {
            return true;
        }
        return false;
    }

    /**
     * 格式化文件大小
     *
     * @param size
     * @return
     */
    public static String formatSize(long size) {
        String resultSize;

        double b = size;
        double kb = size / 1024.0;
        double mb = (kb / 1024.0);
        double gb = (mb / 1024.0);
        double tb = (gb / 1024.0);

        DecimalFormat df = new DecimalFormat("0.00");

        if (tb > 1) {
            resultSize = df.format(tb).concat(" TB");
        } else if (gb > 1) {
            resultSize = df.format(gb).concat(" GB");
        } else if (mb > 1) {
            resultSize = df.format(mb).concat(" MB");
        } else if (kb > 1) {
            resultSize = df.format(kb).concat(" KB");
        } else {
            resultSize = df.format(b).concat(" B");
        }

        return resultSize;
    }

    public static float getPercentage(int currentSize, int totalSize) {
        if (currentSize > totalSize) {
            return 0;
        }

        return ((int) (currentSize * 10000.0 / totalSize)) * 1.0f / 100;
    }

    /**
     * 截取url中的默认文件名
     *
     * @param url
     * @return
     */
    public static String getSuffixName(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }

        return url.substring(url.lastIndexOf("/"));
    }

    /**
     * 将字符串进行MD5编码
     *
     * @param str
     * @return
     */
    public static String md5Encode(String str) {
        String tempStr;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(str.getBytes());
            tempStr = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            tempStr = String.valueOf(str.hashCode());
        }
        return tempStr;
    }

    /**
     * bytes to hex string
     *
     * @param bytes
     * @return
     */
    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * 根据文件名解析contentType
     *
     * @param name
     * @return
     */
    public static String getMimeType(String name) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = null;
        try {
            contentTypeFor = fileNameMap.getContentTypeFor(URLEncoder.encode(name, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }
}
