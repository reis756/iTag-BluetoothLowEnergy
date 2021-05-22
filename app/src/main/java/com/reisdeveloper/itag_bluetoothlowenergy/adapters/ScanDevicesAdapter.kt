package com.reisdeveloper.itag_bluetoothlowenergy.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.clj.fastble.BleManager
import com.clj.fastble.data.BleDevice
import com.reisdeveloper.itag_bluetoothlowenergy.R
import com.reisdeveloper.itag_bluetoothlowenergy.databinding.ItemDevicesBinding

class ScanDevicesAdapter : RecyclerView.Adapter<ScanDevicesAdapter.ScanDevicesViewHolder>() {

    private val bleDeviceList = mutableListOf<BleDevice>()
    private var listener: OnDeviceClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanDevicesViewHolder {
        val binding = ItemDevicesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScanDevicesViewHolder(binding, parent.context, listener)
    }

    override fun onBindViewHolder(holder: ScanDevicesViewHolder, position: Int) {
        holder.bind(bleDeviceList[position])
    }

    override fun getItemCount(): Int = bleDeviceList.size

    fun clearScanResults() {
        bleDeviceList.forEach {
            if (!BleManager.getInstance().isConnected(it))
                bleDeviceList.remove(it)
        }
        notifyDataSetChanged()
    }

    fun addDevice(bleDevice: BleDevice) {
        removeDevice(bleDevice)
        bleDeviceList.add(bleDevice)
        notifyDataSetChanged()
    }

    private fun removeDevice(bleDevice: BleDevice) {
        bleDeviceList.remove(bleDevice)
        notifyDataSetChanged()
    }

    interface OnDeviceClickListener {
        fun onConnect(bleDevice: BleDevice)
        fun onDisconnect(bleDevice: BleDevice)
        fun onDetail(bleDevice: BleDevice)
    }

    fun setOnDeviceClickListener(listener: OnDeviceClickListener?) {
        this.listener = listener
    }

    class ScanDevicesViewHolder(
        private val binding: ItemDevicesBinding,
        private val context: Context,
        private val listener: OnDeviceClickListener?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(device: BleDevice) {
            binding.itemTxtDeviceName.text =
                String.format("%s\n%s", device.name ?: "Unknown", device.mac)

            binding.itemTxtDeviceRssi.text = String.format("RSSI: %d", device.rssi)

            val isConnected = BleManager.getInstance().isConnected(device)

            if (isConnected) {
                binding.imgDevice.setImageResource(R.drawable.ic_connected)
                binding.itemBtConnect.text = context.getString(R.string.disconnect)
            } else {
                binding.imgDevice.setImageResource(R.drawable.ic_bluetooth)
                binding.itemBtConnect.text = context.getString(R.string.connect)
            }

            binding.itemBtConnect.setOnClickListener {
                if (isConnected) {
                    binding.itemBtConnect.text = context.getString(R.string.disconnecting)
                    listener?.onDisconnect(device)
                } else {
                    binding.itemBtConnect.text = context.getString(R.string.connecting)
                    listener?.onConnect(device)
                }
            }

            binding.itemContent.setOnClickListener {
                if (isConnected)
                    listener?.onDetail(device)
            }
        }
    }
}