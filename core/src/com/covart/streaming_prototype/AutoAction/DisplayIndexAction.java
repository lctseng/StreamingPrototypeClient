package com.covart.streaming_prototype.AutoAction;

import com.covart.streaming_prototype.ConfigManager;

/**
 * Created by lctseng on 2017/8/30.
 * For NCP project at COVART, NTU
 */

public class DisplayIndexAction extends OnetimeAction {

    public enum Type { COLUMN, ROW, SERIAL };

    private Type type;
    private int index;

    public DisplayIndexAction(Type type, int index){
        super();
        this.type = type;
        this.index = index;
    }

    @Override
    protected void act(float deltaTime) {
        switch(this.type){
            case COLUMN:
                ConfigManager.setDisplayIndexColumn(this.index);
                break;
            case ROW:;
                ConfigManager.setDisplayIndexRow(this.index);
                break;
            case SERIAL:
                ConfigManager.setDisplayIndexSerial(this.index);
                break;
        }
    }
}
