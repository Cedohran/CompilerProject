import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AstNode {
    String value = "?";
    List<AstNode> children = new ArrayList<>();
    AstNodeType nodeType;

    public AstNode() {}

    public AstNode(List<AstNode> children, String value, AstNodeType nodeType) {
        addChildren(children);
        this.value = value;
        this.nodeType = nodeType;
    }

    public static AstNode createNullNode() {
        AstNode nullNode = new AstNode();
        nullNode.nodeType = AstNodeType.NULL_NODE;
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
        if(child.nodeType == AstNodeType.NULL_NODE) return child;
        this.children.add(child);
        return child;
    }

    public void addChildren(List<AstNode> children) {
        for(AstNode child : children) {
            if(child.nodeType != AstNodeType.NULL_NODE) {
                this.children.add(child);
            }
        }
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
