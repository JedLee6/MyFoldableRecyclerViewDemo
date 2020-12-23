package com.happylee.mydemo.entity.node.tree;

import com.chad.library.adapter.base.entity.node.BaseNode;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**二级Item用于显示每个SP的key-value键值对*/
public class SecondLayerNode extends BaseNode{

    //该项在整个二级列表中的位置
    private int itemPosition;
    private String spName;
    //该Node存储的SP键值对的key
    private String spKey;
    //该Node存储的SP键值对的Value
    private String spValue;

    //private String title;

    public SecondLayerNode(int itemPosition, String spName, String spKey, String spValue) {
        this.itemPosition = itemPosition;
        this.spName = spName;
        this.spKey = spKey;
        this.spValue = spValue;
    }

    @Nullable
    @Override
    public List<BaseNode> getChildNode() {
        return null;
    }

    public int getItemPosition() {
        return itemPosition;
    }

    public String getSpKey() {
        return spKey;
    }

    public String getSpValue() {
        return spValue;
    }

    public String getSpName() {
        return spName;
    }

    public void setSpName(String spName) {
        this.spName = spName;
    }
}
