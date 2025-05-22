package Standardizer;

import Parser.Node;

public class AST {
    private Node root;
    private boolean isStandardized = false;

    public AST(Node root) {
        this.setRoot(root);
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public Node getRoot() {
        return this.root;
    }

    public void standardize() {
        if (!this.isStandardized) {
            Standardizer standardizer = new Standardizer(this.root);
            this.isStandardized = true;
        }
    }

    private void preOrderTraverse(Node node, int i) {
        for (int n = 0; n < i; n++) {
            System.out.print(".");
        }
        System.out.println(node.value);
        node.children.forEach((child) -> preOrderTraverse(child, i+1));
    }

    public void printAst() {
        this.preOrderTraverse(this.getRoot(), 0);
    }
}