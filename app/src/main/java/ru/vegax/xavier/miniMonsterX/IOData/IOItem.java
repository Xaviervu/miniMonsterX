package ru.vegax.xavier.miniMonsterX.IOData;

public class IOItem {


    //Member variables representing the title and information about the sport
    private String _itemName;
    private boolean _isOutput;
    private boolean _isOn;
    private boolean _isImpulse;
    private boolean _isChanging;

    /**
     * Constructor for the Sport data model
     *
     * @param itemName The name of the item.
     * @param isOutput The item is of an output type.
     * @param isOn     The input/ output is on
     */
    public IOItem(String itemName, boolean isOutput, boolean isOn, boolean isImpulse) {
        _itemName = itemName;
        _isOutput = isOutput;
        _isOn = isOn;
        _isImpulse = isImpulse;
    }

    /**
     * Gets Item name
     *
     * @return The name of the item.
     */
    String getItemName() {
        return _itemName;
    }

    /**
     * Gets is output
     *
     * @return whether the port is an output
     */
    boolean isOutput() {
        return _isOutput;
    }

    /**
     * Gets is on
     *
     * @return whether the port signal is High or Low
     */
    public boolean isOn() {
        return _isOn;
    }

    public boolean isImpulse() {
        return _isImpulse;
    }

    void setImpulse(boolean impulse) {
        _isImpulse = impulse;
    }

    public boolean isChanging() {
        return _isChanging;
    }

    void setChanging(boolean changing) {
        _isChanging = changing;
    }
}
