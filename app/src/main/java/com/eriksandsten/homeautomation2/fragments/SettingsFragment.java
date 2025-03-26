package com.eriksandsten.homeautomation2.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.eriksandsten.homeautomation2.R;
import com.eriksandsten.homeautomation2.activity.SettingsActivity;
import com.eriksandsten.homeautomation2.helper.HttpHelper;
import org.json.JSONException;
import org.json.JSONObject;

public class SettingsFragment extends Fragment {
    private SettingsActivity settingsActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate custom layout
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        settingsActivity = (SettingsActivity) getActivity();

        // Initialize views
        TextView txtRESTResponse = view.findViewById(R.id.txtRESTResponse);
        Button buttonSave = view.findViewById(R.id.buttonSave);
        Button buttonReset = view.findViewById(R.id.buttonReset);

        txtRESTResponse.setText(HttpHelper.latestRESTCallResponse);

        Button pingAsusServerButton = view.findViewById(R.id.buttonPingASUSServer);
        Button pingRadxaRockServerButton = view.findViewById(R.id.buttonPingRadxaRockServer);
        Button showHDMICECTopology = view.findViewById(R.id.buttonShowHDMICECTopology);
        pingAsusServerButton.setText(getString(R.string.ping, getString(R.string.asus_server)));
        pingRadxaRockServerButton.setText(getString(R.string.ping, getString(R.string.radxa_rock_server)));

        pingAsusServerButton.setOnClickListener(v -> {
            final String response = HttpHelper.performGetRequest(settingsActivity.getProperty("asus_media_server_url"), "/", 1000);
            txtRESTResponse.setText(response);
        });
        pingRadxaRockServerButton.setOnClickListener(v -> {
            final String response = HttpHelper.performGetRequest(settingsActivity.getProperty("radxa_rock_server_url"), "/", 1000);
            txtRESTResponse.setText(response);
        });
        showHDMICECTopology.setOnClickListener(v -> {
            final String response = HttpHelper.performGetRequest(settingsActivity.getProperty("radxa_rock_server_url"), "/tv/topology", 10000);
            try {
                JSONObject jsonObject = new JSONObject(response);
                txtRESTResponse.setText(jsonObject.toString(4));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        // Set click listeners
        buttonSave.setOnClickListener(v -> {
            // Handle save action
            txtRESTResponse.setText("Settings Saved!");
        });

        buttonReset.setOnClickListener(v -> {
            // Handle reset action
            txtRESTResponse.setText("Settings Reset!");
        });
    }
}