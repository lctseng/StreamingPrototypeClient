package com.covart.streaming_prototype;

import com.badlogic.gdx.utils.Disposable;

/**
 * Created by lctseng on 2017/3/11.
 * NTU COV-ART Lab, for NCP project
 */

public interface DisplayAdapter extends Disposable {
    void updateStart();
    void updateEnd();
    void injectImageData(byte[] bufData);
    void disposeExistingTexture();
}
