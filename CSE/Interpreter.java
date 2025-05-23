package CSE;

import java.util.ArrayList;
import java.util.List;

import Exceptions.InterpreterException;
import Parser.Node;
import Parser.Parser;
import Standardizer.AST;
import Standardizer.ASTConstructor;
import Lexer.LexicalAnalyser;
import Lexer.Token;

public class Interpreter {

    /**

        Runs the program by reading the source file, lexing, parsing,
        standardizing and then interpreting it using a CSE machine.

        @param filename   The path to the source code file
        @param isPrintAST Whether to print the serialized AST
        @param isPrintST  Whether to print the standardized AST
        @return The final result of the program evaluation as a String

     **/

    public static String runProgram(String filename, boolean isPrintAST, boolean isPrintST){

        // Initialize lexical analyser with the input filename
        LexicalAnalyser scanner = new LexicalAnalyser(filename);
        List<Token> tokens;
        List<Node> AST;

        try {
            // Perform lexical analysis to get tokens
            tokens = scanner.scan();

            // If token list is empty, program is empty
            if(tokens.isEmpty()){
                System.out.println("Program is Empty");
                return "";
            }

            // Initialize parser with tokens and parse into AST nodes
            Parser parser = new Parser(tokens);
            AST = parser.parse();

            // Serialize the AST to list of strings for further processing
            ArrayList<String> stringAST = parser.serializeAST();

            // Print serialized AST if requested
            if(isPrintAST){
                for(String string : stringAST){
                    System.out.println(string);
                }
            }

            // Create ASTConstructor instance to generate abstract syntax tree object
            ASTConstructor astconstructor = new ASTConstructor();
            AST ast = astconstructor.getAbstractSyntaxTree(stringAST);

            // Standardize the AST for uniform structure
            ast.standardize();

            // Print standardized AST if requested
            if(isPrintST) ast.printAst();

            // Create a MachineConstructor (CSE machine factory)
            MachineConstructor csemfac = new MachineConstructor();

            // Generate CSE machine from the AST
            CSEMachine csemachine = csemfac.getCSEMachine(ast);

            // Return the answer/result of the evaluation from CSE machine
            return csemachine.getAnswer();

        } catch (InterpreterException e) {
            // Print custom exception message if any error occurs
            System.out.println(e.getMessage());
        }

        // Return null if evaluation fails
        return null;
    }
}
