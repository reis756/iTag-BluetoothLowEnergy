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
            binding.isConnected = BleManager.getInstance().isConnected(device)
            binding.bleDevice = device
            binding.viewHolder = this

            setIcon(BleManager.getInstance().isConnected(device))

            binding.executePendingBindings()
        }

        private fun setIcon(isConnected: Boolean) {
            if(isConnected)
                binding.imgDevice.setImageResource(R.drawable.ic_connected)
            else
                binding.imgDevice.setImageResource(R.drawable.ic_bluetooth)
        }

        fun details(device: BleDevice, isConnected: Boolean){
            if (isConnected)
                listener?.onDetail(device)
        }

        fun buttonClick(device: BleDevice, isConnected: Boolean) {
            if (isConnected) {
                binding.itemBtConnect.text = context.getString(R.string.disconnecting)
                listener?.onDisconnect(device)
            } else {
                binding.itemBtConnect.text = context.getString(R.string.connecting)
                listener?.onConnect(device)
            }
        }
    }
}