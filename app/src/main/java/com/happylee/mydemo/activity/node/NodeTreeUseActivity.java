package com.happylee.mydemo.activity.node;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.chad.library.adapter.base.entity.node.BaseNode;
import com.happylee.mydemo.R;
import com.happylee.mydemo.adapter.node.tree.NodeTreeAdapter;
import com.happylee.mydemo.base.BaseActivity;
import com.happylee.mydemo.entity.node.tree.FirstNode;
import com.happylee.mydemo.entity.node.tree.SecondNode;
import com.happylee.mydemo.entity.node.tree.ThirdNode;
import com.happylee.mydemo.utils.Tips;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Happy Lee(李俊德)
 */
public class NodeTreeUseActivity extends BaseActivity {

    private RecyclerView mRecyclerView;
    private NodeTreeAdapter adapter = new NodeTreeAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node_tree);


        setBackBtn();
        setTitle("Node Use (Tree)");

        mRecyclerView = findViewById(R.id.rv_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(adapter);

        adapter.setList(getEntity());

        // 模拟新增node
        /*
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                SecondNode seNode = new SecondNode(new ArrayList<BaseNode>(), "Second Node(This is added)");
                SecondNode seNode2 = new SecondNode(new ArrayList<BaseNode>(), "Second Node(This is added)");
                List<SecondNode> nodes = new ArrayList<>();
                nodes.add(seNode);
                nodes.add(seNode2);
                //第一个夫node，位置为子node的3号位置
                adapter.nodeAddData(adapter.getData().get(0), 2, nodes);
//                adapter.nodeSetData(adapter.getData().get(0), 2, seNode2);
//                adapter.nodeReplaceChildData(adapter.getData().get(0), nodes);
                Tips.show("新插入了两个node", Toast.LENGTH_LONG);
            }
        }, 2000);
        */
        //将SharedPreference数据写入SP
        configSharedPreferencesData();
        //从SP中读取全部数据
        traversalSharedPreferences();

    }

    private List<BaseNode> getEntity() {
        List<BaseNode> list = new ArrayList<>();
        for (int i = 0; i < 8; i++) {

            List<BaseNode> secondNodeList = new ArrayList<>();
            for (int n = 0; n <= 5; n++) {

                List<BaseNode> thirdNodeList = new ArrayList<>();
                for (int t = 0; t <= 3; t++) {
                    ThirdNode node = new ThirdNode("Third Node " + t);
                    thirdNodeList.add(node);
                }

                SecondNode seNode = new SecondNode(thirdNodeList, "Second Node " + n);
                secondNodeList.add(seNode);
            }

            FirstNode entity = new FirstNode(secondNodeList, "First Node " + i);

            // 模拟 默认第0个是展开的
            entity.setExpanded(i == 0);

            list.add(entity);
        }
        return list;
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