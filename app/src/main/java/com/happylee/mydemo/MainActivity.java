package com.happylee.mydemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Jed Lee(李俊德)
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //将SharedPreference数据写入SP
        configSharedPreferencesData();
        //从SP中读取全部数据
        traversalSharedPreferences();
    }

    private void configSharedPreferencesData() {
        SharedPreferences.Editor sharedPreferencesEditor = getSharedPreferences("book", MODE_PRIVATE).edit();
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

        sharedPreferencesEditor = getSharedPreferences("user", MODE_PRIVATE).edit();
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

    private void traversalSharedPreferences(){
        //遍历shared_prefs文件夹下所有的xml文件
        Map<String, String> map = getFilesData(getApplicationInfo().dataDir + "/shared_prefs");
        for (String key :
                map.keySet()) {
            //key就是每个SP的文件名，key.substring(0,key.length()-4)是为了去点SP文件名后面的.xml子字符串
            SharedPreferences sharedPreferences = getSharedPreferences(key.substring(0,key.length()-4), MODE_PRIVATE);
            for (Map.Entry<String, ?> mapEntry:
                    sharedPreferences.getAll().entrySet()){
                String spKey = mapEntry.getKey();
                String spValue = mapEntry.getValue().toString();
                Log.d("TAG", "key:" + spKey + " value:" + spValue);
            }
        }
    }

    /**
     * 获取某文件夹下的文件名和文件内容,存入map集合中
     * @param filePath 需要获取的文件的 路径
     * @return 返回存储文件名和文件内容的map集合
     */
    public static Map<String, String> getFilesData(String filePath){
        Map<String, String> files = new HashMap<>();
        //需要获取的文件的路径
        File file = new File(filePath);
        //存储文件名的String数组
        String[] fileNameLists = file.list();
        //存储文件路径的String数组
        File[] filePathLists = file.listFiles();
        for(int i=0;i<filePathLists.length;i++){
            if(filePathLists[i].isFile()){
                try {//读取指定文件路径下的文件内容
                    String fileData = readFile(filePathLists[i]);
                    //把文件名作为key,文件内容为value 存储在map中
                    files.put(fileNameLists[i], fileData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return files;
    }

    /**
     * 读取指定目录下的文件
     * @param path 文件的路径
     * @return 文件内容
     * @throws IOException
     */
    public static String readFile(File path) throws IOException {
        //创建一个输入流对象
        InputStream is = new FileInputStream(path);
        //定义一个缓冲区，1024代表 1kb
        byte[] bytes = new byte[1024];
        //通过输入流使用read方法读取数据
        int len = is.read(bytes);
        String str = null;
        while(len!=-1){
            //把数据转换为字符串
            str = new String(bytes, 0, len);
            //继续进行读取
            len = is.read(bytes);
        }
        //释放资源
        is.close();
        return str;
    }
}