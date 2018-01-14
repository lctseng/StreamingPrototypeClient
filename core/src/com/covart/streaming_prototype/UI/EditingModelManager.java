package com.covart.streaming_prototype.UI;

import com.badlogic.gdx.Gdx;
import com.covart.streaming_prototype.StringPool;

import java.util.ArrayList;
import java.util.List;

import StreamingFormat.Message;

/**
 * Created by lctseng on 2018/1/12.
 * For NCP project at COVART, NTU
 */

public class EditingModelManager {

    public static class ModelInfo{
        public int modelId;
        public float screenX;
        public float screenY;

    }

    public final Object currentModelLock = new Object();
    public final Object addModelLock = new Object();

    private List<ModelInfo> currentModelList;
    private List<ModelInfo> addModelList;

    private int currentIndex = -1;
    private int addIndex = -1;

    private static void convertMsgsToModelInfos(List<Integer> modelMsgList, List<ModelInfo> modelInfoList){
        modelInfoList.clear();
        // TODO: model id should be replaced
        for(Integer newModelId : modelMsgList){
            ModelInfo modelInfo = new ModelInfo();
            modelInfo.modelId = newModelId;
            modelInfoList.add(modelInfo);
        }
    }

    public EditingModelManager(){
        currentModelList = new ArrayList<ModelInfo>();
        addModelList = new ArrayList<ModelInfo>();
    }

    public void resetAllIndex(){
        currentIndex = addIndex = -1;
    }

    // TODO: model id should be replaced
    public void setupEditing(List<Integer> currentModelMsgList, List<Integer> addModelMsgList){
        convertMsgsToModelInfos(currentModelMsgList, currentModelList);
        convertMsgsToModelInfos(addModelMsgList, addModelList);
        resetAllIndex();
    }

    // TODO: model id should be replaced
    public void addNewModel(int modelId){
        ModelInfo model = new ModelInfo();
        model.modelId = modelId;
        synchronized (currentModelLock){
            currentModelList.add(model);
        }
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        Gdx.app.log("Moving Model Index", "" + currentIndex);
        StringPool.addField("Moving Model Index", "" + currentIndex);
        this.currentIndex = currentIndex;
    }

    public int getAddIndex() {
        return addIndex;
    }

    public void setAddIndex(int addIndex) {
        Gdx.app.log("Adding Model Index", "" + addIndex);
        StringPool.addField("Adding Model Index", "" + addIndex);
        this.addIndex = addIndex;
    }

    public ModelInfo getCurrentModelInfo() {
        if(currentIndex >= 0){
            return currentModelList.get(currentIndex);
        }
        else{
            return null;
        }

    }

    public void setCurrentModel(int id) {
        if(id >= 0){
            // find index by id
            for(int i = 0; i< currentModelList.size(); i++){
                if(currentModelList.get(i).modelId == id){
                    setCurrentIndex(i);
                    return;
                }
            }
            Gdx.app.error("Editing Manager", "Non-exist current model id: " + id);
        }
        else{
            setCurrentIndex(-1);
        }
    }

    public void setCurrentModel(ModelInfo info) {
        if(info != null){
            // find index by info
            for(int i = 0; i< currentModelList.size(); i++){
                if(currentModelList.get(i).modelId == info.modelId){
                    setCurrentIndex(i);
                    return;
                }
            }
            Gdx.app.error("Editing Manager", "Non-exist current model id: " + info.modelId);
        }
        else{
            setCurrentIndex(-1);
        }
    }

    public ModelInfo getAddModelInfo() {
        if(addIndex >= 0){
            return addModelList.get(addIndex);
        }
        else{
            return null;
        }

    }

    public void setAddModel(int id) {
        if(id >= 0){
            // find index by id
            for(int i = 0; i< addModelList.size(); i++){
                if(addModelList.get(i).modelId == id){
                    setAddIndex(i);
                    return;
                }
            }
            Gdx.app.error("Editing Manager", "Non-exist add model id: " + id);
        }
        else{
            setAddIndex(-1);
        }
    }

    public void setAddModel(ModelInfo info) {
        if(info != null){
            // find index by info
            for(int i = 0; i< addModelList.size(); i++){
                if(addModelList.get(i).modelId == info.modelId){
                    setAddIndex(i);
                    return;
                }
            }
            Gdx.app.error("Editing Manager", "Non-exist add model id: " + info.modelId);
        }
        else{
            setAddIndex(-1);
        }
    }

    public List<ModelInfo> getCurrentModelList() {
        return currentModelList;
    }

    public List<ModelInfo> getAddModelList() {
        return addModelList;
    }
}
