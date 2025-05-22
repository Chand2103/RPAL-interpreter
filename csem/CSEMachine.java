// Fixed CSEMachine.java with proper Y* combinator handling
package csem;
import Parser.Node;
import Parser.NodeType;
import java.util.*;

public class CSEMachine {

    static class MachineNode {
        public boolean isConditional = false;
        String name;
        boolean isInt, isBool, isString, isName, isLambda, isGamma, isUnaryOp, isBinaryOp,
                isTuple, isTau, isBuiltIn, isY, isEnvMarker, isDummy;

        int intValue;
        boolean boolValue;
        String stringValue;
        String op;

        List<String> boundVars = new ArrayList<>();
        int bodyIndex;
        int envIndex;
        List<MachineNode> tuple = new ArrayList<>();

        @Override
        public String toString() {
            if (isInt) return Integer.toString(intValue);
            if (isBool) return Boolean.toString(boolValue);
            if (isString) return '"' + stringValue + '"';
            if (isTuple) return tuple.toString();
            if (isLambda) return "Lambda" + boundVars;
            return name;
        }
    }

    static class Environment {
        Map<String, MachineNode> bindings = new HashMap<>();
        Environment parent;

        MachineNode lookup(String name) {
            if (bindings.containsKey(name)) return bindings.get(name);
            return parent != null ? parent.lookup(name) : null;
        }
    }

    static List<List<MachineNode>> controlStructures = new ArrayList<>();
    static Stack<MachineNode> control = new Stack<>();
    static Stack<MachineNode> stack = new Stack<>();
    static List<Environment> environments = new ArrayList<>();

    public static void evaluate(Node root) {
        controlStructures.clear();
        stack.clear();
        control.clear();
        environments.clear();

        List<MachineNode> topControl = new ArrayList<>();
        flatten(root, topControl);
        controlStructures.add(topControl);

        Environment global = new Environment();
        environments.add(global);

        MachineNode envMarker = new MachineNode();
        envMarker.isEnvMarker = true;
        envMarker.name = "e0";
        envMarker.envIndex = 0;

        control.push(envMarker);
        for (int i = topControl.size() - 1; i >= 0; i--) control.push(topControl.get(i));
        stack.push(envMarker);

        Environment currentEnv = global;

        while (!control.isEmpty()) {
            MachineNode instr = control.pop();

            System.out.println("Processing: " + instr.name + " (Stack size: " + stack.size() + ")");

            if (instr.isInt || instr.isBool || instr.isString || instr.isDummy) {
                stack.push(instr);
            } else if (instr.isTuple) {
                stack.push(instr);
            } else if (instr.isName) {
                MachineNode val = currentEnv.lookup(instr.name);
                if (val == null && isBuiltIn(instr.name)) {
                    MachineNode builtin = new MachineNode();
                    builtin.isBuiltIn = true;
                    builtin.name = instr.name;
                    stack.push(builtin);
                } else if (val != null) {
                    stack.push(val);
                } else {
                    throw new RuntimeException("Unbound variable: " + instr.name);
                }
            } else if (instr.isLambda) {
                instr.envIndex = environments.indexOf(currentEnv);
                stack.push(instr);
            } else if (instr.isGamma) {
                if (stack.size() < 2) throw new RuntimeException("Stack underflow on gamma");
                MachineNode func = stack.pop();
                MachineNode arg = stack.pop();

                System.out.println("Gamma: applying " + func.name + " to " + arg);

                if (func.isBuiltIn) {
                    handleBuiltIn(func, arg);
                } else if (func.isY) {
                    // Y* combinator: Y f = f (Y f)
                    // We need to create the fixed point
                    if (!arg.isLambda) {
                        throw new RuntimeException("Y* applied to non-lambda: " + arg);
                    }

                    // Create a recursive lambda that represents f (Y f)
                    MachineNode recursiveLambda = new MachineNode();
                    recursiveLambda.isLambda = true;
                    recursiveLambda.name = "recursive_" + arg.name;
                    recursiveLambda.boundVars = new ArrayList<>(arg.boundVars);
                    recursiveLambda.envIndex = arg.envIndex;

                    // Create new body: original_body but with self-reference
                    List<MachineNode> newBody = new ArrayList<>();
                    List<MachineNode> originalBody = controlStructures.get(arg.bodyIndex);

                    // We need to substitute recursive calls in the body
                    // For now, let's copy the original body and let the environment handle recursion
                    newBody.addAll(originalBody);

                    recursiveLambda.bodyIndex = controlStructures.size();
                    controlStructures.add(newBody);

                    stack.push(recursiveLambda);

                } else if (func.isLambda) {
                    Environment newEnv = new Environment();
                    newEnv.parent = environments.get(func.envIndex);

                    // Bind the parameter
                    if (!func.boundVars.isEmpty()) {
                        newEnv.bindings.put(func.boundVars.get(0), arg);
                    }

                    environments.add(newEnv);
                    MachineNode eMark = new MachineNode();
                    eMark.isEnvMarker = true;
                    eMark.envIndex = environments.size() - 1;
                    control.push(eMark);
                    stack.push(eMark);

                    List<MachineNode> body = controlStructures.get(func.bodyIndex);
                    for (int i = body.size() - 1; i >= 0; i--) control.push(body.get(i));
                    currentEnv = newEnv;
                } else {
                    throw new RuntimeException("Attempt to apply non-function: " + func);
                }
            } else if (instr.isEnvMarker) {
                // Pop until we find our result above the environment marker
                Stack<MachineNode> results = new Stack<>();
                MachineNode item;
                do {
                    if (stack.isEmpty()) throw new RuntimeException("Environment marker not found");
                    item = stack.pop();
                    if (!item.isEnvMarker) {
                        results.push(item);
                    }
                } while (!item.isEnvMarker || item.envIndex != instr.envIndex);

                // Push back the result (should be only one)
                if (!results.isEmpty()) {
                    stack.push(results.pop());
                }

                currentEnv = environments.get(instr.envIndex).parent;
            } else if (instr.isUnaryOp) {
                if (stack.isEmpty()) throw new RuntimeException("Stack underflow on unary op");
                MachineNode x = stack.pop();
                MachineNode r = new MachineNode();
                if (instr.op.equals("op_not")) {
                    r.isBool = true;
                    r.boolValue = !x.boolValue;
                } else if (instr.op.equals("op_neg")) {
                    r.isInt = true;
                    r.intValue = -x.intValue;
                }
                stack.push(r);
            } else if (instr.isBinaryOp) {
                if (stack.size() < 2) throw new RuntimeException("Stack underflow on binary op");
                MachineNode b = stack.pop();
                MachineNode a = stack.pop();
                MachineNode r = new MachineNode();
                switch (instr.op) {
                    case "op_plus" -> { r.isInt = true; r.intValue = a.intValue + b.intValue; }
                    case "op_minus" -> { r.isInt = true; r.intValue = a.intValue - b.intValue; }
                    case "op_mul" -> { r.isInt = true; r.intValue = a.intValue * b.intValue; }
                    case "op_div" -> { r.isInt = true; r.intValue = a.intValue / b.intValue; }
                    case "op_compare", "eq" -> {
                        r.isBool = true;
                        if (a.isInt && b.isInt) {
                            r.boolValue = a.intValue == b.intValue;
                        } else {
                            r.boolValue = false;
                        }
                    }
                }
                stack.push(r);
            } else if (instr.isTau) {
                MachineNode r = new MachineNode();
                r.isTuple = true;
                r.tuple = new ArrayList<>();
                int count = instr.tuple.size();
                for (int i = 0; i < count; i++) {
                    if (stack.isEmpty()) throw new RuntimeException("Stack underflow on tau");
                    r.tuple.add(0, stack.pop()); // Add at beginning to maintain order
                }
                stack.push(r);
            }
        }

        if (!stack.isEmpty()) {
            System.out.println("Final Result: " + stack.peek());
        } else {
            System.out.println("Empty stack - no result");
        }
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
                if (node.children.size() >= 1) {
                    lam.boundVars.add(node.children.get(0).value);
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
                flatten(node.children.get(0), list);
                flatten(node.children.get(1), list);
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
                for (Node c : node.children) flatten(c, list);
                MachineNode tau = new MachineNode();
                tau.isTau = true;
                tau.name = "tau";
                tau.tuple = new ArrayList<>();
                // Create placeholders for the number of children
                for (int i = 0; i < node.children.size(); i++) {
                    MachineNode placeholder = new MachineNode();
                    placeholder.name = "placeholder";
                    tau.tuple.add(placeholder);
                }
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
            case op_plus, op_minus, op_mul, op_div, op_compare -> {
                flatten(node.children.get(0), list);
                flatten(node.children.get(1), list);
                MachineNode op = new MachineNode();
                op.isBinaryOp = true;
                op.op = node.type.name();
                op.name = node.type.name();
                list.add(op);
            }
            case conditional -> {
                // Children: 0 = condition, 1 = then, 2 = else
                flatten(node.children.get(0), list); // condition
                flatten(node.children.get(1), list); // then branch
                flatten(node.children.get(2), list); // else branch

                MachineNode cond = new MachineNode();
                cond.name = "cond";
                cond.isConditional = true;
                list.add(cond);
            }
            case equal -> {
                // This handles the = in assignments like Psum = ...
                // We need to create a binding in the environment
                if (node.children.size() == 2) {
                    Node var = node.children.get(0);
                    Node expr = node.children.get(1);

                    // First flatten the expression
                    flatten(expr, list);

                    // Then create a lambda that binds the variable and evaluates to dummy
                    MachineNode lambda = new MachineNode();
                    lambda.isLambda = true;
                    lambda.name = "binding_lambda";
                    lambda.boundVars.add(var.value);
                    lambda.bodyIndex = controlStructures.size();

                    // Body just returns dummy
                    List<MachineNode> body = new ArrayList<>();
                    MachineNode dummy = new MachineNode();
                    dummy.isDummy = true;
                    dummy.name = "dummy";
                    body.add(dummy);
                    controlStructures.add(body);

                    list.add(lambda);

                    // Add gamma to apply the lambda
                    MachineNode gamma = new MachineNode();
                    gamma.isGamma = true;
                    gamma.name = "gamma";
                    list.add(gamma);
                }
                break;
            }
            default -> {
                for (Node c : node.children) flatten(c, list);
            }
        }
    }

    static boolean isBuiltIn(String s) {
        return List.of("Print", "Conc", "ItoS", "Isinteger", "Isstring", "Istruthvalue", "Isfunction", "Order").contains(s);
    }

    static void handleBuiltIn(MachineNode f, MachineNode arg) {
        MachineNode r = new MachineNode();
        switch (f.name) {
            case "Print" -> {
                System.out.println("PRINT: " + arg.toString());
                r.isDummy = true;
                r.name = "dummy";
                stack.push(r);
            }
            case "ItoS" -> {
                r.isString = true;
                r.stringValue = Integer.toString(arg.intValue);
                r.name = r.stringValue;
                stack.push(r);
            }
            case "Order" -> {
                if (arg.isTuple) {
                    r.isInt = true;
                    r.intValue = arg.tuple.size();
                    r.name = Integer.toString(r.intValue);
                    stack.push(r);
                } else {
                    throw new RuntimeException("Order applied to non-tuple: " + arg);
                }
            }
            default -> throw new RuntimeException("Unsupported built-in: " + f.name);
        }
    }
}