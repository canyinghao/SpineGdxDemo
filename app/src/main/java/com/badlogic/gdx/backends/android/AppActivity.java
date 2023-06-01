//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.badlogic.gdx.backends.android;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build.VERSION;
import android.os.Debug;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout.LayoutParams;

import androidx.appcompat.app.AppCompatActivity;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.android.surfaceview.FillResolutionStrategy;
import com.badlogic.gdx.backends.android.surfaceview.ResolutionStrategy;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.SnapshotArray;

public class AppActivity extends AppCompatActivity implements AndroidApplicationBase {
    protected AndroidGraphics graphics;
    protected AndroidInput input;
    protected AndroidAudio audio;
    protected AndroidFiles files;
    protected AndroidNet net;
    protected AndroidClipboard clipboard;
    protected ApplicationListener listener;
    public Handler handler;
    protected boolean firstResume = true;
    protected final Array<Runnable> runnables = new Array<>();
    protected final Array<Runnable> executedRunnables = new Array<>();
    protected final SnapshotArray<LifecycleListener> lifecycleListeners = new SnapshotArray<>(LifecycleListener.class);
    private final Array<AndroidEventListener> androidEventListeners = new Array<>();
    protected int logLevel = 2;
    protected ApplicationLogger applicationLogger;
    protected boolean useImmersiveMode = false;
    protected boolean hideStatusBar = false;
    private int wasFocusChanged = -1;
    private boolean isWaitingForAudio = false;
    private LifecycleListener lifecycleListener;
    private boolean isInit;
    private AppAndroidInput defaultAndroidInput;
    public AppActivity() {
    }

    public void initialize(ApplicationListener listener) {
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        this.initialize(listener, config);
    }

    public void initialize(ApplicationListener listener, AndroidApplicationConfiguration config) {
        this.init(listener, config, false);
    }

    public View initializeForView(ApplicationListener listener) {
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        return this.initializeForView(listener, config);
    }

    public View initializeForView(ApplicationListener listener, AndroidApplicationConfiguration config) {
        this.init(listener, config, true);
        return this.graphics.getView();
    }

    private void init(ApplicationListener listener, AndroidApplicationConfiguration config, boolean isForView) {
        if (this.getVersion() < 14) {
            throw new GdxRuntimeException("LibGDX requires Android API Level 14 or later.");
        } else {

            this.setApplicationLogger(new AndroidApplicationLogger());
            this.graphics = new AndroidGraphicsIm(this, config, (ResolutionStrategy)(config.resolutionStrategy == null ? new FillResolutionStrategy() : config.resolutionStrategy));
            this.input = this.createInput(this, this, this.graphics.view, config);
            this.audio = this.createAudio(this, config);
            this.files = this.createFiles();
            this.net = new AndroidNet(this, config);
            this.listener = listener;
            this.handler = new Handler();
            this.useImmersiveMode = config.useImmersiveMode;
            this.hideStatusBar = config.hideStatusBar;
            this.clipboard = new AndroidClipboard(this);
            if(this.lifecycleListener!=null){
                this.removeLifecycleListener(this.lifecycleListener);
            }
            this.lifecycleListener = new LifecycleListener() {
                public void resume() {
                }

                public void pause() {
                    try {
                        AppActivity.this.audio.pause();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }

                public void dispose() {
                    try {
                        AppActivity.this.audio.dispose();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            };
            this.addLifecycleListener(this.lifecycleListener);
            Gdx.app = this;
            Gdx.input = this.getInput();
            Gdx.audio = this.getAudio();
            Gdx.files = this.getFiles();
            Gdx.graphics = this.getGraphics();
            Gdx.net = this.getNet();
            if (!isForView) {
                try {
                    this.requestWindowFeature(1);
                } catch (Exception var5) {
                    this.log("AndroidApplication", "Content already displayed, cannot request FEATURE_NO_TITLE", var5);
                }

                this.getWindow().setFlags(1024, 1024);
                this.getWindow().clearFlags(2048);
                this.setContentView(this.graphics.getView(), this.createLayoutParams());
            }

            this.createWakeLock(config.useWakelock);
            this.hideStatusBar(this.hideStatusBar);
            this.useImmersiveMode(this.useImmersiveMode);
            if (this.useImmersiveMode && this.getVersion() >= 19) {
                AndroidVisibilityListener vlistener = new AndroidVisibilityListener();
                vlistener.createListener(this);
            }

            if (this.getResources().getConfiguration().keyboard != 1) {
                this.input.setKeyboardAvailable(true);
            }
            isInit = true;

        }
    }

    protected LayoutParams createLayoutParams() {
        LayoutParams layoutParams = new LayoutParams(-1, -1);
        layoutParams.gravity = 17;
        return layoutParams;
    }

    protected void createWakeLock(boolean use) {
        if (use) {
            this.getWindow().addFlags(128);
        }

    }

    protected void hideStatusBar(boolean hide) {
        if (hide) {
            View rootView = this.getWindow().getDecorView();
            rootView.setSystemUiVisibility(1);
        }
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        this.useImmersiveMode(this.useImmersiveMode);
        this.hideStatusBar(this.hideStatusBar);
        if (hasFocus) {
            this.wasFocusChanged = 1;
            if (this.isWaitingForAudio) {
                if(this.audio!=null){
                    this.audio.resume();
                }
                this.isWaitingForAudio = false;
            }
        } else {
            this.wasFocusChanged = 0;
        }

    }

    @TargetApi(19)
    public void useImmersiveMode(boolean use) {
        try {
            if (use && this.getVersion() >= 19) {
                View view = this.getWindow().getDecorView();
                int code = 5894;
                view.setSystemUiVisibility(code);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    protected void onPause() {
        if(!isInit){
            super.onPause();
            return;
        }
        try {
            boolean isContinuous = this.graphics.isContinuousRendering();
            boolean isContinuousEnforced = AndroidGraphics.enforceContinuousRendering;
            AndroidGraphics.enforceContinuousRendering = true;
            this.graphics.setContinuousRendering(true);
            this.graphics.pause();
            this.input.onPause();
            if (this.isFinishing()) {
                this.graphics.clearManagedCaches();
                this.graphics.destroy();
            }

            AndroidGraphics.enforceContinuousRendering = isContinuousEnforced;
            this.graphics.setContinuousRendering(isContinuous);
            this.graphics.onPauseGLSurfaceView();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    protected void onResume() {
        try {

            if(!isInit){
                firstResume = false;
                super.onResume();
                return;
            }
            Gdx.app = this;
            Gdx.input = this.getInput();
            Gdx.audio = this.getAudio();
            Gdx.files = this.getFiles();
            Gdx.graphics = this.getGraphics();
            Gdx.net = this.getNet();
            this.input.onResume();
            if (this.graphics != null) {
                this.graphics.onResumeGLSurfaceView();
                if(!firstResume){
                    this.graphics.resume();
                }
                firstResume = false;
            }
            this.isWaitingForAudio = true;
            if (this.wasFocusChanged == 1 || this.wasFocusChanged == -1) {
                this.audio.resume();
                this.isWaitingForAudio = false;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        super.onResume();
    }


    protected void onDestroy() {
        try {
            if(handler!=null){
                handler.removeCallbacksAndMessages(null);
            }
            handler = null;
            listener = null;
            if(defaultAndroidInput!=null){
                defaultAndroidInput.destroy();
            }
            defaultAndroidInput = null;
            net = null;
            graphics = null;
            clipboard = null;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        super.onDestroy();

        Gdx.app = null;
        Gdx.input = null;
        Gdx.audio = null;
        Gdx.files = null;
        Gdx.graphics = null;
        Gdx.net = null;
    }

    public ApplicationListener getApplicationListener() {
        return this.listener;
    }

    public Audio getAudio() {
        return this.audio;
    }

    public AndroidInput getInput() {
        return this.input;
    }

    public Files getFiles() {
        return this.files;
    }

    public Graphics getGraphics() {
        return this.graphics;
    }

    public Net getNet() {
        return this.net;
    }

    public ApplicationType getType() {
        return ApplicationType.Android;
    }

    public int getVersion() {
        return VERSION.SDK_INT;
    }

    public long getJavaHeap() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    public long getNativeHeap() {
        return Debug.getNativeHeapAllocatedSize();
    }

    public Preferences getPreferences(String name) {
        return new AndroidPreferences(this.getSharedPreferences(name, 0));
    }

    public Clipboard getClipboard() {
        return this.clipboard;
    }

    public void postRunnable(Runnable runnable) {
        synchronized(this.runnables) {
            this.runnables.add(runnable);
            Gdx.graphics.requestRendering();
        }
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        try {
            boolean keyboardAvailable = false;
            if (config.hardKeyboardHidden == 1) {
                keyboardAvailable = true;
            }
            if(this.input!=null){
                this.input.setKeyboardAvailable(keyboardAvailable);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public void exit() {
        this.handler.post(new Runnable() {
            public void run() {
                AppActivity.this.finish();
            }
        });
    }

    public void debug(String tag, String message) {
        if (this.logLevel >= 3) {
            this.getApplicationLogger().debug(tag, message);
        }

    }

    public void debug(String tag, String message, Throwable exception) {
        if (this.logLevel >= 3) {
            this.getApplicationLogger().debug(tag, message, exception);
        }

    }

    public void log(String tag, String message) {
        if (this.logLevel >= 2) {
            this.getApplicationLogger().log(tag, message);
        }

    }

    public void log(String tag, String message, Throwable exception) {
        if (this.logLevel >= 2) {
            this.getApplicationLogger().log(tag, message, exception);
        }

    }

    public void error(String tag, String message) {
        if (this.logLevel >= 1) {
            this.getApplicationLogger().error(tag, message);
        }

    }

    public void error(String tag, String message, Throwable exception) {
        if (this.logLevel >= 1) {
            this.getApplicationLogger().error(tag, message, exception);
        }

    }

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    public int getLogLevel() {
        return this.logLevel;
    }

    public void setApplicationLogger(ApplicationLogger applicationLogger) {
        this.applicationLogger = applicationLogger;
    }

    public ApplicationLogger getApplicationLogger() {
        return this.applicationLogger;
    }

    public void addLifecycleListener(LifecycleListener listener) {
        synchronized(this.lifecycleListeners) {
            this.lifecycleListeners.add(listener);
        }
    }

    public void removeLifecycleListener(LifecycleListener listener) {
        synchronized(this.lifecycleListeners) {
            this.lifecycleListeners.removeValue(listener, true);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        synchronized(this.androidEventListeners) {
            try {
                for(int i = 0; i < this.androidEventListeners.size; ++i) {
                    ((AndroidEventListener)this.androidEventListeners.get(i)).onActivityResult(requestCode, resultCode, data);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }

        }
    }

    public void addAndroidEventListener(AndroidEventListener listener) {
        synchronized(this.androidEventListeners) {
            try {
                this.androidEventListeners.add(listener);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public void removeAndroidEventListener(AndroidEventListener listener) {
        synchronized(this.androidEventListeners) {
            try {
                this.androidEventListeners.removeValue(listener, true);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public Context getContext() {
        return this;
    }

    public Array<Runnable> getRunnables() {
        return this.runnables;
    }

    public Array<Runnable> getExecutedRunnables() {
        return this.executedRunnables;
    }

    public SnapshotArray<LifecycleListener> getLifecycleListeners() {
        return this.lifecycleListeners;
    }

    public Window getApplicationWindow() {
        return this.getWindow();
    }

    public Handler getHandler() {
        return this.handler;
    }

    public AndroidAudio createAudio(Context context, AndroidApplicationConfiguration config) {
        return new DefaultAndroidAudio(context, config);
    }

    public AndroidInput createInput(Application activity, Context context, Object view, AndroidApplicationConfiguration config) {
        if(defaultAndroidInput==null){
            defaultAndroidInput =new AppAndroidInput(this, getApplicationContext(), graphics.view, config);
        }
        return defaultAndroidInput;
    }

    protected AndroidFiles createFiles() {
        this.getFilesDir();
        return new DefaultAndroidFiles(this.getAssets(), this, true);
    }

    public void setInit(boolean init) {
        isInit = init;
    }

    static {
        GdxNativesLoader.load();
    }


}
