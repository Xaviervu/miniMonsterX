package ru.vegax.xavier.miniMonsterX.IOData;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.Switch;

import java.util.ArrayList;

import ru.vegax.xavier.miniMonsterX.R;

public abstract class IOAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements AdapterView.OnClickListener,
        AdapterView.OnLongClickListener {


    private static final int INPUT_ELEMENT = 0;
    private static final int OUTPUT_ELEMENT = 1;
    //Member variables
    private ArrayList<IOItem> _itemsData;
    private Context _context;


    /**
     * Constructor that passes in the sports data and the context
     *
     * @param itemsData ArrayList containing the item's data
     * @param context   Context of the application
     */
    IOAdapter(Context context, ArrayList<IOItem> itemsData) {
        _itemsData = itemsData;
        _context = context;
    }


    /**
     * Required method for determining the size of the data set.
     *
     * @return Size of the data set.
     */
    @Override
    public int getItemCount() {

        return _itemsData.size();
    }

    // get item type for an specific item in list

    @Override
    public int getItemViewType(int position) {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        if (_itemsData.get(position).isOutput()) {
            return OUTPUT_ELEMENT;
        } else {
            return INPUT_ELEMENT;
        }

    }

    /**
     * Required method for creating the viewholder objects.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return The newly create ViewHolder.
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case OUTPUT_ELEMENT:
                return new ViewHolderOutputs(LayoutInflater.from(_context).inflate(R.layout.list_item_output, parent, false));
            default:
                return new ViewHolderInputs(LayoutInflater.from(_context).inflate(R.layout.list_item_input, parent, false));
        }

    }

    /**
     * Required method that binds the data to the viewholder.
     *
     * @param holder   The viewholder into which the data should be put.
     * @param position The adapter position.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        switch (holder.getItemViewType()) {
            case INPUT_ELEMENT:
                ViewHolderInputs viewHolderInputs = (ViewHolderInputs) holder;
                //Populate the textviews with data
                viewHolderInputs.bindTo(_itemsData.get(position));

                break;

            case OUTPUT_ELEMENT:
                ViewHolderOutputs viewHolderOutputs = (ViewHolderOutputs) holder;
                //Get current item
                IOItem currentItem = _itemsData.get(position);
                //Populate the textviews with data
                viewHolderOutputs.bindTo(currentItem);


                Switch switchItem = viewHolderOutputs.getSwitch();
                //disable swipe for the switch, only click on the item will trigger the output

                switchItem.setOnTouchListener((v, event) -> {
                    if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
                        return true;
                    } else if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        if (currentItem.isOutput()) {
                            currentItem.setChanging(true);
                        }
                        return false;
                    } else {
                        return false;
                    }

                });
                switchItem.setFocusableInTouchMode(false);
                switchItem.setTag(position);
                switchItem.setOnClickListener(this);
                switchItem.setOnLongClickListener(this);
                break;
        }

    }

    /**
     * ViewHolder class that represents each row of data in the RecyclerView
     */
    class ViewHolderInputs extends RecyclerView.ViewHolder {
        private CheckedTextView _checkedTextView;

        /**
         * Constructor for the ViewHolder, used in onCreateViewHolder().
         *
         * @param itemView The rootview of the list_item_input_input.xml layout file
         */
        ViewHolderInputs(View itemView) {
            super(itemView);
            _checkedTextView = itemView.findViewById(R.id.checkedTextView1);
        }

        void bindTo(IOItem currentItem) {
            _checkedTextView.setText(currentItem.getItemName());
            _checkedTextView.setChecked(currentItem.isOn());
        }
    }

    class ViewHolderOutputs extends RecyclerView.ViewHolder {


        private final ImageView _imgView;
        private Switch _switch;

        Switch getSwitch() {
            return _switch;
        }

        /**
         * Constructor for the ViewHolder, used in onCreateViewHolder().
         *
         * @param itemView The rootview of the list_item_input.xmlut.xml layout file
         */
        ViewHolderOutputs(View itemView) {
            super(itemView);
            //Initialize the views
            _switch = itemView.findViewById(R.id.switch1);
            _imgView = itemView.findViewById(R.id.imageViewOutputs);
        }

        void bindTo(IOItem currentItem) {
            _switch.setText(currentItem.getItemName());
            _switch.setChecked(currentItem.isOn());

            if (currentItem.isImpulse()) {
                _imgView.setImageResource(R.drawable.output_timer);
            } else {
                _imgView.setImageResource(R.drawable.output);
            }
        }
    }
}
