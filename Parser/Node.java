package Parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Node {
    public NodeType type;
    public String value;
    public List<Node> children;

    public Node(NodeType type, String value) {
        this.type = type;
        this.value = value;
        this.children = new ArrayList<>();
    }

    public Node(NodeType type, String value, Node... children) {
        this.type = type;
        this.value = value;
        this.children = Arrays.asList(children);
    }

    public Node(NodeType type, String value, List<Node> children) {
        this.type = type;
        this.value = value;
        this.children = children;
    }

}

