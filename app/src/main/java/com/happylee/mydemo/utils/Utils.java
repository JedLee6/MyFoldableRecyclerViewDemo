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
import com.happylee.mydemo.entity.node.tree.SecondLayerNode;

import org.w3c.dom.Node;

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

}
