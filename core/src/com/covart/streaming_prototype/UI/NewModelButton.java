package com.covart.streaming_prototype.UI;

import java.util.Locale;

/**
 * Created by lctseng on 2018/1/11.
 * For NCP project at COVART, NTU
 */

public class NewModelButton extends ModelButton {

    public NewModelButton(int modelId) {
        super(modelId, String.format(Locale.TAIWAN,"new_model_%d.png", modelId));
    }

}
