# Kosaraju-Sharir Algorithm for Strongly Connected Components  
**CSCI 6410 – Design and Analysis of Algorithms (Assignment 3)**

---

## 6. Input Format

The program reads from **standard input (stdin)** in the following format:
n
e
a1 b1
a2 b2
…
ae be
Where:

- `n` = number of vertices in the directed graph  
  - Vertices are numbered from `0` to `n - 1`
- `e` = number of directed edges
- Each of the next `e` lines contains two integers `ai bi`, representing a **directed edge from `ai` to `bi`**

**Important notes:**
- Edges may appear in any order
- Self-loops are allowed (e.g., `7 7`)
- The input is guaranteed to be consistent

---

## 7. Output Format

The program prints two parts:

---

### **Part 1: Strongly Connected Components (SCCs)**
The given graph has K Strongly Connected Components.
Strongly Connected Component #0: v1,v2,v3.
Strongly Connected Component #1: v4.
…
- `K` = total number of SCCs
- Each SCC lists its vertices separated by commas and ends with a period
- Component numbering starts at 0
- The order of components depends on DFS finishing times (as per Kosaraju-Sharir)

---

### **Part 2: Kernel Graph (Condensation Graph)**

After printing the SCCs, the program prints the **Kernel Graph** in **the same format as the input graph**:
K
E
c1 c2
c3 c4
…
Where:

- Each vertex `0...K-1` represents one SCC
- There is a directed edge from component `ci` to `cj` **if there exists any edge in the original graph from a vertex in SCC `ci` to a vertex in SCC `cj`**
- **No parallel edges** are included (multiple edges between same components are printed only once)
- Self-loops (ci → ci) are **not printed**

---

## 8. Sample Input / Sample Output

### **Sample Input**
10
13
1 4
4 3
3 1
8 1
2 6
6 9
9 2
7 7
5 0
0 5
4 8
6 6
9 6
### **Sample Output**
The given graph has 4 Strongly Connected Components.
Strongly Connected Component #0: 1,4,3,8.
Strongly Connected Component #1: 2,6,9.
Strongly Connected Component #2: 7.
Strongly Connected Component #3: 5,0.

4
2
0 1
3 0
Explanation of output:
- The graph has 4 SCCs
- SCC #0 contains {1,4,3,8}
- SCC #1 contains {2,6,9}
- SCC #2 contains {7}
- SCC #3 contains {5,0}
- Kernel Graph has 4 vertices (one per SCC) and 2 directed edges

---

## 9. How to Compile and Run

### ✅ Option A: Compile and run locally (Java)

**Compile:**
```bash
javac scc.java
Run (waits for input):
java scc
Run with input file:
java scc < input.txt

Option B: Run with inline input (for quick testing)
echo -e "3\n3\n0 1\n1 2\n2 0" | java scc

Option C: Run on the ECU csstu server

1. Upload your files (scc.java, README.md) to the server (using scp or editing directly).

2. SSH into the server:
ssh yourpirateid@csstu.intra.ecu.edu

3. Compile on server:
javac scc.java

4. Run on server:
java scc < input.txt

or interactively:
java scc
(type input manually, press Ctrl+D when done)

