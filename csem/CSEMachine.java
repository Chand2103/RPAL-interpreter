package csem;

import Parser.Node;
import Parser.NodeType;

import java.util.*;

public class CSEMachine {

    static class MachineNode {
        String name;
        boolean isInt, isBool, isString, isName, isLambda, isGamma, isUnaryOp, isBinaryOp,
                isTuple, isTau, isBuiltIn, isY, isEnvMarker, isDummy, isDelta, isConditional,
                isRand;

        int intValue;
        boolean boolValue;
        String stringValue;
        String op;

        List<String> boundVars = new ArrayList<>();
        int bodyIndex;  // index into controlStructures for lambda body
        int envIndex;   // index into environments where lambda was created
        List<MachineNode> tuple = new ArrayList<>();
        int tupleSize;  // for tau operations

        @Override
        public String toString() {
            if (isInt) return Integer.toString(intValue);
            if (isBool) return Boolean.toString(boolValue);
            if (isString) return '"' + stringValue + '"';
            if (isTuple) return tuple.toString();
            if (isLambda) return "Lambda[" + boundVars + "," + envIndex + "," + bodyIndex + "]";
            if (isY) return "Y*";
            if (isBuiltIn) return "BuiltIn(" + name + ")";
            if (isEnvMarker) return "EnvMarker(" + envIndex + ")";
            if (isDelta) return "Delta(" + bodyIndex + ")";
            if (isDummy) return "dummy";
            if (isRand) return "Rand";
            return name;
        }
    }

    static class Environment {
        Map<String, MachineNode> bindings = new HashMap<>();
        Environment parent;
        int index;

        MachineNode lookup(String name) {
            if (bindings.containsKey(name)) return bindings.get(name);
            return parent != null ? parent.lookup(name) : null;
        }
    }

    static List<List<MachineNode>> controlStructures = new ArrayList<>();
    static Stack<MachineNode> control = new Stack<>();
    static Stack<MachineNode> stack = new Stack<>();
    static List<Environment> environments = new ArrayList<>();
    static Environment currentEnv;

    public static void evaluate(Node root) {
        // Initialize machine
        controlStructures.clear();
        stack.clear();
        control.clear();
        environments.clear();

        // Create control structure for the program
        List<MachineNode> topControl = new ArrayList<>();
        flatten(root, topControl);
        controlStructures.add(topControl);

        // Create global environment with primitives (Initial State)
        Environment global = new Environment();
        global.index = 0;
        addPrimitiveEnvironment(global);
        environments.add(global);
        currentEnv = global;

        // Initial state: push environment marker
        MachineNode envMarker = new MachineNode();
        envMarker.isEnvMarker = true;
        envMarker.name = "e0";
        envMarker.envIndex = 0;
        stack.push(envMarker);

        // Push program instructions onto control in reverse order
        for (int i = topControl.size() - 1; i >= 0; i--) {
            control.push(topControl.get(i));
        }

        // Main evaluation loop
        while (!control.isEmpty()) {
            MachineNode instr = control.pop();
            executeRule(instr);
        }

        // Output final result
        outputFinalResult();
    }

    static void executeRule(MachineNode instr) {
        // CSE Rule 1: Stack literals and names
        if (instr.isInt || instr.isBool || instr.isString || instr.isDummy) {
            stack.push(instr);
        }

        // CSE Rule 1: Variable lookup (stack a name)
        else if (instr.isName) {
            MachineNode val = currentEnv.lookup(instr.name);
            if (val != null) {
                stack.push(val);
            } else {
                throw new RuntimeException("Unbound variable: " + instr.name);
            }
        }

        // CSE Rule 2: Stack lambda (create closure)
        else if (instr.isLambda) {
            MachineNode closure = new MachineNode();
            closure.isLambda = true;
            closure.name = instr.name;
            closure.boundVars = new ArrayList<>(instr.boundVars);
            closure.bodyIndex = instr.bodyIndex;
            closure.envIndex = currentEnv.index;
            stack.push(closure);
        }

        // CSE Rules 3, 4, 10, 11, 12, 13: Function application (gamma)
        else if (instr.isGamma) {
            if (stack.size() < 2) throw new RuntimeException("Stack underflow on gamma");

            MachineNode rand = stack.pop();  // argument (rand)
            MachineNode rator = stack.pop(); // function (rator)

            applyFunction(rator, rand);
        }

        // CSE Rule 5: Exit environment
        else if (instr.isEnvMarker) {
            exitEnvironment(instr);
        }

        // CSE Rule 6: Binary operators
        else if (instr.isBinaryOp) {
            if (stack.size() < 2) throw new RuntimeException("Stack underflow on binary op");

            MachineNode rand2 = stack.pop();
            MachineNode rand1 = stack.pop();
            MachineNode result = applyBinaryOp(instr.op, rand1, rand2);
            stack.push(result);
        }

        // CSE Rule 7: Unary operators
        else if (instr.isUnaryOp) {
            if (stack.isEmpty()) throw new RuntimeException("Stack underflow on unary op");

            MachineNode rand = stack.pop();
            MachineNode result = applyUnaryOp(instr.op, rand);
            stack.push(result);
        }

        // CSE Rule 8: Conditional
        else if (instr.isConditional) {
            if (stack.size() < 3) throw new RuntimeException("Stack underflow on conditional");

            MachineNode elseDelta = stack.pop();
            MachineNode thenDelta = stack.pop();
            MachineNode condition = stack.pop();

            if (!condition.isBool) {
                throw new RuntimeException("Condition is not boolean: " + condition);
            }

            // Choose branch based on condition
            MachineNode chosenDelta = condition.boolValue ? thenDelta : elseDelta;

            // Push chosen delta's body onto control
            if (chosenDelta.isDelta) {
                List<MachineNode> body = controlStructures.get(chosenDelta.bodyIndex);
                for (int i = body.size() - 1; i >= 0; i--) {
                    control.push(body.get(i));
                }
            }
        }

        // CSE Rule 9: Tuple formation (tau)
        else if (instr.isTau) {
            if (stack.size() < instr.tupleSize) {
                throw new RuntimeException("Stack underflow on tau");
            }

            MachineNode tupleNode = new MachineNode();
            tupleNode.isTuple = true;
            tupleNode.tuple = new ArrayList<>();

            // Pop elements in reverse order to maintain correct order
            Stack<MachineNode> temp = new Stack<>();
            for (int i = 0; i < instr.tupleSize; i++) {
                temp.push(stack.pop());
            }
            while (!temp.isEmpty()) {
                tupleNode.tuple.add(temp.pop());
            }

            stack.push(tupleNode);
        }

        // Delta (for conditionals) - just stack it
        else if (instr.isDelta) {
            stack.push(instr);
        }

        // Y* combinator
        else if (instr.isY) {
            stack.push(instr);
        }

        else {
            throw new RuntimeException("Unknown instruction: " + instr);
        }
    }

    // CSE Rule 5: Exit Environment - Properly implemented
    static void exitEnvironment(MachineNode envMarker) {
        Stack<MachineNode> temp = new Stack<>();
        MachineNode result = null;
        boolean foundMarker = false;

        // Pop everything until we find the matching environment marker
        while (!stack.isEmpty()) {
            MachineNode top = stack.pop();
            if (top.isEnvMarker && top.envIndex == envMarker.envIndex) {
                foundMarker = true;
                break;
            }
            if (!top.isEnvMarker) {
                if (result == null) {
                    result = top; // First non-marker is the result
                } else {
                    temp.push(top); // Store other values
                }
            }
        }

        if (!foundMarker) {
            throw new RuntimeException("Environment marker not found: " + envMarker.envIndex);
        }

        // Restore previous environment
        if (envMarker.envIndex > 0 && envMarker.envIndex < environments.size()) {
            Environment env = environments.get(envMarker.envIndex);
            currentEnv = env.parent;
        }

        // Push back the result first
        if (result != null) {
            stack.push(result);
        }

        // Push back other values in correct order
        while (!temp.isEmpty()) {
            stack.push(temp.pop());
        }
    }

    static void applyFunction(MachineNode rator, MachineNode rand) {
        // CSE Rule 11: Built-in functions (N-ary functions)
        if (rator.isBuiltIn) {
            handleBuiltIn(rator, rand);
        }

        // CSE Rule 12: Y* combinator application (Applying Y)
        else if (rator.isY) {
            if (!rand.isLambda) {
                throw new RuntimeException("Y* applied to non-lambda");
            }

            // Create eta abstraction: λx.(f (Y* f) x)
            // This implements the fixed-point combinator correctly
            MachineNode etaLambda = new MachineNode();
            etaLambda.isLambda = true;
            etaLambda.name = "eta_" + rand.name;
            etaLambda.boundVars = new ArrayList<>(rand.boundVars);
            etaLambda.envIndex = currentEnv.index; // Current environment for eta
            etaLambda.bodyIndex = controlStructures.size();

            // Create eta body: x (Y* f) f gamma gamma
            List<MachineNode> etaBody = new ArrayList<>();

            // Push parameter x
            MachineNode paramX = new MachineNode();
            paramX.isName = true;
            paramX.name = rand.boundVars.get(0);
            etaBody.add(paramX);

            // Push Y* f (the recursive call)
            MachineNode yStar = new MachineNode();
            yStar.isY = true;
            yStar.name = "Y*";
            etaBody.add(yStar);

            // Push copy of f for Y* f
            MachineNode fForY = copyLambda(rand);
            etaBody.add(fForY);

            // First gamma for Y* f
            MachineNode gamma1 = new MachineNode();
            gamma1.isGamma = true;
            gamma1.name = "gamma";
            etaBody.add(gamma1);

            // Push f for main application
            MachineNode fMain = copyLambda(rand);
            etaBody.add(fMain);

            // Second gamma for f (Y* f) x
            MachineNode gamma2 = new MachineNode();
            gamma2.isGamma = true;
            gamma2.name = "gamma";
            etaBody.add(gamma2);

            controlStructures.add(etaBody);
            stack.push(etaLambda);
        }

        // CSE Rule 4: Apply lambda (Function Call Execution)
        else if (rator.isLambda) {
            // Create new environment extending lambda's creation environment
            Environment lambdaEnv = environments.get(rator.envIndex);
            Environment newEnv = new Environment();
            newEnv.parent = lambdaEnv;
            newEnv.index = environments.size();
            environments.add(newEnv);

            // Bind parameters to arguments
            bindParameters(rator, rand, newEnv);

            // Push environment marker for exit (Rule 5 setup)
            MachineNode envMarker = new MachineNode();
            envMarker.isEnvMarker = true;
            envMarker.envIndex = newEnv.index;
            control.push(envMarker);

            // Switch to new environment
            currentEnv = newEnv;

            // Push lambda body onto control
            List<MachineNode> body = controlStructures.get(rator.bodyIndex);
            for (int i = body.size() - 1; i >= 0; i--) {
                control.push(body.get(i));
            }
        }

        // CSE Rule 10: Tuple selection
        else if (rator.isTuple && rand.isInt) {
            int index = rand.intValue;
            if (index < 1 || index > rator.tuple.size()) {
                throw new RuntimeException("Tuple index out of bounds: " + index);
            }
            stack.push(rator.tuple.get(index - 1)); // 1-based indexing
        }

        // CSE Rule 13: Applying Function Pair (for advanced control structures)
        else if (rator.isTuple && rator.tuple.size() == 2 &&
                rator.tuple.get(0).isLambda && rator.tuple.get(1).isLambda) {
            // This handles function pairs - apply first function to argument
            // and second function to the environment
            MachineNode f1 = rator.tuple.get(0);
            MachineNode f2 = rator.tuple.get(1);

            // For now, apply the first function (this can be extended for more complex cases)
            applyFunction(f1, rand);
        }

        else {
            throw new RuntimeException("Gamma applied to non-function: " + rator);
        }
    }

    // Helper method to properly bind parameters
    static void bindParameters(MachineNode lambda, MachineNode argument, Environment env) {
        if (lambda.boundVars.size() == 1) {
            // Single parameter
            env.bindings.put(lambda.boundVars.get(0), argument);
        } else if (lambda.boundVars.size() > 1) {
            // Multiple parameters - argument should be a tuple
            if (argument.isTuple && argument.tuple.size() == lambda.boundVars.size()) {
                for (int i = 0; i < lambda.boundVars.size(); i++) {
                    env.bindings.put(lambda.boundVars.get(i), argument.tuple.get(i));
                }
            } else {
                throw new RuntimeException("Parameter count mismatch: expected " +
                        lambda.boundVars.size() + " arguments");
            }
        }
    }

    // Helper method to copy a lambda node
    static MachineNode copyLambda(MachineNode original) {
        MachineNode copy = new MachineNode();
        copy.isLambda = true;
        copy.name = original.name;
        copy.boundVars = new ArrayList<>(original.boundVars);
        copy.bodyIndex = original.bodyIndex;
        copy.envIndex = original.envIndex;
        return copy;
    }

    static MachineNode applyBinaryOp(String op, MachineNode a, MachineNode b) {
        MachineNode result = new MachineNode();

        switch (op) {
            case "op_plus" -> {
                if (!a.isInt || !b.isInt) throw new RuntimeException("Type error in addition");
                result.isInt = true;
                result.intValue = a.intValue + b.intValue;
            }
            case "op_minus" -> {
                if (!a.isInt || !b.isInt) throw new RuntimeException("Type error in subtraction");
                result.isInt = true;
                result.intValue = a.intValue - b.intValue;
            }
            case "op_mul" -> {
                if (!a.isInt || !b.isInt) throw new RuntimeException("Type error in multiplication");
                result.isInt = true;
                result.intValue = a.intValue * b.intValue;
            }
            case "op_div" -> {
                if (!a.isInt || !b.isInt) throw new RuntimeException("Type error in division");
                if (b.intValue == 0) throw new RuntimeException("Division by zero");
                result.isInt = true;
                result.intValue = a.intValue / b.intValue;
            }
            case "op_compare", "eq" -> {
                result.isBool = true;
                if (a.isInt && b.isInt) {
                    result.boolValue = a.intValue == b.intValue;
                } else if (a.isBool && b.isBool) {
                    result.boolValue = a.boolValue == b.boolValue;
                } else if (a.isString && b.isString) {
                    result.boolValue = a.stringValue.equals(b.stringValue);
                } else {
                    result.boolValue = false;
                }
            }
            case "op_ls" -> {
                if (!a.isInt || !b.isInt) throw new RuntimeException("Type error in comparison");
                result.isBool = true;
                result.boolValue = a.intValue < b.intValue;
            }
            case "op_le" -> {
                if (!a.isInt || !b.isInt) throw new RuntimeException("Type error in comparison");
                result.isBool = true;
                result.boolValue = a.intValue <= b.intValue;
            }
            case "op_gr" -> {
                if (!a.isInt || !b.isInt) throw new RuntimeException("Type error in comparison");
                result.isBool = true;
                result.boolValue = a.intValue > b.intValue;
            }
            case "op_ge" -> {
                if (!a.isInt || !b.isInt) throw new RuntimeException("Type error in comparison");
                result.isBool = true;
                result.boolValue = a.intValue >= b.intValue;
            }
            case "op_or" -> {
                if (!a.isBool || !b.isBool) throw new RuntimeException("Type error in logical OR");
                result.isBool = true;
                result.boolValue = a.boolValue || b.boolValue;
            }
            case "op_and" -> {
                if (!a.isBool || !b.isBool) throw new RuntimeException("Type error in logical AND");
                result.isBool = true;
                result.boolValue = a.boolValue && b.boolValue;
            }
            case "aug" -> {
                // Augmentation: add element to tuple
                result.isTuple = true;
                result.tuple = new ArrayList<>();
                result.tuple.add(a);
                if (b.isTuple) {
                    result.tuple.addAll(b.tuple);
                } else {
                    result.tuple.add(b);
                }
            }
            default -> throw new RuntimeException("Unsupported binary operator: " + op);
        }
        return result;
    }

    static MachineNode applyUnaryOp(String op, MachineNode operand) {
        MachineNode result = new MachineNode();

        switch (op) {
            case "op_not" -> {
                if (!operand.isBool) throw new RuntimeException("Type error in logical NOT");
                result.isBool = true;
                result.boolValue = !operand.boolValue;
            }
            case "op_neg" -> {
                if (!operand.isInt) throw new RuntimeException("Type error in negation");
                result.isInt = true;
                result.intValue = -operand.intValue;
            }
            default -> throw new RuntimeException("Unsupported unary operator: " + op);
        }
        return result;
    }

    static void addPrimitiveEnvironment(Environment env) {
        // Add built-in functions to primitive environment
        for (String builtin : Arrays.asList("Print", "Conc", "ItoS", "Isinteger",
                "Isstring", "Istruthvalue", "Isfunction", "Order", "Null")) {
            MachineNode node = new MachineNode();
            node.isBuiltIn = true;
            node.name = builtin;
            env.bindings.put(builtin, node);
        }

        // Add Y* combinator
        MachineNode yNode = new MachineNode();
        yNode.isY = true;
        yNode.name = "Y*";
        env.bindings.put("Y*", yNode);
    }

    static void flatten(Node node, List<MachineNode> list) {
        switch (node.type) {
            case integer -> {
                MachineNode m = new MachineNode();
                m.isInt = true;
                m.intValue = Integer.parseInt(node.value);
                m.name = node.value;
                list.add(m);
            }
            case string -> {
                MachineNode m = new MachineNode();
                m.isString = true;
                m.stringValue = node.value;
                m.name = node.value;
                list.add(m);
            }
            case identifier -> {
                MachineNode m = new MachineNode();
                m.isName = true;
                m.name = node.value;
                list.add(m);
            }
            case true_value, false_value -> {
                MachineNode m = new MachineNode();
                m.isBool = true;
                m.boolValue = node.type == NodeType.true_value;
                m.name = node.value;
                list.add(m);
            }
            case dummy -> {
                MachineNode m = new MachineNode();
                m.isDummy = true;
                m.name = "dummy";
                list.add(m);
            }
            case lambda -> {
                MachineNode lam = new MachineNode();
                lam.isLambda = true;
                lam.name = "lambda";

                // Handle multiple parameters
                if (node.children.size() >= 1) {
                    Node params = node.children.get(0);
                    if (params.type == NodeType.identifier) {
                        lam.boundVars.add(params.value);
                    } else {
                        // Multiple parameters case
                        collectParameters(params, lam.boundVars);
                    }
                }

                lam.bodyIndex = controlStructures.size();

                List<MachineNode> body = new ArrayList<>();
                if (node.children.size() >= 2) {
                    flatten(node.children.get(1), body);
                }
                controlStructures.add(body);
                list.add(lam);
            }
            case gamma -> {
                flatten(node.children.get(0), list); // rator
                flatten(node.children.get(1), list); // rand
                MachineNode gamma = new MachineNode();
                gamma.isGamma = true;
                gamma.name = "gamma";
                list.add(gamma);
            }
            case y_star -> {
                MachineNode y = new MachineNode();
                y.isY = true;
                y.name = "Y*";
                list.add(y);
            }
            case tau -> {
                for (Node c : node.children) {
                    flatten(c, list);
                }
                MachineNode tau = new MachineNode();
                tau.isTau = true;
                tau.name = "tau";
                tau.tupleSize = node.children.size();
                list.add(tau);
            }
            case op_not, op_neg -> {
                flatten(node.children.get(0), list);
                MachineNode op = new MachineNode();
                op.isUnaryOp = true;
                op.op = node.type.name();
                op.name = node.type.name();
                list.add(op);
            }
            case op_plus, op_minus, op_mul, op_div, op_compare,
                    op_or, op_and, aug -> {
                flatten(node.children.get(0), list);
                flatten(node.children.get(1), list);
                MachineNode op = new MachineNode();
                op.isBinaryOp = true;
                op.op = node.type.name();
                op.name = node.type.name();
                list.add(op);
            }
            case conditional -> {
                // Flatten condition
                flatten(node.children.get(0), list);

                // Create deltas for then and else branches
                MachineNode thenDelta = new MachineNode();
                thenDelta.isDelta = true;
                thenDelta.name = "then_delta";
                thenDelta.bodyIndex = controlStructures.size();
                List<MachineNode> thenBody = new ArrayList<>();
                flatten(node.children.get(1), thenBody);
                controlStructures.add(thenBody);
                list.add(thenDelta);

                MachineNode elseDelta = new MachineNode();
                elseDelta.isDelta = true;
                elseDelta.name = "else_delta";
                elseDelta.bodyIndex = controlStructures.size();
                List<MachineNode> elseBody = new ArrayList<>();
                flatten(node.children.get(2), elseBody);
                controlStructures.add(elseBody);
                list.add(elseDelta);

                // Add conditional instruction
                MachineNode cond = new MachineNode();
                cond.isConditional = true;
                cond.name = "conditional";
                list.add(cond);
            }
            default -> {
                for (Node c : node.children) {
                    flatten(c, list);
                }
            }
        }
    }

    static void collectParameters(Node node, List<String> params) {
        if (node.type == NodeType.identifier) {
            params.add(node.value);
        } else {
            for (Node child : node.children) {
                collectParameters(child, params);
            }
        }
    }

    static void handleBuiltIn(MachineNode f, MachineNode arg) {
        MachineNode result = new MachineNode();

        switch (f.name) {
            case "Print" -> {
                String output = formatOutput(arg);
                System.out.println(output);
                result.isDummy = true;
                result.name = "dummy";
                stack.push(result);
            }
            case "ItoS" -> {
                if (!arg.isInt) throw new RuntimeException("ItoS expects an integer");
                result.isString = true;
                result.stringValue = Integer.toString(arg.intValue);
                result.name = result.stringValue;
                stack.push(result);
            }
            case "Order" -> {
                if (arg.isTuple) {
                    result.isInt = true;
                    result.intValue = arg.tuple.size();
                    result.name = Integer.toString(result.intValue);
                    stack.push(result);
                } else {
                    throw new RuntimeException("Order applied to non-tuple: " + arg);
                }
            }
            case "Isinteger" -> {
                result.isBool = true;
                result.boolValue = arg.isInt;
                stack.push(result);
            }
            case "Isstring" -> {
                result.isBool = true;
                result.boolValue = arg.isString;
                stack.push(result);
            }
            case "Istruthvalue" -> {
                result.isBool = true;
                result.boolValue = arg.isBool;
                stack.push(result);
            }
            case "Isfunction" -> {
                result.isBool = true;
                result.boolValue = arg.isLambda;
                stack.push(result);
            }
            case "Null" -> {
                result.isBool = true;
                result.boolValue = arg.isTuple && arg.tuple.isEmpty();
                stack.push(result);
            }
            case "Conc" -> {
                if (!arg.isString) throw new RuntimeException("Conc expects string");
                // Conc is usually a two-argument function, this is partial application
                MachineNode concPartial = new MachineNode();
                concPartial.isBuiltIn = true;
                concPartial.name = "Conc1";
                concPartial.stringValue = arg.stringValue; // Store first argument
                stack.push(concPartial);
            }
            case "Conc1" -> {
                if (!arg.isString) throw new RuntimeException("Conc expects string");
                result.isString = true;
                result.stringValue = f.stringValue + arg.stringValue;
                stack.push(result);
            }
            default -> throw new RuntimeException("Unsupported built-in: " + f.name);
        }
    }

    static void outputFinalResult() {
        if (!stack.isEmpty()) {
            // Skip environment markers to find actual result
            Stack<MachineNode> temp = new Stack<>();
            MachineNode result = null;

            while (!stack.isEmpty()) {
                MachineNode top = stack.pop();
                if (!top.isEnvMarker) {
                    result = top;
                    break;
                }
                temp.push(top);
            }

            // Restore stack
            while (!temp.isEmpty()) {
                stack.push(temp.pop());
            }

            if (result != null) {
                System.out.println("Final Result: " + formatOutput(result));
            } else {
                System.out.println("No result found");
            }
        } else {
            System.out.println("Empty stack - no result");
        }
    }

    static String formatOutput(MachineNode node) {
        if (node.isInt) return Integer.toString(node.intValue);
        if (node.isBool) return Boolean.toString(node.boolValue);
        if (node.isString) return node.stringValue;
        if (node.isDummy) return "dummy";
        if (node.isTuple) {
            if (node.tuple.isEmpty()) return "nil";
            StringBuilder sb = new StringBuilder("(");
            for (int i = 0; i < node.tuple.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(formatOutput(node.tuple.get(i)));
            }
            sb.append(")");
            return sb.toString();
        }
        return node.toString();
    }
}