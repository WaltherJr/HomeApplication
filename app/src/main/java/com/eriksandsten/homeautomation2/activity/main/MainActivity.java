package com.eriksandsten.homeautomation2.activity.main;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.eriksandsten.homeautomation2.R;
import com.eriksandsten.homeautomation2.activity.SettingsActivity;
import com.eriksandsten.homeautomation2.adapter.WrappedDeviceList;
import com.eriksandsten.homeautomation2.domain.Room;
import com.eriksandsten.homeautomation2.domain.dirigera.Device;
import com.eriksandsten.homeautomation2.domain.dirigera.DevicePatchRequest;
import com.eriksandsten.homeautomation2.domain.dirigera.ListDevicesRequest;
import com.eriksandsten.homeautomation2.utils.BluetoothSpeaker;
import com.eriksandsten.homeautomation2.helper.DirigeraHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import reactor.netty.http.client.HttpClient;

public class MainActivity extends BaseActivity {
    private SslContext sslContext;
    @Getter
    private GoogleAccountCredential googleAccountCredentials;
    @Getter
    private String mainGoogleAccountName;
    @Getter
    private WebClient webClient;
    @Getter
    public WrappedDeviceList wrappedDeviceList = new WrappedDeviceList();
    @Getter
    public Map<String, List<Device>> devicesByRoom;
    @Getter
    public List<Room> roomsWithDevices;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private BluetoothSpeaker speakers;
    private MainActivityGUI mainActivityGUI = new MainActivityGUI();

    private void initWebClientAndSSLContext() {
        try {
            final String dirigeraApiUrl = getProperty("dirigera_server_url");
            sslContext = SslContextBuilder
                    .forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
            webClient = WebClient.builder().baseUrl(dirigeraApiUrl)
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
        final Device[] turnOffPowerOutletRequest = DirigeraHelper.patchFieldRequest(attributes -> attributes.isOn(Boolean.FALSE));
        new DevicePatchRequest(getProperty("media_server_power_outlet_guid"), turnOffPowerOutletRequest).execute(webClient);
    }

    public boolean toggleIsOn(String deviceId) {
        final boolean currentState = wrappedDeviceList.getDeviceById(deviceId).getAttributes().getIsOn();
        final boolean newState = !currentState;
        final Device[] turnOffPowerOutletRequest = DirigeraHelper.patchFieldRequest(attributes -> attributes.isOn(newState));
        new DevicePatchRequest(deviceId, turnOffPowerOutletRequest).execute(webClient);
        return newState;
    }

    public void setLightLevel(String deviceId, String lightLevel) {
        final Device[] setLightLevelRequest = DirigeraHelper.patchFieldRequest(attributes -> attributes.lightLevel(Integer.parseInt(lightLevel)));
        new DevicePatchRequest(deviceId, setLightLevelRequest).execute(webClient);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWebClientAndSSLContext();
        wrappedDeviceList.setDevices(new ListDevicesRequest().execute(webClient));
        initDeviceListUpdater();
        devicesByRoom = wrappedDeviceList.getDevices().stream().collect(Collectors.groupingBy(device -> Optional.ofNullable(device.getRoom()).map(com.eriksandsten.homeautomation2.domain.dirigera.Room::getName).orElse("")));
        roomsWithDevices = devicesByRoom.entrySet().stream().map(entry -> new Room(entry.getKey(), Room.mapRoomNameToRoomType(entry.getKey()), entry.getValue())).collect(Collectors.toList());
        speakers = new BluetoothSpeaker(this);
        mainActivityGUI.createGUI(getBaseContext(), this);
        loadGoogleAccountCredentials(getApplicationContext());
    }

    private void initDeviceListUpdater() {
        Long updateInterval = Long.parseLong(getProperty("fetch_all_devices_interval_in_ms"));
        scheduler.scheduleWithFixedDelay(this::updateDeviceList, 0, updateInterval, TimeUnit.MILLISECONDS);
    }

    private void loadGoogleAccountCredentials(Context context) {
        googleAccountCredentials = GoogleAccountCredential.usingOAuth2(context, List.of(CalendarScopes.CALENDAR)).setBackOff(new ExponentialBackOff());
        mainGoogleAccountName = getProperty("main_google_account_name");
        Account[] accounts = googleAccountCredentials.getAllAccounts();
        /*Account mainGoogleAccount = Arrays.stream(accounts)
                .filter(account -> account.name.equals(mainGoogleAccountName)).findFirst()
                .orElseThrow(() -> new NoSuchElementException("No Google account with name \"%s\" present".formatted(mainGoogleAccountName)));
        googleAccountCredentials.setSelectedAccountName(mainGoogleAccount.name);*/
    }

    private void updateDeviceList() {
        var devices = new ListDevicesRequest().execute(webClient);
        wrappedDeviceList.setDevices(devices);
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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
