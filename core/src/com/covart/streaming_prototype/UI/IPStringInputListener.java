package com.covart.streaming_prototype.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.covart.streaming_prototype.ConfigManager;
import com.covart.streaming_prototype.Net.Connection;

/**
 * Created by lctseng on 2017/10/30.
 * For NCP project
 *
 *
 *
 * at COVART, NTU
 */

public class IPStringInputListener implements Input.TextInputListener {
    private TextButton button;

    @Override
    public void input(String text) {
        if(Connection.validateServerString(text)){
            ConfigManager.setSelectedIP(text);
            button.setText(text);
        }
        else{
            Gdx.app.log("IP UI", "IP Not valid:" + text);
        }

    }

    @Override
    public void canceled() {

    }

    public TextButton getButton() {
        return button;
    }

    public void setButton(TextButton button) {
        this.button = button;
    }
}
