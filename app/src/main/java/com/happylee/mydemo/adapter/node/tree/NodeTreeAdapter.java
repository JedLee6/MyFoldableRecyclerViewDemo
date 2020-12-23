package com.happylee.mydemo.adapter.node.tree;

import com.chad.library.adapter.base.BaseNodeAdapter;
import com.chad.library.adapter.base.entity.node.BaseNode;
import com.happylee.mydemo.adapter.node.tree.provider.FirstLayerNodeProvider;
import com.happylee.mydemo.adapter.node.tree.provider.SecondLayerNodeProvider;
import com.happylee.mydemo.entity.node.tree.FirstLayerNode;
import com.happylee.mydemo.entity.node.tree.SecondLayerNode;

import org.jetbrains.annotations.NotNull;
import java.util.List;

public class NodeTreeAdapter extends BaseNodeAdapter {

    public NodeTreeAdapter() {
        super();
        addNodeProvider(new FirstLayerNodeProvider());
        //addNodeProvider(new SecondProvider());
        addNodeProvider(new SecondLayerNodeProvider());
    }

    /**
     * 自行根据数据、位置等信息，返回 item 类型
     */
    @Override
    protected int getItemType(@NotNull List<? extends BaseNode> data, int position) {
        BaseNode node = data.get(position);
        if (node instanceof FirstLayerNode) {
            return 1;
        } else if (node instanceof SecondLayerNode) {
            return 2;
        }
        return -1;
    }

    public static final int EXPAND_COLLAPSE_PAYLOAD = 110;
}
