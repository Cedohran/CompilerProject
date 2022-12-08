public class Ast {
    AstNode root;
    public Ast(String rootData) {
        root = new AstNode();
        root.value = rootData;
    }

    public Ast(AstNode root) {
        this.root = root;
    }

    public void printTree() {
        print(this.root);
    }

    private void print(AstNode root) {
        AstNode current;
        current = root;
        if(current.children.size() == 0) {
            return;
        }
        for(AstNode child : current.children) {
            System.out.print(child);
        }
        System.out.println("");
        for(AstNode child : current.children) {
            print(child);
        }
    }
}
