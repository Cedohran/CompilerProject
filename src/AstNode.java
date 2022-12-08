import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AstNode {
    String value = "?";
    AstNode parent = null;
    List<AstNode> children;
    AstNodeType type;

    public AstNode() {
        children = new ArrayList<>();
    }

    //terminal constructor
    public AstNode(String value, AstNodeType type) {
        this.value = value;
        children = new ArrayList<>();
        this.type = type;
    }

    //non-terminal constructor
    public AstNode(List<AstNode> children, String value) {
        this.value = value;
        this.children = new ArrayList<>();
        this.children.addAll(children);
        this.type = AstNodeType.NON_TERMINAL;
    }

    public DataType getDataType() {
        return null;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder(500000);
        print(buffer, "", "");
        return buffer.toString();
    }

    public AstNode addChild(AstNode child) {
        this.children.add(child);
        return child;
    }

    public void addChildren(List<AstNode> children) {
        this.children.addAll(children);
    }

    private void print(StringBuilder buffer, String prefix, String childrenPrefix) {
        buffer.append(prefix);
        buffer.append(value);
        //type
        buffer.append('\n');
        for (Iterator<AstNode> it = children.iterator(); it.hasNext();) {
            AstNode next = it.next();
            if (it.hasNext()) {
                next.print(buffer, childrenPrefix + " |--- ", childrenPrefix + " |   ");
            } else {
                next.print(buffer, childrenPrefix + " '--- ", childrenPrefix + "     ");
            }
        }
    }
}
