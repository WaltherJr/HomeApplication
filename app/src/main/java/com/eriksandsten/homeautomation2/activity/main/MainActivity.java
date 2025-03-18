package com.eriksandsten.homeautomation2.activity.main;

import androidx.viewpager2.widget.ViewPager2;
import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import com.eriksandsten.homeautomation2.R;
import com.eriksandsten.homeautomation2.activity.SettingsActivity;
import com.eriksandsten.homeautomation2.adapter.WrappedDeviceList;
import com.eriksandsten.homeautomation2.domain.Rum;
import com.eriksandsten.homeautomation2.domain.dirigera.Device;
import com.eriksandsten.homeautomation2.domain.dirigera.DevicePatchRequest;
import com.eriksandsten.homeautomation2.domain.dirigera.FetchDevicesRequest;
import com.eriksandsten.homeautomation2.domain.dirigera.Room;
import com.eriksandsten.homeautomation2.helper.CustomSpinnerAdapter;
import com.eriksandsten.homeautomation2.helper.SpinnerData;
import com.eriksandsten.homeautomation2.utils.BluetoothSpeaker;
import com.eriksandsten.homeautomation2.helper.OnTVHelper;
import com.eriksandsten.homeautomation2.fragments.MainViewPagerAdapter;
import com.eriksandsten.homeautomation2.helper.DirigeraHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.tabs.TabLayout;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.net.ssl.SSLException;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.Getter;
import okhttp3.FormBody;
import reactor.netty.http.client.HttpClient;

public class MainActivity extends BaseActivity {
    TabLayout tabLayout;
    ViewPager2 viewPager;
    MainViewPagerAdapter mainViewPagerAdapter;
    @Getter
    private GoogleAccountCredential googleAccountCredentials;
    @Getter
    private String mainGoogleAccountName;
    private SslContext sslContext;
    private WebClient webClient;
    public WrappedDeviceList wrappedDeviceList = new WrappedDeviceList();
    public Map<String, List<Device>> devicesByRoom;
    public List<Rum> roomsWithDevices;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private BluetoothSpeaker speakers;
    private LinearLayout bottomPanel;
    private ImageButton muteButton;
    private ImageButton standbyButton;
    private ImageButton turnOffApartmentLightsButton;
    private int initialHeight;
    private int initialWidth;
    private float startY;
    private float startX;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private void initWebClientAndSSLContext() {
        try {
            sslContext = SslContextBuilder
                    .forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
            webClient = WebClient.builder().baseUrl(getProperty("dirigera_server_url"))
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(getProperty("dirigera_api_token")))
                    .clientConnector(new ReactorClientHttpConnector(
                            HttpClient.create().responseTimeout(Duration.ofSeconds(10)).secure(sslSpec -> sslSpec.sslContext(sslContext))
                    )).build();
        } catch (final SSLException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    public void setMediaServerOnStatus(boolean status) {
        final Device[] turnOffPowerOutletRequest = DirigeraHelper.patchIsOnFieldRequest(Boolean.FALSE);
        DevicePatchRequest.execute(webClient, getProperty("media_server_power_outlet_guid"), turnOffPowerOutletRequest);
    }

    public boolean toggleIsOn(String deviceId) {
        final boolean currentState = wrappedDeviceList.getDeviceById(deviceId).getAttributes().getIsOn();
        final boolean newState = !currentState;
        final Device[] turnOffPowerOutletRequest = DirigeraHelper.patchIsOnFieldRequest(newState);
        DevicePatchRequest.execute(webClient, deviceId, turnOffPowerOutletRequest);
        return newState;
    }

    public void setLightLevel(String deviceId, String lightLevel) {
        final Device[] setLightLevelRequest = DirigeraHelper.patchLightLevelRequest(Short.parseShort(lightLevel));
        DevicePatchRequest.execute(webClient, deviceId, setLightLevelRequest);
    }

    private Spinner createHDMIChannelSpinner() {
        Spinner spinner = findViewById(R.id.activeHDMIChannelSpinner);
        List<SpinnerData<Integer>> itemList = List.of(new SpinnerData<>("HDMI 1", 1), new SpinnerData<>("HDMI 2", 2), new SpinnerData<>("HDMI 3", 3));
        CustomSpinnerAdapter<Integer> adapter = new CustomSpinnerAdapter<>(this, itemList);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerData<Integer> selectedItem = (SpinnerData<Integer>) parent.getItemAtPosition(position);
                String response = OnTVHelper.performPutRequest(getProperty("radxa_rock_media_server_url"), "/tv/hdmi-channel",
                        new FormBody.Builder().add("channel", String.valueOf(selectedItem.getData())).build(), 5000);
                Toast.makeText(MainActivity.this, "Selected HDMI channel: " + selectedItem.getData(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return spinner;
    }

    private Spinner createTVStandbySpinner() {
        Spinner spinner = findViewById(R.id.tvStandbySpinner);
        List<SpinnerData<Boolean>> itemList = List.of(new SpinnerData<>(getString(R.string.on), Boolean.FALSE), new SpinnerData<>(getString(R.string.off), Boolean.TRUE));
        CustomSpinnerAdapter<Boolean> adapter = new CustomSpinnerAdapter<>(this, itemList);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerData<Boolean> selectedItem = (SpinnerData<Boolean>) parent.getItemAtPosition(position);
                String response = OnTVHelper.performPutRequest(getProperty("radxa_rock_media_server_url"), "/tv/standby",
                        new FormBody.Builder().add("standby", String.valueOf(selectedItem.getData())).build(), 5000);
                Toast.makeText(MainActivity.this, "On/off: " + selectedItem.getData(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return spinner;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        mainViewPagerAdapter = new MainViewPagerAdapter(this);
        viewPager.setAdapter(mainViewPagerAdapter);
        viewPager.setUserInputEnabled(false); // Disable swiping
        bottomPanel = findViewById(R.id.bottomPanel);
        Spinner hdmiChannelSpinner = createHDMIChannelSpinner();
        Spinner onOffSpinner = createTVStandbySpinner();
        muteButton = findViewById(R.id.muteBtn);
        standbyButton = findViewById(R.id.standbyBtn);
        turnOffApartmentLightsButton = findViewById(R.id.turnOffApartmentLightsBtn);
        muteButton.setImageBitmap(fetchDrawable("icons/mute/mute2.png"));
        standbyButton.setImageBitmap(fetchDrawable("icons/standby/standby1.png"));
        turnOffApartmentLightsButton.setImageBitmap(fetchDrawable("icons/light switch/lightswitch2.png"));
        speakers = new BluetoothSpeaker(this);
        initWebClientAndSSLContext();
        wrappedDeviceList.setDevices(FetchDevicesRequest.execute(webClient));
        scheduler.scheduleWithFixedDelay(this::updateDeviceList, 0, Long.parseLong(getProperty("fetch_all_devices_interval_in_ms")), TimeUnit.MILLISECONDS);
        devicesByRoom = wrappedDeviceList.getDevices().stream().collect(Collectors.groupingBy(device -> Optional.ofNullable(device.getRoom()).map(Room::getName).orElse("")));
        roomsWithDevices = devicesByRoom.entrySet().stream().map(entry -> new Rum(entry.getKey(), Rum.mapRoomNameToRoomType(entry.getKey()), entry.getValue())).collect(Collectors.toList());

        loadGoogleAccountCredentials(getBaseContext());
        turnOffApartmentLightsButton.setOnClickListener(this::onTurnOffApartmentLightsAndPowerOutletsButtonClick);
        muteButton.setOnClickListener(this::onMuteButtonClick);
        standbyButton.setOnClickListener(this::onStandbyButtonClick);

        tabLayout.addOnTabSelectedListener(new OnTabSelectedListener(viewPager));
        bottomPanel.setOnTouchListener(this::bottomPanelTouchEvent);
        muteButton.setOnTouchListener(this::onMuteButtonTouchEvent);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select();
            }
        });
    }

    private void loadGoogleAccountCredentials(Context context) {
        googleAccountCredentials = GoogleAccountCredential.usingOAuth2(context, List.of(CalendarScopes.CALENDAR)).setBackOff(new ExponentialBackOff());
        mainGoogleAccountName = getProperty("main_google_account_name");
        Account mainGoogleAccount = Arrays.stream(googleAccountCredentials.getAllAccounts())
                .filter(account -> account.name.equals(mainGoogleAccountName)).findFirst()
                .orElseThrow(() -> new NoSuchElementException("No Google account with name \"%s\" present".formatted(mainGoogleAccountName)));
        googleAccountCredentials.setSelectedAccountName(mainGoogleAccount.name);
    }

    private void updateDeviceList() {
        var devices = FetchDevicesRequest.execute(webClient);
        wrappedDeviceList.setDevices(devices);
    }

    public void onTurnOffApartmentLightsAndPowerOutletsButtonClick(View view) {
        DirigeraHelper.turnOffApartmentLightsAndPowerOutlets(webClient, Long.parseLong(getProperty("turn_off_apartment_lights_initial_delay_in_ms")));
    }

    public void onStandbyButtonClick(View view) {
        if (getString(R.string.on).contentEquals(standbyButton.getContentDescription())) {
            standbyButton.setContentDescription(getString(R.string.off));
        } else {
            standbyButton.setContentDescription(getString(R.string.on));
        }

        new Thread(() -> {
            var a = OnTVHelper.performPutRequest(getProperty("asus_media_server_url"),
                    "/tv/standbystate", new FormBody.Builder()
                            .add("standby", "true")
                            .build(), 2000);

            System.out.println(a);
        }).start();
    }

    public void onMuteButtonClick(View view) {
        boolean muteTV = false;

        if (getString(R.string.mute).contentEquals(muteButton.getContentDescription())) {
            muteButton.setContentDescription(getString(R.string.unmute));
            muteButton.setImageBitmap(fetchDrawable("icons/unmute/unmute1.png"));
            muteTV = true;
        } else {
            muteButton.setContentDescription(getString(R.string.mute));
            muteButton.setImageBitmap(fetchDrawable("icons/mute/mute2.png"));
        }

        new Thread(() -> {
            var a = OnTVHelper.performPutRequest(getProperty("asus_media_server_url"),
                    "/tv/mutestate", new FormBody.Builder()
                            .add("muted", "false")
                            .build(), 2000);

            System.out.println(a);
        }).start();
    }

    public boolean bottomPanelTouchEvent(View view, MotionEvent event) {
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

    private Handler handler = new Handler();
    private boolean isHolding = false;

    // Runnable that will be triggered after the specified duration
    private Runnable holdRunnable = () -> {
        if (isHolding) {
            Toast.makeText(MainActivity.this, "Button held for 3 seconds!", Toast.LENGTH_SHORT).show();
        }
    };
    public boolean onMuteButtonTouchEvent(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isHolding = true;
                    handler.postDelayed(holdRunnable, 1000);
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isHolding = false;
                    handler.removeCallbacks(holdRunnable);
                    return true;
            }
            return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Settings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void Settings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
