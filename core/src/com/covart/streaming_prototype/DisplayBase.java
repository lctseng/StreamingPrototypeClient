package com.covart.streaming_prototype;

import com.badlogic.gdx.utils.Disposable;

/**
 * Created by lctseng on 2017/3/11.
 * NTU COV-ART Lab, for NCP project
 */

public abstract class DisplayBase implements Disposable {
    abstract void updateStart();
    abstract void updateEnd();
    abstract void injectImageData(byte[] bufData);
    abstract void disposeExistingTexture();
    boolean touchDragged (int screenX, int screenY, int pointer){
        return false;
    }
}
