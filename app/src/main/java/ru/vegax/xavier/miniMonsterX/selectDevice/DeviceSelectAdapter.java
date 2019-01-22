package ru.vegax.xavier.miniMonsterX.selectDevice;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Objects;
import java.util.Set;

import ru.vegax.xavier.miniMonsterX.R;

public abstract class DeviceSelectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements AdapterView.OnClickListener {


    private final String _currDevice;
    //Member variables
    private Set<String> _itemsData;
    private Context _context;


    /**
     * Constructor that passes in the sports data and the context
     *
     * @param context    Context of the application
     * @param itemsData  ArrayList containing the item's data
     * @param currDevice name of the selected device
     */
    DeviceSelectAdapter(Context context, Set<String> itemsData, String currDevice) {
        _itemsData = itemsData;
        _context = context;
        _currDevice = currDevice;
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
        return new DeviceSelectAdapter.ViewHolder(LayoutInflater.from(_context).inflate(R.layout.list_item_devices, parent, false));
    }

    /**
     * Required method that binds the data to the viewholder.
     *
     * @param holder   The viewholder into which the data should be put.
     * @param position The adapter position.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        DeviceSelectAdapter.ViewHolder viewHolder = (DeviceSelectAdapter.ViewHolder) holder;
        String currentItem = (String) Objects.requireNonNull(_itemsData.toArray())[position];
        viewHolder.bindTo(currentItem);
        //Get current item

        if (currentItem.equals(_currDevice)) {
            Context context = viewHolder.itemView.getContext();
            Resources res = context.getResources();

            viewHolder.getTxtVDevice().setTextColor(ContextCompat.getColor(context, R.color.ic_toggle_background));
        }
        viewHolder.getTxtVDevice().setOnClickListener(this);
        viewHolder.getBtnDelete().setTag(currentItem);
        viewHolder.getBtnDelete().setOnClickListener(this);

    }


    /**
     * ViewHolder class that represents each row of data in the RecyclerView
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView _txtVDevice;

        private Button _btnDelete;

        /**
         * Constructor for the ViewHolder, used in onCreateViewHolder().
         *
         * @param itemView The rootview of the list_item_input_input.xml layout file
         */
        ViewHolder(View itemView) {
            super(itemView);
            _txtVDevice = itemView.findViewById(R.id.txtVDevices);
            _btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bindTo(String currentDevice) {
            _txtVDevice.setText(currentDevice);
        }

        TextView getTxtVDevice() {
            return _txtVDevice;
        }

        Button getBtnDelete() {
            return _btnDelete;
        }

    }

}
