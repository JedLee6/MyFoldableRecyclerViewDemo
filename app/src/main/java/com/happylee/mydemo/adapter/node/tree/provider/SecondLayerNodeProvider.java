package com.happylee.mydemo.adapter.node.tree.provider;

import com.happylee.mydemo.R;
import com.happylee.mydemo.entity.node.tree.SecondLayerNode;
import com.chad.library.adapter.base.entity.node.BaseNode;
import com.chad.library.adapter.base.provider.BaseNodeProvider;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;

public class SecondLayerNodeProvider extends BaseNodeProvider {

    @Override
    public int getItemViewType() {
        return 2;
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_node_second_layer;
    }

    @Override
    public void convert(@NotNull BaseViewHolder helper, @NotNull BaseNode data) {
        SecondLayerNode entity = (SecondLayerNode) data;
        helper.setText(R.id.sp_key_text_view, entity.getSpKey());
        //在实际输出过程中要注意将Object转化为String类型
        helper.setText(R.id.sp_value_text_view, entity.getSpValue().toString());
    }
}
