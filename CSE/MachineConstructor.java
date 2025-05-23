package CSE;

import Symbols.*;
import java.util.ArrayList;

import Standardizer.AST;
import Standardizer.Node;

public class MachineConstructor {
    // Initial environment symbol E(0)
    private E e0 = new E(0);

    // Counters for Lambda and Delta identifiers
    private int i = 1;
    private int j = 0;

    // Default constructor
    public MachineConstructor() {

    }

    /**

        Converts a syntax tree Node into the corresponding Symbol object
        based on the node's data value.

        @param node The Node from the AST
        @return A Symbol representing the node (operator, operand, etc.)

     **/
    public Symbol getSymbol(Node node) {
        switch (node.getData()) {
            // Unary operators
            case "not":
            case "neg":
                return new Uop(node.getData());
            // Binary operators
            case "+":
            case "-":
            case "*":
            case "/":
            case "**":
            case "&":
            case "or":
            case "eq":
            case "ne":
            case "ls":
            case "le":
            case "gr":
            case "ge":
            case "aug":
                return new Bop(node.getData());
            // Gamma operator
            case "gamma":
                return new Gamma();
            // Tau operator, number of children passed to constructor
            case "tau":
                return new Tau(node.children.size());
            // Ystar operator
            case "<Y*>":
                return new Ystar();
            // Operand cases (Identifiers, Integers, Strings, etc.)
            default:
                if (node.getData().startsWith("<ID:")) {
                    // Extract identifier name without <ID: and >
                    return new Id(node.getData().substring(4, node.getData().length() - 1));
                } else if (node.getData().startsWith("<INT:")) {
                    return new Int(node.getData().substring(5, node.getData().length() - 1));
                } else if (node.getData().startsWith("<STR:")) {
                    // Note substring adjusted for 2 chars at end to remove "> and possibly extra char
                    return new Str(node.getData().substring(6, node.getData().length() - 2));
                } else if (node.getData().startsWith("<nil")) {
                    return new Tup();
                } else if (node.getData().startsWith("<true>")) {
                    return new Bool("true");
                } else if (node.getData().startsWith("<false>")) {
                    return new Bool("false");
                } else if (node.getData().startsWith("<dummy>")) {
                    return new Dummy();
                } else {
                    System.out.println("Err node: " + node.getData());
                    return new Err();
                }
        }
    }

    /**

        Constructs a B symbol which represents a list of symbols
        obtained from pre-order traversal of the node subtree.

        @param node The root Node to traverse
        @return A B symbol containing symbols collected from subtree

     **/
    public B getB(Node node) {
        B b = new B();
        b.symbols = this.getPreOrderTraverse(node);
        return b;
    }

    /**

        Creates a Lambda symbol from a lambda node.
        Adds the Delta part from the second child node,
        and extracts identifiers from the first child node (which may be comma separated).

        @param node The lambda Node
        @return A Lambda symbol with delta and identifiers initialized

     **/

    public Lambda getLambda(Node node) {
        Lambda lambda = new Lambda(this.i++);  // assign unique id to lambda
        lambda.setDelta(this.getDelta(node.children.get(1)));  // set delta from second child

        // If multiple identifiers separated by ",", add each identifier separately
        if (",".equals(node.children.get(0).getData())) {
            for (Node identifier : node.children.get(0).children) {
                lambda.identifiers.add(new Id(identifier.getData().substring(4, node.getData().length() - 1)));
            }
        } else {
            // Single identifier case
            lambda.identifiers.add(new Id(node.children.get(0).getData().substring(4, node.children.get(0).getData().length() - 1)));
        }
        return lambda;
    }

    /**

        Performs pre-order traversal of the AST node tree,
        converting nodes to Symbols and recursively collecting children.
        Handles special nodes like lambda and "->".

        @param node The current Node to traverse
        @return List of Symbols corresponding to subtree

     **/

    private ArrayList<Symbol> getPreOrderTraverse(Node node) {
        ArrayList<Symbol> symbols = new ArrayList<Symbol>();

        if ("lambda".equals(node.getData())) {
            symbols.add(this.getLambda(node));
        } else if ("->".equals(node.getData())) {
            // For arrow node, add two deltas, beta symbol, and B symbol of first child
            symbols.add(this.getDelta(node.children.get(1)));
            symbols.add(this.getDelta(node.children.get(2)));
            symbols.add(new Beta());
            symbols.add(this.getB(node.children.get(0)));
        } else {
            // Regular node: add its symbol and recurse on children
            symbols.add(this.getSymbol(node));
            for (Node child : node.children) {
                symbols.addAll(this.getPreOrderTraverse(child));
            }
        }

        return symbols;
    }

    /**

        Creates a Delta symbol for the given node by
        collecting symbols from pre-order traversal.

        @param node The Node to convert to Delta
        @return Delta symbol containing subtree symbols

     **/
    public Delta getDelta(Node node) {
        Delta delta = new Delta(this.j++);
        delta.symbols = this.getPreOrderTraverse(node);
        return delta;
    }

    /**

        Builds the control list for the CSE machine from the AST.
        Starts with environment symbol e0 and then the Delta for the root node.

        @param ast The abstract syntax tree
        @return List of Symbols forming the control part of the machine
     */

    public ArrayList<Symbol> getControl(AST ast) {
        ArrayList<Symbol> control = new ArrayList<Symbol>();
        control.add(this.e0);  // initial environment symbol
        control.add(this.getDelta(ast.getRoot()));  // delta representing the program
        return control;
    }

    /**

        Initializes the stack for the CSE machine.
        Starts with the environment symbol e0.

        @return List of Symbols representing the stack

     **/
    public ArrayList<Symbol> getStack() {
        ArrayList<Symbol> stack = new ArrayList<Symbol>();
        stack.add(this.e0);
        return stack;
    }

    /**

        Initializes the environment list for the CSE machine.
        Starts with the environment symbol e0.

        @return List of E environments

     **/
    public ArrayList<E> getEnvironment() {
        ArrayList<E> environment = new ArrayList<E>();
        environment.add(this.e0);
        return environment;
    }

    /**

        Constructs and returns a new CSEMachine using
        the control, stack, and environment generated from the AST.

        @param ast The abstract syntax tree of the program
        @return Initialized CSEMachine ready for execution

     **/
    public CSEMachine getCSEMachine(AST ast) {
        return new CSEMachine(this.getControl(ast), this.getStack(), this.getEnvironment());
    }
}
