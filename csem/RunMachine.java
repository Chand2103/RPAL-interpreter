package csem;

import Parser.Parser;
import Parser.Node;
import Parser.NodeType;
import Standardizer.Standardizer;
import lexer.LexicalAnalyser;
import lexer.Token;

import java.util.List;

public class RunMachine {
    public static void main(String[] args) {
        LexicalAnalyser lex = new LexicalAnalyser("t2.txt");
        List<Token> passingtokens = lex.getTokens();
        Parser parser = new Parser(passingtokens);
        Node root = parser.parse();

        Standardizer standardizer = new Standardizer(root);
        Node standardizedRoot = standardizer.getRoot();

        CSEMachine.evaluate(standardizedRoot);
    }
}

