package ru.vegax.xavier.minimonsterx.select_device

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ru.vegax.xavier.minimonsterx.R
import ru.vegax.xavier.minimonsterx.repository.DeviceData

abstract class DeviceSelectAdapter
internal constructor(private val mContext: Context, //Member variables
                     private val mItemsData: List<DeviceData>, private val mCurrDevice: DeviceData?) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), View.OnClickListener {

    override fun getItemCount(): Int {

        return mItemsData.size
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item_devices, parent, false))
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val viewHolder = holder as ViewHolder
        val currentItem = mItemsData[position]
        viewHolder.bindTo(currentItem)
        //Get current item

        if (currentItem == mCurrDevice) {
            val context = viewHolder.itemView.context

            viewHolder.txtVDevice.setTextColor(ContextCompat.getColor(context, R.color.ic_toggle_background))
        }
        viewHolder.txtVDevice.tag = currentItem.deviceId
        viewHolder.txtVDevice.setOnClickListener(this)
        viewHolder.btnDelete.tag = currentItem.deviceId
        viewHolder.btnDelete.setOnClickListener(this)

    }


    internal inner class ViewHolder

    (itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtVDevice: TextView = itemView.findViewById(R.id.txtVDevices)

        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        fun bindTo(currentDevice: DeviceData) {
            txtVDevice.text = currentDevice.deviceName
        }

    }

}
