package Standardizer;

public class AST {
    private Node root;

    public AST(Node root) {
        this.setRoot(root);
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public Node getRoot() {
        return this.root;
    }

    /**

        Standardizes the AST by invoking standardize on the root node,
        only if the tree is not already standardized.

     */
    public void standardize() {
        if (!this.root.isStandardized) {
            this.root.standardize();
        }
    }

    private void preOrderTraverse(Node node, int i) {
        // Print dots proportional to the depth (i) to visually represent the tree structure
        for (int n = 0; n < i; n++) {
            System.out.print(".");
        }
        // Print the data contained in the current node
        System.out.println(node.getData());

        // Recursively call preOrderTraverse on each child, increasing indentation level by 1
        node.children.forEach((child) -> preOrderTraverse(child, i + 1));
    }


    public void printAst() {
        this.preOrderTraverse(this.getRoot(), 0);
    }
}
