package com.example.android_java_arduino_controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_BT_PERMISSION = 2;
    static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    AppCompatButton bluetoothBtn, scanButton;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice bluetoothDevice;
    BluetoothSocket bluetoothSocket;

    OutputStream outputStream;

    ListView deviceListView;
    private boolean isConnected = false;
    Switch seederSwitch, moistureSwitch;
    AppCompatImageButton upButton, downButton, leftButton, stopButton, rightButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothBtn = findViewById(R.id.bluetoothBtn);
        upButton = findViewById(R.id.upButton);
        downButton = findViewById(R.id.downButton);
        rightButton = findViewById(R.id.rightButton);
        leftButton = findViewById(R.id.leftButton);
        stopButton = findViewById(R.id.stopButton);
        scanButton = findViewById(R.id.scanBtn);
        deviceListView = findViewById(R.id.deviceListView);
        seederSwitch = findViewById(R.id.seederSwitch);
        moistureSwitch = findViewById(R.id.moistureSwitch);


        bluetoothBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    connectBluetooth();
                }
            }
        });
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBluetoothDeviceDiscovery();
            }
        });


        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData('U');
            }
        });


        downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData('D');
            }
        });


        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData('L');
            }
        });


        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData('R');
            }
        });


        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData('P');
            }
        });


        moistureSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    sendData('M');

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            moistureSwitch.setChecked(false);
                        }
                    }, 10000);
                }

            }
        });


        seederSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    sendData('S');

                }
                else {
                    // Switch is unchecked
                    // You can add handling for this scenario if needed
                }
            }
        });

    }

    private void connectBluetooth() {

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            showToast("Bluetooth not supported");
            return;
        }

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN}, 100);
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            // Bluetooth is not enabled, show dialog to request permission
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        if (!isConnected){

            bluetoothDevice = bluetoothAdapter.getRemoteDevice("00:22:03:01:01:52"); // Replace with your HC-05 MAC address
            if (bluetoothDevice == null) {

                showToast("Bluetooth device not found");

                return;
            } else System.out.println(bluetoothDevice.getName());

            try {
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(mUUID);
                bluetoothSocket.connect();
                bluetoothBtn.setText(R.string.disconnect);
                scanButton.setVisibility(View.GONE);
                deviceListView.setVisibility(View.GONE);
                isConnected = true;
                outputStream = bluetoothSocket.getOutputStream();
                showToast(bluetoothDevice.getName()+" connected");

            } catch (IOException e) {
                e.printStackTrace();
                showToast("Failed to connect Bluetooth");
            }

        } else {
            try {
                if (bluetoothSocket != null) {
                    bluetoothSocket.close();
                    outputStream = null;
                    showToast("Bluetooth disconnected");
                    bluetoothBtn.setText(R.string.connect_to_hc); // Change button text to "Connect"
                    isConnected = false; // Update connection status
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startBluetoothDeviceDiscovery() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            showToast("Bluetooth not supported");
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            // Check if permission is granted to enable Bluetooth
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                // Permissions not granted, request permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, REQUEST_ENABLE_BT_PERMISSION);
                return;
            }

        }
//        System.out.println(bluetoothAdapter.getBondedDevices());
        ArrayAdapter<String> deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        deviceListView.setAdapter(deviceAdapter);

        // Get bonded devices
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();

        // Populate the adapter with bonded device names
        for (BluetoothDevice device : bondedDevices) {
            deviceAdapter.add(device.getName() + " (" + device.getAddress() + ")");
        }

        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);

                // Extract the device name from the selected item
                String deviceName = selectedItem.split("\\(")[0].trim();

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN}, 100);
                    return;
                }

                // Iterate through bonded devices to find the device with matching name
                for (BluetoothDevice device : bondedDevices) {
                    if (device.getName() != null && device.getName().equals(deviceName)) {
                        // Check if the device name matches the Bluetooth module name
                        if (device.getName().equals("HC-05")) {
                            // Connect to the Bluetooth module
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                connectBluetooth();
                            }
                        } else {
                            // Handle the case where the selected device is not the Bluetooth module
                            showToast("Selected device is not HC-05");
                        }
                        return;
                    }
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ENABLE_BT_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, check if Bluetooth is enabled
                if (!bluetoothAdapter.isEnabled()) {
                    // Check if permission is granted to enable Bluetooth
                    if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                        // Permissions not granted, request permission
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, REQUEST_ENABLE_BT_PERMISSION);
                        return;
                    }

                    // Permissions granted, show dialog to request enabling Bluetooth
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else {
                    // Bluetooth is already enabled, proceed with discovery
                    startBluetoothDeviceDiscovery();
                }
            } else {
                // Permission denied, handle accordingly
                showToast("Permission denied to enable Bluetooth");
            }
        }
    }

    private void sendData(char data) {
        if (outputStream != null) {
            try {
                outputStream.write(data);
                showToast("Data sent: " + data);
            } catch (IOException e) {
                e.printStackTrace();
                showToast("Failed to send data");
            }
        } else {
            showToast("Bluetooth not connected");
        }
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


}