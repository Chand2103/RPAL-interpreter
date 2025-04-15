package Standardizer;

import Parser.Node;
import Parser.NodeType;

import java.util.List;

public class Standardizer {
    Node root;

    public Standardizer (Node root){
        this.root=root;
    }

    public void standardize(Node n){
        for(Node y : n.children){
          standardize(y);
        }
        standardizer(n);
    }
    public void standardizer(Node n) {
        switch (n.value) {
            //let
            case "let":
                Node temp = n.children.get(0).children.get(1);
                n.children.get(0).children.set(1,n.children.get(1));
                n.children.set(1,temp);
                n.value = "gamma";
                n.type = NodeType.gamma;
                n.children.get(0).value = "lambda";
                n.children.get(0).type  = NodeType.lambda;
                break;
            //where
            case "where":
                Node lambda = new Node(NodeType.lambda,"lambda");
                Node P = n.children.get(0);
                Node E = n.children.get(1).children.get(1);
                n.children.set(0,lambda);
                n.children.get(0).children.add(n.children.get(1).children.get(0));
                n.children.get(0).children.add(P);
                n.children.set(1,E);
                n.value = "gamma";
                n.type = NodeType.gamma;
                break;
            //fcn_form
            case "fcn_form":
                List<Node> variables = null;
                for(int i=1;i<n.children.size()-1;i++){
                    variables.add(n.children.get(i));
                }
                Node Pf = n.children.get(0);
                Node Ef = n.children.get(n.children.size()-1);
                n.children.clear();
                n.children.add(Pf);
                Node curr = n;
                for(Node x : variables){
                    Node new_lambda = new Node(NodeType.lambda,"lambda");
                    curr.children.add(new_lambda);
                    curr = new_lambda;
                    curr.children.add(x);
                }
                curr.children.add(Ef);
                n.value = "=";
                n.type = NodeType.equal;
                break;
//            //tuples
//            case "tau":
//                List <Node> childs = null;
//                for(Node x: n.children){
//                    childs.add(x);
//                }
//                n.children.clear();
//                Node current = n;
//                for(Node x : childs){
//                    current.type = NodeType.gamma;
//                    current.value = "gamma";
//                    Node node1 = new Node(NodeType.gamma,"gamma");
//                    current.children.add(node1);
//                    current.children.add(x);
//                    current.children.get(0).children.add(new Node(NodeType.aug,"aug"));
//                    Node next = new Node(NodeType.nil,"nil");
//                    current.children.get(0).children.add(next);
//                    current = next;
//                }
//                break;
            //multiparameter functions
            case "lambda":
                if(n.children.size()>2){
                    List<Node> parameters = null;
                    for(int i=0;i<n.children.size();i++){
                        parameters.add(n.children.get(i));
                    }
                    Node expression = n.children.get(n.children.size()-1);
                    n.children.clear();
                    Node cur = n;
                    for(int i=0;i<parameters.size();i++){
                        Node x = parameters.get(i);
                        if(i==parameters.size()-1){
                            cur.children.add(x);
                            cur.children.add(expression);
                        }
                        else{
                            cur.children.add(x);
                            Node next = new Node(NodeType.lambda,"lambda");
                            cur.children.add(next);
                            cur = next;
                        }
                    }
                }
                break;
            //within
            case "within":
                Node x1 = n.children.get(0).children.get(0);
                Node e1 = n.children.get(0).children.get(1);
                Node x2 = n.children.get(1).children.get(0);
                Node e2 = n.children.get(1).children.get(1);
                n.children.clear();
                n.children.add(x2);
                n.children.add(new Node(NodeType.gamma,"gamma"));
                n.children.get(1).children.add(new Node(NodeType.lambda,"lambda"));
                n.children.get(1).children.add(e1);
                n.children.get(1).children.get(0).children.add(x1);
                n.children.get(1).children.get(0).children.add(e2);
                n.type = NodeType.equal;
                n.value = "=";
                break;
            //unary and binary operations
            //@
            case "@":
                Node E1 = n.children.get(0);
                Node N = n.children.get(1);
                Node E2 = n.children.get(2);
                n.children.clear();
                n.children.add(new Node(NodeType.gamma,"gamma"));
                n.children.add(E2);
                n.children.get(0).children.add(N);
                n.children.get(0).children.add(E1);
                n.type = NodeType.gamma;
                n.value = "gamma";
                break;
            //simultaneous definitions
            case "and":
                Node comma = new Node(NodeType.comma,",");
                Node tau = new Node(NodeType.tau,"tau");
                for(Node x:n.children){
                    Node variable = x.children.get(0);
                    Node ex = x.children.get(1);
                    comma.children.add(variable);
                    tau.children.add(ex);
                }
                n.children.clear();
                n.children.add(comma);
                n.children.add(tau);
                n.type = NodeType.equal;
                n.value = "=";
                break;
            //conditional operator
            //rec
            case "rec":
                Node X_rec = n.children.get(0).children.get(0);
                Node E_rec = n.children.get(0).children.get(1);
                n.children.clear();
                n.children.add(X_rec);
                n.children.add(new Node(NodeType.gamma,"gamma"));
                n.children.get(1).children.add(new Node(NodeType.y_star,"y_star"));
                n.children.get(1).children.add(new Node(NodeType.lambda,"lambda"));
                n.children.get(1).children.get(1).children.add(X_rec);
                n.children.get(1).children.get(1).children.add(E_rec);
                n.type = NodeType.equal;
                n.value = "=";
                break;
        }
    }
}
