# Java Parser and State Diagram Generator

This project implements a parser for a set of production rules using the LR(1) parsing algorithm. It constructs a state diagram and parsing table based on the given grammar rules. The project reads rules from a file, generates the LR(1) states, and builds the parsing table. It then checks if a given input string can be parsed using the constructed parsing table.

## Features

- **LR(1) Parser**: Constructs the parsing table for the LR(1) grammar.
- **State Diagram**: Constructs the state transitions for the LR(1) parser.
- **Input Parsing**: Reads and parses the input rules from a file and evaluates the given input string.

## How It Works

1. **File Input**: 
    - The program reads the grammar rules from a file named `rules` located in the project's root directory.
2. **Rule Processing**:
    - The program identifies non-terminals, terminals, and generates an augmented grammar.
    - It then constructs the LR(1) state diagram and parses the input string using the generated states.
3. **Output**:
    - Displays the LR(1) parsing states.
    - Displays the GOTO transitions.
    - Builds and displays the parsing table.
    - Parses the user input string and determines if it is accepted by the grammar.

## How to Use

1. **Add Rules**: Place your grammar rules in a file named `rules` in the project's root directory.
2. **Run the Program**: Compile and run the `Main` class. The program will read the rules, generate the state diagram and parsing table, and then prompt you to enter a string for parsing.
3. **Input Parsing**: Enter a string, and the program will check if it is accepted based on the parsing table.

## Example

### Input Rules:
S->AA 
A->aA 
A->b
### User Input String:
baab
### Output:
r0: S'->S r1: S->AA r2: A->aA r3: A->b NonTerminals: S,A Terminals: a,b ... Input string 'baab' accepted

## Requirements

- **Java 8 or higher**
- **File `rules` in project root** with grammar rules

## Project Structure

- **Main.java**: The main entry point for the program, which handles reading the grammar rules, constructing the states, generating the parsing table, and parsing the input string.
- **Auxiliary Methods**: Various helper methods for:
  - Finding non-terminals and terminals.
  - Constructing closures and GOTO transitions.
  - Displaying the state diagram and parsing table.
  - Parsing the input string using the stack-based parsing algorithm.

## License

This project is licensed under the MIT License.

