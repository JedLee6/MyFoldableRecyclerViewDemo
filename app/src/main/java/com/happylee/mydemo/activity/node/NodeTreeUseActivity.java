package com.happylee.mydemo.activity.node;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.entity.node.BaseNode;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.happylee.mydemo.R;
import com.happylee.mydemo.adapter.node.tree.NodeTreeAdapter;
import com.happylee.mydemo.base.BaseActivity;
import com.happylee.mydemo.entity.node.tree.FirstLayerNode;
import com.happylee.mydemo.entity.node.tree.SecondLayerNode;
import com.happylee.mydemo.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
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
    public static final int UPDATE_SP_NAME = 1;
    public static final int UPDATE_SP_KEY = 2;
    public static final int UPDATE_SP_VALUE = 3;
    /**xml文件的后缀*/
    private static final String XML_FILE_SUFFIX = ".xml";
    private RecyclerView mRecyclerView;
    private NodeTreeAdapter adapter = new NodeTreeAdapter();
    /**用于存储所有sp文件的键值对*/
    private List<Map<String, String>> spMapList = new ArrayList<>();
    /**存储firstLayerNode的列表*/
    private List<FirstLayerNode> firstLayerNodeList = new ArrayList<>();;

    private View clickedView;
    private Object clickedNodeEntity;

    /**实例化一个MyHandler对象*/
    private Handler handler = new MyHandler(this);

    static class MyHandler extends Handler {
        //注意下面的NodeTreeUseActivity类是MyHandler类所在的外部类，即所在的Activity
        WeakReference<NodeTreeUseActivity> NodeTreeUseActivityWeakReference;

        MyHandler(NodeTreeUseActivity nodeTreeUseActivity) {
            NodeTreeUseActivityWeakReference = new WeakReference<>(nodeTreeUseActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            NodeTreeUseActivity nodeTreeUseActivity = NodeTreeUseActivityWeakReference.get();
            switch (msg.what) {
                case UPDATE_SP_NAME:
                    //Object[] objArray = (Object[]) msg.obj;
                    int clickedFirstLayerPosition = ((FirstLayerNode) nodeTreeUseActivity.clickedNodeEntity).getItemPosition();
                    TextView textViewToBeChanged= (TextView) nodeTreeUseActivity.clickedView;
                    String originalFileName = textViewToBeChanged.getText().toString();
                    String changedSPFileName = msg.obj.toString();
                    //在数据中更新SP文件名
                    nodeTreeUseActivity.firstLayerNodeList.get(clickedFirstLayerPosition).setTitle(changedSPFileName+XML_FILE_SUFFIX);
                    //通知adapter刷新RecyclerView数据
                    nodeTreeUseActivity.adapter.notifyDataSetChanged();
                    //在SP中修改数据
                    Utils.changeFileName(nodeTreeUseActivity.getApplicationInfo().dataDir + "/shared_prefs/" + originalFileName, changedSPFileName);
                    break;
                case UPDATE_SP_KEY:
                    TextView textViewToBeChanged2= (TextView) nodeTreeUseActivity.clickedView;
                    String originalSPKey = textViewToBeChanged2.getText().toString();
                    String changedSPKey = msg.obj.toString();
                    String spName = ((SecondLayerNode) nodeTreeUseActivity.clickedNodeEntity).getSpName();
                    //拿到原来SP中key对应的value
                    SharedPreferences sharedPreferences = nodeTreeUseActivity.getSharedPreferences(spName.substring(0, spName.length() - 4), MODE_PRIVATE);
                    //String originalValue=sharedPreferences.getString()
                    //删除原来的SP中key所在的键值对

                    //在SP中更新key
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    //sharedPreferences.getAll()
                default:
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node_tree);

        setBackBtn();
        setTitle("Node Use (Tree)");

        mRecyclerView = findViewById(R.id.rv_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(adapter);

        //把SP文件名和SP的键值对全部存储到firstLayerNodeList和secondLayerNodeList中
        traversalSharedPreferences();

        adapter.setList(firstLayerNodeList);
        // 先注册需要点击的子控件id（注意，请不要写在convert方法里）
        adapter.addChildClickViewIds(R.id.sp_name, R.id.sp_key_text_view, R.id.sp_value_text_view);
        // 设置子控件点击监听
        adapter.setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                //要更新的数据类型，可以是sp_name、sp_key、sp_value
                int updateType = 0;
                //用户点击的Item的实例，例如FirstLayerNode或者SecondLayerNode
                clickedNodeEntity = adapter.getItem(position);
                //Toast.makeText(NodeTreeUseActivity.this, "itemPosition:"+((FirstLayerNode)clickedNodeEntity).getTitle(), Toast.LENGTH_SHORT).show();
                //用户点击的View的实例，例如TextView
                clickedView = view;
                if (view.getId() == R.id.sp_name) {
                    updateType = 1;
                    Toast.makeText(NodeTreeUseActivity.this, "你点击了sp_name，其一级列表中的position为"+((FirstLayerNode)clickedNodeEntity).getItemPosition(), Toast.LENGTH_SHORT).show();
                } else if (view.getId() == R.id.sp_key_text_view) {
                    updateType = 2;
                    Toast.makeText(NodeTreeUseActivity.this, "你点击了sp_key，其二级列表中的position为" + ((SecondLayerNode) clickedNodeEntity).getItemPosition(), Toast.LENGTH_SHORT).show();
                } else if (view.getId() == R.id.sp_value_text_view) {
                    updateType = 3;
                    Toast.makeText(NodeTreeUseActivity.this, "你点击了sp_value,其二级列表中的position为" + ((SecondLayerNode) clickedNodeEntity).getItemPosition(), Toast.LENGTH_SHORT).show();
                }
                //调用Dialog，用于获取用户想修改的数值
                Utils.showDialog(NodeTreeUseActivity.this, handler, updateType);
                //Toast.makeText(NodeTreeUseActivity.this, "你点击了这个child,position为" + position, Toast.LENGTH_SHORT).show();

            }
        });
    }

    /**把SP文件名和SP的键值对全部存储到firstLayerNodeList和secondLayerNodeList中*/
    private void traversalSharedPreferences(){
        //初始化
        firstLayerNodeList = new ArrayList<>();
        //spFileNamesArray存储的是shared_prefs文件夹下所有的xml文件的文件名
        String[] spFileNamesArray = Utils.getFileNames(getApplicationInfo().dataDir + "/shared_prefs");
        //该sp文件的在一级列表中的排序位置
        int spPosition = 0;
        //第一层循环，每次首先获得SP的文件名
        for (int i = 0; i < spFileNamesArray.length; i++) {
            //spFileName就是每个SP的文件名
            String spFileName = spFileNamesArray[i];
            //通过SP名字拿到该SP对象，从而可以遍历其中的键值对
            //就是每个SP的文件名.substring(0,就是每个SP的文件名.length()-4)是为了去点SP文件名后面的".xml"子字符串
            SharedPreferences sharedPreferences = getSharedPreferences(spFileName.substring(0, spFileName.length() - 4), MODE_PRIVATE);

            //创建一个List<BaseNode>用于存储SP键值对
            List<BaseNode> secondLayerNodeList = new ArrayList<>();
            //该键值对的在二级列表中的排序位置
            int spMapPosition = 0;

            //第二层循环，每次可获得一个SP键值对的key和value
            for (Map.Entry<String, ?> mapEntry:
                sharedPreferences.getAll().entrySet()){
                String spKey = mapEntry.getKey();
                String spValue = mapEntry.getValue().toString();
                SecondLayerNode secondLayerNode = new SecondLayerNode(spMapPosition,spFileName, spKey, spValue);
                secondLayerNodeList.add(secondLayerNode);
                spMapPosition++;
            }
            //将SP的名字配置一级列表Item的TextView标题文字
            FirstLayerNode firstLayerNode = new FirstLayerNode(secondLayerNodeList, spPosition, spFileName);
            firstLayerNodeList.add(firstLayerNode);
            spPosition++;
        }

    }

}