package ru.vegax.xavier.minimonsterx.iodata

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.ImageView
import android.widget.Switch
import java.util.ArrayList

import ru.vegax.xavier.minimonsterx.R

abstract class IOAdapter
/**
 * Constructor that passes in the sports data and the context
 *
 * @param mItemsData ArrayList containing the item's data
 * @param mContext   Context of the application
 */
internal constructor(private val mContext: Context, //Member variables
                     private val mItemsData: ArrayList<IOItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), View.OnClickListener, View.OnLongClickListener {


    /**
     * Required method for determining the size of the data set.
     *
     * @return Size of the data set.
     */
    override fun getItemCount(): Int {

        return mItemsData.size
    }

    // get item type for an specific item in list

    override fun getItemViewType(position: Int): Int {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        return if (mItemsData[position].isOutput) {
            OUTPUT_ELEMENT
        } else {
            INPUT_ELEMENT
        }

    }

    /**
     * Required method for creating the viewholder objects.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return The newly create ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            OUTPUT_ELEMENT -> ViewHolderOutputs(LayoutInflater.from(mContext).inflate(R.layout.list_item_output, parent, false))
            else -> ViewHolderInputs(LayoutInflater.from(mContext).inflate(R.layout.list_item_input, parent, false))
        }

    }

    /**
     * Required method that binds the data to the viewholder.
     *
     * @param holder   The viewholder into which the data should be put.
     * @param position The adapter position.
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder.itemViewType) {
            INPUT_ELEMENT -> {
                val viewHolderInputs = holder as ViewHolderInputs
                //Populate the textviews with data
                viewHolderInputs.bindTo(mItemsData[position])
            }

            OUTPUT_ELEMENT -> {
                val viewHolderOutputs = holder as ViewHolderOutputs
                //Get current item
                val currentItem = mItemsData[position]
                //Populate the textviews with data
                viewHolderOutputs.bindTo(currentItem)


                val switchItem = viewHolderOutputs.switch
                //disable swipe for the switch, only click on the item will trigger the output

                switchItem.setOnTouchListener { _, event ->
                    when( event.actionMasked){
                        MotionEvent.ACTION_MOVE -> true
                        MotionEvent.ACTION_DOWN -> {
                            if (currentItem.isOutput) {
                                currentItem.isChanging = true
                            }
                            false
                        }
                        else -> false

                    }
                }
                switchItem.isFocusableInTouchMode = false
                switchItem.tag = position
                switchItem.setOnClickListener(this)
                switchItem.setOnLongClickListener(this)
            }
        }

    }

    /**
     * ViewHolder class that represents each row of data in the RecyclerView
     */
    internal inner class ViewHolderInputs
    /**
     * Constructor for the ViewHolder, used in onCreateViewHolder().
     *
     * @param itemView The rootview of the list_item_input_input.xml layout file
     */
    (itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val _checkedTextView: CheckedTextView = itemView.findViewById(R.id.checkedTextView1)

        fun bindTo(currentItem: IOItem) {
            _checkedTextView.text = currentItem.itemName
            _checkedTextView.isChecked = currentItem.isOn
        }
    }

    internal inner class ViewHolderOutputs
    /**
     * Constructor for the ViewHolder, used in onCreateViewHolder().
     *
     * @param itemView The rootview of the list_item_input.xmlut.xml layout file
     */
    (itemView: View) : RecyclerView.ViewHolder(itemView) {


        private val _imgView: ImageView = itemView.findViewById(R.id.imageViewOutputs)
        val switch: Switch = itemView.findViewById(R.id.switch1)

        fun bindTo(currentItem: IOItem) {
            switch.text = currentItem.itemName
            switch.isChecked = currentItem.isOn

            if (currentItem.isImpulse) {
                _imgView.setImageResource(R.drawable.output_timer)
            } else {
                _imgView.setImageResource(R.drawable.output)
            }
        }
    }

    companion object {


        private const val INPUT_ELEMENT = 0
        private const val OUTPUT_ELEMENT = 1
    }
}
