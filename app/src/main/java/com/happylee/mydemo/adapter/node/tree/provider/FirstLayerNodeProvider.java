package com.happylee.mydemo.adapter.node.tree.provider;

import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import androidx.core.view.ViewCompat;

import com.happylee.mydemo.R;
import com.happylee.mydemo.adapter.node.tree.NodeTreeAdapter;
import com.happylee.mydemo.entity.node.tree.FirstLayerNode;
import com.chad.library.adapter.base.entity.node.BaseNode;
import com.chad.library.adapter.base.provider.BaseNodeProvider;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**由于使用了Provider，所以相关事务并不在Adapter里处理。需要在Provider中对数据进行绑定。
 * @author didi*/
public class FirstLayerNodeProvider extends BaseNodeProvider {

    @Override
    public int getItemViewType() {
        return 1;
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_node_first_layer;
    }

    /**BaseViewHolder是ViewHolder基类，里面用一个List存储了一个Item用到的各种View，例如TextView或ImageView*/
    @Override
    public void convert(@NotNull BaseViewHolder helper, @NotNull BaseNode data) {
        FirstLayerNode entity = (FirstLayerNode) data;
        //设置这个Item的文本描述
        helper.setText(R.id.sp_name, entity.getTitle());
        //设置这个Item中ImageView的图片资源，是个箭头图片
        helper.setImageResource(R.id.iv, R.mipmap.arrow_r);
        //让箭头完成旋转
        setArrowSpin(helper, data, false);
    }

    @Override
    public void convert(@NotNull BaseViewHolder helper, @NotNull BaseNode data, @NotNull List<?> payloads) {
        for (Object payload : payloads) {
            if (payload instanceof Integer && (int) payload == NodeTreeAdapter.EXPAND_COLLAPSE_PAYLOAD) {
                // 增量刷新，使用动画变化箭头
                setArrowSpin(helper, data, true);
            }
        }
    }

    /**让每个Item末尾的箭头是否旋转，可以使用动画或不使用动画*/
    private void setArrowSpin(BaseViewHolder helper, BaseNode data, boolean isAnimate) {
        FirstLayerNode entity = (FirstLayerNode) data;

        ImageView imageView = helper.getView(R.id.iv);

        if (entity.isExpanded()) {
            //isAnimate是true代表使用动画完成箭头的旋转，否则不使用动画
            if (isAnimate) {
                ViewCompat.animate(imageView).setDuration(200)
                        .setInterpolator(new DecelerateInterpolator())
                        .rotation(0f)
                        .start();
            } else {
                imageView.setRotation(0f);
            }
        } else {
            if (isAnimate) {
                ViewCompat.animate(imageView).setDuration(200)
                        .setInterpolator(new DecelerateInterpolator())
                        .rotation(90f)
                        .start();
            } else {
                imageView.setRotation(90f);
            }
        }
    }

    /**整个Item的点击事件，折叠或者展开Item*/
    @Override
    public void onClick(@NotNull BaseViewHolder helper, @NotNull View view, BaseNode data, int position) {
        // 这里使用payload进行增量刷新（避免整个item刷新导致的闪烁，不自然）
        getAdapter().expandOrCollapse(position, true, true, NodeTreeAdapter.EXPAND_COLLAPSE_PAYLOAD);
    }

}
