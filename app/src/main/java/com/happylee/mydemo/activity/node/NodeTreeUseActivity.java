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
    private List<FirstLayerNode> firstLayerNodeList = new ArrayList<>();

    private View clickedView;
    private Object clickedNodeEntity;

    public Object getClickedNodeEntity() {
        return clickedNodeEntity;
    }

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
            //提取3种修改情况下的TextView
            TextView textViewToBeChanged = (TextView) nodeTreeUseActivity.clickedView;
            //原来SP中的sp_name
            String originalSPName;
            //不带后缀".xml"的sp_name
            String spNameWithoutSuffix;
            //原来SP中的key
            String originalSPKey;
            //通过Object拿到原来SP中key对应的value，但是也必须知道value的类型
            Object originalValue;
            //通过Entity拿到sp的value，再读取原来value的类型
            String originalValueType;
            //分别拿到SP和SP.Editor的对象
            SharedPreferences sharedPreferences;
            SharedPreferences.Editor sharedPreferencesEditor;

            switch (msg.what) {
                case UPDATE_SP_NAME:
                    //Object[] objArray = (Object[]) msg.obj;
                    int clickedFirstLayerPosition = ((FirstLayerNode) nodeTreeUseActivity.clickedNodeEntity).getItemPosition();
                    originalSPName = textViewToBeChanged.getText().toString();
                    String changedSPName = msg.obj.toString();
                    //在数据中更新SP文件名
                    nodeTreeUseActivity.firstLayerNodeList.get(clickedFirstLayerPosition).setTitle(changedSPName+XML_FILE_SUFFIX);
                    //通知adapter刷新RecyclerView数据
                    nodeTreeUseActivity.adapter.notifyDataSetChanged();
                    //在SP中修改数据
                    Utils.changeFileName(nodeTreeUseActivity.getApplicationInfo().dataDir + "/shared_prefs/" + originalSPName, changedSPName);
                    break;

                case UPDATE_SP_KEY:
                    //拿到原来SP中的sp_name
                    originalSPName = ((SecondLayerNode) nodeTreeUseActivity.clickedNodeEntity).getSpName();
                    //不带后缀".xml"的sp_name
                    spNameWithoutSuffix = originalSPName.substring(0, originalSPName.length() - 4);
                    //拿到原来SP中的key
                    originalSPKey = textViewToBeChanged.getText().toString();
                    //拿到需要修改后的SP中的key
                    String changedSPKey = msg.obj.toString();

                    //通过Object拿到原来SP中key对应的value，但是也必须知道value的类型
                    originalValue = ((SecondLayerNode) nodeTreeUseActivity.clickedNodeEntity).getSpValue();
                    //通过Entity拿到sp的value，再读取原来value的类型
                    originalValueType = originalValue.getClass().getSimpleName();

                    //分别拿到SP和SP.Editor的对象
                    sharedPreferences = nodeTreeUseActivity.getSharedPreferences(spNameWithoutSuffix, MODE_PRIVATE);
                    sharedPreferencesEditor = sharedPreferences.edit();
                    //删除原来的SP中key所在的键值对
                    sharedPreferencesEditor.remove(originalSPKey);

                    //在SP中插入新的key-value，通过判断sp新key对应value的类型从而执行对应的SP.Editor写入语句
                    switch (originalValueType) {
                        case "Boolean":
                            sharedPreferencesEditor.putBoolean(changedSPKey, (Boolean) originalValue);
                            break;

                        case "Integer":
                            sharedPreferencesEditor.putInt(changedSPKey, (Integer) originalValue);
                            break;

                        case "Long":
                            sharedPreferencesEditor.putLong(changedSPKey, (Long) originalValue);
                            break;

                        case "Float":
                            sharedPreferencesEditor.putFloat(changedSPKey, (Float) originalValue);
                            break;

                        case "String":
                            sharedPreferencesEditor.putString(changedSPKey, (String) originalValue);
                            break;

                        case "HashSet":
                            sharedPreferencesEditor.putStringSet(changedSPKey, (Set<String>) originalValue);
                            break;

                        default:
                            break;
                    }
                    sharedPreferencesEditor.commit();
                    break;

                case UPDATE_SP_VALUE:
                    //拿到原来SP中的sp_name
                    originalSPName = ((SecondLayerNode) nodeTreeUseActivity.clickedNodeEntity).getSpName();
                    //不带后缀".xml"的sp_name
                    spNameWithoutSuffix = originalSPName.substring(0, originalSPName.length() - 4);
                    //拿到原来SP中的key
                    originalSPKey = ((SecondLayerNode) nodeTreeUseActivity.clickedNodeEntity).getSpKey();
                    //拿到需要修改后的SP中的value
                    String changedSPValue = msg.obj.toString();
                    //通过Object拿到原来SP中key对应的value，但是也必须知道value的类型
                    originalValue = ((SecondLayerNode) nodeTreeUseActivity.clickedNodeEntity).getSpValue();
                    //通过Entity拿到sp的value，再读取原来value的类型
                    originalValueType = originalValue.getClass().getSimpleName();


                    //分别拿到SP和SP.Editor的对象
                    sharedPreferences = nodeTreeUseActivity.getSharedPreferences(spNameWithoutSuffix, MODE_PRIVATE);
                    sharedPreferencesEditor = sharedPreferences.edit();

                    //根据spValue的6中类型，对用户输入的changedSPValue进行不同的处理
                    switch (originalValueType) {
                        case "Boolean":
                            if ("true".equals(changedSPValue)) {
                                sharedPreferencesEditor.putBoolean(originalSPKey, true);
                            } else if ("false".equals(changedSPValue)) {
                                sharedPreferencesEditor.putBoolean(originalSPKey, false);
                            } else {
                                Toast.makeText(nodeTreeUseActivity, "输入不合法，请输入true或者false", Toast.LENGTH_SHORT).show();
                            }
                            break;

                        case "Integer":
                            try {
                                sharedPreferencesEditor.putInt(originalSPKey, Integer.parseInt(changedSPValue));
                            } catch (NumberFormatException e) {
                                Toast.makeText(nodeTreeUseActivity, "输入不合法，请输入合法的Integer型整数", Toast.LENGTH_SHORT).show();
                            }
                            break;

                        case "Long":
                            try {
                                sharedPreferencesEditor.putLong(originalSPKey, Long.parseLong(changedSPValue));
                            } catch (NumberFormatException e) {
                                Toast.makeText(nodeTreeUseActivity, "输入不合法，请输入合法的Long型整数", Toast.LENGTH_SHORT).show();
                            }
                            break;

                        case "Float":
                            try {
                                sharedPreferencesEditor.putFloat(originalSPKey, Float.parseFloat(changedSPValue));
                            } catch (NumberFormatException e) {
                                Toast.makeText(nodeTreeUseActivity, "输入不合法，请输入合法的Float型浮点数", Toast.LENGTH_SHORT).show();
                            }
                            break;

                        case "String":
                            sharedPreferencesEditor.putString(originalSPKey, changedSPValue);
                            break;

                        case "HashSet":
                            if (Utils.isIllegalStringSet(changedSPValue)) {
                                //如果用户输入的是合法的特性StringSet，则解析成Set<String>
                                Set<String> parsedStringSet = Utils.parseStringSet(changedSPValue);
                                sharedPreferencesEditor.putStringSet(originalSPKey, parsedStringSet);
                            } else {
                                Toast.makeText(nodeTreeUseActivity, "输入不合法，请输入合法的Set<String>型数据，例如[\"a\",\"b\",\"c\"]", Toast.LENGTH_SHORT).show();
                            }
                            break;

                        default:
                            break;
                    }
                    sharedPreferencesEditor.commit();
                    break;

                default:
                    break;
            }

            //更新数据源
            nodeTreeUseActivity.traversalSharedPreferences();
            //按道理可以不写这行代码，因为firstLayerNodeList明明持有的原对象的引用
            nodeTreeUseActivity.adapter.setList(nodeTreeUseActivity.firstLayerNodeList);
            //通知Adapter更新数据
            nodeTreeUseActivity.adapter.notifyDataSetChanged();

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
                    updateType = UPDATE_SP_NAME;
                    Toast.makeText(NodeTreeUseActivity.this, "你点击了sp_name，其一级列表中的position为"+((FirstLayerNode)clickedNodeEntity).getItemPosition(), Toast.LENGTH_SHORT).show();
                } else if (view.getId() == R.id.sp_key_text_view) {
                    updateType = UPDATE_SP_KEY;
                    Toast.makeText(NodeTreeUseActivity.this, "你点击了sp_key，其二级列表中的position为" + ((SecondLayerNode) clickedNodeEntity).getItemPosition(), Toast.LENGTH_SHORT).show();
                    Object object = ((SecondLayerNode) clickedNodeEntity).getSpValue();
                    //Toast.makeText(NodeTreeUseActivity.this, "你点击了sp_key，其value类型为" + object.getClass()+" 它是否instanceof Set<?>:"+(object instanceof Set<?>), Toast.LENGTH_SHORT).show();

                } else if (view.getId() == R.id.sp_value_text_view) {
                    updateType = UPDATE_SP_VALUE;
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
        firstLayerNodeList.clear();
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
                //用Object继续保存spValue的原有数据类型
                Object spValue = mapEntry.getValue();
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