Course: CSCI 6410 — Fall 2025Assignment: #3 — Kosaraju–Sharir SCCLanguage/Runtime: Java 17 (tested with javac 17, java 17)Files: scc.java, readme

Build

javac scc.java

This produces scc.class.

Run

The program reads from stdin and writes to stdout.

# Example
cat << 'EOF' | java scc
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
EOF

Output format

SCC report (human-readable):

The given graph has K Strongly Connected Components.

For each component i: Strongly Connected Component #i: v1,v2,...,vk.

Kernel Graph (component DAG) in the same format as the input graph:

First line: number of kernel vertices (= K)

Second line: number of kernel edges

Next lines: one u v per edge, with parallel edges removed.

Note: Vertices within an SCC are printed in the order discovered by the second DFS pass. Kernel edges are printed in an arbitrary (but deterministic for a fixed JVM) order.

Algorithm & Complexit