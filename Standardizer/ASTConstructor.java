package Standardizer;

import java.util.ArrayList;

public class ASTConstructor {

    public ASTConstructor() {

    }

    /**

        Parses a serialized list of AST node strings and reconstructs the AST.
        The input is a list where each string represents a node with leading dots
        indicating its depth in the tree.

     **/

    public AST getAbstractSyntaxTree(ArrayList<String> data) {
        Node root = NodeConstructor.getNode(data.get(0), 0);

        // Keep track of the previous node for linking children/parents
        Node previous_node = root;

        int current_depth = 0;

        // Iterate over the rest of the serialized nodes (starting from index 1)
        for (String s : data.subList(1, data.size())) {
            int i = 0;  // index to traverse the string
            int d = 0;  // depth of the current node, counted by dots

            // Count leading dots to find current node's depth
            while (s.charAt(i) == '.') {
                d++;  // increment depth for each dot
                i++;  // move to next character
            }

            // Create a new node with data after the dots and depth d
            Node current_node = NodeConstructor.getNode(s.substring(i), d);

            // If depth increased, current node is child of previous node
            if (current_depth < d) {
                previous_node.children.add(current_node);
                current_node.setParent(previous_node);
            } else {
                // Depth stayed same or decreased:
                // Move up the tree until we find the correct parent depth
                while (previous_node.getDepth() != d) {
                    previous_node = previous_node.getParent();
                }
                // Add current node as a sibling under the correct parent
                previous_node.getParent().children.add(current_node);
                current_node.setParent(previous_node.getParent());
            }

            // Update previous node and depth to current node and depth
            previous_node = current_node;
            current_depth = d;
        }
        return new AST(root);
    }
}
