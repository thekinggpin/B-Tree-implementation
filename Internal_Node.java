//All insert/delete/search operations directed here
public class Internal_Node extends Node {
    private final Bplustree bplustree;
    int maxDegree;
    int minDegree;
    int degree;
    Internal_Node leftSibling;
    Internal_Node rightSibling;
    Integer[] keys;
    Node[] childPointers;

    //Constructor
    public Internal_Node(Bplustree bplustree, int m, Integer[] keys) {
        this.bplustree = bplustree;
        this.maxDegree = m;
        this.minDegree = (int) Math.ceil(m / 2.0);
        this.degree = 0;
        this.keys = keys;
        this.childPointers = new Node[this.maxDegree + 1];
    }

    public Internal_Node(Bplustree bplustree, int m, Integer[] keys, Node[] pointers) {
        this.bplustree = bplustree;
        this.maxDegree = m;
        this.minDegree = (int) Math.ceil(m / 2.0);
        this.degree = bplustree.linearNullSearch(pointers);
        this.keys = keys;
        this.childPointers = pointers;
    }

    //Appends Child pointer at the end of the node
    public void appendChildPointer(Node pointer) {
        this.childPointers[degree] = pointer;
        this.degree++;
    }

    //returns index of pointer
    public int findIndexOfPointer(Node pointer) {
        for (int i = 0; i < childPointers.length; i++) {
            if (childPointers[i] == pointer) {
                return i;
            }
        }
        return -1;
    }

    // this method inserts child pointer
    public void insertChildPointer(Node pointer, int index) {
        for (int i = degree - 1; i >= index; i--) {
            childPointers[i + 1] = childPointers[i];
        }
        this.childPointers[index] = pointer;
        this.degree++;
    }

    // Checks for Node Deficiency
    public boolean isDeficient() {
        return this.degree < this.minDegree;
    }

    //Checks if a node can lend or not
    public boolean isLendable() {
        return this.degree > this.minDegree;
    }

    //Checks if a node is merg
    public boolean isMergeable() {
        return this.degree == this.minDegree;
    }

    //Checks if a node is overfull
    public boolean isOverfull() {
        return this.degree > this.maxDegree ;
    }

    //Pre-pends a child pointer
    public void prependChildPointer(Node pointer) {
        for (int i = degree - 1; i >= 0; i--) {
            childPointers[i + 1] = childPointers[i];
        }
        this.childPointers[0] = pointer;
        this.degree++;
    }
    //used for merging
    public void removeKey(int index) {
        this.keys[index] = null;
    }
//Delete with indexing
    public void removePointer(int index) {
        this.childPointers[index] = null;
        this.degree--;
    }

    //used in delete with pointer
    public void removePointer(Node pointer) {
        for (int i = 0; i < childPointers.length; i++) {
            if (childPointers[i] == pointer) {
                this.childPointers[i] = null;
            }
        }
        this.degree--;
    }
}
