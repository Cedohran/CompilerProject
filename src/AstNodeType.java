import java.util.Set;

public enum AstNodeType {
    NON_TERMINAL(), ID(), VAR_TYPE(), LIT(), OP(), CMP_SMBL(), LGC_SMBL(), NULL_NODE();
    //some nice constants
    public static Set<String> ARIT_CMP_SMBLS = Set.of("<", ">", "<=", ">=");
    public static Set<String> EQ_SMBLS = Set.of("==", "!=");
}
