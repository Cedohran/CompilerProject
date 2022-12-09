import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AstNode {
    String value = "?";
    List<AstNode> children;
    AstNodeType type;

    public AstNode() {
        children = new ArrayList<>();
    }

    public AstNode(List<AstNode> children, String value, AstNodeType type) {
        this.value = value;
        this.children = children;
        this.type = type;
    }

    public static AstNode createNullNode() {
        AstNode nullNode = new AstNode();
        nullNode.type = AstNodeType.NULL_NODE;
        nullNode.children = new ArrayList<>();
        nullNode.value = "";
        return nullNode;
    }

    public DataType getDataType() {
        return null;
    }

    public boolean hasChild(){
        return !children.isEmpty();
    }

    public AstNode addChild(AstNode child) {
        this.children.add(child);
        return child;
    }

    public void addChildren(List<AstNode> children) {
        this.children.addAll(children);
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder(500000);
        print(buffer, "", "");
        return buffer.toString();
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
