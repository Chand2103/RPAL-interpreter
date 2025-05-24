/**
 * Control Stack Environment (CSE) Machine Implementation
 * 
 * This code implements a CSE machine which is an abstract machine
 * for evaluating functional programming languages. This processes expressions by maintaining three
 * components a control stack (expressions to evaluate), a value stack (computed results), and
 * environment chain (variable bindings), executing transition rules until the final result is computed.
 */

package CSE;

import Symbols.*;
import java.util.ArrayList;


public class CSEMachine {
    private ArrayList<Symbol> control;     // Control stack - contains expressions to evaluate
    private ArrayList<Symbol> stack;       // Value stack - contains computed values
    private ArrayList<E> environment;      // Environment chain - contains variable bindings

    public CSEMachine(ArrayList<Symbol> control, ArrayList<Symbol> stack, ArrayList<E> environment) {
        this.setControl(control);
        this.setStack(stack);
        this.setEnvironment(environment);
    }

    public void setControl(ArrayList<Symbol> control) {
        this.control = control;
    }

    public void setStack(ArrayList<Symbol> stack) {
        this.stack = stack;
    }

    public void setEnvironment(ArrayList<E> environment) {
        this.environment = environment;
    }

    /**
     * Main execution loop - processes control stack until empty
     * Implements all CSE machine transition rules
     */
    public void execute() {
        E currentEnvironment = this.environment.get(0);
        int j = 1; // Environment counter for creating new environments
        
        while (!control.isEmpty()) {
            // Pop the top symbol from control stack for processing
            Symbol currentSymbol = control.get(control.size()-1);
            control.remove(control.size()-1);
            
            // CSE RULE 1: Variable Lookup
            // If symbol is an identifier, look up its value in current environment
            // Transition: <Id, S, E> → <S[E(Id)], E>
            if (currentSymbol instanceof Id) {
                this.stack.add(0, currentEnvironment.lookup((Id) currentSymbol));
                
            // CSE RULE 2: Lambda Abstraction
            // Lambda expressions are closures - capture current environment
            // Transition: <λ, S, E> → <S[λ[E]], E>
            } else if (currentSymbol instanceof Lambda) {
                Lambda lambda = (Lambda) currentSymbol;
                lambda.setEnvironment(currentEnvironment.getIndex());
                this.stack.add(0, lambda);
                
            // CSE RULE 3: Gamma Application
            // Function application - handles multiple cases based on operator type
            } else if (currentSymbol instanceof Gamma) {
                Symbol nextSymbol = this.stack.get(0); // Get function/operator from stack
                this.stack.remove(0);
                
                // CSE RULE 4: Lambda Application (Single Parameter)
                // CSE RULE 11: Lambda Application (Multiple Parameters - Tuple)
                // Apply lambda function by creating new environment with parameter bindings
                // Transition: <γ, S[λ[k]][v], Ek> → <Ej, S[Ej], Ej+E_lambda>
                if (nextSymbol instanceof Lambda) {
                    Lambda lambda = (Lambda) nextSymbol;
                    E e = new E(j++);
                    
                    // Single parameter binding
                    if (lambda.identifiers.size() == 1) {
                        e.values.put(lambda.identifiers.get(0), this.stack.get(0));
                        this.stack.remove(0);
                    } else {
                        // Multiple parameter binding via tuple destructuring
                        Tup tup = (Tup) this.stack.get(0);
                        this.stack.remove(0);
                        int i = 0;
                        for (Id id: lambda.identifiers) {
                            e.values.put(id, tup.symbols.get(i++));
                        }
                    }
                    
                    // Link new environment to lambda's captured environment
                    for (E environment: this.environment) {
                        if (environment.getIndex() == lambda.getEnvironment()) {
                            e.setParent(environment);
                        }
                    }
                    
                    currentEnvironment = e;
                    this.control.add(e);                    // Add environment marker to control
                    this.control.add(lambda.getDelta());    // Add lambda body to control
                    this.stack.add(0, e);                   // Push environment to stack
                    this.environment.add(e);                // Add to environment chain
                    
                // CSE RULE 10: Tuple Selection
                // Select nth element from tuple using integer index
                // Transition: <γ, S[τ][n], E> → <S[τ_n], E>
                } else if (nextSymbol instanceof Tup) {
                    Tup tup = (Tup) nextSymbol;
                    int i = Integer.parseInt(this.stack.get(0).getData());
                    this.stack.remove(0);
                    this.stack.add(0, tup.symbols.get(i-1)); // 1-based indexing
                    
                // CSE RULE 12: Y* Combinator (Fixed Point)
                // Implements recursive function definitions
                // Transition: <γ, S[Y*][λ], E> → <S[η], E>
                } else if (nextSymbol instanceof Ystar) {
                    Lambda lambda = (Lambda) this.stack.get(0);
                    this.stack.remove(0);
                    Eta eta = new Eta();
                    eta.setIndex(lambda.getIndex());
                    eta.setEnvironment(lambda.getEnvironment());
                    eta.setIdentifier(lambda.identifiers.get(0));
                    eta.setLambda(lambda);
                    this.stack.add(0, eta);
                    
                // CSE RULE 13: Eta Reduction
                // Handle recursive function calls through eta abstraction
                // Transition: <γ, S[η][v], E> → <γγ, S[η][λ][v], E>
                } else if (nextSymbol instanceof Eta) {
                    Eta eta = (Eta) nextSymbol;
                    Lambda lambda = eta.getLambda();
                    this.control.add(new Gamma());
                    this.control.add(new Gamma());
                    this.stack.add(0, eta);
                    this.stack.add(0, lambda);
                    

                // Handle primitive operations and predicates
                } else {
                    if ("Print".equals(nextSymbol.getData())) {
                        // Print function - output value (implementation specific)
                        
                    } else if ("Stem".equals(nextSymbol.getData())) {
                        // Get first character of string
                        Symbol s = this.stack.get(0);
                        this.stack.remove(0);
                        s.setData(s.getData().substring(0, 1));
                        this.stack.add(0, s);
                        
                    } else if ("Stern".equals(nextSymbol.getData())) {
                        // Get string without first character
                        Symbol s = this.stack.get(0);
                        this.stack.remove(0);
                        s.setData(s.getData().substring(1));
                        this.stack.add(0, s);
                        
                    } else if ("Conc".equals(nextSymbol.getData())) {
                        // Concatenate two strings
                        Symbol s1 = this.stack.get(0);
                        Symbol s2 = this.stack.get(1);
                        this.stack.remove(0);
                        this.stack.remove(0);
                        s1.setData(s1.getData() + s2.getData());
                        this.stack.add(0, s1);
                        
                    } else if ("Order".equals(nextSymbol.getData())) {
                        // Get tuple size/order
                        Tup tup = (Tup) this.stack.get(0);
                        this.stack.remove(0);
                        Int n = new Int(Integer.toString(tup.symbols.size()));
                        this.stack.add(0, n);
                        
                    } else if ("Null".equals(nextSymbol.getData())) {
                        // Check if tuple is empty
                        Tup tup = (Tup) this.stack.get(0);
                        this.stack.remove(0);
                        this.stack.add(0, new Bool(Boolean.toString(tup.symbols.isEmpty())));
                        
                    } else if ("Itos".equals(nextSymbol.getData())) {
                        // Convert integer to string
                        Symbol s = this.stack.get(0);
                        this.stack.remove(0);
                        if (s instanceof Int) {
                            this.stack.add(0, new Str(s.getData()));
                        } else {
                            this.stack.add(0, new Err());
                        }
                        
                    } else if ("Isinteger".equals(nextSymbol.getData())) {
                        // Type predicate: check if value is integer
                        if (this.stack.get(0) instanceof Int) {
                            this.stack.add(0, new Bool("true"));
                        } else {
                            this.stack.add(0, new Bool("false"));
                        }
                        this.stack.remove(1);
                        
                    } else if ("Isstring".equals(nextSymbol.getData())) {
                        // Type predicate: check if value is string
                        if (this.stack.get(0) instanceof Str) {
                            this.stack.add(0, new Bool("true"));
                        } else {
                            this.stack.add(0, new Bool("false"));
                        }
                        this.stack.remove(1);
                        
                    } else if ("Istuple".equals(nextSymbol.getData())) {
                        // Type predicate: check if value is tuple
                        if (this.stack.get(0) instanceof Tup) {
                            this.stack.add(0, new Bool("true"));
                        } else {
                            this.stack.add(0, new Bool("false"));
                        }
                        this.stack.remove(1);
                        
                    } else if ("Isdummy".equals(nextSymbol.getData())) {
                        // Type predicate: check if value is dummy
                        if (this.stack.get(0) instanceof Dummy) {
                            this.stack.add(0, new Bool("true"));
                        } else {
                            this.stack.add(0, new Bool("false"));
                        }
                        this.stack.remove(1);
                        
                    } else if ("Istruthvalue".equals(nextSymbol.getData())) {
                        // Type predicate: check if value is boolean
                        if (this.stack.get(0) instanceof Bool) {
                            this.stack.add(0, new Bool("true"));
                        } else {
                            this.stack.add(0, new Bool("false"));
                        }
                        this.stack.remove(1);
                        
                    } else if ("Isfunction".equals(nextSymbol.getData())) {
                        // Type predicate: check if value is function
                        if (this.stack.get(0) instanceof Lambda) {
                            this.stack.add(0, new Bool("true"));
                        } else {
                            this.stack.add(0, new Bool("false"));
                        }
                        this.stack.remove(1);
                    }
                }
                
            // CSE RULE 5: Environment Removal
            // Remove environment from stack and restore previous environment
            // Transition: <E, S[E][v], E'> → <S[v], E_prev>
            } else if (currentSymbol instanceof E) {
                this.stack.remove(1); // Remove environment from stack
                
                // Mark current environment as removed
                this.environment.get(((E) currentSymbol).getIndex()).setIsRemoved(true);
                
                // Find most recent non-removed environment
                int y = this.environment.size();
                while (y > 0) {
                    if (!this.environment.get(y-1).getIsRemoved()) {
                        currentEnvironment = this.environment.get(y-1);
                        break;
                    } else {
                        y--;
                    }
                }
                
            // CSE RULE 6: Unary Operator Application  
            // CSE RULE 7: Binary Operator Application
            // Apply primitive operators to operands
            } else if (currentSymbol instanceof Rator) {
                if (currentSymbol instanceof Uop) {
                    // Unary operation: <uop, S[v], E> → <S[uop(v)], E>
                    Symbol rator = currentSymbol;
                    Symbol rand = this.stack.get(0);
                    this.stack.remove(0);
                    stack.add(0, this.applyUnaryOperation(rator, rand));
                }
                if (currentSymbol instanceof Bop) {
                    // Binary operation: <bop, S[v2][v1], E> → <S[bop(v1,v2)], E>
                    Symbol rator = currentSymbol;
                    Symbol rand1 = this.stack.get(0);
                    Symbol rand2 = this.stack.get(1);
                    this.stack.remove(0);
                    this.stack.remove(0);
                    this.stack.add(0, this.applyBinaryOperation(rator, rand1, rand2));
                }
                
            // CSE RULE 8: Conditional Evaluation (Beta)
            // Select branch based on boolean condition
            // Transition: <β, S[b], E> with controls [then, else] → execute selected branch
            } else if (currentSymbol instanceof Beta) {
                if (Boolean.parseBoolean(this.stack.get(0).getData())) {
                    // Condition is true - remove else branch, execute then branch
                    this.control.remove(control.size()-1);
                } else {
                    // Condition is false - remove then branch, execute else branch  
                    this.control.remove(control.size()-2);
                }
                this.stack.remove(0); // Remove boolean condition from stack
                
            // CSE RULE 9: Tuple Construction (Tau)
            // Create tuple from n values on stack
            // Transition: <τn, S[v1]...[vn], E> → <S[(v1,...,vn)], E>
            } else if (currentSymbol instanceof Tau) {
                Tau tau = (Tau) currentSymbol;
                Tup tup = new Tup();
                for (int i = 0; i < tau.getN(); i++) {
                    tup.symbols.add(this.stack.get(0));
                    this.stack.remove(0);
                }
                this.stack.add(0, tup);
                

            // Expand delta (function body) into control stack
            } else if (currentSymbol instanceof Delta) {
                this.control.addAll(((Delta) currentSymbol).symbols);
                
 
            // Expand conditional branch into control stack
            } else if (currentSymbol instanceof B) {
                this.control.addAll(((B) currentSymbol).symbols);
                

            // Push literal values (integers, strings, booleans) directly to stack
            // Transition: <literal, S, E> → <S[literal], E>
            } else {
                this.stack.add(0, currentSymbol);
            }
        }
    }

    /**
     * Debug method: Print current control stack state
     */
    public void printControl() {
        System.out.print("Control: ");
        for (Symbol symbol: this.control) {
            System.out.print(symbol.getData());
            if (symbol instanceof Lambda) {
                System.out.print(((Lambda) symbol).getIndex());
            } else if (symbol instanceof Delta) {
                System.out.print(((Delta) symbol).getIndex());
            } else if (symbol instanceof E) {
                System.out.print(((E) symbol).getIndex());
            } else if (symbol instanceof Eta) {
                System.out.print(((Eta) symbol).getIndex());
            }
            System.out.print(",");
        }
        System.out.println();
    }

    /**
     * Debug method: Print current value stack state
     */
    public void printStack() {
        System.out.print("Stack: ");
        for (Symbol symbol: this.stack) {
            System.out.print(symbol.getData());
            if (symbol instanceof Lambda) {
                System.out.print(((Lambda) symbol).getIndex());
            } else if (symbol instanceof Delta) {
                System.out.print(((Delta) symbol).getIndex());
            } else if (symbol instanceof E) {
                System.out.print(((E) symbol).getIndex());
            } else if (symbol instanceof Eta) {
                System.out.print(((Eta) symbol).getIndex());
            }
            System.out.print(",");
        }
        System.out.println();
    }

    /**
     * Debug method: Print environment chain structure
     */
    public void printEnvironment() {
        for (Symbol symbol: this.environment) {
            System.out.print("e"+((E) symbol).getIndex()+ " --> ");
            if (((E) symbol).getIndex()!=0) {
                System.out.println("e"+((E) symbol).getParent().getIndex());
            } else {
                System.out.println();
            }
        }
    }

    /**
     * Apply unary operations (negation, logical not)
     * Supports: neg (arithmetic negation), not (logical negation)
     */
    public Symbol applyUnaryOperation(Symbol rator, Symbol rand) {
        if ("neg".equals(rator.getData())) {
            int val = Integer.parseInt(rand.getData());
            return new Int(Integer.toString(-1*val));
        } else if ("not".equals(rator.getData())) {
            boolean val = Boolean.parseBoolean(rand.getData());
            return new Bool(Boolean.toString(!val));
        } else {
            return new Err();
        }
    }

    /**
     * Apply binary operations (arithmetic, logical, comparison, tuple operations)
     * Supports: +, -, *, /, **, &, or, eq, ne, ls, le, gr, ge, aug
     */
    public Symbol applyBinaryOperation(Symbol rator, Symbol rand1, Symbol rand2) {
        if ("+".equals(rator.getData())) {
            int val1 = Integer.parseInt(rand1.getData());
            int val2 = Integer.parseInt(rand2.getData());
            return new Int(Integer.toString(val1+val2));
        } else if ("-".equals(rator.getData())) {
            int val1 = Integer.parseInt(rand1.getData());
            int val2 = Integer.parseInt(rand2.getData());
            return new Int(Integer.toString(val1-val2));
        } else if ("*".equals(rator.getData())) {
            int val1 = Integer.parseInt(rand1.getData());
            int val2 = Integer.parseInt(rand2.getData());
            return new Int(Integer.toString(val1*val2));
        } else if ("/".equals(rator.getData())) {
            int val1 = Integer.parseInt(rand1.getData());
            int val2 = Integer.parseInt(rand2.getData());
            return new Int(Integer.toString(val1/val2));
        } else if ("**".equals(rator.getData())) {
            int val1 = Integer.parseInt(rand1.getData());
            int val2 = Integer.parseInt(rand2.getData());
            return new Int(Integer.toString((int) Math.pow(val1, val2)));
        } else if ("&".equals(rator.getData())) {
            boolean val1 = Boolean.parseBoolean(rand1.getData());
            boolean val2 = Boolean.parseBoolean(rand2.getData());
            return new Bool(Boolean.toString(val1 && val2));
        } else if ("or".equals(rator.getData())) {
            boolean val1 = Boolean.parseBoolean(rand1.getData());
            boolean val2 = Boolean.parseBoolean(rand2.getData());
            return new Bool(Boolean.toString(val1 || val2));
        } else if ("eq".equals(rator.getData())) {
            String val1 = rand1.getData();
            String val2 = rand2.getData();
            return new Bool(Boolean.toString(val1.equals(val2)));
        } else if ("ne".equals(rator.getData())) {
            String val1 = rand1.getData();
            String val2 = rand2.getData();
            return new Bool(Boolean.toString(!val1.equals(val2)));
        } else if ("ls".equals(rator.getData())) {
            int val1 = Integer.parseInt(rand1.getData());
            int val2 = Integer.parseInt(rand2.getData());
            return new Bool(Boolean.toString(val1 < val2));
        } else if ("le".equals(rator.getData())) {
            int val1 = Integer.parseInt(rand1.getData());
            String s1=rand2.getData();
            int val2 = Integer.parseInt(s1);
            return new Bool(Boolean.toString(val1 <= val2));
        } else if ("gr".equals(rator.getData())) {
            int val1 = Integer.parseInt(rand1.getData());
            int val2 = Integer.parseInt(rand2.getData());
            return new Bool(Boolean.toString(val1 > val2));
        } else if ("ge".equals(rator.getData())) {
            int val1 = Integer.parseInt(rand1.getData());
            int val2 = Integer.parseInt(rand2.getData());
            return new Bool(Boolean.toString(val1 >= val2));
        } else if ("aug".equals(rator.getData())) {
            // Tuple augmentation - add element to tuple
            if (rand2 instanceof Tup) {
                ((Tup) rand1).symbols.addAll(((Tup) rand2).symbols);
            } else {
                ((Tup) rand1).symbols.add(rand2);
            }
            return rand1;
        } else {
            return new Err();
        }
    }

    /**
     * Convert tuple to string representation for output
     */
    public String getTupleValue(Tup tup) {
        String temp = "(";
        for (Symbol symbol: tup.symbols) {
            if (symbol instanceof Tup) {
                temp = temp + this.getTupleValue((Tup) symbol) + ", ";
            } else {
                temp = temp + symbol.getData() + ", ";
            }
        }
        temp = temp.substring(0, temp.length()-2) + ")";
        return temp;
    }

    /**
     * Execute the CSE machine and return final result
     */
    public String getAnswer() {
        this.execute();
        if (stack.get(0) instanceof Tup) {
            return this.getTupleValue((Tup) stack.get(0));
        }
        return stack.get(0).getData();
    }
}
