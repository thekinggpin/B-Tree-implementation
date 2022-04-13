import java.util.Arrays;

//Leaf Node operations to check/insert/delete into key value pairs
public class Leaf_Node extends Node {
    private final Bplustree bplustree;
    int min_Number_Pairs;
    int max_Number_Pairs;
    int numPairs;
    Leaf_Node leftSibling;
    Leaf_Node rightSibling;
    Container[] dictionary;
    //setters
    public Leaf_Node(Bplustree bplustree, int m, Container dp) {
        this.bplustree = bplustree;
        this.min_Number_Pairs = (int) (Math.ceil(m / 2) - 1);
        this.max_Number_Pairs = m - 1;
        this.dictionary = new Container[m];
        this.numPairs = 0;

        this.insert_into_container(dp);
    }


    public Leaf_Node(Bplustree bplustree, int m, Container[] dps, Internal_Node parent) {
        this.bplustree = bplustree;
        this.min_Number_Pairs = (int) (Math.ceil(m / 2) - 1);
        this.max_Number_Pairs = m - 1;
        this.dictionary = dps;
        this.numPairs = bplustree.linearNullSearch(dps);
        this.parent = parent;
    }

    // Delete's/ Sets Null to an index(key)
    public void delete_container(int index) {

        // Delete dictionary pair from leaf
        this.dictionary[index] = null;

        // Decrement numPairs
        numPairs--;
    }

    // Insert into a container of key value pairs and sorts
    public boolean insert_into_container(Container dp) {
        if (this.isFull()) {

            /* Flow of execution goes here when numPairs == max_Number_Pairs */

            return false;
        } else {

            // Insert dictionary pair, increment numPairs, sort dictionary
            this.dictionary[numPairs] = dp;
            numPairs++;
            Arrays.sort(this.dictionary, 0, numPairs);

            return true;
        }
    }

    // Checks for Node Deficient
    public boolean isDeficient() {
        return numPairs < min_Number_Pairs;
    }

    // Checks if a leaf node is Full
    public boolean isFull() {
        return numPairs == max_Number_Pairs;
    }

    // This checks if a key-value pair is capable of lending pairs
    public boolean isLendable() {
        return numPairs > min_Number_Pairs;
    }

    // Checks if a leaf is mergeable
    public boolean isMergeable() {
        return numPairs == min_Number_Pairs;
    }
}
