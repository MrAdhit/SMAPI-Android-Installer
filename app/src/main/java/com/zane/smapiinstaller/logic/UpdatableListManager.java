package com.zane.smapiinstaller.logic;

import android.view.View;

import com.google.common.base.Predicate;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.zane.smapiinstaller.entity.UpdatableList;
import com.zane.smapiinstaller.utils.FileUtils;
import com.zane.smapiinstaller.utils.JSONUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 在线列表更新管理器
 * @param <T> 列表类型
 */
public class UpdatableListManager<T extends UpdatableList> {
    private static boolean updateChecked = false;

    private static UpdatableList updatableList = null;

    private List<Predicate<T>> onChangedListener = new ArrayList<>();

    /**
     * @param root      context容器
     * @param filename  本地文件名
     * @param tClass    目标类型
     * @param updateUrl 更新地址
     */
    public UpdatableListManager(View root, String filename, Class<T> tClass, String updateUrl) {
        updatableList = FileUtils.getAssetJson(root.getContext(), filename, tClass);
        if(!updateChecked) {
            updateChecked = true;
            OkGo.<String>get(updateUrl).execute(new StringCallback(){
                @Override
                public void onSuccess(Response<String> response) {
                    UpdatableList content = JSONUtil.fromJson(response.body(), tClass);
                    if(content != null && updatableList.getVersion() < content.getVersion()) {
                        FileUtils.writeAssetJson(root.getContext(), filename, content);
                        updatableList = content;
                        for (Predicate<T> listener : onChangedListener) {
                            listener.apply(getList());
                        }
                    }
                }
            });
        }
    }

    /**
     * @return 列表
     */
    public T getList() {
        return (T) updatableList;
    }

    /**
     * 注册列表变化监听器
     * @param onChanged 回调
     */
    public void registerListChangeListener(Predicate<T> onChanged) {
        this.onChangedListener.add(onChanged);
    }
}
