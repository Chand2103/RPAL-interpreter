# RPAL Interpreter

This repository contains an interpreter for the RPAL (Right-reference Pedagogic Algorithmic Language) language implemented in Java.

## Getting Started

Follow the instructions below to clone, compile, and run the RPAL interpreter.

### Prerequisites

- Java Development Kit (JDK) installed (version 8 or higher recommended)
- Git installed (to clone the repo)

### Installation

1. **Clone the repository:**

   ```bash
   git clone https://github.com/Chand2103/RPAL-interpreter.git
   cd rpal-interpreter
   ```

2. **Compile the interpreter:**

   ```bash
   javac myrpal.java
   ```
   or using mingw
   ```bash
   mingw32-make 
   ```   

## Usage

Run the interpreter with the following commands depending on the desired output:

- **To get just the output:**

  ```bash
  java myrpal <filename>
  ```

- **To get only the Standardized Tree (ST):**

  ```bash
  java myrpal -st <filename>
  ```

- **To get only the Abstract Syntax Tree (AST):**

  ```bash
  java myrpal -ast <filename>
  ```

### Explanation

- `-st`: Displays only the Standardized Tree
- `-ast`: Displays only the Abstract Syntax Tree
- **Combined switches** (`-ast -st` or `-st -ast`): Displays **both trees**, with the **Abstract Syntax Tree (AST)** shown **first**, followed by the **Standardized Tree (ST)** — **regardless of the order** in which the switches are passed.

## Example

```bash
java myrpal example.rpal
java myrpal -st example.rpal
java myrpal -ast example.rpal
```

