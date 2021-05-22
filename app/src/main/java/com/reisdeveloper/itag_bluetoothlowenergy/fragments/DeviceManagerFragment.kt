package com.reisdeveloper.itag_bluetoothlowenergy.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleIndicateCallback
import com.clj.fastble.callback.BleRssiCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.utils.HexUtil
import com.reisdeveloper.itag_bluetoothlowenergy.databinding.FragmentDeviceManagerBinding
import java.util.*

class DeviceManagerFragment : Fragment() {

    private var _binding: FragmentDeviceManagerBinding? = null

    private val binding get() = _binding!!

    private val writeServiceUUID: UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb")
    private val writeCharacteristicUUID: UUID = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb")
    private val indicateServiceUUID: UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
    private val indicateCharacteristicUUID: UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")

    private val bleDevice: BleDevice? by lazy {
        arguments?.getParcelable(HomeFragment.BLE_DEVICE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDeviceManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btAlert.setOnClickListener {
            bleWrite()
        }

        bleDevice?.let {
            iTagRssi()
            bleIndicate()
        }
    }

    private fun bleRssi() {
        BleManager.getInstance().readRssi(
            bleDevice,
            object : BleRssiCallback() {
                override fun onRssiFailure(exception: BleException) {}
                override fun onRssiSuccess(rssi: Int) {
                    when(rssi){
                        in -69..-1 -> binding.txtDistance.text = "-1m"
                        in -80..-70 -> binding.txtDistance.text = "1m"
                        in -89..-81 -> binding.txtDistance.text = "2m"
                        else -> binding.txtDistance.text = "+3m"
                    }
                }
            })
    }

    private fun bleIndicate() {
        BleManager.getInstance().indicate(
            bleDevice,
            indicateServiceUUID.toString(),
            indicateCharacteristicUUID.toString(),
            object : BleIndicateCallback() {
                override fun onIndicateSuccess() {
                    Toast.makeText(requireContext(), "Device Button pressed", Toast.LENGTH_LONG).show()
                }
                override fun onIndicateFailure(exception: BleException?) {}
                override fun onCharacteristicChanged(data: ByteArray?) {
                    Toast.makeText(requireContext(), "Device Button pressed", Toast.LENGTH_LONG).show()
                }

            }
        )
    }

    private fun bleWrite(){
        BleManager.getInstance().write(
            bleDevice,
            writeServiceUUID.toString(),
            writeCharacteristicUUID.toString(),
            HexUtil.hexStringToBytes("teste"),
            object : BleWriteCallback() {
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {}
                override fun onWriteFailure(exception: BleException?) {}
            }
        )

    }

    private fun iTagRssi() {
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                bleDevice?.let { bleRssi() }
            }
        },0,1000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}