import java.io.*;

/**
 * CSCI 6410 — Assignment 3
 * Kosaraju–Sharir algorithm for Strongly Connected Components (SCC)
 *
 * Requirements satisfied:
 *  - Adjacency list representation (array-based, O(V+E) memory)
 *  - Reverse graph built explicitly (also adjacency list)
 *  - First phase: DFS on reverse graph, push vertex onto an explicit stack
 *    after exploring (reverse postorder). Implemented with an iterative DFS
 *    that simulates recursion to avoid stack overflows.
 *  - Second phase: DFS on original graph in the order popped from the order stack.
 *  - Total time complexity: O(V + E)
 *  - No Java collections for core algorithm; minimal custom stacks and graph arrays.
 *  - Input from stdin, output to stdout as specified.
 *  - Also prints the Kernel Graph (component DAG) in the same input format
 *    (first the number of component-vertices, then number of edges, then the edge list),
 *    with parallel edges removed.
 */
public class scc {
    // -------- Fast input (ints only) --------
    static final class FastScanner {
        private final InputStream in;
        private final byte[] buffer = new byte[1 << 16];
        private int ptr = 0, len = 0;
        FastScanner(InputStream is) { this.in = is; }
        private int read() throws IOException {
            if (ptr >= len) {
                len = in.read(buffer);
                ptr = 0;
                if (len <= 0) return -1;
            }
            return buffer[ptr++];
        }
        int nextInt() throws IOException {
            int c, s = 1, x = 0;
            do { c = read(); } while (c <= ' ');
            if (c == '-') { s = -1; c = read(); }
            while (c > ' ') {
                x = x * 10 + (c - '0');
                c = read();
            }
            return x * s;
        }
    }

    // -------- Simple int stack --------
    static final class IntStack {
        int[] a; int top = 0;
        IntStack(int cap) { a = new int[Math.max(1, cap)]; }
        void push(int v) { if (top == a.length) grow(); a[top++] = v; }
        int pop() { return a[--top]; }
        int peek() { return a[top - 1]; }
        boolean isEmpty() { return top == 0; }
        int size() { return top; }
        void clear() { top = 0; }
        private void grow() {
            int[] b = new int[a.length << 1];
            System.arraycopy(a, 0, b, 0, a.length);
            a = b;
        }
    }

    // -------- Adjacency list using parallel arrays (head, to, next) --------
    static final class Graph {
        final int n, m;
        final int[] head;      // head[v] = index of first edge from v (or -1)
        final int[] to, next;  // edge i: v -> to[i], next[i] is next edge index from v
        private int edgePtr = 0;
        Graph(int n, int m) {
            this.n = n; this.m = m;
            head = new int[n];
            to = new int[m];
            next = new int[m];
            for (int i = 0; i < n; i++) head[i] = -1;
        }
        void addEdge(int u, int v) {
            to[edgePtr] = v;
            next[edgePtr] = head[u];
            head[u] = edgePtr++;
        }
    }

    // -------- Minimal open-addressing LongHashSet for (src,dst) pairs --------
    // Used to deduplicate edges in the kernel graph in O(1) expected time.
    static final class LongHashSet {
        private long[] keys;
        private boolean[] used;
        private int sz = 0;
        LongHashSet(int expected) {
            int cap = 1;
            while (cap < expected * 2) cap <<= 1; // load factor <= 0.5
            keys = new long[cap];
            used = new boolean[cap];
        }
        private int mask() { return keys.length - 1; }
        private int hash(long x) {
            x ^= (x >>> 33);
            x *= 0xff51afd7ed558ccdL;
            x ^= (x >>> 33);
            x *= 0xc4ceb9fe1a85ec53L;
            x ^= (x >>> 33);
            return (int)x & mask();
        }
        boolean add(long x) {
            int i = hash(x);
            while (used[i]) {
                if (keys[i] == x) return false;
                i = (i + 1) & mask();
            }
            used[i] = true; keys[i] = x; sz++;
            if (sz * 2 > keys.length) rehash();
            return true;
        }
        int size() { return sz; }
        long[] toArray() {
            long[] out = new long[sz];
            int k = 0;
            for (int i = 0; i < keys.length; i++) if (used[i]) out[k++] = keys[i];
            return out;
        }
        private void rehash() {
            long[] oldK = keys; boolean[] oldU = used;
            keys = new long[oldK.length << 1];
            used = new boolean[keys.length];
            sz = 0;
            for (int i = 0; i < oldK.length; i++) if (oldU[i]) add(oldK[i]);
        }
    }

    public static void main(String[] args) throws Exception {
        FastScanner fs = new FastScanner(System.in);
        int n = fs.nextInt();
        int m = fs.nextInt();
        Graph g = new Graph(n, m);
        Graph gr = new Graph(n, m);
        int[] eu = new int[m]; // store original edges for kernel construction
        int[] ev = new int[m];
        for (int i = 0; i < m; i++) {
            int u = fs.nextInt();
            int v = fs.nextInt();
            eu[i] = u; ev[i] = v;
            g.addEdge(u, v);
            gr.addEdge(v, u); // reverse graph
        }

        // ---- Phase 1: DFS on reverse graph to get reverse postorder (order stack) ----
        boolean[] seen = new boolean[n];
        IntStack order = new IntStack(n);

        // iterative DFS that produces reverse postorder by simulating recursion
        int[] it = new int[n]; // current edge iterator per node
        IntStack stack = new IntStack(n);
        boolean[] entered = new boolean[n];

        for (int s = 0; s < n; s++) if (!seen[s]) {
            // start DFS at s
            stack.push(s);
            entered[s] = true; seen[s] = true; it[s] = gr.head[s];
            while (!stack.isEmpty()) {
                int v = stack.peek();
                int ei = it[v];
                if (ei != -1) {
                    int to = gr.to[ei];
                    it[v] = gr.next[ei];
                    if (!seen[to]) {
                        seen[to] = true; it[to] = gr.head[to];
                        stack.push(to);
                    }
                } else {
                    // finished v
                    order.push(v);
                    stack.pop();
                }
            }
        }

        // ---- Phase 2: DFS on original graph in reverse postorder to assign components ----
        int[] comp = new int[n];
        for (int i = 0; i < n; i++) comp[i] = -1;
        int compCnt = 0;
        // reuse arrays
        seen = new boolean[n];
        it = new int[n];
        stack.clear();

        while (!order.isEmpty()) {
            int s = order.pop();
            if (comp[s] != -1) continue;
            // start DFS from s on G, assign comp id compCnt
            stack.push(s);
            comp[s] = compCnt; it[s] = g.head[s];
            while (!stack.isEmpty()) {
                int v = stack.peek();
                int ei = it[v];
                if (ei != -1) {
                    int w = g.to[ei];
                    it[v] = g.next[ei];
                    if (comp[w] == -1) {
                        comp[w] = compCnt; it[w] = g.head[w];
                        stack.push(w);
                    }
                } else {
                    stack.pop();
                }
            }
            compCnt++;
        }

        // ---- Gather vertices per component for printing ----
        int[] sz = new int[compCnt];
        for (int v = 0; v < n; v++) sz[comp[v]]++;
        int[][] members = new int[compCnt][];
        for (int i = 0; i < compCnt; i++) members[i] = new int[sz[i]];
        int[] idx = new int[compCnt];
        for (int v = 0; v < n; v++) {
            int c = comp[v];
            members[c][idx[c]++] = v;
        }

        // ---- Output SCC listing ----
        StringBuilder out = new StringBuilder();
        out.append("The given graph has ").append(compCnt).append(" Strongly Connected Components.\n");
        for (int c = 0; c < compCnt; c++) {
            out.append("Strongly Connected Component #").append(c).append(": ");
            for (int j = 0; j < members[c].length; j++) {
                out.append(members[c][j]);
                if (j + 1 < members[c].length) out.append(',');
            }
            out.append('.').append('\n');
        }

        // ---- Build Kernel Graph (component DAG), removing parallel edges ----
        LongHashSet edgeSet = new LongHashSet(m);
        for (int i = 0; i < m; i++) {
            int cu = comp[eu[i]];
            int cv = comp[ev[i]];
            if (cu != cv) {
                long key = (((long)cu) << 32) ^ (cv & 0xffffffffL);
                edgeSet.add(key);
            }
        }
        long[] kEdges = edgeSet.toArray();

        // Print SCC info first
        System.out.print(out.toString());

        // ---- Print Kernel Graph in input format (n_k, e_k, edges) ----
        // As no order was specified, we print edges in the arbitrary order returned by the set.
        StringBuilder kout = new StringBuilder();
        kout.append(compCnt).append('\n');
        kout.append(kEdges.length).append('\n');
        for (long ke : kEdges) {
            int u = (int)(ke >>> 32);
            int v = (int)ke;
            kout.append(u).append(' ').append(v).append('\n');
        }
        System.out.print(kout.toString());
    }
}
