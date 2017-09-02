package com.covart.streaming_prototype.Utils.Easing;

/**
 * Created by lctseng on 2017/9/1.
 * For NCP project at COVART, NTU
 */

public class EasingQuadIn extends EasingBase {

    public EasingQuadIn() {
    }

    public EasingQuadIn(float startValue, float endValue, float duration) {
        super(startValue, endValue, duration);
    }

    @Override
    public float valueAtWithoutBoundaryCheck(float time) {
        return diffValue*(time/=duration)*time + startValue;
    }
}
