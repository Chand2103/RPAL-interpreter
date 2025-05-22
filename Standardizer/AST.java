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

        // Format output according to node type
        switch(node.type) {
            case identifier:
                System.out.println("<ID:" + node.value + ">");
                break;
            case integer:
                System.out.println("<INT:" + node.value + ">");
                break;
            case string:
                System.out.println("<STR:" + node.value + ">");
                break;
            case true_value:
                System.out.println("<" + node.value + ">");
                break;
            case false_value:
                System.out.println("<" + node.value + ">");
                break;
            case nil:
                System.out.println("<" + node.value + ">");
                break;
            case dummy:
                System.out.println("<" + node.value + ">");
                break;
            case fcn_form:
                System.out.println("function_form");
                break;
            case y_star:
                System.out.println("<Y*>");
                break;
            default:
                System.out.println(node.value);
        }

        node.children.forEach((child) -> preOrderTraverse(child, i+1));
    }

    public void printAst() {
        this.preOrderTraverse(this.getRoot(), 0);
    }
}