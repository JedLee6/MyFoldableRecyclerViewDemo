package com.happylee.mydemo.adapter.node.tree;

import com.chad.library.adapter.base.BaseNodeAdapter;
import com.chad.library.adapter.base.entity.node.BaseNode;
import com.happylee.mydemo.adapter.node.tree.provider.FirstProvider;
import com.happylee.mydemo.adapter.node.tree.provider.SecondProvider;
import com.happylee.mydemo.adapter.node.tree.provider.ThirdProvider;
import com.happylee.mydemo.entity.node.tree.FirstNode;
import com.happylee.mydemo.entity.node.tree.SecondNode;
import com.happylee.mydemo.entity.node.tree.ThirdNode;

import org.jetbrains.annotations.NotNull;
import java.util.List;

public class NodeTreeAdapter extends BaseNodeAdapter {

    public NodeTreeAdapter() {
        super();
        addNodeProvider(new FirstProvider());
        addNodeProvider(new SecondProvider());
        addNodeProvider(new ThirdProvider());
    }

    /**
     * 自行根据数据、位置等信息，返回 item 类型
     */
    @Override
    protected int getItemType(@NotNull List<? extends BaseNode> data, int position) {
        BaseNode node = data.get(position);
        if (node instanceof FirstNode) {
            return 1;
        } else if (node instanceof SecondNode) {
            return 2;
        } else if (node instanceof ThirdNode) {
            return 3;
        }
        return -1;
    }

    public static final int EXPAND_COLLAPSE_PAYLOAD = 110;
}
