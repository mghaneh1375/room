package bogen.studio.Room.Utility;
public class PairValue {

    private Object Key;
    private Object Value;

    public PairValue(Object Key, Object Value)
    {
        this.Key = Key;
        this.Value = Value;
    }

    public Object getKey() {
        return Key;
    }

    public Object getValue() {
        return Value;
    }

}
