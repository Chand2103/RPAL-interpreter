RPAL Interpreter
This repository contains an interpreter for the RPAL (Right-reference Pedagogic Algorithmic Language) language implemented in Java.

Getting Started
Follow the instructions below to clone, compile, and run the RPAL interpreter.

Prerequisites
Java Development Kit (JDK) installed (version 8 or higher recommended)

Git installed (to clone the repo)

Installation
Clone the repository:

bash
Copy
Edit
git clone https://github.com/yourusername/rpal-interpreter.git
cd rpal-interpreter
Compile the interpreter:

bash
Copy
Edit
javac myrpal.java
Usage
Run the interpreter with the following commands depending on the desired output:

To get just the output:

bash
Copy
Edit
java myrpal <filename>
To get output + Standardized Tree (ST):

bash
Copy
Edit
java myrpal -st <filename>
To get output + Abstract Syntax Tree (AST):

bash
Copy
Edit
java myrpal -ast <filename>
Explanation
-st: Displays the Standardized Tree along with the program output.

-ast: Displays the Abstract Syntax Tree along with the program output.

Example
bash
Copy
Edit
java myrpal example.rpal
java myrpal -st example.rpal
java myrpal -ast example.rpal
