# Parsing Table 
LR(1) parsing table implementation in Java

Introduction
This project is a Java implementation of an LR(0) parser, a type of bottom-up parser used for syntax analysis in compilers. The program reads a context-free grammar from a file, constructs the LR(0) parsing table, and then parses user-provided input strings to determine if they belong to the language defined by the grammar.

Features
Grammar Input: Reads grammar rules from a rules file located in the project directory.
Parsing Table Generation: Constructs the LR(0) parsing table based on the provided grammar.
Parsing Process: Parses input strings entered by the user and reports acceptance or rejection.
Detailed Output: Displays the parsing table, states, and step-by-step parsing actions.
Requirements
Java Development Kit (JDK): Version 8 or higher.
Text File: A rules file containing the grammar rules in the specified format.
Development Environment: Any Java IDE or a text editor with command-line compilation.
How to Use
1. Prepare the Grammar Rules
Create a file named rules in the project directory.

Write your grammar rules in the following format:

css
Copy code
S->A
A->aA
A->b
Each rule should be on a separate line, with the left-hand side (non-terminal), followed by ->, and then the right-hand side.

2. Compile the Program
Open a command prompt or terminal in the project directory and run:

bash
Copy code
javac Main.java
3. Run the Program
Execute the compiled program:

bash
Copy code
java Main
4. Enter Input Strings
When prompted, input a string to parse according to the grammar:

c
Copy code
Enter a string:
aab
The program will display the parsing process and indicate whether the string is accepted or rejected.

Sample Output
vbnet
Copy code
r0: S'->S
r1: S->A
r2: A->aA
r3: A->b
NonTerminals: S,A
Terminals: a,b
I0: S'->.S,S->.A,A->.aA,A->.b
I1: S'->S.
I2: S->A.
I3: A->a.A
I4: A->b.
I5: A->aA.
I6: A->a.A
Gotos: 0S1,0A2,0a3,0b4,2A5,3A6,3a3,3b4
The parsing table:
                  action                goto
state       a       b       $       S       A
0        s3      s4                1       2
1                        accept
2                        r1
3        s3      s4                -       6
4                        r3
5                        r2
6        s3      s4                -       6
Enter a string:
aab
Stack          Input           Action
0              aab$            s3
0 3            ab$             s3
0 3 3          b$              s4
0 3 3 4        $               r3
0 3 3          $               r3
0 3            $               r3
0              $               r1
0 2            $               r1
0              $               accept
Input string 'aab$' accepted
Notes
Grammar Restrictions: The program assumes the grammar is suitable for LR(0) parsing and does not handle conflicts or ambiguities.
File Location: Ensure the rules file is in the same directory where the program is executed.
Error Handling: The program provides basic error messages if the input string is not accepted.
Understanding the Code
The main components of the program are:

Reading Grammar Rules: The program reads rules from the rules file and processes them to identify terminals and non-terminals.
Closure and Goto Functions: Implements the LR(0) item closure and Goto functions to build the canonical collection of LR(0) items.
Parsing Table Construction: Creates the action and goto tables used for parsing input strings.
Parsing Process: Uses a stack to simulate the parsing process, displaying each action taken.
Extending the Program
Grammar Validation: Enhance the program to check for grammar conflicts and report them.
Enhanced Parsing: Modify the parser to handle SLR(1) or LR(1) grammars for more complex language constructs.
User Interface: Develop a graphical user interface (GUI) for easier interaction.
License
This project is open-source and available for modification and distribution. Please attribute the original work when making significant changes.
