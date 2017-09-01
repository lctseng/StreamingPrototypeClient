package com.covart.streaming_prototype.Utils.Easing;

/**
 * Created by lctseng on 2017/9/1.
 * For NCP project at COVART, NTU
 */

public class EasingLinear extends EasingBase {

    public EasingLinear() {
    }

    public EasingLinear(float startValue, float endValue, float duration) {
        super(startValue, endValue, duration);
    }

    @Override
    public float valueAtWithoutBoundaryCheck(float time) {
        return startValue + diffValue * (time/duration);
    }
}
