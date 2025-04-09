package Parser;

import java.util.*;
import lexer.LexicalAnalyser;
import lexer.Token;
import lexer.TokenType;

public class Parser {
    private List<Token> tokens;
    private int index = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.tokens.add(new Token(TokenType.EndOfTokens, "EOF"));
    }

    private Token peek() {
        return tokens.get(index);
    }

    private Token consume() {
        return tokens.get(index++);
    }

    private void expect(String expectedValue) {
        if (!peek().getValue().equals(expectedValue)) {
            throw new RuntimeException("Expected '" + expectedValue + "' but found '" + peek().getValue() + "'");
        }
        consume();
    }

    public Node parse() {
        Node root = E();
        if (!peek().getType().equals(TokenType.EndOfTokens)) {
            throw new RuntimeException("Unparsed tokens remain!");
        } else {
            System.out.println("successfully parsed");
        }
        return root;
    }

    private Node E() {
        Token token = peek();
        if (token.getType() == TokenType.KEYWORD && token.getValue().equals("let")) {
            consume();
            Node d = D();
            expect("in");
            Node e = E();
            return new Node(NodeType.let, "let", d, e);
        } else if (token.getType() == TokenType.KEYWORD && token.getValue().equals("fn")) {
            consume();
            List<Node> params = new ArrayList<>();
            while (peek().getType() == TokenType.IDENTIFIER || peek().getValue().equals("(")) {
                params.add(Vb());
            }
            expect(".");
            Node body = E();
            params.add(body);
            return new Node(NodeType.lambda, "lambda", params);
        } else {
            return Ew();
        }
    }

    private Node Ew() {
        Node t = T();
        if (peek().getValue().equals("where")) {
            consume();
            Node dr = Dr();
            return new Node(NodeType.where, "where", t, dr);
        }
        return t;
    }

    private Node T() {
        List<Node> elements = new ArrayList<>();
        elements.add(Ta());
        while (peek().getValue().equals(",")) {
            consume();
            elements.add(Ta());
        }
        return elements.size() > 1 ? new Node(NodeType.tau, "tau", elements) : elements.get(0);
    }

    private Node Ta() {
        Node left = Tc();
        while (peek().getValue().equals("aug")) {
            consume();
            Node right = Tc();
            left = new Node(NodeType.aug, "aug", left, right);
        }
        return left;
    }

    private Node Tc() {
        Node left = B();
        if (peek().getValue().equals("->")) {
            consume();
            Node mid = Tc();
            expect("|");
            Node right = Tc();
            return new Node(NodeType.conditional, "->", left, mid, right);
        }
        return left;
    }

    private Node B() {
        Node left = Bt();
        while (peek().getValue().equals("or")) {
            consume();
            Node right = Bt();
            left = new Node(NodeType.op_or, "or", left, right);
        }
        return left;
    }

    private Node Bt() {
        Node left = Bs();
        while (peek().getValue().equals("&")) {
            consume();
            Node right = Bs();
            left = new Node(NodeType.op_and, "&", left, right);
        }
        return left;
    }

    private Node Bs() {
        if (peek().getValue().equals("not")) {
            consume();
            Node child = Bp();
            return new Node(NodeType.op_not, "not", child);
        }
        return Bp();
    }

    private Node Bp() {
        Node left = A();
        Token token = peek();
        if (Arrays.asList("<", "<=", ">", ">=", "eq", "ne", "gr", "ge", "ls", "le").contains(token.getValue())) {
            consume();
            Node right = A();
            String type = switch (token.getValue()) {
                case ">" -> "gr";
                case ">=" -> "ge";
                case "<" -> "ls";
                case "<=" -> "le";
                default -> token.getValue();
            };
            return new Node(NodeType.op_compare, type, left, right);
        }
        return left;
    }

    private Node A() {
        if (peek().getValue().equals("+")) {
            consume();
            return At();
        } else if (peek().getValue().equals("-")) {
            consume();
            Node at = At();
            return new Node(NodeType.op_neg, "neg", at);
        }
        Node left = At();
        while (peek().getValue().equals("+") || peek().getValue().equals("-")) {
            String op = consume().getValue();
            Node right = At();
            left = new Node(op.equals("+") ? NodeType.op_plus : NodeType.op_minus, op, left, right);
        }
        return left;
    }

    private Node At() {
        Node left = Af();
        while (peek().getValue().equals("*") || peek().getValue().equals("/")) {
            String op = consume().getValue();
            Node right = Af();
            left = new Node(op.equals("*") ? NodeType.op_mul : NodeType.op_div, op, left, right);
        }
        return left;
    }

    private Node Af() {
        Node left = Ap();
        if (peek().getValue().equals("**")) {
            consume();
            Node right = Af();
            return new Node(NodeType.op_pow, "**", left, right);
        }
        return left;
    }

    private Node Ap() {
        Node left = R();
        while (peek().getValue().equals("@")) {
            consume();
            Token id = consume();
            if (id.getType() != TokenType.IDENTIFIER) throw new RuntimeException("Expected identifier after '@'");
            Node idNode = new Node(NodeType.identifier, id.getValue());
            Node right = R();
            left = new Node(NodeType.at, "@", left, idNode, right);
        }
        return left;
    }

    private Node R() {
        Node left = Rn();
        while ((peek().getType() == TokenType.IDENTIFIER || peek().getType() == TokenType.INTEGER || peek().getType() == TokenType.STRING
                || List.of("true", "false", "nil", "dummy").contains(peek().getValue()) || peek().getValue().equals("("))) {
            Node right = Rn();
            left = new Node(NodeType.gamma, "gamma", left, right);
        }
        return left;
    }

    private Node Rn() {
        Token token = peek();
        switch (token.getType()) {
            case IDENTIFIER:
                consume();
                return new Node(NodeType.identifier, token.getValue());
            case INTEGER:
                consume();
                return new Node(NodeType.integer, token.getValue());
            case STRING:
                consume();
                return new Node(NodeType.string, token.getValue());
            case KEYWORD:
                consume();
                return switch (token.getValue()) {
                    case "true" -> new Node(NodeType.true_value, "true");
                    case "false" -> new Node(NodeType.false_value, "false");
                    case "nil" -> new Node(NodeType.nil, "nil");
                    case "dummy" -> new Node(NodeType.dummy, "dummy");
                    default -> throw new RuntimeException("Unexpected keyword: " + token.getValue());
                };
            case PUNCTUATION:
                if (token.getValue().equals("(")) {
                    consume();
                    Node e = E();
                    expect(")");
                    return e;
                } else {
                    throw new RuntimeException("Unexpected punctuation: " + token.getValue());
                }
            default:
                throw new RuntimeException("Unexpected token in Rn: " + token);
        }
    }

    private Node D() {
        Node left = Da();
        if (peek().getValue().equals("within")) {
            consume();
            Node right = D();
            return new Node(NodeType.within, "within", left, right);
        }
        return left;
    }

    private Node Da() {
        List<Node> definitions = new ArrayList<>();
        definitions.add(Dr());
        while (peek().getValue().equals("and")) {
            consume();
            definitions.add(Dr());
        }
        return definitions.size() > 1 ? new Node(NodeType.and, "and", definitions) : definitions.get(0);
    }

    private Node Dr() {
        if (peek().getValue().equals("rec")) {
            consume();
            return new Node(NodeType.rec, "rec", Db());
        }
        return Db();
    }

    private Node Db() {
        if (peek().getValue().equals("(")) {
            consume();
            Node inner = D();
            expect(")");
            return inner;
        } else if (peek().getType() == TokenType.IDENTIFIER) {
            Token id = consume();
            if (peek().getType() == TokenType.IDENTIFIER || peek().getValue().equals("(")) {
                Node idNode = new Node(NodeType.identifier, id.getValue());
                List<Node> params = new ArrayList<>();
                while (peek().getType() == TokenType.IDENTIFIER || peek().getValue().equals("(")) {
                    params.add(Vb());
                }
                expect("=");
                Node body = E();
                params.add(0, idNode);
                params.add(body);
                return new Node(NodeType.fcn_form, "fcn_form", params);
            } else if (peek().getValue().equals("=")) {
                consume();
                Node expr = E();
                return new Node(NodeType.equal, "=", new Node(NodeType.identifier, id.getValue()), expr);
            }
        }
        throw new RuntimeException("Invalid Db");
    }

    private Node Vb() {
        if (peek().getValue().equals("(")) {
            consume();
            if (peek().getValue().equals(")")) {
                consume();
                return new Node(NodeType.empty_params, "()");
            } else {
                Node list = V1();
                expect(")");
                return list;
            }
        } else if (peek().getType() == TokenType.IDENTIFIER) {
            return new Node(NodeType.identifier, consume().getValue());
        }
        throw new RuntimeException("Invalid Vb");
    }

    private Node V1() {
        List<Node> vars = new ArrayList<>();
        vars.add(new Node(NodeType.identifier, consume().getValue()));
        while (peek().getValue().equals(",")) {
            consume();
            vars.add(new Node(NodeType.identifier, consume().getValue()));
        }
        if(vars.size()>=2) {
            return new Node(NodeType.comma, ",", vars);
        }
        else{
            return vars.getFirst();
        }
    }

    public static void main(String[] args) {
        LexicalAnalyser lex = new LexicalAnalyser("t1.txt");
        List<Token> passingtokens = lex.getTokens();
        Parser parser = new Parser(passingtokens);
        Node root = parser.parse();
        display(root, "");
    }

    public static void display(Node x, String prefix) {
        System.out.println(prefix + x.value);
        for (Node y : x.children) {
            String newprefix = prefix + ".";
            display(y, newprefix);
        }
    }
}
