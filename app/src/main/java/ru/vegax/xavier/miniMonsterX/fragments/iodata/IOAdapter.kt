package ru.vegax.xavier.miniMonsterX.fragments.iodata

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import ru.vegax.xavier.miniMonsterX.R
import ru.vegax.xavier.miniMonsterX.auxiliar.hide
import ru.vegax.xavier.miniMonsterX.auxiliar.show
import ru.vegax.xavier.miniMonsterX.databinding.ListItemInputBinding
import ru.vegax.xavier.miniMonsterX.databinding.ListItemOutputBinding
import java.util.*

class IOAdapter(private val context: Context, //Member variables
                val mItemsData: ArrayList<IOItem>,
                val clickListener: (Int) -> Unit,
                val editNameListener: (Int) -> Unit,
                val editTypeListener: (Int) -> Unit,
                val tempClickListener: (Int) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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

            OUTPUT_ELEMENT -> ViewHolderOutputs(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.list_item_output, parent, false))
            else -> ViewHolderInputs(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.list_item_input, parent, false))
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder.itemViewType) {
            INPUT_ELEMENT -> {
                val viewHolderInputs = holder as ViewHolderInputs
                val currentItem = mItemsData[position]
                viewHolderInputs.bindTo(currentItem)
                viewHolderInputs.binding.checkedTextView1.setOnLongClickListener {
                    clickListener.invoke(currentItem.portId)
                    true
                }
                viewHolderInputs.binding.txtVTemperature.setOnClickListener {
                    tempClickListener.invoke(currentItem.portId)
                }
            }

            OUTPUT_ELEMENT -> {
                val viewHolderOutputs = holder as ViewHolderOutputs
                val currentItem = mItemsData[position]
                viewHolderOutputs.bindTo(currentItem)
                val switchItem = viewHolderOutputs.binding.switchOutput
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
                switchItem.setOnClickListener {
                    clickListener.invoke(currentItem.portId)
                }
                switchItem.setOnLongClickListener {
                    editNameListener.invoke(currentItem.portId)
                    true
                }

                viewHolderOutputs.binding.imageViewOutputs.setOnLongClickListener {
                    editTypeListener.invoke(currentItem.portId)
                    true
                }

                viewHolderOutputs.binding.txtVTemperature.setOnClickListener {
                    tempClickListener.invoke(currentItem.portId)
                }
            }
        }

    }

    internal inner class ViewHolderInputs(val binding: ListItemInputBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindTo(currentItem: IOItem) {
            with(binding) {
                checkedTextView1.text = currentItem.itemName
                checkedTextView1.isChecked = currentItem.isOn
                with(txtVTemperature) {
                    if (currentItem.temperature != null) {
                        show()
                        text = context.getString(R.string.temperature, currentItem.temperature.toString())
                    } else {
                        hide()
                    }
                }
            }
        }
    }

    internal inner class ViewHolderOutputs(val binding: ListItemOutputBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindTo(currentItem: IOItem) {
            with(binding) {
                switchOutput.text = currentItem.itemName
                switchOutput.isChecked = currentItem.isOn
                with(txtVTemperature) {
                    if (currentItem.temperature != null) {
                        show()
                        text = context.getString(R.string.temperature, currentItem.temperature.toString())
                    } else {
                        hide()
                    }
                }

                if (currentItem.isImpulse) {
                    imageViewOutputs.setImageResource(R.drawable.ic_output_timer)
                } else {
                    imageViewOutputs.setImageResource(R.drawable.ic_output)
                }
            }
        }

    }

    companion object {
        private const val INPUT_ELEMENT = 0
        private const val OUTPUT_ELEMENT = 1

    }
}
