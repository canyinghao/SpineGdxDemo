package com.badlogic.gdx.backends.android;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.surfaceview.ResolutionStrategy;

import javax.microedition.khronos.opengles.GL10;

public class AndroidGraphicsIm extends AndroidGraphics{
    public AndroidGraphicsIm(AndroidApplicationBase application, AndroidApplicationConfiguration config, ResolutionStrategy resolutionStrategy) {
        super(application, config, resolutionStrategy);
    }

    public AndroidGraphicsIm(AndroidApplicationBase application, AndroidApplicationConfiguration config, ResolutionStrategy resolutionStrategy, boolean focusableView) {
        super(application, config, resolutionStrategy, focusableView);
    }


    @Override
    public void onDrawFrame(GL10 gl) {

        try{
            if(Gdx.app==null){
                return;
            }
            super.onDrawFrame(gl);
        }catch (Throwable e){
            e.printStackTrace();
        }

    }
}
