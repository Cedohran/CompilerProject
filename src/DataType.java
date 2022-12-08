public enum DataType {
    INT("int"), STR("string"), BOOL("bool"), FLOAT("float64");

    private final String name;

    DataType(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }
}
