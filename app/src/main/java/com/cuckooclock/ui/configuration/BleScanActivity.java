package com.cuckooclock.ui.configuration;

import static android.view.View.VISIBLE;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cuckooclock.R;
import com.cuckooclock.databinding.ActivityBleScanBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BleScanActivity extends AppCompatActivity {

    private Context mContext;
    private ActivityBleScanBinding mBleScanBinding;

    // Bluetooth scanning
    private static final int REQUEST_CODE_BLUETOOTH_PERMISSIONS = 100;
    private Map<String, ScanResult> mDeviceMap;
    private List<ScanResult> mBleList;
    private ScanCallback mBleScanCallback;
    private ExecutorService mThreadPool;
    private Future<Boolean> mUpdateFuture;
    private BleAdapter mBleAdapter;
    private boolean mbPermissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        // Tell the user he hasn't configured his cuckoo yet
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setMessage(R.string.cuckoo_not_yet_configured_msg);
        alertDialogBuilder.setTitle(R.string.cuckoo_not_yet_configured_title);
        // This is a mandatory step
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setNegativeButton(
                "Go back...",
                (DialogInterface.OnClickListener) (dialog, which) -> {
                    finish();
                }
        );
        alertDialogBuilder.setPositiveButton(
                "Let's go, chap!",
                (DialogInterface.OnClickListener) (dialog, which) -> {
                    Toast bluetoothDiasbledToast = Toast.makeText(
                            mContext,
                            R.string.bluetooth_disabled,
                            Toast.LENGTH_SHORT
                    );

                    while(!BluetoothAdapter.getDefaultAdapter().isEnabled()) {

                        SystemClock.sleep(2000);

                    }
                }
        );
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ble_scan);

        mThreadPool = Executors.newSingleThreadExecutor();

        mBleList = new LinkedList<>();
        mBleAdapter = new BleAdapter();
        // TODO mBleScanBinding.contentBleScanRef..setAdapter(mBleAdapter);

        mDeviceMap = new HashMap<>();
        mBleScanCallback = new ScanCallback();

        mBleScanBinding = ActivityBleScanBinding.inflate(getLayoutInflater());
        mBleScanBinding.contentBleScanRef.refreshBleDevicesLayout.setColorSchemeResources(R.color.purple_700);
        mBleScanBinding.contentBleScanRef.refreshBleDevicesLayout.setOnRefreshListener(this::scanBleDevices);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        scanBleDevices();
    }

    private void onIntervalScanUpdate(boolean over) {

        List<ScanResult> devices = new ArrayList<>(mDeviceMap.values());
        devices.sort((dev1, dev2) -> {
            Integer rssi1 = dev1.getRssi();
            Integer rssi2 = dev2.getRssi();
            return rssi2.compareTo(rssi1);
        });

        runOnUiThread( ()-> {
            mBleList.clear();
            mBleList.addAll(devices);
            mBleAdapter.notifyDataSetChanged();

            if (over) {
                mBleScanBinding.contentBleScanRef.refreshBleDevicesLayout.setRefreshing(false);
            }
        });
    }

    private boolean checkBluetoothPermission() {
        if ((ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) !=
                PackageManager.PERMISSION_GRANTED) || !mbPermissionGranted) {
            // Allow the usage of bluetooth
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_BLUETOOTH_PERMISSIONS);
        }
        return mbPermissionGranted;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed
                mbPermissionGranted = true;
            } else {
                Toast.makeText(
                        mContext,
                        "Permission is not granted, cannot use Bluetooth.",
                        Toast.LENGTH_SHORT
                ).show();
                mbPermissionGranted = false;
            }
        }
    }

    private void scanBleDevices() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
        if (!bluetoothAdapter.isEnabled() || scanner == null) {
            Toast.makeText(mContext, R.string.bluetooth_disabled, Toast.LENGTH_SHORT).show();
            return;
        }

        // Clear BLE data
        mDeviceMap.clear();
        mBleList.clear();

        if (checkBluetoothPermission()) {

            mBleScanBinding.contentBleScanRef.devicesCardView.setVisibility(VISIBLE);
            // Start scanning BLE devices
            scanner.startScan(
                    null,
                    new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(),
                    mBleScanCallback
            );
        }

        mUpdateFuture = mThreadPool.submit(() -> {
            BluetoothLeScanner inScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
            if (inScanner != null) {
                inScanner.stopScan(mBleScanCallback);
            }
            onIntervalScanUpdate(true);
            return true;
        });
    }

    private class ScanCallback extends android.bluetooth.le.ScanCallback {

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                onBleScan(result);
            }
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            onBleScan(result);
        }

        private void onBleScan(ScanResult scanResult) {

//  TODO          String name = scanResult.getDevice().getName();
//            if (!TextUtils.isEmpty(mBlufiFilter)) {
//                if (name == null || !name.startsWith(mBlufiFilter)) {
//                    return;
//                }
//            }

            mDeviceMap.put(scanResult.getDevice().getAddress(), scanResult);
        }
    }

    private class BleHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ScanResult scanResult;
        ActivityBleScanBinding mBinding;

        BleHolder(ActivityBleScanBinding binding) {
            super(binding.getRoot());

            this.mBinding = binding;
            binding.contentBleScanRef.refreshBleDevicesLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View currentView) {
            Snackbar.make(currentView, "Refreshing!", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null)
                    .setAnchorView(R.id.button_bird).show();
        }
    }

    private class BleAdapter extends RecyclerView.Adapter<BleHolder> {

        @NonNull
        @Override
        public BleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ActivityBleScanBinding binding = ActivityBleScanBinding.inflate(getLayoutInflater(), parent, false);
            return new BleHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull BleHolder holder, int position) {
            if( checkBluetoothPermission()){
                ScanResult scanResult = mBleList.get(position);
                holder.scanResult = scanResult;

                BluetoothDevice device = scanResult.getDevice();

                String name = device.getName() == null ? getString(R.string.unknown) : device.getName();
                holder.mBinding.contentBleScanRef.textDeviceName.setText(name);

                SpannableStringBuilder info = new SpannableStringBuilder();
                info.append("Mac:").append(device.getAddress())
                        .append(" RSSI:").append(String.valueOf(scanResult.getRssi()));
                info.setSpan(new ForegroundColorSpan(0xFF9E9E9E), 0, 21, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                info.setSpan(new ForegroundColorSpan(0xFF8D6E63), 21, info.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                holder.mBinding.contentBleScanRef.textDeviceInfo.setText(info);
            }
        }

        @Override
        public int getItemCount() {
            return mBleList.size();
        }
    }

}