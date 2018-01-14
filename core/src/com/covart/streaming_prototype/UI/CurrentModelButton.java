package com.covart.streaming_prototype.UI;

import java.util.Locale;

/**
 * Created by lctseng on 2018/1/11.
 * For NCP project at COVART, NTU
 */

public class CurrentModelButton extends ModelButton {

    public CurrentModelButton(EditingModelManager.ModelInfo model) {
        super(model, String.format(Locale.TAIWAN,"current_model_%d.png", model.modelId));
    }

}
