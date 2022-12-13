import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AstNode {
    private String value = "?";
    private List<AstNode> children = new ArrayList<>();
    private AstNodeType nodeType;
    private DataType dataType = DataType.UNDEF;

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
        nullNode.dataType = DataType.UNDEF;
        return nullNode;
    }

    public String getText() {
        return value;
    }

    public List<AstNode> children() {
        return children;
    }

    public AstNodeType nodeType() {
        return nodeType;
    }

    public DataType dataType() {
        if(value.equals("int")) return DataType.INT;
        if(value.equals("string")) return DataType.STR;
        if(value.equals("float64")) return DataType.FLOAT;
        if(value.equals("bool")) return DataType.BOOL;
        return dataType;
    }

    public void setDataType(DataType type) {
        this.dataType = type;
    }

    public void setText(String value) {
        this.value = value;
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
