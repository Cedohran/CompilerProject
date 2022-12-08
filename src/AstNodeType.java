public enum AstNodeType {
    NON_TERMINAL("NON_TERMINAL"), ID("ID"), VAR_TYPE("VAR_TYPE"),
    LIT("LIT"), OP("OP"), CMP_SMBL("cmp"), LGC_SMBL("lgc");

    private final String name;

    AstNodeType(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }
}
