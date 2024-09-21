package com.example._pill_20102669_datastruc;


public class UnionFind {

    public int[] height;
    public int[] parent;
    public UnionFind(int size) {
        height = new int[size];
        parent = new int[size];
        for (int i = 0; i < size; i++) {
            parent[i] = i;
        }
    }

    public int find(int x) {     // Find the root of the set that element x belongs to with path compression.
        if (parent[x] == -1) return -1;           // If x is not part of any set (represented by -1), return -1.

        if (parent[x] != x) {        // If x is not the root, recursively find the root and apply path compression.
            parent[x] = find(parent[x]);
        }
        return parent[x]; // Return the root of the set.
    }

    public void union(int x, int y) {     // Union two sets containing elements x and y using union by rank.
        int rootX = find(x);
        int rootY = find(y);

        if (rootX == rootY) return;        // If both elements are already in the same set, do nothing.

        if (height[rootX] < height[rootY]) {            // Union by rank: attach the shorter tree under the taller tree.
            parent[rootX] = rootY; // RootX's tree is shorter, so attach it to rootY.
        } else if (height[rootX] > height[rootY]) {
            parent[rootY] = rootX; // RootY's tree is shorter, so attach it to rootX.
        } else {
            // If both trees have the same height, attach rootY's tree to rootX and increment rootX's height.
            parent[rootY] = rootX;
            height[rootX]++;
        }
    }

    public void displayDSAsText(int width) {      // Display the disjoint set structure as text with a specified width per line.
        // Iterate through the parent array and print the root of each element.
        for (int i = 0; i < parent.length; i++) {
            System.out.print(find(i) + ((i + 1) % width == 0 ? "\n" : " "));
        }
    }
}