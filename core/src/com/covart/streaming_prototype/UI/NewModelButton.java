package com.covart.streaming_prototype.UI;

import java.util.Locale;

/**
 * Created by lctseng on 2018/1/11.
 * For NCP project at COVART, NTU
 */

public class NewModelButton extends ModelButton {

    public NewModelButton(EditingModelManager.ModelInfo model) {
        super(model, String.format(Locale.TAIWAN,"new_model_%d.png", model.modelId));
    }

}
