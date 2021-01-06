package ru.vegax.xavier.miniMonsterX.fragments.iodata.port_select

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import ru.vegax.xavier.miniMonsterX.R
import ru.vegax.xavier.miniMonsterX.databinding.ListItemPortsBinding
import ru.vegax.xavier.miniMonsterX.repository.DeviceData

class PortSelectAdapter(private val deviceData: DeviceData, private val isOutputList: List<Boolean>, private val onClickListener: (newDeviceData: DeviceData) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int {
        return deviceData.impulseTypes.size
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PortsViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.list_item_ports, parent, false))
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val viewHolder = holder as PortsViewHolder
        with(viewHolder.binding) {
            chkBoxShown.isChecked = !deviceData.hiddenInputs[position]
            txtVPort.text = deviceData.portNames[position]
            val portId = if (isOutputList[position]) R.drawable.ic_output else R.drawable.ic_input
            imgVOutputType.setImageDrawable(AppCompatResources.getDrawable(imgVOutputType.context, portId))
            portLayout.setOnClickListener {
                deviceData.hiddenInputs[position] = chkBoxShown.isChecked
                onClickListener.invoke(deviceData)
            }
        }
    }

    internal inner class PortsViewHolder(val binding: ListItemPortsBinding) : RecyclerView.ViewHolder(binding.root)
    companion object {
        private const val TAG = "PortSelectAdapter"
    }
}
