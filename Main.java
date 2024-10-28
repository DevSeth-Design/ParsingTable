import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Stack;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        File fp = new File(System.getProperty("user.dir") + File.separator + "rules");// changed for vs code

        Scanner sc = new Scanner(fp);
        Hashtable<Integer, String> states = new Hashtable<Integer, String>();
        String rules = "";
        while (sc.hasNextLine()) {
            rules += sc.nextLine() + "\n";
        }
        String[] rulesCollections = rules.split("\n");
        String r0 = rulesCollections[0].charAt(0) + "'->" + rulesCollections[0].charAt(0);
        System.out.println("r0: " + r0);
        for (int i = 0; i < rulesCollections.length; i++) {
            System.out.println("r" + (i + 1) + ": " + rulesCollections[i]);
        }
        String nonTerminals = findNonTerminals(rulesCollections);
        System.out.println("NonTerminals: " + nonTerminals);
        String terminals = findTerminals(rulesCollections);
        System.out.println("Terminals: " + terminals);

        // generate the augmented rule
        String I0 = r0.substring(0, r0.indexOf("->") + 2) + "." + r0.substring(r0.indexOf("->") + 2);

        I0 = Closure(I0, nonTerminals, rulesCollections);

        states.put(0, I0);
        String Gotos = constructStateDiagram(states, nonTerminals, rulesCollections);
        displayStates(states);
        System.out.println("Gotos: " + Gotos);
        Hashtable<String, String> parsingTable = parsingTable(states, Gotos, terminals, nonTerminals, rulesCollections);
        displayParsingTable(states, parsingTable, terminals, nonTerminals);
        parsedStack(UserInput(), parsingTable, rulesCollections);
        sc.close();
    }

    public static String findNonTerminals(String[] rulesCollections) {
        String nonTerminals = "";
        char sep = ',';
        for (int i = 0; i < rulesCollections.length; i++) {
            char symbol = rulesCollections[i].charAt(0);
            if (nonTerminals.indexOf(symbol) == -1) {
                nonTerminals += Character.toString(symbol) + sep;
            }
        }
        return nonTerminals.substring(0, nonTerminals.length() - 1);
    }

    public static String findTerminals(String[] rulesCollections) {
        String terminals = "";
        char sep = ',';
        for (int i = 0; i < rulesCollections.length; i++) {
            String rhs = rulesCollections[i].substring(rulesCollections[i].indexOf("->") + 2);
            for (int j = 0; j < rhs.length(); j++) {
                char symbol = rhs.charAt(j);
                if (terminals.indexOf(symbol) == -1 && findNonTerminals(rulesCollections).indexOf(symbol) == -1) {
                    terminals += Character.toString(symbol) + sep;
                }
            }
        }
        return terminals.substring(0, terminals.length() - 1);
    }

    public static String Goto(String state, char symbol, String nonTerminals, String[] rulesCollections) {
        String[] items = state.split(",");
        String newState = "", toUpdate = "";
        for (int i = 0; i < items.length; i++) {
            String rhs = items[i].substring(items[i].indexOf("->") + 2);
            if (rhs.indexOf("." + symbol) != -1) {
                toUpdate += items[i] + ",";
            }
        }
        if (toUpdate.equals("")) {
            return "";
        } else {
            toUpdate = toUpdate.substring(0, toUpdate.length() - 1);
            String[] s = toUpdate.split(",");
            for (int i = 0; i < s.length; i++) {
                if (s[i].indexOf(".") + 2 == s[i].length()) {
                    newState += s[i].substring(0, s[i].indexOf(".")) + symbol + ".";
                } else {
                    newState += s[i].substring(0, s[i].indexOf(".")) + symbol + "."
                            + s[i].substring(s[i].indexOf(symbol, s[i].indexOf(".")) + 1);
                }
            }
            return Closure(newState, nonTerminals, rulesCollections);
        }
    }

    public static String Closure(String state, String nonTerminals, String[] rulesCollections) {
        // boolean array used to identify if the symbol has been added
        boolean[] tags = new boolean[(nonTerminals.length() + 1) / 2];
        for (int i = 0; i < tags.length; i++)
            tags[i] = false;
        for (int i = 0; i < tags.length; i++) {
            String[] items = state.split(",");
            for (int j = 0; j < items.length; j++) {
                // fetch the symbol after dot
                if (items[j].indexOf(".") + 1 == items[j].length()) {
                    return state;
                } else {
                    char symbol = items[j].charAt(items[j].indexOf(".") + 1);
                    // if the symbol is a nonTerminal and not added so far
                    if (nonTerminals.indexOf(symbol) != -1) {
                        int k = (nonTerminals.indexOf(symbol) + 1) / 2;
                        if (tags[k])
                            continue;
                        // look for rules that begin with the symbol
                        for (int l = 0; l < rulesCollections.length; l++) {
                            char lhs = rulesCollections[l].charAt(0);
                            if (lhs == symbol) {
                                // add rules into the state with dot on rhs
                                int idx = rulesCollections[l].indexOf("->");
                                state += "," + rulesCollections[l].substring(0, idx + 2) + "."
                                        + rulesCollections[l].substring(idx + 2);
                            }
                        }
                        // mark the symbol as added
                        tags[k] = true;
                    }
                }
            }
        }
        return state;
    }

    public static String constructStateDiagram(Hashtable<Integer, String> states, String nonTerminals,
            String[] rulesCollections) {
        String Gotos = "";
        int n = 1, stateSize = states.size(), gotoSize = Gotos.split(",").length;
        while (true) {
            for (int i = 0; i < states.size(); i++) {
                for (int j = 0; j < states.get(i).split(",").length; j++) {
                    String item = states.get(i).split(",")[j];
                    if (item.indexOf(".") + 1 == item.length()) {
                        continue;
                    } else {
                        char symbol = item.charAt(item.indexOf(".") + 1);
                        String dest = Goto(item, symbol, nonTerminals, rulesCollections);
                        // System.out.println(item + " " + symbol + " " + dest + " " +
                        // states.contains(dest) + " " + i + "" + symbol + "" + findKey(states, dest));
                        if (!states.contains(dest)) {
                            states.put(n++, dest);
                            Gotos += i + "" + symbol + "" + findKey(states, dest) + ",";
                        } else {
                            String str = i + "" + symbol + "" + findKey(states, dest) + ",";
                            if (Gotos.indexOf(str) == -1) {
                                Gotos += str;
                            }
                        }
                    }
                }
            }
            // if there is no change made into either state or goto, terminate the loop
            if (stateSize != states.size() || gotoSize != Gotos.split(",").length) {
                stateSize = states.size();
                gotoSize = Gotos.split(",").length;
            } else {
                break;
            }
        }
        return Gotos.substring(0, Gotos.length() - 1);
    }

    public static Hashtable<String, String> parsingTable(Hashtable<Integer, String> states, String Gotos,
            String terminals, String nonTerminals, String[] rulesCollections) {
        // Gotos: 0S1,0A2,0a3,0b4,2A5,2a3,2b4,3A6,3a3,3b4
        Hashtable<String, String> pT = new Hashtable<String, String>();
        String[] actions = Gotos.split(",");
        // adding shift actions
        for (int i = 0; i < actions.length; i++) {
            char symbol = actions[i].charAt(1);
            if (terminals.indexOf(symbol) != -1) {
                pT.put(actions[i].substring(0, 2), "s" + actions[i].charAt(2));
            } else if (nonTerminals.indexOf(symbol) != -1) {
                pT.put(actions[i].substring(0, 2), "" + actions[i].charAt(2));
            }
        }
        // adding reduce actions
        String I0 = rulesCollections[0].charAt(0) + "'->" + rulesCollections[0].charAt(0) + ".";
        // add <"1$", "accept">
        for (int i = 0; i < states.size(); i++) {
            if (states.get(i).indexOf(I0) != -1) {
                pT.put(i + "$", "accept");
                break;
            }
        }
        for (int i = 0; i < states.size(); i++) {
            for (int j = 0; j < rulesCollections.length; j++) {
                if (states.get(i).indexOf(rulesCollections[j] + ".") != -1) {
                    // i: state number, j: rule number
                    for (int k = 0; k < terminals.split(",").length; k++) {
                        pT.put(i + "" + terminals.split(",")[k], "r" + (j + 1));
                    }
                    // add i$rj
                    pT.put(i + "$", "r" + (j + 1));
                }
            }
        }
        return pT;
    }

    public static int findKey(Hashtable<Integer, String> states, String value) {
        Enumeration<Integer> keys = states.keys();
        while (keys.hasMoreElements()) {
            int key = keys.nextElement();
            if (states.get(key).equals(value)) {
                return key;
            }
        }
        return -1;
    }

    public static void displayStates(Hashtable<Integer, String> states) {
        for (int i = 0; i < states.size(); i++) {
            System.out.println("I" + i + ": " + states.get(i));
        }
    }

    public static void displayParsingTable(Hashtable<Integer, String> states, Hashtable<String, String> pT,
            String terminals, String nonTerminals) {
        System.out.printf("The parsing table:\n%24s%18s\n", "action", "goto");
        System.out.print("state");
        String firstRow = terminals.replace(",", "") + "$" + nonTerminals.replace(",", "");
        System.out.printf("%8c%8c%8c%9c%8c\n", firstRow.charAt(0), firstRow.charAt(1), '$', firstRow.charAt(3),
                firstRow.charAt(4));
        for (int i = 0; i < states.size(); i++) {
            System.out.printf("%-6d", i);
            for (int j = 0; j < firstRow.length(); j++) {
                String result = pT.get(i + "" + firstRow.charAt(j));
                if (result == null) {
                    System.out.printf("%8c", ' ');
                } else {
                    System.out.printf("%8s", result);
                }
            }
            System.out.println();
        }
    }

    public static boolean parsedStack(String input, Hashtable<String, String> pT, String[] rulesCollections) {
        boolean accepted = false;
        Stack<Integer> stack = new Stack<>();// stack of state integers.
        stack.push(0);
        Stack<Character> charStack = new Stack<>();

        input += "$";
        int i = 0;

        // header
        System.out.printf("%-10s%-15s%-10s\n", "Stack", "Input", "Action");
        int seperatorLenght = 30 + input.length();
        for (int j = 0; j < seperatorLenght; j++) {
            System.out.print("-");
        }
        System.out.print("\n");

        while (true) {
            int currentState = stack.peek();
            char currentSymbol = input.charAt(i);
            String action = pT.get(currentState + "" + currentSymbol);

            // Print the current state of the stack and input, and the action
            System.out.printf("%-15s %-15s %-10s\n", printStack(stack), input.substring(i), action);

            // Format and print the current state of the stack and input
            printStack(stack);
            if (action == null) {
                System.out.println("Input string" + input + " was not accepted.");
                accepted = false;
                return accepted;
            }

            if (action.equals("accept")) {
                System.out.print("Input string " + "/'" + input + "/'" + " accepted");
                accepted = true;
                return accepted;
            } else if (action.startsWith("s")) {
                int nextState = Integer.parseInt(action.substring(1));
                stack.push(nextState);
                charStack.push(currentSymbol);
                i++;
            } else if (action.startsWith("r")) {
                // Reduce action: pop symbols and states based on the rule being reduced
                int ruleNumber = Integer.parseInt(action.substring(1));
                String rule = rulesCollections[ruleNumber - 1];
                String rhs = rule.split("->")[1]; // Get the right-hand side of the rule

                // Pop states and symbols corresponding to the rule's right-hand side length
                for (int j = 0; j < rhs.length(); j++) {
                    stack.pop();
                    charStack.pop();
                }

                // Get the non-terminal from the rule's left-hand side
                char lhs = rule.charAt(0);
                charStack.push(lhs); // Push the non-terminal

                // Perform a Goto action to determine the next state
                int gotoState = Integer.parseInt(pT.get(stack.peek() + "" + lhs));
                stack.push(gotoState);
            }

        }

    }

    public static String UserInput() {
        System.out.print("Enter a string: \n");
        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine();
        return input;
    }

    public static String printStack(Stack<Integer> stack) {
        StringBuilder printableStack = new StringBuilder();
        for (int i = 0; i < stack.size(); i++) {
            printableStack.append(stack.get(i)).append(" ");
        }
        return printableStack.toString();
    }
}
