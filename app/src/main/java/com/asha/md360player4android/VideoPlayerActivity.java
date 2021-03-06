package com.asha.md360player4android;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MD360DirectorFactory;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.BarrelDistortionConfig;
import com.asha.vrlib.model.MDPinchConfig;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.util.SimpleArrayMap;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.asha.vrlib.MDDirectorCamUpdate;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.MDHitEvent;
import com.asha.vrlib.model.MDHotspotBuilder;
import com.asha.vrlib.model.MDPosition;
import com.asha.vrlib.model.MDRay;
import com.asha.vrlib.model.MDViewBuilder;
import com.asha.vrlib.model.position.MDMutablePosition;
import com.asha.vrlib.plugins.MDAbsPlugin;
import com.asha.vrlib.plugins.MDWidgetPlugin;
import com.asha.vrlib.plugins.hotspot.IMDHotspot;
import com.asha.vrlib.plugins.hotspot.MDAbsHotspot;
import com.asha.vrlib.plugins.hotspot.MDAbsView;
import com.asha.vrlib.plugins.hotspot.MDSimpleHotspot;
import com.asha.vrlib.plugins.hotspot.MDView;
import com.asha.vrlib.texture.MD360BitmapTexture;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static android.animation.PropertyValuesHolder.ofFloat;
import static com.squareup.picasso.MemoryPolicy.NO_CACHE;
import static com.squareup.picasso.MemoryPolicy.NO_STORE;

public  class VideoPlayerActivity extends Activity {

    private static final String TAG = "VideoPlayerActivity";
    private MediaPlayerWrapper mMediaPlayerWrapper = new MediaPlayerWrapper();

    private static final SparseArray<String> sDisplayMode = new SparseArray<>();
    private static final SparseArray<String> sInteractiveMode = new SparseArray<>();
    private static final SparseArray<String> sProjectionMode = new SparseArray<>();
    private static final SparseArray<String> sAntiDistortion = new SparseArray<>();
    private static final SparseArray<String> sPitchFilter = new SparseArray<>();
    private static final SparseArray<String> sFlingEnabled = new SparseArray<>();

    static {
//        sDisplayMode.put(MDVRLibrary.DISPLAY_MODE_NORMAL,"NORMAL");
        sDisplayMode.put(MDVRLibrary.DISPLAY_MODE_GLASS,"GLASS");

//        sInteractiveMode.put(MDVRLibrary.INTERACTIVE_MODE_MOTION,"MOTION");
//        sInteractiveMode.put(MDVRLibrary.INTERACTIVE_MODE_TOUCH,"TOUCH");
//        sInteractiveMode.put(MDVRLibrary.INTERACTIVE_MODE_MOTION_WITH_TOUCH,"M & T");
//        sInteractiveMode.put(MDVRLibrary.INTERACTIVE_MODE_CARDBORAD_MOTION,"CARDBOARD M");
        sInteractiveMode.put(MDVRLibrary.INTERACTIVE_MODE_CARDBORAD_MOTION_WITH_TOUCH,"CARDBOARD M&T");

        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_SPHERE,"SPHERE");
//        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_DOME180,"DOME 180");
//        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_DOME230,"DOME 230");
//        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_DOME180_UPPER,"DOME 180 UPPER");
//        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_DOME230_UPPER,"DOME 230 UPPER");
//        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_STEREO_SPHERE_HORIZONTAL,"STEREO H SPHERE");
//        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_STEREO_SPHERE_VERTICAL,"STEREO V SPHERE");
//        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_PLANE_FIT,"PLANE FIT");
//        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_PLANE_CROP,"PLANE CROP");
//        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_PLANE_FULL,"PLANE FULL");
//        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_MULTI_FISH_EYE_HORIZONTAL,"MULTI FISH EYE HORIZONTAL");
//        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_MULTI_FISH_EYE_VERTICAL,"MULTI FISH EYE VERTICAL");
//        sProjectionMode.put(CustomProjectionFactory.CUSTOM_PROJECTION_FISH_EYE_RADIUS_VERTICAL,"CUSTOM MULTI FISH EYE");

//        sAntiDistortion.put(1,"ANTI-ENABLE");
//        sAntiDistortion.put(0,"ANTI-DISABLE");
//
//        sPitchFilter.put(1,"FILTER PITCH");
//        sPitchFilter.put(0,"FILTER NOP");
//
//        sFlingEnabled.put(1, "FLING ENABLED");
//        sFlingEnabled.put(0, "FLING DISABLED");
    }

    public static void startVideo(Context context, Uri uri){
        start(context, uri, VideoPlayerActivity.class);
    }

    public static void startBitmap(Context context, Uri uri){
        start(context, uri, BitmapPlayerActivity.class);
    }

    public static void startCubemap(Context context, Uri uri){
        start(context, uri, CubemapPlayerActivity.class);
    }

    private static void start(Context context, Uri uri, Class<? extends Activity> clz){
        Intent i = new Intent(context,clz);
        i.setData(uri);
        context.startActivity(i);
    }

    private MDVRLibrary mVRLibrary;

    // load resource from android drawable and remote url.
    private MDVRLibrary.IImageLoadProvider mImageLoadProvider = new ImageLoadProvider();

    // load resource from android drawable only.
    private MDVRLibrary.IImageLoadProvider mAndroidProvider = new AndroidProvider(this);

    private List<MDAbsPlugin> plugins = new LinkedList<>();

    private MDPosition logoPosition = MDMutablePosition.newInstance().setY(-8.0f).setYaw(-90.0f);

    private MDPosition[] positions = new MDPosition[]{
            MDPosition.newInstance().setZ(-8.0f).setYaw(-45.0f),
            MDPosition.newInstance().setZ(-18.0f).setYaw(15.0f).setAngleX(15),
            MDPosition.newInstance().setZ(-10.0f).setYaw(-10.0f).setAngleX(-15),
            MDPosition.newInstance().setZ(-10.0f).setYaw(30.0f).setAngleX(30),
            MDPosition.newInstance().setZ(-10.0f).setYaw(-30.0f).setAngleX(-30),
            MDPosition.newInstance().setZ(-5.0f).setYaw(30.0f).setAngleX(60),
            MDPosition.newInstance().setZ(-3.0f).setYaw(15.0f).setAngleX(-45),
            MDPosition.newInstance().setZ(-3.0f).setYaw(15.0f).setAngleX(-45).setAngleY(45),
            MDPosition.newInstance().setZ(-3.0f).setYaw(0.0f).setAngleX(90),
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // no title
        requestWindowFeature(Window.FEATURE_NO_TITLE); // full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_md_using_surface_view);
        EventBus.getDefault().register(this);
        // init VR Library
        mVRLibrary = createVRLibrary();
        final Activity activity = this;
        final List<View> hotspotPoints = new LinkedList<>();
        hotspotPoints.add(findViewById(R.id.hotspot_point1));
        hotspotPoints.add(findViewById(R.id.hotspot_point2));

        SpinnerHelper.with(this)
                .setData(sDisplayMode)
                .setDefault(mVRLibrary.getDisplayMode())
                .setClickHandler(new SpinnerHelper.ClickHandler() {
                    @Override
                    public void onSpinnerClicked(int index, int key, String value) {
                        mVRLibrary.switchDisplayMode(VideoPlayerActivity.this, key);
                        int i = 0;
                        int size = key == MDVRLibrary.DISPLAY_MODE_GLASS ? 2 : 1;
                        for (View point : hotspotPoints){
                            point.setVisibility(i < size ? View.VISIBLE : View.GONE);
                            i++;
                        }
                    }
                })
                .init(R.id.spinner_display);

        SpinnerHelper.with(this)
                .setData(sInteractiveMode)
                .setDefault(mVRLibrary.getInteractiveMode())
                .setClickHandler(new SpinnerHelper.ClickHandler() {
                    @Override
                    public void onSpinnerClicked(int index, int key, String value) {
                        mVRLibrary.switchInteractiveMode(VideoPlayerActivity.this, key);
                    }
                })
                .init(R.id.spinner_interactive);

        SpinnerHelper.with(this)
                .setData(sProjectionMode)
                .setDefault(mVRLibrary.getProjectionMode())
                .setClickHandler(new SpinnerHelper.ClickHandler() {
                    @Override
                    public void onSpinnerClicked(int index, int key, String value) {
                        mVRLibrary.switchProjectionMode(VideoPlayerActivity.this, key);
                    }
                })
                .init(R.id.spinner_projection);

        SpinnerHelper.with(this)
                .setData(sAntiDistortion)
                .setDefault(mVRLibrary.isAntiDistortionEnabled() ? 1 : 0)
                .setClickHandler(new SpinnerHelper.ClickHandler() {
                    @Override
                    public void onSpinnerClicked(int index, int key, String value) {
                        mVRLibrary.setAntiDistortionEnabled(key != 0);
                    }
                })
                .init(R.id.spinner_distortion);

        findViewById(R.id.button_add_plugin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int index = (int) (Math.random() * 100) % positions.length;
                MDPosition position = positions[index];
                MDHotspotBuilder builder = MDHotspotBuilder.create(mImageLoadProvider)
                        .size(4f,4f)
                        .provider(0, activity, android.R.drawable.star_off)
                        .provider(1, activity, android.R.drawable.star_on)
                        .provider(10, activity, android.R.drawable.checkbox_off_background)
                        .provider(11, activity, android.R.drawable.checkbox_on_background)
                        .listenClick(new MDVRLibrary.ITouchPickListener() {
                            @Override
                            public void onHotspotHit(IMDHotspot hitHotspot, MDRay ray) {
                                if (hitHotspot instanceof MDWidgetPlugin){
                                    MDWidgetPlugin widgetPlugin = (MDWidgetPlugin) hitHotspot;
                                    widgetPlugin.setChecked(!widgetPlugin.getChecked());
                                }
                            }
                        })
                        .title("star" + index)
                        .position(position)
                        .status(0,1)
                        .checkedStatus(10,11);

                MDWidgetPlugin plugin = new MDWidgetPlugin(builder);

                plugins.add(plugin);
                getVRLibrary().addPlugin(plugin);
                Toast.makeText(VideoPlayerActivity.this, "add plugin position:" + position, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.button_add_plugin_logo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MDHotspotBuilder builder = MDHotspotBuilder.create(mImageLoadProvider)
                        .size(4f,4f)
                        .provider(activity, R.drawable.moredoo_logo)
                        .title("logo")
                        .position(logoPosition)
                        .listenClick(new MDVRLibrary.ITouchPickListener() {
                            @Override
                            public void onHotspotHit(IMDHotspot hitHotspot, MDRay ray) {
                                Toast.makeText(VideoPlayerActivity.this, "click logo", Toast.LENGTH_SHORT).show();
                            }
                        });
                MDAbsHotspot hotspot = new MDSimpleHotspot(builder);
                plugins.add(hotspot);
                getVRLibrary().addPlugin(hotspot);
                Toast.makeText(VideoPlayerActivity.this, "add plugin logo" , Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.button_remove_plugin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (plugins.size() > 0){
                    MDAbsPlugin plugin = plugins.remove(plugins.size() - 1);
                    getVRLibrary().removePlugin(plugin);
                }
            }
        });

        findViewById(R.id.button_remove_plugins).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plugins.clear();
                getVRLibrary().removePlugins();
            }
        });

        findViewById(R.id.button_add_hotspot_front).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MDHotspotBuilder builder = MDHotspotBuilder.create(mImageLoadProvider)
                        .size(4f,4f)
                        .provider(activity, R.drawable.moredoo_logo)
                        .title("front logo")
                        .tag("tag-front")
                        .position(MDPosition.newInstance().setZ(-12.0f).setY(-1.0f));
                MDAbsHotspot hotspot = new MDSimpleHotspot(builder);
                hotspot.rotateToCamera();
                plugins.add(hotspot);
                getVRLibrary().addPlugin(hotspot);
            }
        });

        findViewById(R.id.button_rotate_to_camera_plugin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IMDHotspot hotspot = getVRLibrary().findHotspotByTag("tag-front");
                if (hotspot != null){
                    hotspot.rotateToCamera();
                }
            }
        });

        findViewById(R.id.button_add_md_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                TextView textView = new TextView(activity);
//                textView.setBackgroundColor(0x55FFCC11);
//                textView.setText("Hello world.");
                FrameLayout relativeLayout = (FrameLayout) findViewById(R.id.framelayout);
                LayoutInflater inflater = LayoutInflater.from(VideoPlayerActivity.this);
                View view = inflater.inflate(R.layout.layout_test, relativeLayout, false);

                MDViewBuilder builder = MDViewBuilder.create()
                        .provider(view, 720/*view width*/, 1280/*view height*/)
                        .size(2f,2f)
                        .position(MDPosition.newInstance().setZ(-1.0f))
                        .title("md view")
                        .tag("tag-md-text-view")
                        ;

                MDAbsView mdView = new MDView(builder);
                plugins.add(mdView);
                getVRLibrary().addPlugin(mdView);

//                TextView textView = new TextView(activity);
//                textView.setBackgroundColor(0x55FFCC11);
//                textView.setText("Hello world.");
//
//                MDViewBuilder builder = MDViewBuilder.create()
//                        .provider(textView, 400/*view width*/, 100/*view height*/)
//                        .size(4, 1)
//                        .position(MDPosition.newInstance().setZ(-12.0f))
//                        .title("md view")
//                        .tag("tag-md-text-view")
//                        ;
//
//                MDAbsView mdView = new MDView(builder);
//                plugins.add(mdView);
//                getVRLibrary().addPlugin(mdView);
            }
        });

        findViewById(R.id.button_update_md_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    MDAbsView mdView = getVRLibrary().findViewByTag("tag-md-text-view");
                    if (mdView != null) {
                        TextView textView = mdView.castAttachedView(TextView.class);
                        textView.setText("Cheer up!");
                        textView.setBackgroundColor(0x8800FF00);
                        mdView.invalidate();
                    }
                }catch (Throwable t){t.printStackTrace();}
            }
        });

        findViewById(R.id.button_md_view_hover).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               addControl();
            }
        });

        final TextView hotspotText = (TextView) findViewById(R.id.hotspot_text);
        final TextView directorBriefText = (TextView) findViewById(R.id.director_brief_text);
        getVRLibrary().setEyePickChangedListener(new MDVRLibrary.IEyePickListener2() {
            @Override
            public void onHotspotHit(MDHitEvent hitEvent) {
                IMDHotspot hotspot = hitEvent.getHotspot();
                long hitTimestamp = hitEvent.getTimestamp();
                String text = hotspot == null ? "nop" : String.format(Locale.CHINESE, "%s  %fs", hotspot.getTitle(), (System.currentTimeMillis() - hitTimestamp) / 1000.0f );
                hotspotText.setText(text);
                myprogress(text,System.currentTimeMillis() - hitTimestamp);
                String brief = getVRLibrary().getDirectorBrief().toString();
                directorBriefText.setText(brief);
                if (System.currentTimeMillis() - hitTimestamp > 1000*60*60){//5秒钟
                    getVRLibrary().resetEyePick();
                }
            }
        });

        findViewById(R.id.button_camera_little_planet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MDDirectorCamUpdate cameraUpdate = getVRLibrary().updateCamera();
                PropertyValuesHolder near = ofFloat("near", cameraUpdate.getNearScale(), -0.5f);
                PropertyValuesHolder eyeZ = PropertyValuesHolder.ofFloat("eyeZ", cameraUpdate.getEyeZ(), 18f);
                PropertyValuesHolder pitch = PropertyValuesHolder.ofFloat("pitch", cameraUpdate.getPitch(), 90f);
                PropertyValuesHolder yaw = PropertyValuesHolder.ofFloat("yaw", cameraUpdate.getYaw(), 90f);
                PropertyValuesHolder roll = PropertyValuesHolder.ofFloat("roll", cameraUpdate.getRoll(), 0f);
                startCameraAnimation(cameraUpdate, near, eyeZ, pitch, yaw, roll);
            }
        });

        findViewById(R.id.button_camera_to_normal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MDDirectorCamUpdate cameraUpdate = getVRLibrary().updateCamera();
                PropertyValuesHolder near = ofFloat("near", cameraUpdate.getNearScale(), 0f);
                PropertyValuesHolder eyeZ = PropertyValuesHolder.ofFloat("eyeZ", cameraUpdate.getEyeZ(), 0f);
                PropertyValuesHolder pitch = PropertyValuesHolder.ofFloat("pitch", cameraUpdate.getPitch(), 0f);
                PropertyValuesHolder yaw = PropertyValuesHolder.ofFloat("yaw", cameraUpdate.getYaw(), 0f);
                PropertyValuesHolder roll = PropertyValuesHolder.ofFloat("roll", cameraUpdate.getRoll(), 0f);
                startCameraAnimation(cameraUpdate, near, eyeZ, pitch, yaw, roll);
            }
        });

        SpinnerHelper.with(this)
                .setData(sPitchFilter)
                .setDefault(0)
                .setClickHandler(new SpinnerHelper.ClickHandler() {
                    @Override
                    public void onSpinnerClicked(int index, int key, String value) {
                        MDVRLibrary.IDirectorFilter filter = key == 0 ? null : new MDVRLibrary.DirectorFilterAdatper() {
                            @Override
                            public float onFilterPitch(float input) {
                                if (input > 70){
                                    return 70;
                                }

                                if (input < -70){
                                    return -70;
                                }

                                return input;
                            }
                        };

                        getVRLibrary().setDirectorFilter(filter);
                    }
                })
                .init(R.id.spinner_pitch_filter);

        SpinnerHelper.with(this)
                .setData(sFlingEnabled)
                .setDefault(getVRLibrary().isFlingEnabled() ? 1 : 0)
                .setClickHandler(new SpinnerHelper.ClickHandler() {
                    @Override
                    public void onSpinnerClicked(int index, int key, String value) {
                        getVRLibrary().setFlingEnabled(key == 1);
                    }
                })
                .init(R.id.spinner_fling_enable);



        mMediaPlayerWrapper.init();
        mMediaPlayerWrapper.setPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                cancelBusy();
                if (getVRLibrary() != null){
                    getVRLibrary().notifyPlayerChanged();
                }
            }
        });

        mMediaPlayerWrapper.getPlayer().setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                String error = String.format("Play Error what=%d extra=%d",what,extra);
                Toast.makeText(VideoPlayerActivity.this, error, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        mMediaPlayerWrapper.getPlayer().setOnVideoSizeChangedListener(new IMediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
                getVRLibrary().onTextureResize(width, height);
            }
        });

try {
    mMediaPlayerWrapper.pause();
    mMediaPlayerWrapper.destroy();
    mMediaPlayerWrapper.init();
    mMediaPlayerWrapper.openRemoteFile(Environment.getExternalStorageDirectory()+"/a.mp4");
//    mMediaPlayerWrapper.openRemoteFile(DemoActivity.sPath + "video_31b451b7ca49710719b19d22e19d9e60.mp4");
    mMediaPlayerWrapper.prepare();
}catch (Throwable t){t.printStackTrace();}
        mMediaPlayerWrapper.getPlayer().setScreenOnWhilePlaying(true);

        mMediaPlayerWrapper.getPlayer().setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override public void onCompletion(IMediaPlayer iMediaPlayer) {
                SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
                try {
                    MDAbsView mdView = getVRLibrary().findViewByTag("txt_left");
                    if (mdView != null) {
                        TextView textView = mdView.castAttachedView(TextView.class);
                        textView.setText(formatter.format(mMediaPlayerWrapper.getPlayer().getCurrentPosition()));
                        mdView.invalidate();
                        double index=(double)mMediaPlayerWrapper.getPlayer().getCurrentPosition();
                        double total=(double)mMediaPlayerWrapper.getPlayer().getDuration();
                        ((HoverView) view).setSeek((index/total));
                    }
                }catch (Throwable t){t.printStackTrace();}

                try {
                    MDAbsView mdView = getVRLibrary().findViewByTag("hover");
                    if (mdView != null) {
                        HoverView hoverView = mdView.castAttachedView(HoverView.class);
                        double index=(double)mMediaPlayerWrapper.getPlayer().getDuration();
                        double total=(double)mMediaPlayerWrapper.getPlayer().getDuration();
                        hoverView.setSeek((index/total));
                        mdView.invalidate();
                    }
                }catch (Throwable t){t.printStackTrace();}



            }
        });

        mMediaPlayerWrapper.getPlayer().setOnBufferingUpdateListener(new IMediaPlayer.OnBufferingUpdateListener() {
            @Override public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {

                SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
                try {
                    MDAbsView mdView = getVRLibrary().findViewByTag("txt_left");
                    if (mdView != null) {
                        TextView textView = mdView.castAttachedView(TextView.class);
                        textView.setText(formatter.format(mMediaPlayerWrapper.getPlayer().getCurrentPosition()));
                        mdView.invalidate();
                    }
                }catch (Throwable t){t.printStackTrace();}
                try {
                    MDAbsView mdView = getVRLibrary().findViewByTag("hover");
                    if (mdView != null) {
                        HoverView hoverView = mdView.castAttachedView(HoverView.class);
                        double index=(double)mMediaPlayerWrapper.getPlayer().getCurrentPosition();
                        double total=(double)mMediaPlayerWrapper.getPlayer().getDuration();
                        hoverView.setSeek((index/total));
                        mdView.invalidate();
                    }
                }catch (Throwable t){t.printStackTrace();}

            }
        });

        mMediaPlayerWrapper.getPlayer().setOnSeekCompleteListener(new IMediaPlayer.OnSeekCompleteListener() {
            @Override public void onSeekComplete(IMediaPlayer iMediaPlayer) {
                Log.i("myvr","Seek完成:"+iMediaPlayer);

            }
        });






        findViewById(R.id.control_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaPlayerWrapper.pause();
                mMediaPlayerWrapper.destroy();
                mMediaPlayerWrapper.init();
                mMediaPlayerWrapper.openRemoteFile(DemoActivity.sPath + "a.mp4");
                mMediaPlayerWrapper.prepare();
            }
        });


    }

    View view;
    private void addControl(){
       //背景
        MDPosition pos_back=MDPosition.newInstance().setAngleX(4f).setY(-1.5f).setZ(-9.0f);//.setYaw(15.0f);
        MDHotspotBuilder builder_back = MDHotspotBuilder.create(mImageLoadProvider)
                .size(7f,2f)
                .provider(this, R.mipmap.heia)
                .title("pos_back")
                .position(pos_back);
        MDAbsHotspot hotspot_back = new MDSimpleHotspot(builder_back);
        plugins.add(hotspot_back);
        getVRLibrary().addPlugin(hotspot_back);

        //两个时间
        MDPosition pos_txt_left=MDPosition.newInstance().setX(-4.9f).setY(-0.3f).setZ(-15.0f);//.setYaw(15.0f);
        MDPosition pos_txt_right=MDPosition.newInstance().setX(5.1f).setY(-0.3f).setZ(-15.0f);//.setYaw(15.0f);
        TextView txt_left = new TextView(this);
                 txt_left.setTextColor(0xffffffff);
                 txt_left.setText("00:00");

                MDViewBuilder builder_txt_left = MDViewBuilder.create()
                        .provider(txt_left, 110/*view width*/, 50/*view height*/)
                        .size(1.0f, 1)
                        .position(pos_txt_left)
                        .title("txt_left")
                        .tag("txt_left") ;

                MDAbsView mdView_txt_left = new MDView(builder_txt_left);
                plugins.add(mdView_txt_left);
                getVRLibrary().addPlugin(mdView_txt_left);

        TextView txt_right = new TextView(this);
                 txt_right.setTextColor(0xffffffff);
                 txt_right.setText("05:25");

        MDViewBuilder builder_txt_right = MDViewBuilder.create()
                .provider(txt_right, 110/*view width*/, 50/*view height*/)
                .size(1.0f, 1)
                .position(pos_txt_right)
                .title("txt_right")
                .tag("txt_right") ;
        MDAbsView mdView_txt_right= new MDView(builder_txt_right);
        plugins.add(mdView_txt_right);
        getVRLibrary().addPlugin(mdView_txt_right);




        //进度条
        MDPosition pos_progress=MDPosition.newInstance().setAngleX(6f).setY(-2.8f).setZ(-23.0f);//.setYaw(15.0f);
          view = new HoverView(VideoPlayerActivity.this);//view 层自带进度值 外部调用
          view.setBackgroundColor(0x9083CC39);

        MDViewBuilder builder = MDViewBuilder.create()
                .provider(view, 300/*view width*/, 200/*view height*/)
                .size(12f, 0.5f)
                .position(pos_progress)
                .title("hover")
                .tag("hover");

        MDAbsView mdView = new MDView(builder);
//        mdView.rotateToCamera();
        plugins.add(mdView);
        getVRLibrary().addPlugin(mdView);

        //更新时间
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
        try {
            MDAbsView fi_txt_right = getVRLibrary().findViewByTag("txt_right");
            if (fi_txt_right != null) {
                TextView textView = fi_txt_right.castAttachedView(TextView.class);
                textView.setText(formatter.format(mMediaPlayerWrapper.getPlayer().getDuration()));
                fi_txt_right.invalidate();
            }
        }catch (Throwable t){t.printStackTrace();}



        //快进播放等
        MDPosition pos_p1=MDPosition.newInstance().setX(-3.3f).setY(-2.0f).setZ(-15.0f);//.setYaw(15.0f);
        MDHotspotBuilder builder_p1 = MDHotspotBuilder.create(mImageLoadProvider)
                .size(2f,2f)
                .provider(0, this, R.mipmap.p1_k)
                .provider(1, this, R.mipmap.p1)
                .title("p1")
                .position(pos_p1)
                .status(0,1)
                .checkedStatus(1,0);
        MDWidgetPlugin plugin_p1 = new MDWidgetPlugin(builder_p1);
        plugins.add(plugin_p1);
        getVRLibrary().addPlugin(plugin_p1);


        //快进播放等
        MDPosition pos_p2=MDPosition.newInstance().setX(-0.5f).setY(-2.0f).setZ(-15.0f);//.setYaw(15.0f);
        MDHotspotBuilder builder_p2 = MDHotspotBuilder.create(mImageLoadProvider)
                .size(2f,2f)
                .provider(0, this, R.mipmap.p2_k)
                .provider(1, this, R.mipmap.p2)
                .title("p2")
                .position(pos_p2)
                .status(0,1)
                .checkedStatus(1,0);
        MDWidgetPlugin plugin_p2 = new MDWidgetPlugin(builder_p2);
        plugins.add(plugin_p2);
        getVRLibrary().addPlugin(plugin_p2);

       //快进播放等
        MDPosition pos_p4=MDPosition.newInstance().setX(2.5f).setY(-2f).setZ(-15.0f);//.setYaw(15.0f);
        MDHotspotBuilder builder_p4 = MDHotspotBuilder.create(mImageLoadProvider)
                .size(2f,2f)
                .provider(0, this, R.mipmap.p4_k)
                .provider(1, this, R.mipmap.p4)
                .title("p4")
                .position(pos_p4)
                .status(0,1)
                .checkedStatus(1,0);
        MDWidgetPlugin plugin_p4 = new MDWidgetPlugin(builder_p4);
        plugins.add(plugin_p4);
        getVRLibrary().addPlugin(plugin_p4);


        //快进播放等
        MDPosition pos_p5=MDPosition.newInstance().setX(4.8f).setY(-2.0f).setZ(-15.0f);//.setYaw(15.0f);
        MDHotspotBuilder builder_p5 = MDHotspotBuilder.create(mImageLoadProvider)
                .size(1.5f,1.5f)
                .provider(0, this, R.mipmap.p5_k)
                .provider(1, this, R.mipmap.p5)
                .title("p4")
                .position(pos_p5)
                .status(0,1)
                .checkedStatus(1,0);
        MDWidgetPlugin plugin_p5 = new MDWidgetPlugin(builder_p5);
        plugins.add(plugin_p5);
        getVRLibrary().addPlugin(plugin_p5);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void vr_pres(String str) {
        Log.i("vrpres","接收到进度:"+str);
        if (str.startsWith("vr")){
            str=str.substring(2,str.length());
            double dd=Double.parseDouble(str);
            Log.i("vrpres","接收到进度1_after:"+dd);
            Log.i("vrpres","接收到进度1_double:"+(double)mMediaPlayerWrapper.getPlayer().getDuration());
            double result =dd*mMediaPlayerWrapper.getPlayer().getDuration();
            Log.i("vrpres","接收到进度1_result:"+result);
            mMediaPlayerWrapper.getPlayer().seekTo((int)result);

        }

    }

    static  long time =0;
    private void myprogress(String tag,long times){
        MDAbsView mdView;
        HoverView hoverView;
        time=times;
        if (!tag.startsWith("nop")) {
           try {
               mdView = getVRLibrary().findViewByTag("hover");
               hoverView = mdView.castAttachedView(HoverView.class);
           }catch (Throwable t){t.printStackTrace();return;}
            if (time>1000*3){
                hoverView.setFoucus(true);
                mdView.invalidate();
            }
        }else {
//            try {
//                mdView = getVRLibrary().findViewByTag("hover");
//                hoverView = mdView.castAttachedView(HoverView.class);
//            }catch (Throwable t){t.printStackTrace();return;}
//                hoverView.setFoucus(false);
//                mdView.invalidate();
        }
    }
    private ValueAnimator animator;
    private void startCameraAnimation(final MDDirectorCamUpdate cameraUpdate, PropertyValuesHolder... values){
        if (animator != null){
            animator.cancel();
        }

        animator = ValueAnimator.ofPropertyValuesHolder(values).setDuration(2000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float near = (float) animation.getAnimatedValue("near");
                float eyeZ = (float) animation.getAnimatedValue("eyeZ");
                float pitch = (float) animation.getAnimatedValue("pitch");
                float yaw = (float) animation.getAnimatedValue("yaw");
                float roll = (float) animation.getAnimatedValue("roll");
                cameraUpdate.setEyeZ(eyeZ).setNearScale(near).setPitch(pitch).setYaw(yaw).setRoll(roll);
            }
        });
        animator.start();
    }

    protected MDVRLibrary createVRLibrary() {
        return MDVRLibrary.with(this)
                .displayMode(MDVRLibrary.DISPLAY_MODE_NORMAL)
                .interactiveMode(MDVRLibrary.INTERACTIVE_MODE_MOTION)
                .asVideo(new MDVRLibrary.IOnSurfaceReadyCallback() {
                    @Override
                    public void onSurfaceReady(Surface surface) {
                        mMediaPlayerWrapper.setSurface(surface);
                    }
                })
                .ifNotSupport(new MDVRLibrary.INotSupportCallback() {
                    @Override
                    public void onNotSupport(int mode) {
                        String tip = mode == MDVRLibrary.INTERACTIVE_MODE_MOTION
                                ? "onNotSupport:MOTION" : "onNotSupport:" + String.valueOf(mode);
                        Toast.makeText(VideoPlayerActivity.this, tip, Toast.LENGTH_SHORT).show();
                    }
                })
                .pinchConfig(new MDPinchConfig().setMin(1.0f).setMax(8.0f).setDefaultValue(0.1f))
                .pinchEnabled(true)
                .directorFactory(new MD360DirectorFactory() {
                    @Override
                    public MD360Director createDirector(int index) {
                        return MD360Director.builder().setPitch(90).build();
                    }
                })
//                .directorFactory(new DirectorFactory())//左右眼不一样
                .projectionFactory(new CustomProjectionFactory())
                .barrelDistortionConfig(new BarrelDistortionConfig().setDefaultEnabled(false).setScale(0.95f))
                .build(findViewById(R.id.gl_view));
    }
    public MDVRLibrary getVRLibrary() {
        return mVRLibrary;
    }
    @Override protected void onResume() {
        super.onResume();
        mVRLibrary.onResume(this);
        mMediaPlayerWrapper.resume();
    }
    @Override protected void onPause() {
        super.onPause();
        mVRLibrary.onPause(this);
        mMediaPlayerWrapper.pause();
    }
    @Override protected void onDestroy() {
        super.onDestroy();//view=null;
        mVRLibrary.onDestroy();
        mMediaPlayerWrapper.pause();
        mMediaPlayerWrapper.destroy();
        EventBus.getDefault().unregister(this);
    }
    @Override public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mVRLibrary.onOrientationChanged(this);
    }
    protected Uri getUri() {
        Intent i = getIntent();
        if (i == null || i.getData() == null){
            return null;
        }
        return i.getData();
    }
    public void cancelBusy(){
        findViewById(R.id.progress).setVisibility(View.GONE);
    }
    public void busy(){
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
    }
    // android impl
    private class AndroidProvider implements MDVRLibrary.IImageLoadProvider {

        Activity activity;

        public AndroidProvider(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onProvideBitmap(Uri uri, MD360BitmapTexture.Callback callback) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(uri));
                callback.texture(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    }
    // picasso impl
    private class ImageLoadProvider implements MDVRLibrary.IImageLoadProvider{
     private SimpleArrayMap<Uri,Target> targetMap = new SimpleArrayMap<>();
        @Override public void onProvideBitmap(final Uri uri, final MD360BitmapTexture.Callback callback) {

            final Target target = new Target() {

                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    // texture
                    callback.texture(bitmap);
                    targetMap.remove(uri);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    targetMap.remove(uri);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };
            targetMap.put(uri, target);
            Picasso.with(getApplicationContext()).load(uri).config(Bitmap.Config.RGB_565).resize(callback.getMaxTextureSize(),callback.getMaxTextureSize()).onlyScaleDown().centerInside().into(target);//.memoryPolicy(NO_CACHE, NO_STORE)  不缓存导致每次都重新加载 显示慢
        }
    }
}
