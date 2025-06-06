package com.cuckooclock.ui.configuration;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cuckooclock.R;
import com.cuckooclock.databinding.FragmentBleScanBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BleScanFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BleScanFragment extends Fragment {

    private Context mContext;
    private FragmentBleScanBinding mFragmentBleScanBinding;

    // Bluetooth scanning
    private static final int REQUEST_CODE_BLUETOOTH_PERMISSIONS = 100;
    private Map<String, ScanResult> mDeviceMap;
    private List<ScanResult> mBleList;
    private ScanCallback mBleScanCallback;
    private ExecutorService mThreadPool;
    private Future<Boolean> mUpdateFuture;
    private BleAdapter mBleAdapter;
    private boolean mbPermissionGranted;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public BleScanFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BleScanFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BleScanFragment newInstance(String param1, String param2) {
        BleScanFragment fragment = new BleScanFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mContext = requireContext();
        mThreadPool = Executors.newSingleThreadExecutor();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mFragmentBleScanBinding = FragmentBleScanBinding.inflate(inflater, container, false);
        return inflater.inflate(R.layout.fragment_ble_scan, container, false);
    }

    private void onIntervalScanUpdate(boolean over) {

        List<ScanResult> devices = new ArrayList<>(mDeviceMap.values());
        devices.sort((dev1, dev2) -> {
            Integer rssi1 = dev1.getRssi();
            Integer rssi2 = dev2.getRssi();
            return rssi2.compareTo(rssi1);
        });

        requireActivity().runOnUiThread( ()-> {
            mBleList.clear();
            mBleList.addAll(devices);
            mBleAdapter.notifyDataSetChanged();

            if (over) {
                mFragmentBleScanBinding.refreshBleDevices.setRefreshing(false);
            }
        });
    }

    private boolean checkBluetoothPermission() {
        if ((ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) !=
                PackageManager.PERMISSION_GRANTED) || !mbPermissionGranted) {
            // Allow the usage of bluetooth
            ActivityCompat.requestPermissions(requireActivity(),
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

    private void scanBlufiDevices() {
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
        FragmentBleScanBinding mBinding;

        BleHolder(FragmentBleScanBinding binding) {
            super(binding.getRoot());

            this.mBinding = binding;
            binding.refreshBleDevices.setOnClickListener(this);
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
            FragmentBleScanBinding binding = FragmentBleScanBinding.inflate(getLayoutInflater(), parent, false);
            return new BleHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull BleHolder holder, int position) {
            if( checkBluetoothPermission()){
                ScanResult scanResult = mBleList.get(position);
                holder.scanResult = scanResult;

                BluetoothDevice device = scanResult.getDevice();

                String name = device.getName() == null ? getString(R.string.unknown) : device.getName();
                holder.mBinding.textDeviceName.setText(name);

                SpannableStringBuilder info = new SpannableStringBuilder();
                info.append("Mac:").append(device.getAddress())
                        .append(" RSSI:").append(String.valueOf(scanResult.getRssi()));
                info.setSpan(new ForegroundColorSpan(0xFF9E9E9E), 0, 21, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                info.setSpan(new ForegroundColorSpan(0xFF8D6E63), 21, info.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                holder.mBinding.textDeviceInfo.setText(info);
                }
        }

        @Override
        public int getItemCount() {
            return mBleList.size();
        }
    }


}