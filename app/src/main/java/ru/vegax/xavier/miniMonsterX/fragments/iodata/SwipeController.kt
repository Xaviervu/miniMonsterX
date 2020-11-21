package ru.vegax.xavier.miniMonsterX.fragments.iodata

import android.R.attr.*
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.MotionEvent
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import ru.vegax.xavier.miniMonsterX.R
import kotlin.math.max
import kotlin.math.min


internal enum class ButtonsState {
    GONE, LEFT_VISIBLE, RIGHT_VISIBLE
}

internal class SwipeController(private val buttonsActions: SwipeControllerActions) : ItemTouchHelper.Callback() {
    private var swipeBack = false
    private var buttonShowedState = ButtonsState.GONE
    private  var buttonInstance: RectF? = null
    private var currentItemViewHolder: RecyclerView.ViewHolder? = null
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return makeMovementFlags(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        if (swipeBack) {
            swipeBack = buttonShowedState != ButtonsState.GONE
            return 0
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        var deltaX = dX
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (buttonShowedState != ButtonsState.GONE) {
                if (buttonShowedState == ButtonsState.LEFT_VISIBLE) deltaX = max(deltaX, buttonWidth)
                if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) deltaX = min(deltaX, -buttonWidth)
                super.onChildDraw(c, recyclerView, viewHolder, deltaX, dY, actionState, isCurrentlyActive)
            } else {
                setTouchListener(c, recyclerView, viewHolder, deltaX, dY, actionState, isCurrentlyActive)
            }
        }
        if (buttonShowedState == ButtonsState.GONE) {
            super.onChildDraw(c, recyclerView, viewHolder, deltaX, dY, actionState, isCurrentlyActive)
        }
        currentItemViewHolder = viewHolder
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchListener(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        recyclerView.setOnTouchListener { _, event ->
            swipeBack = event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_UP
            if (swipeBack) {
                if (dX < -buttonWidth) buttonShowedState = ButtonsState.RIGHT_VISIBLE else if (dX > buttonWidth) buttonShowedState = ButtonsState.LEFT_VISIBLE
                if (buttonShowedState != ButtonsState.GONE) {
                    setTouchDownListener(c, recyclerView, viewHolder, dY, actionState, isCurrentlyActive)
                    setItemsClickable(recyclerView, false)
                }
            }

            false
        }


    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchDownListener(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        recyclerView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                setTouchUpListener(c, recyclerView, viewHolder, dY, actionState, isCurrentlyActive)
            }
            false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchUpListener(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        recyclerView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                super@SwipeController.onChildDraw(c, recyclerView, viewHolder, 0f, dY, actionState, isCurrentlyActive)
                recyclerView.setOnTouchListener { _, _ -> false }
                setItemsClickable(recyclerView, true)
                swipeBack = false
                if (buttonInstance?.contains(event.x, event.y) == true) {
                    if (buttonShowedState == ButtonsState.LEFT_VISIBLE) {
                        buttonsActions.onLeftClicked(viewHolder.adapterPosition)
                    } else if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) {
                        buttonsActions.onRightClicked(viewHolder.adapterPosition)
                    }
                }
                buttonShowedState = ButtonsState.GONE
                currentItemViewHolder = null
            }
            false
        }
    }

    private fun setItemsClickable(recyclerView: RecyclerView, isClickable: Boolean) {
        for (i in 0 until recyclerView.childCount) {
            recyclerView.getChildAt(i).isClickable = isClickable
        }
    }

    private fun drawButtons(c: Canvas, viewHolder: RecyclerView.ViewHolder) {
        val horizontalPadding = 40
        val buttonWidthWithoutPadding = buttonWidth - horizontalPadding
        val verticalPadding = 40
        val corners = 16f
        val iconPadding = 20
        val itemView = viewHolder.itemView
        val p = Paint()
        val leftButton = RectF(itemView.left.toFloat() + 2 * horizontalPadding,
                itemView.top.toFloat() + verticalPadding,
                itemView.left + buttonWidthWithoutPadding, itemView.bottom.toFloat() - 2 * verticalPadding)
        p.color = ContextCompat.getColor(itemView.context, R.color.filled_button_border)
        c.drawRoundRect(leftButton, corners, corners, p)

        var icon: Drawable? = ContextCompat.getDrawable(itemView.context,R.drawable.ic_thermometer)
        icon?.setBounds(leftButton.left.toInt()+iconPadding, leftButton.top.toInt()+iconPadding,
                leftButton.right.toInt()-iconPadding, leftButton.bottom.toInt()-iconPadding)
        icon?.draw(c)

        val rightButton = RectF(itemView.right - buttonWidthWithoutPadding,
                itemView.top.toFloat() + verticalPadding, itemView.right.toFloat() - 2 * horizontalPadding,
                itemView.bottom.toFloat() - 2 * verticalPadding)
        p.color = ContextCompat.getColor(itemView.context, R.color.filled_button_border)
        c.drawRoundRect(rightButton, corners, corners, p)


        icon = ContextCompat.getDrawable(itemView.context,R.drawable.ic_baseline_delete_24)
        icon?.setBounds(rightButton.left.toInt()+iconPadding, rightButton.top.toInt()+iconPadding,
                rightButton.right.toInt()-iconPadding, rightButton.bottom.toInt()-iconPadding)
        icon?.draw(c)

        buttonInstance = null
        if (buttonShowedState == ButtonsState.LEFT_VISIBLE) {
            buttonInstance = leftButton
        } else if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) {
            buttonInstance = rightButton
        }
    }
    @ColorInt
    fun Context.getColorFromAttr(
            attrColor: Int,
            typedValue: TypedValue = TypedValue(),
            resolveRefs: Boolean = true
    ): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }
    private fun drawText(text: String, c: Canvas, button: RectF, p: Paint) {
        val textSize = 60f
        p.color = Color.WHITE
        p.isAntiAlias = true
        p.textSize = textSize
        val textWidth = p.measureText(text)
        c.drawText(text, button.centerX() - textWidth / 2, button.centerY() + textSize / 2, p)
    }

    fun onDraw(c: Canvas) {
        currentItemViewHolder?.let { drawButtons(c, it) }
    }

    companion object {
        private const val buttonWidth = 250f
    }

}