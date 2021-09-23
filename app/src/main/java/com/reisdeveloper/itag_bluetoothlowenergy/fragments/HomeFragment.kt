package com.reisdeveloper.itag_bluetoothlowenergy.fragments

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.reisdeveloper.itag_bluetoothlowenergy.R
import com.reisdeveloper.itag_bluetoothlowenergy.adapters.ScanDevicesAdapter
import com.reisdeveloper.itag_bluetoothlowenergy.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private var binding: FragmentHomeBinding? = null

    private val permissions = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val scanDevicesAdapter = ScanDevicesAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initBleManager()

        binding?.fab?.setOnClickListener {
            checkPermissions()
        }

        setupResultSearchDevicesList()
    }

    private fun initBleManager() {
        BleManager.getInstance().init(requireActivity().application)
        BleManager.getInstance()
            .enableLog(true)
            .setReConnectCount(1, 5000)
            .setSplitWriteNum(20)
            .setConnectOverTime(10000).operateTimeout = 5000
    }

    private fun setupResultSearchDevicesList() {
        with(binding?.rvDeviceList) {
            this?.setHasFixedSize(true)
            this?.itemAnimator = null
            this?.adapter = scanDevicesAdapter
            this?.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        }

        scanDevicesAdapter.setOnDeviceClickListener(
            object : ScanDevicesAdapter.OnDeviceClickListener {
                override fun onConnect(bleDevice: BleDevice) {
                    bleConnect(bleDevice)
                }

                override fun onDisconnect(bleDevice: BleDevice) {
                    bleDisconnect(bleDevice)
                }

                override fun onDetail(bleDevice: BleDevice) {
                    val bundle = Bundle()
                    bundle.putParcelable(BLE_DEVICE, bleDevice)
                    findNavController().navigate(
                        R.id.action_FirstFragment_to_SecondFragment,
                        bundle
                    )
                }
            }
        )
    }

    private fun checkPermissions() {
        if (allPermissionsGranted()) {
            onPermissionGranted()
        } else {
            requestPermissions(permissions, REQUEST_CODE_PERMISSIONS)
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS)
            if (allPermissionsGranted())
                onPermissionGranted()
            else
                onPermissionDenied()
    }

    private fun onPermissionGranted() {
        gpsNeeded()
    }

    private fun onPermissionDenied() {
        Toast.makeText(
            requireContext(),
            "Permissions needed to scan bluetooth devices",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun allPermissionsGranted() = permissions.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun bleScan() {
        BleManager.getInstance().scan(object : BleScanCallback() {
            override fun onScanStarted(success: Boolean) {
                binding?.homePvSerchDevices?.visibility = View.VISIBLE
                scanDevicesAdapter.clearScanResults()
            }
            override fun onScanning(bleDevice: BleDevice) {
                scanDevicesAdapter.addDevice(bleDevice)
            }
            override fun onScanFinished(scanResultList: List<BleDevice>) {
                binding?.homePvSerchDevices?.visibility = View.GONE
            }
        })
    }

    private fun bleConnect(bleDevice: BleDevice) {
        BleManager.getInstance().connect(bleDevice, object : BleGattCallback() {
            override fun onStartConnect() {}
            override fun onConnectFail(bleDevice: BleDevice, exception: BleException) {
                Toast.makeText(requireContext(), "Connection failed", Toast.LENGTH_SHORT).show()
            }
            override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {
                scanDevicesAdapter.addDevice(bleDevice)
                binding?.homePvSerchDevices?.visibility = View.GONE
            }
            override fun onDisConnected(
                isActiveDisConnected: Boolean,
                bleDevice: BleDevice,
                gatt: BluetoothGatt,
                status: Int
            ) {
                scanDevicesAdapter.addDevice(bleDevice)
            }
        })
    }

    private fun bleDisconnect(bleDevice: BleDevice) {
        BleManager.getInstance().disconnect(bleDevice)
    }

    private fun gpsNeeded(){
        if(checkGPSIsOpen())
            bleScan()
        else{
            val alertDialog: AlertDialog? = activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setTitle(R.string.gps_must_be_enable)
                    setMessage(R.string.please_activate_your_gps)
                    setPositiveButton(R.string.active_gps) {dialog,_ ->
                        actvateGps()
                        dialog.dismiss()
                    }
                    setNegativeButton(R.string.not_now) {dialog,_ ->
                        dialog.dismiss()
                    }
                }
                builder.create()
            }
            alertDialog?.show()
        }
    }

    private fun checkGPSIsOpen(): Boolean {
        val locationManager =
            context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
                ?: return false
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun actvateGps(){
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    companion object {
        const val REQUEST_CODE_PERMISSIONS = 9999
        const val BLE_DEVICE = "bleDevice"
    }

}