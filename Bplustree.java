import java.lang.*;
import java.util.*;
import java.io.*;
/**
 * B+ tree has key-value Pairs only in its leaves, the rest internal nodes contain only keys.
 * Can have redundant Keys.
 *
 */

public class Bplustree {
    Internal_Node root_node;
    int degree;
    Leaf_Node leaf_node;

    public Bplustree(int m) {
        this.degree = m;
        this.root_node = null;
    }



    // insert key,value into tree
    public void insert_key_value(int key, double value){
        if (isEmpty()) {

            // Create leaf node as first node in B plus tree (root_node is null)
            Leaf_Node leaf = new Leaf_Node(this, this.degree, new Container(this, key, value));

            // Set as first leaf node (can be used later for in-order leaf traversal)
            this.leaf_node = leaf;

        } else {

            // Find leaf node to insert into
            Leaf_Node leaf = (this.root_node == null) ? this.leaf_node :
                    findLeafNode(key);

            // Insert into leaf node fails if node becomes overfull
            if (!leaf.insert_into_container(new Container(this, key, value))) {

                // Sort all the key-value pairs with the included pair to be inserted
                leaf.dictionary[leaf.numPairs] = new Container(this, key, value);
                leaf.numPairs++;
                sortDictionary(leaf.dictionary);

                // Split the sorted pairs into two halves
                int midpoint = getMidpoint();
                Container[] halfDict = splitContainer(leaf, midpoint);

                if (leaf.parent == null) {

                    // Create internal node to serve as parent, use key-value midpoint key
                    Integer[] parent_keys = new Integer[this.degree];
                    parent_keys[0] = halfDict[0].key;
                    Internal_Node parent = new Internal_Node(this, this.degree, parent_keys);
                    leaf.parent = parent;
                    parent.appendChildPointer(leaf);

                } else {


                    // Add new key to parent for proper indexing
                    int newParentKey = halfDict[0].key;
                    leaf.parent.keys[leaf.parent.degree - 1] = newParentKey;
                    Arrays.sort(leaf.parent.keys, 0, leaf.parent.degree);
                }

                // Create new Leaf_Node that holds the other half
                Leaf_Node newLeafNode = new Leaf_Node(this, this.degree, halfDict, leaf.parent);

                // Update child pointers of parent node
                int pointerIndex = leaf.parent.findIndexOfPointer(leaf) + 1;
                leaf.parent.insertChildPointer(newLeafNode, pointerIndex);

                // Make leaf nodes siblings of one another
                newLeafNode.rightSibling = leaf.rightSibling;
                if (newLeafNode.rightSibling != null) {
                    newLeafNode.rightSibling.leftSibling = newLeafNode;
                }
                leaf.rightSibling = newLeafNode;
                newLeafNode.leftSibling = leaf;

                if (this.root_node == null) {

                    // Set the root_node of B+ tree to be the parent
                    this.root_node = leaf.parent;

                } else {

                    Internal_Node in = leaf.parent;
                    while (in != null) {
                        if (in.isOverfull()) {
                            splitInternaleafode(in);
                        } else {
                            break;
                        }
                        in = in.parent;
                    }
                }
            }
        }
    }

    //simple tree traversal Search
    public Double search(int key) {

        // If B+ tree is completely empty, simply return null
        if (isEmpty()) { return null; }

        // Find leaf node that holds the key-value key
        Leaf_Node leaf = (this.root_node == null) ? this.leaf_node : findLeafNode(key);

        // Perform binary search to find index of key within key-value
        Container[] dps = leaf.dictionary;
        int index = binary_search(dps, leaf.numPairs, key);

        // If index negative, the key doesn't exist in B+ tree
        if (index < 0) {
            return null;
        } else {
            return dps[index].value;
        }
    }

    //Ranged search
    public ArrayList<Double> range_based_search(int lowerBound, int upperBound) {

        // Instantiate Double array to hold values
        ArrayList<Double> values = new ArrayList<Double>();

        // Iterate through the doubly linked list of leaves
        Leaf_Node currNode = this.leaf_node;
        while (currNode != null) {

            // Iterate through the key-value of each node
            Container dps[] = currNode.dictionary;
            for (Container dp : dps) {
                if (dp == null) { break; }

                // Include value if its key fits within the provided range
                if (lowerBound <= dp.key && dp.key <= upperBound) {
                    values.add(dp.value);
                }
            }

            currNode = currNode.rightSibling;

        }

        return values;
    }
    public void delete_key(int key) {

        if (isEmpty()) {

            System.err.println("Invalid Delete: The B+ tree is currently empty.");

        } else {

            // Get leaf node and attempt to find index of key to delete
            Leaf_Node leaf = (this.root_node == null) ? this.leaf_node : findLeafNode(key);
            int dpIndex = binary_search(leaf.dictionary, leaf.numPairs, key);


            if (dpIndex < 0) {

                /* Flow of execution goes here when key is absent in B+ tree */

                System.err.println("Invalid Delete: Key unable to be found.");

            } else {

                // Successfully delete the key-value pair
                leaf.delete_container(dpIndex);

                // Check for deficiencies
                if (leaf.isDeficient()) {

                    Leaf_Node sibling;
                    Internal_Node parent = leaf.parent;

                    // Borrow: First, check the left sibling, then the right sibling
                    if (leaf.leftSibling != null &&
                            leaf.leftSibling.parent == leaf.parent &&
                            leaf.leftSibling.isLendable()) {

                        sibling = leaf.leftSibling;
                        Container borrowedDP = sibling.dictionary[sibling.numPairs - 1];

						/* Insert borrowed key-value pair, sort key-value,
						   and delete key-value pair from sibling */
                        leaf.insert_into_container(borrowedDP);
                        sortDictionary(leaf.dictionary);
                        sibling.delete_container(sibling.numPairs - 1);

                        // Update key in parent if necessary
                        int pointerIndex = findIndexOfPointer(parent.childPointers, leaf);
                        if (!(borrowedDP.key >= parent.keys[pointerIndex - 1])) {
                            parent.keys[pointerIndex - 1] = leaf.dictionary[0].key;
                        }

                    } else if (leaf.rightSibling != null &&
                            leaf.rightSibling.parent == leaf.parent &&
                            leaf.rightSibling.isLendable()) {

                        sibling = leaf.rightSibling;
                        Container borrowedDP = sibling.dictionary[0];

						/* Insert borrowed key-value pair, sort key-value,
					       and delete key-value pair from sibling */
                        leaf.insert_into_container(borrowedDP);
                        sibling.delete_container(0);
                        sortDictionary(sibling.dictionary);

                        // Update key in parent if necessary
                        int pointerIndex = findIndexOfPointer(parent.childPointers, leaf);
                        if (!(borrowedDP.key < parent.keys[pointerIndex])) {
                            parent.keys[pointerIndex] = sibling.dictionary[0].key;
                        }

                    }

                    // Merge: First, check the left sibling, then the right sibling
                    else if (leaf.leftSibling != null &&
                            leaf.leftSibling.parent == leaf.parent &&
                            leaf.leftSibling.isMergeable()) {

                        sibling = leaf.leftSibling;
                        int pointerIndex = findIndexOfPointer(parent.childPointers, leaf);

                        // Remove key and child pointer from parent
                        parent.removeKey(pointerIndex - 1);
                        parent.removePointer(leaf);

                        // Update sibling pointer
                        sibling.rightSibling = leaf.rightSibling;

                        // Check for deficiencies in parent
                        if (parent.isDeficient()) {
                            handleDeficiency(parent);
                        }

                    } else if (leaf.rightSibling != null &&
                            leaf.rightSibling.parent == leaf.parent &&
                            leaf.rightSibling.isMergeable()) {

                        sibling = leaf.rightSibling;
                        int pointerIndex = findIndexOfPointer(parent.childPointers, leaf);

                        // Remove key and child pointer from parent
                        parent.removeKey(pointerIndex);
                        parent.removePointer(pointerIndex);

                        // Update sibling pointer
                        sibling.leftSibling = leaf.leftSibling;
                        if (sibling.leftSibling == null) {
                            leaf_node = sibling;
                        }

                        if (parent.isDeficient()) {
                            handleDeficiency(parent);
                        }
                    }

                } else if (this.root_node == null && this.leaf_node.numPairs == 0) {


                    // Set first leaf as null to indicate B+ tree is empty
                    this.leaf_node = null;

                } else {

                    sortDictionary(leaf.dictionary);

                }
            }
        }
    }

   public int binary_search(Container[] dps, int numPairs, int t) {

        //This method performs a binary search on a sorted kv pair using a comparator

        Comparator<Container> c = new Comparator<Container>() {
            @Override
            public int compare(Container o1, Container o2) {
                Integer a = Integer.valueOf(o1.key);
                Integer b = Integer.valueOf(o2.key);
                return a.compareTo(b);
            }
        };
        //Array Binary Search
        return Arrays.binarySearch(dps, 0, numPairs, new Container(this, t, 0), c);
    }

//Traverses down the tree
    public Leaf_Node findLeafNode(int key) {

        // Initialize key and index
        Integer[] bunch_of_keys = this.root_node.keys;
        int i;

        // Find next node on path to leaf node
        for (i = 0; i < this.root_node.degree - 1; i++) {
            if (key < bunch_of_keys[i]) { break; }
        }

        
        Node child = this.root_node.childPointers[i];
        if (child instanceof Leaf_Node) {
            return (Leaf_Node)child;
        } else {
            return findLeafNode((Internal_Node)child, key);
        }
    }

    public Leaf_Node findLeafNode(Internal_Node node, int key) {

        // Initialize keys and index variable
        Integer[] keys = node.keys;
        int i;

        // Find next node on path to appropriate leaf node
        for (i = 0; i < node.degree - 1; i++) {
            if (key < keys[i]) { break; }
        }

		/* Return node if it is a Leaf_Node object,
		   otherwise repeat the search function a level down */
        Node childNode = node.childPointers[i];
        if (childNode instanceof Leaf_Node) {
            return (Leaf_Node)childNode;
        } else {
            return findLeafNode((Internal_Node)node.childPointers[i], key);
        }
    }

    // Works as a pointer locator
    public int findIndexOfPointer(Node[] pointers, Leaf_Node node) {
        int i;
        for (i = 0; i < pointers.length; i++) {
            if (pointers[i] == node) { break; }
        }
        return i;
    }

// mid index finder
    public int getMidpoint() {
        return (int)Math.ceil((this.degree + 1) / 2.0) - 1;
    }

//Does borrowing and merging
    public void handleDeficiency(Internal_Node in) {

        Internal_Node sibling;
        Internal_Node parent = in.parent;

        // Remedy deficient root_node node
        if (this.root_node == in) {
            for (int i = 0; i < in.childPointers.length; i++) {
                if (in.childPointers[i] != null) {
                    if (in.childPointers[i] instanceof Internal_Node) {
                        this.root_node = (Internal_Node)in.childPointers[i];
                        this.root_node.parent = null;
                    } else if (in.childPointers[i] instanceof Leaf_Node) {
                        this.root_node = null;
                    }
                }
            }
        }

        // Borrow:
        else if (in.leftSibling != null && in.leftSibling.isLendable()) {
            sibling = in.leftSibling;
        } else if (in.rightSibling != null && in.rightSibling.isLendable()) {
            sibling = in.rightSibling;

            // Copy 1 key and pointer from sibling (atm just 1 key)
            int borrowedKey = sibling.keys[0];
            Node pointer = sibling.childPointers[0];

            // Copy root_node key and pointer into parent
            in.keys[in.degree - 1] = parent.keys[0];
            in.childPointers[in.degree] = pointer;

            // Copy borrowedKey into root_node
            parent.keys[0] = borrowedKey;

            // Delete key and pointer from sibling
            sibling.removePointer(0);
            Arrays.sort(sibling.keys);
            sibling.removePointer(0);
            shiftDown(in.childPointers, 1);
        }

        // Merge:
        else if (in.leftSibling != null && in.leftSibling.isMergeable()) {

        } else if (in.rightSibling != null && in.rightSibling.isMergeable()) {
            sibling = in.rightSibling;


            // delete key from parent
            sibling.keys[sibling.degree - 1] = parent.keys[parent.degree - 2];
            Arrays.sort(sibling.keys, 0, sibling.degree);
            parent.keys[parent.degree - 2] = null;

            // Copy in's child pointer over to sibling's list of child pointers
            for (int i = 0; i < in.childPointers.length; i++) {
                if (in.childPointers[i] != null) {
                    sibling.prependChildPointer(in.childPointers[i]);
                    in.childPointers[i].parent = sibling;
                    in.removePointer(i);
                }
            }

            // Delete child pointer from grandparent to deficient node
            parent.removePointer(in);

            // Remove left sibling
            sibling.leftSibling = in.leftSibling;
        }


        if (parent != null && parent.isDeficient()) {
            handleDeficiency(parent);
        }
    }


    public boolean isEmpty() {
        return leaf_node == null;
    }
    
    public int linearNullSearch(Container[] dps) {
        for (int i = 0; i <  dps.length; i++) {
            if (dps[i] == null) { return i; }
        }
        return -1;
    }

    //Returns Null search
    public int linearNullSearch(Node[] pointers) {
        for (int i = 0; i <  pointers.length; i++) {
            if (pointers[i] == null) { return i; }
        }
        return -1;
    }

    //This method is used to shift down a set of pointers that are prepended

    public void shiftDown(Node[] pointers, int amount) {
        Node[] newPointers = new Node[this.degree + 1];
        for (int i = amount; i < pointers.length; i++) {
            newPointers[i - amount] = pointers[i];
        }
        pointers = newPointers;
    }

    // A sorter to handle null values
    public void sortDictionary(Container[] dictionary) {
        Arrays.sort(dictionary, new Comparator<Container>() {
            @Override
            public int compare(Container o1, Container o2) {
                if (o1 == null && o2 == null) { return 0; }
                if (o1 == null) { return 1; }
                if (o2 == null) { return -1; }
                return o1.compareTo(o2);
            }
        });
    }
    public Node[] splitChildPointers(Internal_Node in, int split) {

        Node[] pointers = in.childPointers;
        Node[] halfPointers = new Node[this.degree + 1];

        // Copy half of the values into halfPointers while updating original keys
        for (int i = split + 1; i < pointers.length; i++) {
            halfPointers[i - split - 1] = pointers[i];
            in.removePointer(i);
        }

        return halfPointers;
    }

    //Splits a container
    public Container[] splitContainer(Leaf_Node leaf, int split) {

        Container[] dictionary = leaf.dictionary;
        Container[] halfDict = new Container[this.degree];

        // Copy half of the values into halfDict
        for (int i = split; i < dictionary.length; i++) {
            halfDict[i - split] = dictionary[i];
            leaf.delete_container(i);
        }

        return halfDict;
    }


    // when an internal node is overfull for an insert then this method is called

    public void splitInternaleafode(Internal_Node in) {

        // Acquire parent
        Internal_Node parent = in.parent;

        // Split keys and pointers in half
        int midpoint = getMidpoint();
        int newParentKey = in.keys[midpoint];
        Integer[] halfKeys = splitKeys(in.keys, midpoint);
        Node[] halfPointers = splitChildPointers(in, midpoint);

        // Change degree of original Internal_Node in
        in.degree = linearNullSearch(in.childPointers);

        // Create new sibling internal node and add half of keys and pointers
        Internal_Node sibling = new Internal_Node(this, this.degree, halfKeys, halfPointers);
        for (Node pointer : halfPointers) {
            if (pointer != null) { pointer.parent = sibling; }
        }

        // Make internal nodes siblings of one another
        sibling.rightSibling = in.rightSibling;
        if (sibling.rightSibling != null) {
            sibling.rightSibling.leftSibling = sibling;
        }
        in.rightSibling = sibling;
        sibling.leftSibling = in;

        if (parent == null) {

            // Create new root_node node and add midpoint key and pointers
            Integer[] keys = new Integer[this.degree];
            keys[0] = newParentKey;
            Internal_Node newRoot = new Internal_Node(this, this.degree, keys);
            newRoot.appendChildPointer(in);
            newRoot.appendChildPointer(sibling);
            this.root_node = newRoot;

            // Add pointers from children to parent
            in.parent = newRoot;
            sibling.parent = newRoot;

        } else {

            // Add key to parent
            parent.keys[parent.degree - 1] = newParentKey;
            Arrays.sort(parent.keys, 0, parent.degree);

            // Set up pointer to new sibling
            int pointerIndex = parent.findIndexOfPointer(in) + 1;
            parent.insertChildPointer(sibling, pointerIndex);
            sibling.parent = parent;
        }
    }

   //Splitting an internal node object
    public Integer[] splitKeys(Integer[] keys, int split) {

        Integer[] halfKeys = new Integer[this.degree];

        // Remove split-indexed value from keys
        keys[split] = null;

        // Copy half of the values into halfKeys while updating original keys
        for (int i = split + 1; i < keys.length; i++) {
            halfKeys[i - split - 1] = keys[i];
            keys[i] = null;
        }

        return halfKeys;
    }

    //Delete a key from the tree

    public static void main(String[] args) {


        if (args.length != 1) {
            System.err.println("usage: java Bplustree <file_name>");
            System.exit(-1);
        }

        // Reading from file
        String fileName = args[0];
        try {

            // Prepare to read input file read
            File file = new File(System.getProperty("user.dir") + "/" + fileName);
            Scanner sc = new Scanner(file);

            // Create O/P file in which search results will be stored
            FileWriter logger = new FileWriter("output_file.txt", false);
            boolean firstLine = true;

            // Create initial B+ tree
            Bplustree bplustree = null;

            // Perform an operation for each line in the input file
            while (sc.hasNextLine()) {
                String line = sc.nextLine().replace(" ", "");
                String[] tokens = line.split("[(,)]");

                switch (tokens[0]) {

                    case "Initialize":
                        bplustree = new Bplustree(Integer.parseInt(tokens[1]));
                        break;

                    // Insert a key-value pair
                    case "Insert":
                        bplustree.insert_key_value(Integer.parseInt(tokens[1]), Double.parseDouble(tokens[2]));
                        break;

                    // Delete a key-value pair
                    case "Delete":
                        bplustree.delete_key(Integer.parseInt(tokens[1]));
                        break;

                    // Perform a search operation
                    case "Search":
                        String result = "";

                        //  search for a range of operation
                        if (tokens.length == 3) {
                            ArrayList<Double> values = bplustree.range_based_search(
                                    Integer.parseInt(tokens[1]),
                                    Integer.parseInt(tokens[2]));

                            // Record search result as a String
                            if (values.size() != 0) {
                                for (double v : values) { result += v + ", "; }
                                result = result.substring(0, result.length() - 2);
                            } else {
                                result = "Null";
                            }

                        }

                        // Perform search operation
                        else {

                            Double value = bplustree.search(Integer.parseInt(tokens[1]));
                            result = (value == null) ? "Null" :
                                    Double.toString(value);
                        }

                        // O/P search result in .txt file
                        if (firstLine) {
                            logger.write(result);
                            firstLine = false;
                        } else {
                            logger.write("\n" + result);
                        }
                        logger.flush();

                        break;
                    default:
                        throw new IllegalArgumentException("\"" + tokens[0] +
                                "\"" + " is an unacceptable input.");
                }
            }

            // Close O/P file
            logger.close();

        } catch (FileNotFoundException e) {
            System.err.println(e);
        } catch (IllegalArgumentException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

}