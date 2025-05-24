package Parser;

import Lexer.Token;
import Lexer.TokenType;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;


/**

 Parser for constructing an Abstract Syntax Tree (AST) from a list of tokens.
 Supports recursive descent parsing based on grammar rules .
 Outputs a node-based AST and an indented string representation.

 **/


public class Parser {
    private List<Token> tokens;
    private List<Node> AST; // Last element will be root of the tree
    private ArrayList<String> stringAST;

    public Parser(List<Token> tokens) {
        this.tokens=tokens;
        AST = new ArrayList<>();
        stringAST = new ArrayList<>();
    }

    public List<Node> parse(){
        tokens.add(new Token(TokenType.EndOfTokens,""));
        E();
        if(tokens.get(0).type.equals(TokenType.EndOfTokens)) {
            return AST;
        }
        else {
            System.out.println("Error: Parsing could not be completed.");
            System.out.println("Unprocessed tokens found:");
            for (Token token : tokens) {
                System.out.println("<" + token.type + ", " + token.value + ">");
            }
            return null;
        }
    }

    /**

     Converts the internal Abstract Syntax Tree (AST) into a serialized list of strings.
     Each node is represented as a string with indentation reflecting the tree structure.
     Uses a stack-based approach to traverse the AST nodes and formats them into a human-readable form.
     Finally, reverses the resulting list to present the AST in the correct order.
     @return A list of strings representing the serialized AST.

     **/

    public ArrayList<String> serializeAST(){
        String dots = "";
        List<Node> stack= new ArrayList<Node>();

        while(!AST.isEmpty()) {
            if(stack.isEmpty()) {
                if(AST.get(AST.size()-1).noOfChildren==0) {
                    formatAndAddNode(dots,AST.remove(AST.size()-1));
                }
                else {
                    Node node = AST.remove(AST.size()-1);
                    stack.add(node);
                }
            }
            else {
                if(AST.get(AST.size()-1).noOfChildren>0) {
                    Node node = AST.remove(AST.size()-1);
                    stack.add(node);
                    dots += ".";
                }
                else {
                    stack.add(AST.remove(AST.size()-1));
                    dots += ".";
                    while(stack.get(stack.size()-1).noOfChildren==0) {
                        formatAndAddNode(dots,stack.remove(stack.size()-1));
                        if(stack.isEmpty()) break;
                        dots = dots.substring(0, dots.length() - 1);
                        Node node =stack.remove(stack.size()-1);
                        node.noOfChildren--;
                        stack.add(node);

                    }
                }

            }
        }

        // Reverse the list
        Collections.reverse(stringAST);
        return stringAST;
    }

    /**

     Formats a single AST node into a string representation and adds it to the output list.
     @param dots Indentation string representing the current node's depth in the AST
     @param node The AST node to format and add

     **/

    void formatAndAddNode(String dots, Node node) {
        switch(node.type) {
            case identifier:
                stringAST.add(dots+"<ID:"+node.value+">");
                break;
            case integer:
                stringAST.add(dots+"<INT:"+node.value+">");
                break;
            case string:
                stringAST.add(dots+"<STR:"+node.value+">");
                break;
            case true_value:
                stringAST.add(dots+"<"+node.value+">");
                break;
            case false_value:
                stringAST.add(dots+"<"+node.value+">");
                break;
            case nil:
                stringAST.add(dots+"<"+node.value+">");
                break;
            case dummy:
                stringAST.add(dots+"<"+node.value+">");
                break;
            case fcn_form:
                stringAST.add(dots+"function_form");
                break;
            default :
                stringAST.add(dots+node.value);
        }
    }


    /**

      ==================== Expressions ====================
      E  → 'let' D 'in' E
         | 'fn' Vb+ '.' E
         | Ew

     **/

    void E() {
        int n=0;
        Token token=tokens.get(0);
        if(token.type.equals(TokenType.KEYWORD) && Arrays.asList("let", "fn").contains(token.value)) {
            if(token.value.equals("let")) {
                tokens.remove(0);
                D();
                if(!tokens.get(0).value.equals("in")) {
                    System.out.println("Parsing Failed: Error at E — expected 'in'.");
                }
                tokens.remove(0);
                E();
                AST.add(new Node(NodeType.let,"let",2));

            }
            else {
                tokens.remove(0); // Remove fn
                do {
                    Vb();
                    n++;
                } while(tokens.get(0).type.equals(TokenType.IDENTIFIER) || tokens.get(0).value.equals("("));
                if(!tokens.get(0).value.equals(".")) {
                    System.out.println("Parsing Failed: Error at E — expected '.'.");
                }
                tokens.remove(0);
                E();
                AST.add(new Node(NodeType.lambda,"lambda",n+1));
            }
        }
        else
            Ew();
    }

    /**

     ==================== Expressions ====================
     Ew → T 'where' Dr
         | T

     **/

    void Ew() {
        T();
        if(tokens.get(0).value.equals("where")){
            tokens.remove(0); // Remove where
            Dr();
            AST.add(new Node(NodeType.where,"where",2));
        }

    }

    /**

     ==================== Tuple Expressions ====================
     T  → Ta (',' Ta)+
        | Ta

     **/

    void T() {
        Ta();
        int n = 1;
        while (tokens.get(0).value.equals(",")){
              tokens.remove(0); // Remove comma
            Ta();
            ++n;
        }
        if (n > 1) {
            AST.add(new Node(NodeType.tau,"tau",n));
        }
    }

    /**

     ==================== Tuple Expressions ====================
     Ta → Ta 'aug' Tc
        | Tc

     **/

    void Ta(){
        Tc();
        while(tokens.get(0).value.equals("aug")){
            tokens.remove(0); //Remove aug
            Tc();
            AST.add(new Node(NodeType.aug,"aug",2));
        }
    }

    /**

     ==================== Tuple Expressions ====================
     Tc → B '->' Tc '|' Tc
         | B

     **/

    void Tc(){
        B();
        if(tokens.get(0).value.equals("->")){
            // System.out.println(tokens.get(0).value);
            tokens.remove(0); // Remove '->'
            Tc();
            if(!tokens.get(0).value.equals("|")){
                System.out.println("Parsing Failed: Error at Tc — expected '|'.");
            }
            tokens.remove(0); //Remove '|'
            Tc();
            AST.add(new Node(NodeType.conditional,"->",3));
        }
    }

    /**

     ==================== Boolean Expressions ====================
     B  → B 'or' Bt
        | Bt

     **/

    void B(){
        Bt();
        while(tokens.get(0).value.equals("or")){
            tokens.remove(0); //Remove 'or'
            Bt();
            AST.add(new Node(NodeType.op_or,"or",2));
        }
    }

    /**

     ==================== Boolean Expressions ====================
     Bt → Bt '&' Bs
        | Bs

     **/

    void Bt(){
        Bs();
        while(tokens.get(0).value.equals("&")){
            tokens.remove(0); //Remove '&'
            Bs();
            AST.add(new Node(NodeType.op_and,"&",2));
        }
    }

    /**

     ==================== Boolean Expressions ====================
     Bs → 'not' Bp
        | Bp

     **/

    void Bs(){
        if(tokens.get(0).value.equals("not")){
            tokens.remove(0); //Remove 'not'
            Bp();
            AST.add(new Node(NodeType.op_not,"not",1));
        }
        else Bp();
    }

    /**

     ==================== Boolean Expressions ====================
     Bp → A ('gr' | '>')
        | A ('ge' | '>=')
        | A ('ls' | '<')
        | A ('le' | '<=')
        | A 'eq' A
        | A 'ne' A
        | A

     **/

    void Bp() {
        A();
        Token token = tokens.get(0);
        if(Arrays.asList(">", ">=", "<", "<=").contains(token.value)
                || Arrays.asList("gr", "ge", "ls", "le", "eq", "ne").contains(token.value)){
            tokens.remove(0);
            A();
            switch(token.value){
                case ">":
                    AST.add(new Node(NodeType.op_compare,"gr",2));
                    break;
                case ">=":
                    AST.add(new Node(NodeType.op_compare,"ge",2));
                    break;
                case "<":
                    AST.add(new Node(NodeType.op_compare,"ls",2));
                    break;
                case "<=":
                    AST.add(new Node(NodeType.op_compare,"le",2));
                    break;
                default:
                    AST.add(new Node(NodeType.op_compare,token.value,2));
                    break;
            }
        }
    }

    /**

     ==================== Arithmetic Expressions ====================
     A → A '+' At
       | A '-' At
       | '+' At
       | '-' At
       | At

     **/

    void A(){
        if (tokens.get(0).value.equals("+")) {
            tokens.remove(0); //Remove unary plus
            At();
        } else if (tokens.get(0).value.equals("-")) {
            tokens.remove(0); // Remove unary minus
            At();
            AST.add(new Node(NodeType.op_neg,"neg",1));
        } else {
            At();
        }
        while (Arrays.asList("+", "-").contains(tokens.get(0).value)) {
            Token currentToken = tokens.get(0); //save present token
            tokens.remove(0); // Remove plus or minus operators
            At();
            if(currentToken.value.equals("+")) AST.add(new Node(NodeType.op_plus,"+",2));
            else AST.add(new Node(NodeType.op_minus,"-",2));
        }

    }

    /**

     ==================== Arithmetic Expressions ====================
     At → At '*' Af
       | At '/' Af
       | Af

     **/

    void At(){
        Af();
        while(Arrays.asList("*", "/").contains(tokens.get(0).value)){
            Token currentToken = tokens.get(0); //save present token
            tokens.remove(0); // Remove multiply or divide operators
            Af();
            if(currentToken.value.equals("*")) AST.add(new Node(NodeType.op_mul,"*",2));
            else AST.add(new Node(NodeType.op_div,"/",2));
        }
    }

    /**

     ==================== Arithmetic Expressions ====================
     Af → Ap '**' Af
        | Ap

     **/

    void Af(){
        Ap();
        if(tokens.get(0).value.equals("**")){
            tokens.remove(0); //Remove power operator
            Af();
            AST.add(new Node(NodeType.op_pow,"**",2));
        }
    }

    /**

     ==================== Arithmetic Expressions ====================
     Ap → Ap '@' '<IDENTIFIER>' R
        | R

     **/

    void Ap(){
        R();
        while(tokens.get(0).value.equals("@")){
            tokens.remove(0); //Remove @

            if(!tokens.get(0).type.equals(TokenType.IDENTIFIER)){
                System.out.println("Parsing Failed: Error at Ap — expected IDENTIFIER.");
            }
            AST.add(new Node(NodeType.identifier,tokens.get(0).value,0));
            tokens.remove(0); // Remove IDENTIFIER

            R();
            AST.add(new Node(NodeType.at,"@",3));
        }
    }

    /**

     ==================== Rators And Rands ====================
     R → R Rn
       | Rn

     **/

    void R(){
        Rn();
        while((Arrays.asList(TokenType.IDENTIFIER, TokenType.INTEGER, TokenType.STRING).contains(tokens.get(0).type))
                ||(Arrays.asList("true", "false", "nil", "dummy").contains(tokens.get(0).value))
                ||(tokens.get(0).value.equals("("))) {

            Rn();
            AST.add(new Node(NodeType.gamma,"gamma",2));
        }
    }

    /**

     ==================== Rators And Rands ====================
     Rn → '<IDENTIFIER>'
        | '<INTEGER>'
        | '<STRING>'
        | 'true'
        | 'false'
        | 'nil'
        | '(' E ')'
        | 'dummy'

     **/

    void Rn() {
        switch (tokens.get(0).type) {
            case IDENTIFIER:
                AST.add(new Node(NodeType.identifier, tokens.get(0).value, 0));
                tokens.remove(0);
                break;
            case INTEGER:
                AST.add(new Node(NodeType.integer, tokens.get(0).value, 0));
                tokens.remove(0);
                break;
            case STRING:
                AST.add(new Node(NodeType.string, tokens.get(0).value, 0));
                tokens.remove(0);
                break;
            case KEYWORD:
                switch (tokens.get(0).value) {
                    case "true":
                        AST.add(new Node(NodeType.true_value, tokens.get(0).value, 0));
                        tokens.remove(0);
                        break;
                    case "false":
                        AST.add(new Node(NodeType.false_value, tokens.get(0).value, 0));
                        tokens.remove(0);
                        break;
                    case "nil":
                        AST.add(new Node(NodeType.nil, tokens.get(0).value, 0));
                        tokens.remove(0);
                        break;
                    case "dummy":
                        AST.add(new Node(NodeType.dummy, tokens.get(0).value, 0));
                        tokens.remove(0);
                        break;
                    default:
                        System.out.println("Parsing Failed: Error at Rn — unexpected keyword encountered.");
                        break;
                }
                break;
            case PUNCTUATION:
                if (tokens.get(0).value.equals("(")) {
                    tokens.remove(0); // Remove '('
                    E();
                    if (!tokens.get(0).value.equals(")")) {
                        System.out.println("Parsing Failed: Error at Rn — expected matching ')'.");
                    }
                    tokens.remove(0); // Remove ')'
                } else {
                    System.out.println("Parsing Failed: Error at Rn — unexpected punctuation.");
                }
                break;
            default:
                System.out.println("Parsing Failed: Error at Rn — unexpected token type.");
                break;
        }
    }


    /**

     ==================== Definitions ====================
     D → Da 'within' D
       | Da

     **/

    void D(){
        Da();
        if(tokens.get(0).value.equals("within")){
            tokens.remove(0); //Remove 'within'
            D();
            AST.add(new Node(NodeType.within,"within",2));
        }
    }

    /**

     ==================== Definitions ====================
     Da → Dr ('and' Dr)+
        | Dr

     **/

    void Da(){
        Dr();
        int n = 1;
        while(tokens.get(0).value.equals("and")){
            tokens.remove(0);
            Dr();
            n++;
        }
        if(n>1) AST.add(new Node(NodeType.and,"and",n));
    }

    /**

     ==================== Definitions ====================
     Dr → 'rec' Db
        | Db

     **/

    void Dr(){
        boolean isRec = false;
        if(tokens.get(0).value.equals("rec")){
            tokens.remove(0);
            isRec = true;
        }
        Db();
        if (isRec) {
            AST.add(new Node(NodeType.rec,"rec",1));
        }
    }

    /**

     ==================== Definitions ====================
     Db → Vl '=' E
        | '<IDENTIFIER>' Vb+ '=' E
        | '(' D ')'

     **/

    void Db() {
        if (tokens.get(0).type.equals(TokenType.PUNCTUATION) && tokens.get(0).value.equals("(")) {
            tokens.remove(0);
            D();
            if (!tokens.get(0).value.equals(")")) {
                System.out.println("Parsing Failed: Error at Db #1 — expected closing ')'.");
            }
            tokens.remove(0);
        }
        else if (tokens.get(0).type.equals(TokenType.IDENTIFIER)) {
            if (tokens.get(1).value.equals("(") || tokens.get(1).type.equals(TokenType.IDENTIFIER)) { // Expect a fcn_form
                AST.add(new Node(NodeType.identifier, tokens.get(0).value, 0));
                tokens.remove(0); // Remove ID

                int n = 1; // Identifier child count
                do {
                    Vb();
                    n++;
                } while (tokens.get(0).type.equals(TokenType.IDENTIFIER) || tokens.get(0).value.equals("("));

                if (!tokens.get(0).value.equals("=")) {
                    System.out.println("Parsing Failed: Error at Db #2 — expected '='.");
                }
                tokens.remove(0);
                E();

                AST.add(new Node(NodeType.fcn_form, "fcn_form", n + 1));
            }
            else if (tokens.get(1).value.equals("=")) {
                AST.add(new Node(NodeType.identifier, tokens.get(0).value, 0));
                tokens.remove(0); // Remove identifier
                tokens.remove(0); // Remove '='
                E();
                AST.add(new Node(NodeType.equal, "=", 2));
            }
            else if (tokens.get(1).value.equals(",")) {
                Vl();
                if (!tokens.get(0).value.equals("=")) {
                    System.out.println("Parsing Failed: Error at Db — expected '=' after variable list.");
                }
                tokens.remove(0);
                E();

                AST.add(new Node(NodeType.equal, "=", 2));
            }
            else {
                System.out.println("Parsing Failed: Error at Db — unexpected token sequence.");
            }
        }
        else {
            System.out.println("Parsing Failed: Error at Db — expected identifier or '('.");
        }
    }


    /**

     ==================== Variables ====================
     Vb → '<IDENTIFIER>'
        | '(' Vl ')'
        | '()'

     **/

    void Vb() {
        if (tokens.get(0).type.equals(TokenType.PUNCTUATION) && tokens.get(0).value.equals("(")) {
            tokens.remove(0);
            boolean isVl = false;

            if (tokens.get(0).type.equals(TokenType.IDENTIFIER)) {
                Vl();
                isVl = true;
            }
            if (!tokens.get(0).value.equals(")")) {
                System.out.println("Parsing Failed: Error at Vb — unmatched ')'.");
            }
            tokens.remove(0);
            if (!isVl) {
                AST.add(new Node(NodeType.empty_params, "()", 0));
            }
        } else if (tokens.get(0).type.equals(TokenType.IDENTIFIER)) {
            AST.add(new Node(NodeType.identifier, tokens.get(0).value, 0));
            tokens.remove(0);
        }
    }


    /**

     ==================== Variables ====================
     Vl → '<IDENTIFIER>' list ','

     **/

    void Vl() {
        int n = 0;
        do {
            if (n > 0) {
                tokens.remove(0); // remove comma
            }
            if (!tokens.get(0).type.equals(TokenType.IDENTIFIER)) {
                System.out.println("Parsing Failed: Error at Vl — expected an identifier.");
            }
            AST.add(new Node(NodeType.identifier, tokens.get(0).value, 0));
            tokens.remove(0);
            n++;
        } while (tokens.get(0).value.equals(","));

        // Only create comma node if there are multiple identifiers
        if (n > 1) {
            AST.add(new Node(NodeType.comma, ",", n));
        }
    }
}