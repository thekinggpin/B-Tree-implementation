/**
 * This class represents a dictionary pair that is to be contained within the
 * leaf nodes of the B+ tree. The class implements the Comparable interface
 * so that the Container objects can be sorted later on.
 */
public class Container implements Comparable<Container> {
    private final Bplustree bplustree;
    int key;
    double value;

    /**
     * Constructor
     */
    public Container(Bplustree bplustree, int key, double value) {
        this.bplustree = bplustree;
        this.key = key;
        this.value = value;
    }

    /**
     * This is a method that allows comparisons to take place between
     * Container objects in order to sort them later on
     */
    @Override
    public int compareTo(Container o) {
        if (key == o.key) {
            return 0;
        } else if (key > o.key) {
            return 1;
        } else {
            return -1;
        }
    }
}
