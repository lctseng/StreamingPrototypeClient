package com.covart.streaming_prototype.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.covart.streaming_prototype.ConfigManager;

import java.util.Locale;

import static com.covart.streaming_prototype.UI.PositionController.Direction.BACKWARD;
import static com.covart.streaming_prototype.UI.PositionController.Direction.DOWN;
import static com.covart.streaming_prototype.UI.PositionController.Direction.FORWARD;
import static com.covart.streaming_prototype.UI.PositionController.Direction.LEFT;
import static com.covart.streaming_prototype.UI.PositionController.Direction.NONE;
import static com.covart.streaming_prototype.UI.PositionController.Direction.RIGHT;
import static com.covart.streaming_prototype.UI.PositionController.Direction.UP;

/**
 * Created by lctseng on 2017/7/15.
 * NTU COV-ART Lab, for NCP project
 */

public class PositionController extends UIComponent {

    public enum Direction {
        NONE, LEFT, RIGHT, UP, DOWN, FORWARD, BACKWARD
    }

    private Table canvas;
    private float fadeTime = 0.2f;

    private float commonRowHeight = 70f;
    private int buttonWidth = 100;
    private float slideBarSize = 50f;

    public PositionController(){;
        createCanvas();
    }

    private void createCanvas(){
        canvas = new Table();
        canvas.setX(Gdx.graphics.getWidth() / 2);
        canvas.setWidth(Gdx.graphics.getWidth() / 2);
        canvas.setHeight(commonRowHeight);
        canvas.setDebug(false);
        canvas.setVisible(false);
        canvas.setY(0);
        canvas.right();
        canvas.bottom();
        canvas.row().height(commonRowHeight);
        addComponents();

    }

    @Override
    void setStage(Stage stage) {
        stage.addActor(canvas);
    }

    public void show(){
        canvas.addAction(Actions.sequence(Actions.alpha(0), Actions.show(),Actions.fadeIn(fadeTime)));
    }

    public void hide(){
        canvas.addAction(Actions.sequence(Actions.fadeOut(fadeTime), Actions.hide()));
    }

    private void addComponents(){
        addDirectionButton(LEFT).width(buttonWidth);
        addDirectionButton(RIGHT).width(buttonWidth);
        addDirectionButton(UP).width(buttonWidth);
        addDirectionButton(DOWN).width(buttonWidth);
        addDirectionButton(FORWARD).width(buttonWidth);
        addDirectionButton(BACKWARD).width(buttonWidth);
        addStepChangeUI();
    }

    private Cell<TextButton> addButton(String buttonText, EventListener listener){
        TextButton button = new TextButton(buttonText, skin);
        button.getStyle().font = largeFont;
        button.setStyle(button.getStyle());
        button.addListener(listener);
        return canvas.add(button);
    }

    private Cell<TextButton> addDirectionButton(final Direction direction){
        String buttonText = "";
        if(direction == LEFT){
            buttonText = "Left";
        }
        else if(direction == RIGHT){
            buttonText = "Right";
        }
        else if(direction == UP){
            buttonText = "Up";
        }
        else if(direction == DOWN){
            buttonText = "Down";
        }
        else if(direction == FORWARD){
            buttonText = "Forward";
        }
        else if(direction == BACKWARD){
            buttonText = "Backward";
        }
        EventListener listener = new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                ConfigManager.setCurrentMoveDirection(direction);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if(ConfigManager.getCurrentMoveDirection() == direction){
                    // cancel it!
                    ConfigManager.setCurrentMoveDirection(NONE);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if(ConfigManager.getCurrentMoveDirection() == direction){
                    // cancel it!
                    ConfigManager.setCurrentMoveDirection(NONE);
                }
            }
        };
        return addButton(buttonText, listener);

    }

    private void addStepChangeUI(){
        // label
        final Label name = new Label(getStepChangeText(), largeLabelStyle);

        // slider
        final HorzSlider slider = new HorzSlider(0.001f, 0.1f, 0.001f, false, skin);
        slider.setValue(ConfigManager.getManuallyMoveStep());
        enlargeSlider(slider);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setManuallyMoveStep(slider.getValue());
                name.setText(getStepChangeText());
            }
        });


        canvas.add(name);
        canvas.add(slider);

    }

    private String getStepChangeText(){
        return String.format(Locale.TAIWAN,"Step ratio: %.4f",ConfigManager.getManuallyMoveStep());
    }

    private void enlargeSlider(HorzSlider slider){
        slider.setCustomWidth(buttonWidth * 2);
        slider.setDebug(false);
        slider.getStyle().background.setMinHeight(slideBarSize);
        slider.getStyle().knob.setMinWidth(slideBarSize);
        slider.getStyle().knob.setMinHeight(slideBarSize);
        slider.setStyle(slider.getStyle());
    }



}
