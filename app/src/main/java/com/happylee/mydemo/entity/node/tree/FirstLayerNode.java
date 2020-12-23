package com.happylee.mydemo.entity.node.tree;

import com.chad.library.adapter.base.entity.node.BaseExpandNode;
import com.chad.library.adapter.base.entity.node.BaseNode;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FirstLayerNode extends BaseExpandNode {

    private List<BaseNode> childNode;
    //该项在整个二级列表中的位置
    private int itemPosition;
    private String title;

    public FirstLayerNode(List<BaseNode> childNode, int itemPosition, String title) {
        this.childNode = childNode;
        this.itemPosition = itemPosition;
        this.title = title;

        setExpanded(false);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Nullable
    @Override
    public List<BaseNode> getChildNode() {
        return childNode;
    }

    public int getItemPosition() {
        return itemPosition;
    }

    public void setItemPosition(int itemPosition) {
        this.itemPosition = itemPosition;
    }
}
