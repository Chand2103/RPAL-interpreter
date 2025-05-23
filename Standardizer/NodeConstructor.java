package Standardizer;

import java.util.ArrayList;

public class NodeConstructor {

    public NodeConstructor() {

    }

    /**

        Creates a new Node with given data and depth.
        Initializes an empty children list.
        @param data Node's data string
        @param depth Node's depth in the AST
        @return newly created Node

     **/
    public static Node getNode(String data, int depth) {
        Node node = new Node();
        node.setData(data);
        node.setDepth(depth);
        node.children = new ArrayList<Node>();
        return node;
    }

    /**

        Creates a new Node with given data, depth, parent, children, and standardized flag.
        @param data Node's data string
        @param depth Node's depth in the AST
        @param parent Node's parent in the AST
        @param children List of child nodes
        @param isStandardize Boolean flag indicating if the node is standardized
        @return newly created Node with specified properties

     */
    public static Node getNode(String data, int depth, Node parent, ArrayList<Node> children, boolean isStandardize) {
        Node node = new Node();
        node.setData(data);
        node.setDepth(depth);
        node.setParent(parent);
        node.children = children;
        node.isStandardized = isStandardize;
        return node;
    }
}
