package com.eriksandsten.homeautomation2.activity.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatSpinner;
import androidx.viewpager2.widget.ViewPager2;
import com.eriksandsten.homeautomation2.R;
import com.eriksandsten.homeautomation2.domain.StandbyState;
import com.eriksandsten.homeautomation2.fragments.BaseFragment;
import com.eriksandsten.homeautomation2.fragments.MainViewPagerAdapter;
import com.eriksandsten.homeautomation2.helper.ASUSMediaClient;
import com.eriksandsten.homeautomation2.helper.DirigeraHelper;
import com.eriksandsten.homeautomation2.helper.HttpHelper;
import com.eriksandsten.homeautomation2.helper.RadxaRockClient;
import com.eriksandsten.homeautomation2.helper.SpinnerData;
import com.eriksandsten.homeautomation2.utils.HomeAutomationUtils;
import com.google.android.material.slider.Slider;
import com.google.android.material.tabs.TabLayout;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import okhttp3.FormBody;

public class MainActivityGUI {
    private ASUSMediaClient asusMediaClient;
    private RadxaRockClient radxaRockClient;
    private Context context;
    private MainActivity mainActivity;
    MainViewPagerAdapter mainViewPagerAdapter;
    TabLayout tabComponent;
    ViewPager2 viewPagerComponent;
    private LinearLayout bottomPanel;
    private ImageButton muteButton;
    private ImageButton standbyButton;
    private ImageButton turnOffApartmentLightsButton;
    private TextView bluetoothSpeakersName;
    private ImageButton bluetoothSpeakersVolumeUpBtn;
    private ImageButton bluetoothSpeakersVolumeDownBtn;

    private AppCompatSpinner tvStandbySpinner;
    private AppCompatSpinner hdmiChannelSpinner;
    private int initialHeight;
    private int initialWidth;
    private float startY;
    private float startX;

    private Handler handler = new Handler();
    private boolean isHolding = false;

    // Runnable that will be triggered after the specified duration
    private Runnable holdRunnable = () -> {
        if (isHolding) {
            Toast.makeText(mainActivity, "Button held for 3 seconds!", Toast.LENGTH_SHORT).show();
        }
    };

    public void createGUI(Context context, MainActivity mainActivity) {
        this.context = context;
        this.mainActivity = mainActivity;
        this.asusMediaClient = new ASUSMediaClient(mainActivity);
        this.radxaRockClient = new RadxaRockClient(mainActivity);

        turnOffApartmentLightsButton = (ImageButton) initComponent(R.id.turnOffApartmentLightsBtn, mainActivity.fetchBitmap("icons/light switch/lightswitch2.png"), null, null);
        standbyButton = (ImageButton) initComponent(R.id.standbyBtn, mainActivity.fetchBitmap("icons/standby/standby1.png"), this::onStandbyButtonClick, null);
        muteButton = (ImageButton) initComponent(R.id.muteBtn, mainActivity.fetchBitmap("icons/mute/mute2.png"), this::onMuteButtonClick, null);
        bottomPanel = (LinearLayout) initComponent(R.id.bottomPanel, null, null, this::onBottomPanelTouch);
        bluetoothSpeakersName = (TextView) mainActivity.findViewById(R.id.bluetoothSpeakersName);
        ImageView bluetoothSpeakersImageView = mainActivity.findViewById(R.id.bluetoothSpeakersIcon);
        bluetoothSpeakersImageView.setImageBitmap(mainActivity.fetchBitmap("icons/speakers/bluetooth-speaker.png"));
        bluetoothSpeakersVolumeUpBtn = (ImageButton) initComponent(R.id.bluetoothSpeakersVolumeUpBtn, mainActivity.fetchBitmap("icons/volume/volume-up-1.png"), this::onSpeakersVolumeUpButtonClick, null);
        bluetoothSpeakersVolumeDownBtn = (ImageButton) initComponent(R.id.bluetoothSpeakersVolumeDownBtn, mainActivity.fetchBitmap("icons/volume/volume-down-1.png"), this::onSpeakersVolumeDownButtonClick, null);

        Slider slider = (Slider) mainActivity.findViewById(R.id.volumeSlider);
        slider.addOnChangeListener(this::onVolumeSliderChange);

        initTabAndViewPagerComponents();
        tabComponent.addOnTabSelectedListener(new OnTabSelectedListener(viewPagerComponent));

        tvStandbySpinner = SpinnerWithData.factory(mainActivity, R.id.tvStandbySpinner, List.of(new SpinnerData<>(mainActivity.getString(R.string.on), Boolean.TRUE), new SpinnerData<>(mainActivity.getString(R.string.off), Boolean.FALSE)),
                this::onTvStandbySpinnerItemSelected);
        hdmiChannelSpinner = SpinnerWithData.factory(mainActivity, R.id.activeHDMIChannelSpinner,
                List.of(new SpinnerData<>("HDMI 1", 1), new SpinnerData<>("HDMI 2", 2), new SpinnerData<>("HDMI 3", 3)), this::onHdmiChannelSpinnerItemSelected);
    }

    void initTabAndViewPagerComponents() {
        mainViewPagerAdapter = new MainViewPagerAdapter(mainActivity);
        tabComponent = mainActivity.findViewById(R.id.tabLayout);
        viewPagerComponent = mainActivity.findViewById(R.id.viewPager);

        tabComponent.addOnTabSelectedListener(new OnTabSelectedListener(viewPagerComponent));
        viewPagerComponent.setAdapter(mainViewPagerAdapter);
        viewPagerComponent.setUserInputEnabled(false); // Disable swiping
        viewPagerComponent.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabComponent.getTabAt(position).select();
            }
        });
    }

    public void onTvStandbySpinnerItemSelected(OnItemSelectedParams params) {
        SpinnerData<Boolean> selectedItem = (SpinnerData<Boolean>) params.parent().getItemAtPosition(params.position());
        Toast.makeText(context, "TV on: " + selectedItem.getData(), Toast.LENGTH_SHORT).show();
        //radxaRockClient.setTVStandbyStateRequest(Boolean.TRUE.equals(selectedItem.getData()) ? StandbyState.ON : StandbyState.OFF);
        /*String response = HttpHelper.performPutRequest(mainActivity.getProperty("radxa_rock_server_url"), "/tv/hdmi-channel",
                "{\"channel\": \"%s\"}".formatted(String.valueOf(selectedItem.getData())), 5000);*/
    }

    public void onHdmiChannelSpinnerItemSelected(OnItemSelectedParams params) {
        SpinnerData<Integer> selectedItem = (SpinnerData<Integer>) params.parent().getItemAtPosition(params.position());
        Toast.makeText(mainActivity, "Selected HDMI channel: " + selectedItem.getData(), Toast.LENGTH_SHORT).show();
        //radxaRockClient.setCurrentHDMIChannelRequest(selectedItem.getData());
        /*String response = HttpHelper.performPutRequest(mainActivity.getProperty("radxa_rock_media_server_url"), "/tv/standby",
                "{\"standby\": \"%s\"}".formatted(String.valueOf(selectedItem.getData())), 5000);*/
    }

    private void onVolumeSliderChange(Slider slider, float value, boolean fromUser) {
        asusMediaClient.setTVChannelVolumeRequest(Float.valueOf(value).intValue());
    }

    public boolean onBottomPanelTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Record the starting point and initial size
                startY = event.getRawY();
                startX = event.getRawX();
                initialHeight = bottomPanel.getHeight();
                initialWidth = bottomPanel.getWidth();
                return true;

            case MotionEvent.ACTION_MOVE:
                // Calculate the distance moved
                float deltaY = event.getRawY() - startY;
                // float deltaX = event.getRawX() - startX;

                // Set new layout size based on movement
                int newHeight = (int) (initialHeight - deltaY);
                // int newWidth = (int) (initialWidth + deltaX);

                // Prevent negative or too small sizes
                if (newHeight > 150) {
                    bottomPanel.getLayoutParams().height = newHeight;
                    //bottomPanel.getLayoutParams().width = newWidth;
                    bottomPanel.requestLayout(); // Apply changes
                }
                return true;
        }
        return false;
    }

    public void setBluetoothSpeakersText(String text) {
        bluetoothSpeakersName.setText(text);
    }

    public void onTurnOffApartmentLightsClick(View view) {
        DirigeraHelper.turnOffApartmentLightsAndPowerOutlets(mainActivity.getWebClient(), Long.parseLong(mainActivity.getProperty("turn_off_apartment_lights_initial_delay_in_ms")));
    }

    public void onSpeakersVolumeUpButtonClick(View view) {
        Toast.makeText(mainActivity, "Volume up!", Toast.LENGTH_SHORT).show();
        mainActivity.getBluetoothSpeakers().volumeUp();
    }

    public void onSpeakersVolumeDownButtonClick(View view) {
        Toast.makeText(mainActivity, "Volume down!", Toast.LENGTH_SHORT).show();
        mainActivity.getBluetoothSpeakers().volumeDown();
    }

    public void onMuteButtonClick(View view) {
        Boolean muted;

        if (mainActivity.getString(R.string.mute).contentEquals(muteButton.getContentDescription())) {
            muted = Boolean.TRUE;
            muteButton.setContentDescription(mainActivity.getString(R.string.unmute));
            muteButton.setImageBitmap(mainActivity.fetchBitmap("icons/unmute/unmute1.png"));
        } else {
            muted = Boolean.FALSE;
            muteButton.setContentDescription(mainActivity.getString(R.string.mute));
            muteButton.setImageBitmap(mainActivity.fetchBitmap("icons/mute/mute2.png"));
        }

        Object hello = radxaRockClient.pingServer();
        Object returnValue = asusMediaClient.setMusicVideoMuteStateRequest(muted);
    }

    public void onStandbyButtonClick(View view) {
        Boolean playing;

        if (mainActivity.getString(R.string.on).contentEquals(standbyButton.getContentDescription())) {
            playing = Boolean.TRUE;
            standbyButton.setContentDescription(mainActivity.getString(R.string.off));
            standbyButton.setImageBitmap(mainActivity.fetchBitmap("icons/standby/standby2.png"));
        } else {
            playing = Boolean.FALSE;
            standbyButton.setContentDescription(mainActivity.getString(R.string.on));
            standbyButton.setImageBitmap(mainActivity.fetchBitmap("icons/standby/standby1.png"));
        }

        Object returnValue = asusMediaClient.setMusicVideoPlayStateRequest(playing);
    }

    protected View initComponent(int componentId, Bitmap graphic, View.OnClickListener onClickCallback, View.OnTouchListener onTouchCallback) {
        View component = mainActivity.findViewById(componentId);

        if (graphic != null && component instanceof ImageButton imageButton) {
            imageButton.setImageBitmap(graphic);
        }
        if (onClickCallback != null) {
            component.setOnClickListener(onClickCallback);
        }
        if (onTouchCallback != null) {
            component.setOnTouchListener(onTouchCallback);
        }

        return component;
    }
}
