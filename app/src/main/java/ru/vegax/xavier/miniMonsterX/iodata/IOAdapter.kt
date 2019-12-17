package ru.vegax.xavier.miniMonsterX.iodata

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.ImageView
import android.widget.Switch
import androidx.recyclerview.widget.RecyclerView
import ru.vegax.xavier.miniMonsterX.R
import java.util.*

abstract class IOAdapter
internal constructor(private val context: Context, //Member variables
                     private val mItemsData: ArrayList<IOItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), View.OnClickListener, View.OnLongClickListener {

    override fun getItemCount(): Int {

        return mItemsData.size
    }

    // get item type for an specific item in list

    override fun getItemViewType(position: Int): Int {
        return if (mItemsData[position].isOutput) {
            OUTPUT_ELEMENT
        } else {
            INPUT_ELEMENT
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            OUTPUT_ELEMENT -> ViewHolderOutputs(LayoutInflater.from(context).inflate(R.layout.list_item_output, parent, false))
            else -> ViewHolderInputs(LayoutInflater.from(context).inflate(R.layout.list_item_input, parent, false))
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder.itemViewType) {
            INPUT_ELEMENT -> {
                val viewHolderInputs = holder as ViewHolderInputs
                viewHolderInputs.bindTo(mItemsData[position])
                viewHolderInputs.mCheckedTextView.setOnLongClickListener(this)
                viewHolderInputs.mCheckedTextView.tag = position
            }

            OUTPUT_ELEMENT -> {
                val viewHolderOutputs = holder as ViewHolderOutputs
                val currentItem = mItemsData[position]
                viewHolderOutputs.bindTo(currentItem)
                val switchItem = viewHolderOutputs.mSwitchOutput
                switchItem.setOnTouchListener { _, event ->
                    when (event.actionMasked) {
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
                viewHolderOutputs.mImgViewOutputType.tag = position
                viewHolderOutputs.mImgViewOutputType.setOnClickListener(this)
            }
        }

    }

    internal inner class ViewHolderInputs
    (itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mCheckedTextView: CheckedTextView = itemView.findViewById(R.id.checkedTextView1)

        fun bindTo(currentItem: IOItem) {
            mCheckedTextView.text = currentItem.itemName
            mCheckedTextView.isChecked = currentItem.isOn
        }
    }

    internal inner class ViewHolderOutputs
    (itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mImgViewOutputType: ImageView = itemView.findViewById(R.id.imageViewOutputs)
        val mSwitchOutput: Switch = itemView.findViewById(R.id.switch1)
        fun bindTo(currentItem: IOItem) {
            mSwitchOutput.text = currentItem.itemName
            mSwitchOutput.isChecked = currentItem.isOn

            if (currentItem.isImpulse) {
                mImgViewOutputType.setImageResource(R.drawable.output_timer)
            } else {
                mImgViewOutputType.setImageResource(R.drawable.output)
            }
        }
    }

    companion object {
        private const val INPUT_ELEMENT = 0
        private const val OUTPUT_ELEMENT = 1

    }
}
