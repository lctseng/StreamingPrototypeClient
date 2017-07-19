package com.covart.streaming_prototype;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.CardBoardAndroidApplication;
import com.covart.streaming_prototype.Image.ImageDecoderH264;

public class AndroidLauncher extends CardBoardAndroidApplication
{
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new StreamingPrototype(new ImageDecoderH264()), config);
	}
}
