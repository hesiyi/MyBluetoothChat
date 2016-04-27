package com.example.mybluetoothchat;

import android.bluetooth.BluetoothAdapter;

import java.util.Set;

public class BluetoothDevice {
	// BluetoothAdapter actions
	public final String Action_Bluetooth_On = BluetoothAdapter.ACTION_REQUEST_ENABLE;
	public final String Action_Bluetooth_Discoverable = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;
	public final String Action_Bluetooth_Discovery_Finish=BluetoothAdapter.ACTION_DISCOVERY_FINISHED;
	// Bluetooth scan mode
	public final int Mode_Connectable_Discoverable = BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE;
	// BluetoothAdapter data
	public final String Data_Dicoverable=BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;

	public BluetoothDevice() {
		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	public boolean isPhoneDevice_Support() {
		if (mBluetoothAdapter == null)
			return false;
		return true;
	}

	public boolean isBluetoothAdapter_Enable()
	{
		return mBluetoothAdapter.isEnabled();
	}
	public boolean isBluetooth_discovering() {
		return mBluetoothAdapter.isDiscovering();
	}
	public int getBluetoothScanMode() {
		return mBluetoothAdapter.getScanMode();
	}
	public Set<android.bluetooth.BluetoothDevice> getPairedDevices() {
		return mBluetoothAdapter.getBondedDevices();
	}
	public BluetoothAdapter getBluetoothAdapter()
	{
		return mBluetoothAdapter;
	}
	public boolean discoveryBluetooth_Start() {
		return mBluetoothAdapter.startDiscovery();
	}
	public void discoveryBluetooth_Cancel() {
		mBluetoothAdapter.cancelDiscovery();
	}
	public android.bluetooth.BluetoothDevice getRomteBluetoothDevice(String address) {
		return mBluetoothAdapter.getRemoteDevice(address);
	}
}
