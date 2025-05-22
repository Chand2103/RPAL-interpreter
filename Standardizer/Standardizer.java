package Standardizer;

import Parser.Node;
import Parser.NodeType;
import Parser.Parser;
import lexer.LexicalAnalyser;
import lexer.Token;

import java.util.ArrayList;
import java.util.List;

public class Standardizer {
    Node root;

    public Standardizer(Node root) {
        this.root = root;
        standardize(root);
    }

    public void standardize(Node n) {
        for (Node y : n.children) {
            standardize(y);
        }
        standardizer(n);
    }

    public void standardizer(Node n) {
        switch (n.value) {
            // let
            case "let":
                Node temp = n.children.get(0).children.get(1);
                n.children.get(0).children.set(1, n.children.get(1));
                n.children.set(1, temp);
                n.value = "gamma";
                n.type = NodeType.gamma;
                n.children.get(0).value = "lambda";
                n.children.get(0).type = NodeType.lambda;
                break;

            // where
            case "where":
                Node lambda = new Node(NodeType.lambda, "lambda");
                Node P = n.children.get(0);
                Node E = n.children.get(1).children.get(1);
                n.children.set(0, lambda);
                n.children.get(0).children.add(n.children.get(1).children.get(0));
                n.children.get(0).children.add(P);
                n.children.set(1, E);
                n.value = "gamma";
                n.type = NodeType.gamma;
                break;

            // fcn_form
            case "fcn_form":
                List<Node> variables = new ArrayList<>();
                for (int i = 1; i < n.children.size() - 1; i++) {
                    variables.add(n.children.get(i));
                }
                Node Pf = n.children.get(0);
                Node Ef = n.children.get(n.children.size() - 1);
                n.children = new ArrayList<>();
                n.children.add(Pf);
                Node curr = n;
                for (Node x : variables) {
                    Node new_lambda = new Node(NodeType.lambda, "lambda");
                    curr.children.add(new_lambda);
                    curr = new_lambda;
                    curr.children.add(x);
                }
                curr.children.add(Ef);
                n.value = "=";
                n.type = NodeType.equal;
                break;

            // tuples
//            case "tau":
//                List<Node> childs = new ArrayList<>(n.children);
//                n.children.clear();
//                Node current = n;
//                for (int i = 0; i < childs.size(); i++) {
//                    Node x = childs.get(i);
//                    current.type = NodeType.gamma;
//                    current.value = "gamma";
//                    Node node1 = new Node(NodeType.gamma, "gamma");
//                    current.children.add(node1);
//                    current.children.add(x);
//                    current.children.get(0).children.add(new Node(NodeType.aug, "aug"));
//
//                    if (i == childs.size() - 1) {
//                        current.children.get(0).children.add(new Node(NodeType.nil, "nil"));
//                    } else {
//                        Node next = new Node(NodeType.nil, "nil");
//                        current.children.get(0).children.add(next);
//                        current = next;
//                    }
//                }
//                break;

            // multiparameter functions
            case "lambda":
                if (n.children.size() > 2) {
                    List<Node> parameters = new ArrayList<>();
                    for (int i = 0; i < n.children.size() - 1; i++) {
                        parameters.add(n.children.get(i));
                    }
                    Node expression = n.children.get(n.children.size() - 1);
                    n.children = new ArrayList<>();
                    Node cur = n;
                    for (int i = 0; i < parameters.size(); i++) {
                        Node x = parameters.get(i);
                        if (i == parameters.size() - 1) {
                            cur.children.add(x);
                            cur.children.add(expression);
                        } else {
                            cur.children.add(x);
                            Node next = new Node(NodeType.lambda, "lambda");
                            cur.children.add(next);
                            cur = next;
                        }
                    }
                }
                break;

            // within
            case "within":
                Node x1 = n.children.get(0).children.get(0);
                Node e1 = n.children.get(0).children.get(1);
                Node x2 = n.children.get(1).children.get(0);
                Node e2 = n.children.get(1).children.get(1);
                n.children = new ArrayList<>();
                n.children.add(x2);
                n.children.add(new Node(NodeType.gamma, "gamma"));
                n.children.get(1).children.add(new Node(NodeType.lambda, "lambda"));
                n.children.get(1).children.add(e1);
                n.children.get(1).children.get(0).children.add(x1);
                n.children.get(1).children.get(0).children.add(e2);
                n.type = NodeType.equal;
                n.value = "=";
                break;

            // unary and binary operations
            // @
            case "@":
                Node E1 = n.children.get(0);
                Node N = n.children.get(1);
                Node E2 = n.children.get(2);
                n.children = new ArrayList<>();
                n.children.add(new Node(NodeType.gamma, "gamma"));
                n.children.add(E2);
                n.children.get(0).children.add(N);
                n.children.get(0).children.add(E1);
                n.type = NodeType.gamma;
                n.value = "gamma";
                break;

            // simultaneous definitions
            case "and":
                Node comma = new Node(NodeType.comma, ",");
                Node tau = new Node(NodeType.tau, "tau");
                for (Node x : n.children) {
                    Node variable = x.children.get(0);
                    Node ex = x.children.get(1);
                    comma.children.add(variable);
                    tau.children.add(ex);
                }
                n.children = new ArrayList<>();
                n.children.add(comma);
                n.children.add(tau);
                n.type = NodeType.equal;
                n.value = "=";
                break;

            // conditional operator
//            case "->":
//                Node condition = n.children.get(0);
//                Node then_part = n.children.get(1);
//                Node else_part = n.children.get(2);
//
//                // Create the standardized form: gamma gamma gamma Cond then else
//                Node gamma1 = new Node(NodeType.gamma, "gamma");
//                Node gamma2 = new Node(NodeType.gamma, "gamma");
//                Node gamma3 = new Node(NodeType.gamma, "gamma");
//                Node cond_node = new Node(NodeType.identifier, "Cond");
//
//                gamma3.children.add(cond_node);
//                gamma3.children.add(condition);
//                gamma2.children.add(gamma3);
//                gamma2.children.add(then_part);
//                gamma1.children.add(gamma2);
//                gamma1.children.add(else_part);
//
//                n.children = gamma1.children;
//                n.type = gamma1.type;
//                n.value = gamma1.value;
//                break;

            // rec
            case "rec":
                Node X_rec = n.children.get(0).children.get(0);
                Node E_rec = n.children.get(0).children.get(1);
                n.children = new ArrayList<>();
                n.children.add(X_rec);
                n.children.add(new Node(NodeType.gamma, "gamma"));
                n.children.get(1).children.add(new Node(NodeType.y_star, "<Y*>"));
                n.children.get(1).children.add(new Node(NodeType.lambda, "lambda"));
                n.children.get(1).children.get(1).children.add(X_rec);
                n.children.get(1).children.get(1).children.add(E_rec);
                n.type = NodeType.equal;
                n.value = "=";
                break;

            default:
                // No standardization needed for other nodes
                break;
        }
    }

    public Node getRoot() {
        return root;
    }

    public static void main(String[] args) {
        LexicalAnalyser lex = new LexicalAnalyser("t1.txt");
        List<Token> passingtokens = lex.getTokens();
        Parser parser = new Parser(passingtokens);
        Node root = parser.parse();
        Standardizer stand = new Standardizer(root);
        display(root, "");
    }

    public static void display(Node x, String prefix) {
        // Format output according to node type
        switch(x.type) {
            case identifier:
                System.out.println(prefix + "<ID:" + x.value + ">");
                break;
            case integer:
                System.out.println(prefix + "<INT:" + x.value + ">");
                break;
            case string:
                System.out.println(prefix + "<STR:" + x.value + ">");
                break;
            case true_value:
                System.out.println(prefix + "<" + x.value + ">");
                break;
            case false_value:
                System.out.println(prefix + "<" + x.value + ">");
                break;
            case nil:
                System.out.println(prefix + "<" + x.value + ">");
                break;
            case dummy:
                System.out.println(prefix + "<" + x.value + ">");
                break;
            case fcn_form:
                System.out.println(prefix + "function_form");
                break;
            case y_star:
                System.out.println(prefix + "<Y*>");
                break;
            default:
                System.out.println(prefix + x.value);
        }

        for (Node y : x.children) {
            String newprefix = prefix + ".";
            display(y, newprefix);
        }
    }
}