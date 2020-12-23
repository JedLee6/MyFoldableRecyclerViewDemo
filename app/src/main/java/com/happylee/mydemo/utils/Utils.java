package com.happylee.mydemo.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.happylee.mydemo.activity.node.NodeTreeUseActivity;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

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

    /**当用户第一次启动APP时对SP初始化，否则不初始化*/
    public static void initSharedPreferences() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("app_sharedpreferences", MODE_PRIVATE);
        boolean isFirstRun = sharedPreferences.getBoolean("isFirstRun", true);
        if (isFirstRun) {
            Log.d("TAG", "第一次运行该APP");
            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
            sharedPreferencesEditor.putBoolean("isFirstRun", false);
            sharedPreferencesEditor.putInt("test", 111);
            sharedPreferencesEditor.putFloat("test", 222);
            sharedPreferencesEditor.putLong("test", 333);
            sharedPreferencesEditor.apply();
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
        sharedPreferencesEditor.apply();
        //sharedPreferencesEditor.commit();
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
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context).setTitle("输入修改后的内容").setView(editText)
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

}
