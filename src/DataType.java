import java.util.Set;

public enum DataType {
    INT(), STR(), BOOL(), FLOAT(), UNDEF();
    //some nice constants
    public static Set<DataType> NUMBERS = Set.of(DataType.INT, DataType.FLOAT);
}
