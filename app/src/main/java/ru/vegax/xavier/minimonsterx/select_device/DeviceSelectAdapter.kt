package ru.vegax.xavier.minimonsterx.select_device

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import ru.vegax.xavier.minimonsterx.R

abstract class DeviceSelectAdapter
/**
 * Constructor that passes in the sports data and the context
 *
 * @param mContext    Context of the application
 * @param mItemsData  ArrayList containing the item's data
 * @param mCurrDevice name of the selected device
 */
internal constructor(private val mContext: Context, //Member variables
                     private val mItemsData: Set<String>, private val mCurrDevice: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), View.OnClickListener {


    /**
     * Required method for determining the size of the data set.
     *
     * @return Size of the data set.
     */
    override fun getItemCount(): Int {

        return mItemsData.size
    }


    /**
     * Required method for creating the viewholder objects.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return The newly create ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item_devices, parent, false))
    }

    /**
     * Required method that binds the data to the viewholder.
     *
     * @param holder   The viewholder into which the data should be put.
     * @param position The adapter position.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val viewHolder = holder as ViewHolder
        val currentItem = mItemsData.toTypedArray()[position]
        viewHolder.bindTo(currentItem)
        //Get current item

        if (currentItem == mCurrDevice) {
            val context = viewHolder.itemView.context

            viewHolder.txtVDevice.setTextColor(ContextCompat.getColor(context, R.color.ic_toggle_background))
        }
        viewHolder.txtVDevice.setOnClickListener(this)
        viewHolder.btnDelete.tag = currentItem
        viewHolder.btnDelete.setOnClickListener(this)

    }


    /**
     * ViewHolder class that represents each row of data in the RecyclerView
     */
    internal inner class ViewHolder
    /**
     * Constructor for the ViewHolder, used in onCreateViewHolder().
     *
     * @param itemView The rootview of the list_item_input_input.xml layout file
     */
    (itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtVDevice: TextView = itemView.findViewById(R.id.txtVDevices)

        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        fun bindTo(currentDevice: String) {
            txtVDevice.text = currentDevice
        }

    }

}
