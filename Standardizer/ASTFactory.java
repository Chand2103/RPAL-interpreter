package Standardizer;

import Parser.Node;
import Parser.NodeType;
import java.util.ArrayList;

public class ASTFactory {

    public ASTFactory() {

    }

    public AST getAbstractSyntaxTree(ArrayList<String> data) {
        Node root = createNodeFromString(data.get(0));
        Node previous_node = root;
        int current_depth = 0;

        for (String s: data.subList(1, data.size())) {
            int i = 0;
            int d = 0;

            while (s.charAt(i) == '.') {
                d++;
                i++;
            }

            Node current_node = createNodeFromString(s.substring(i));

            if (current_depth < d) {
                previous_node.children.add(current_node);
            } else {
                Node parent = previous_node;
                int steps_up = current_depth - d + 1;
                for (int step = 0; step < steps_up; step++) {
                    if (parent.children.isEmpty()) break;
                    // Find the last added child's parent context
                    // This is a simplified approach - in practice you'd need
                    // to maintain a proper parent-child relationship
                }
                // Add to the appropriate parent level
                // This requires a more sophisticated tree building approach
                // For now, we'll use a simplified version
                addNodeAtDepth(root, current_node, d);
            }

            previous_node = current_node;
            current_depth = d;
        }
        return new AST(root);
    }

    private Node createNodeFromString(String data) {
        // Parse node type and value from string representation
        if (data.startsWith("<ID:")) {
            String value = data.substring(4, data.length() - 1);
            return new Node(NodeType.identifier, value);
        } else if (data.startsWith("<INT:")) {
            String value = data.substring(5, data.length() - 1);
            return new Node(NodeType.integer, value);
        } else if (data.startsWith("<STR:")) {
            String value = data.substring(5, data.length() - 1);
            return new Node(NodeType.string, value);
        } else if (data.equals("<true>")) {
            return new Node(NodeType.true_value, "true");
        } else if (data.equals("<false>")) {
            return new Node(NodeType.false_value, "false");
        } else if (data.equals("<nil>")) {
            return new Node(NodeType.nil, "nil");
        } else if (data.equals("<dummy>")) {
            return new Node(NodeType.dummy, "dummy");
        } else if (data.equals("function_form")) {
            return new Node(NodeType.fcn_form, "fcn_form");
        } else {
            // For other cases, try to map string to appropriate NodeType
            NodeType type = getNodeTypeFromString(data);
            return new Node(type, data);
        }
    }

    private NodeType getNodeTypeFromString(String data) {
        return switch (data) {
            case "let" -> NodeType.let;
            case "lambda" -> NodeType.lambda;
            case "gamma" -> NodeType.gamma;
            case "tau" -> NodeType.tau;
            case "aug" -> NodeType.aug;
            case "where" -> NodeType.where;
            case "rec" -> NodeType.rec;
            case "and" -> NodeType.and;
            case "within" -> NodeType.within;
            case "=" -> NodeType.equal;
            case "," -> NodeType.comma;
            case "->" -> NodeType.conditional;
            case "or" -> NodeType.op_or;
            case "&" -> NodeType.op_and;
            case "not" -> NodeType.op_not;
            case "+" -> NodeType.op_plus;
            case "-" -> NodeType.op_minus;
            case "*" -> NodeType.op_mul;
            case "/" -> NodeType.op_div;
            case "**" -> NodeType.op_pow;
            case "neg" -> NodeType.op_neg;
            case "@" -> NodeType.at;
            case "gr", "ge", "ls", "le", "eq", "ne" -> NodeType.op_compare;
            case "<Y*>" -> NodeType.y_star;
            case "()" -> NodeType.empty_params;
            default -> NodeType.identifier; // Default fallback
        };
    }

    private void addNodeAtDepth(Node root, Node nodeToAdd, int targetDepth) {
        // This is a simplified implementation
        // In practice, you'd need to maintain proper tree structure
        // For now, we'll add to the root's children
        if (targetDepth == 1) {
            root.children.add(nodeToAdd);
        } else {
            // Add to the last child at appropriate depth
            if (!root.children.isEmpty()) {
                addNodeAtDepth(root.children.get(root.children.size() - 1), nodeToAdd, targetDepth - 1);
            }
        }
    }
}