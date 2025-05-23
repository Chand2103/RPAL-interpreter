// This code retrieves command-line arguments passed to the program (e.g., java Myrpal -st, -ast, filename),
// and configures the interpreter accordingly by setting the isPrintSt, isPrintAst flags, and the filename (fn).


import CSE.Interpreter;

public class myrpal {

    public static void main(String[] args) {
        String fn;
        boolean isPrintAST=false,isPrintST=false;
        if(args.length==0){
            fn = "t1.txt";
            isPrintAST = true;
            isPrintST = true;
        }
        else if(args.length==3 && (
                (args[0].equalsIgnoreCase("-ast") && args[1].equalsIgnoreCase("-st")) ||
                        (args[0].equalsIgnoreCase("-st") && args[1].equalsIgnoreCase("-ast"))
        )
        )
        {
            fn = args[2];
            isPrintAST=true;
            isPrintST=true;
            Interpreter.runProgram(fn,isPrintAST,isPrintST);
        }
        else if(args.length==2){
            fn=args[1];
            if(args[0].equalsIgnoreCase("-ast")){
                isPrintAST=true;
                Interpreter.runProgram(fn,isPrintAST,isPrintST);
            }
            else if(args[0].equalsIgnoreCase("-st")){
                isPrintST=true;
                Interpreter.runProgram(fn,isPrintAST,isPrintST);
            }
            else{
                System.out.println("Invalid Arguments Passing!");
                return;
            }
        }
        else if(args.length==1){
            fn = args[0];
            System.out.println(Interpreter.runProgram(fn,isPrintAST,isPrintST)); 
        }
        else{
            System.out.println("Invalid Arguments Passing!");
            return;
        }                               
    }
}
