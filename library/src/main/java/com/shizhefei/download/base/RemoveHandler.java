package com.shizhefei.download.base;

public interface RemoveHandler {
    void addRemoveListener(OnRemoveListener onRemoveListener);

    void remove();

    interface OnRemoveListener {
        void onRemove();
    }
}
