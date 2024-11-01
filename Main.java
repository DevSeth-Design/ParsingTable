
/**
 * Main class for LR(0) parser implementation in Java.
 * description: This program implements an LR(0) parser for a given set of rules.
 * The program reads the rules from a file, constructs the augmented grammar, generates the states, and constructs the parsing table.
 * The program then reads an input string from the user and parses it using the parsing table.
 * The program displays the parsing table, the states, and the parsing process.
 * This does not work for complex grammars (see the example in the rules2 file).
 * DEVELOPED BY: Seth Glover
 * DATE: 31/10/2024 
 */
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
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
        StringBuilder newState = new StringBuilder();// removed String to lessen space complexity.
        List<String> itemsToUpdate = new ArrayList<>();

        String[] items = state.split(",");// moved this out to avoid calling it repeatedly
        for (String currentItem : items) {
            int dotIndex = currentItem.indexOf(".");
            if (dotIndex != -1 && dotIndex + 1 < currentItem.length() && currentItem.charAt(dotIndex + 1) == symbol) {
                String updatedItem = currentItem.substring(0, dotIndex) + symbol + "."
                        + currentItem.substring(dotIndex + 2);
                itemsToUpdate.add(updatedItem);
            }
        }

        if (itemsToUpdate.isEmpty()) {
            return "";
        }

        for (String item : itemsToUpdate) {
            newState.append(item).append(",");
        }

        return Closure(newState.substring(0, newState.length() - 1), nonTerminals, rulesCollections);
    }

    // DESC: this uses BFP to process the items in a recursive manner.

    public static String Closure(String state, String nonTerminals, String[] rulesCollections) {
        List<String> closureItems = new ArrayList<>(); // Use List to maintain order of addition
        Queue<String> itemsToProcess = new LinkedList<>();
        String[] initialItems = state.split(",");

        // Add initial state to itemsToProcess and closureItems
        for (String item : initialItems) {
            if (!closureItems.contains(item)) {
                itemsToProcess.add(item);
                closureItems.add(item);
            }
        }

        // Process each item in the queue
        while (!itemsToProcess.isEmpty()) {
            String currentItem = itemsToProcess.poll();
            int dotIndex = currentItem.indexOf(".");

            // If the dot is at the end, skip this item
            if (dotIndex == -1 || dotIndex + 1 >= currentItem.length()) {
                continue;
            }

            char symbolAfterDot = currentItem.charAt(dotIndex + 1);

            // If the symbol after the dot is a non-terminal, add its productions
            if (nonTerminals.indexOf(symbolAfterDot) != -1) {
                for (String rule : rulesCollections) {
                    if (rule.charAt(0) == symbolAfterDot) {
                        String newItem = rule.substring(0, rule.indexOf("->") + 2) + "."
                                + rule.substring(rule.indexOf("->") + 2);

                        // Add the new item if it's not already in the closure
                        if (!closureItems.contains(newItem)) {
                            closureItems.add(newItem);
                            itemsToProcess.add(newItem);
                        }
                    }
                }
            }
        }

        // Return the closure items as a comma-separated string
        return String.join(",", closureItems);
    }

    public static String constructStateDiagram(Hashtable<Integer, String> states, String nonTerminals,
            String[] rulesCollections) {
        StringBuilder Gotos = new StringBuilder();// more efficient space complexity than a String
        int n = 1;
        Queue<Integer> queue = new LinkedList<>();
        Set<String> transitions = new HashSet<>();

        queue.add(0);

        // Iterate through states using a queue to avoid redundant checks
        while (!queue.isEmpty()) {
            int i = queue.poll();
            String[] items = states.get(i).split(",");

            for (String item : items) {
                if (item.indexOf(".") + 1 == item.length()) {
                    continue;
                }

                char symbol = item.charAt(item.indexOf(".") + 1);
                String dest = Goto(item, symbol, nonTerminals, rulesCollections);

                if (!states.containsValue(dest)) {
                    states.put(n, dest);
                    queue.add(n);
                    String transition = i + "" + symbol + "" + n;
                    Gotos.append(transition).append(",");
                    transitions.add(transition);
                    n++;
                } else {
                    int destKey = findKey(states, dest);
                    String transition = i + "" + symbol + "" + destKey;
                    if (!transitions.contains(transition)) {
                        Gotos.append(transition).append(",");
                        transitions.add(transition);
                    }
                }
            }
        }

        return Gotos.length() > 0 ? Gotos.substring(0, Gotos.length() - 1) : "";
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
        // removed enumeration to avoid having to itterate through both key and value
        // seperately.
        for (Map.Entry<Integer, String> entry : states.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
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

    public static void parsedStack(String input, Hashtable<String, String> pT, String[] rulesCollections) {

        Stack<Character> charStack = new Stack<>();
        charStack.push('0'); // Use '0' to represent state 0

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
            char currentState = charStack.peek();
            char currentSymbol = input.charAt(i);
            String action = pT.get(currentState + "" + currentSymbol);

            // Print the current state of the stack and input, and the action
            System.out.printf("%-15s %-15s %-10s\n", printStack(charStack), input.substring(i), action);

            // Format and print the current state of the stack and input
            if (action == null) {
                System.out.println("Input string " + input + " was not accepted.");
            }

            if (action.equals("accept")) {
                System.out.print("Input string " + "/'" + input + "/'" + " accepted");
                break;
            } else if (action.startsWith("s")) {
                int nextState = Integer.parseInt(action.substring(1));
                charStack.push(currentSymbol);
                charStack.push((char) (nextState + '0')); // Push next state as a character
                i++;
            } else if (action.startsWith("r")) {
                // Reduce action: pop symbols and states based on the rule being reduced
                int ruleNumber = Integer.parseInt(action.substring(1));
                String rule = rulesCollections[ruleNumber - 1];
                String rhs = rule.split("->")[1]; // Get the right-hand side of the rule

                // Pop states and symbols corresponding to the rule's right-hand side length
                for (int j = 0; j < rhs.length(); j++) {
                    charStack.pop(); // Pop symbol
                    charStack.pop(); // Pop state
                }

                // Get the non-terminal from the rule's left-hand side
                char lhs = rule.charAt(0);
                charStack.push(lhs); // Push the non-terminal

                // Perform a Goto action to determine the next state
                char gotoState = (char) (Integer.parseInt(pT.get(charStack.get(charStack.size() - 2) + "" + lhs))
                        + '0');
                charStack.push(gotoState);
            }
        }
    }

    public static String UserInput() {
        System.out.print("Enter a string: \n");
        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine();
        sc.close();
        return input;
    }

    public static String printStack(Stack<Character> stack) {
        StringBuilder printableStack = new StringBuilder();
        for (int i = 0; i < stack.size(); i++) {
            printableStack.append(stack.get(i)).append("");
        }
        return printableStack.toString();
    }
}