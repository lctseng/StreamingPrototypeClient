package com.covart.streaming_prototype.UI;

import com.badlogic.gdx.utils.Disposable;

/**
 * Created by lctseng on 2017/4/29.
 * NTU COV-ART Lab, for NCP project
 */

class UIManager implements Disposable{

    private static UIManager ourInstance;

    public static UIManager getInstance() {
        return ourInstance;
    }

    public static void initialize(){
        ourInstance = new UIManager();
    }

    public static void cleanup(){
        ourInstance.dispose();
    }

    private UIManager() {
    }

    @Override
    public void dispose() {

    }
}
