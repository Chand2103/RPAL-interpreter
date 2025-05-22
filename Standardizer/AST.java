package Standardizer;

import Parser.Node;
import java.util.ArrayList;

public class AST {
    private Node root;
    private boolean isStandardized = false;
    private ArrayList<String> standardizedStringAST;

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
            this.standardizedStringAST = standardizer.convertStandardizedAST_toStringAST(this.root);
            this.isStandardized = true;
        }
    }

    public ArrayList<String> getStandardizedStringAST() {
        if (!this.isStandardized) {
            standardize();
        }
        return this.standardizedStringAST;
    }

    public void printStandardizedAST() {
        if (!this.isStandardized) {
            standardize();
        }
        for (String line : this.standardizedStringAST) {
            System.out.println(line);
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