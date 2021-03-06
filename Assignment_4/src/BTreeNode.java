public class BTreeNode {
    //FIELDS
    /**
     * The tree's constant.
     */
    private final int T_VAR;
    /**
     * Array of the keys stored in the node.
     */
    private final String[] keys;
    /**
     * Array of pointers to the nodes children
     */
    private final BTreeNode[] children;
    /**
     * The number of keys in node.
     */
    private int n;
    private boolean isLeaf;

    /**
     * creates a new node which is a leaf, and creates the children and keys arrays with the t inserted
     *
     * @param t the parameter of the tree
     */
    public BTreeNode(int t) {
        this.T_VAR = t;
        isLeaf = true;
        keys = new String[2 * t - 1];
        children = new BTreeNode[2 * t];
        n = 0;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    // Getters and Setters
    public void setLeaf(boolean leaf) {
        isLeaf = leaf;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public String[] getKeys() {
        return keys;
    }

    /**
     * Gets the key in given index in the keys array.
     *
     * @param i the index of the key to get.
     * @return a String with the key.
     */
    public String getKey(int i) {
        if (i < 0 || i >= n) {
            throw new IllegalArgumentException("Index: " + i + " n: " + n);
        }
        return keys[i];
    }

    /**
     * Sets the key in given index in the keys array to given key.
     *
     * @param i   the index to set.
     * @param str the new key to set the place in the array to to.
     */
    private void setKey(int i, String str) {
        if (i < 0 || i > n) {
            throw new IllegalArgumentException("Index: " + i + " n: " + n);
        }
        keys[i] = str;
    }

    /**
     * outputs the node in a specified index
     *
     * @param i index in the children array
     * @return the node located in this index
     */
    public BTreeNode getChild(int i) {
        if (i < 0 || i > n) {
            throw new IllegalArgumentException("Index: " + i + "," + " n: " + n);
        }
        return children[i];
    }

    /**
     * Sets the pointer in given index of the children array to given node.
     *
     * @param i    the index of the child to set.
     * @param node the new node to set the child pointer to.
     */
    public void setChild(int i, BTreeNode node) {
        if (i < 0 || i > n + 1) {
            throw new IllegalArgumentException("Index: " + i + " n: " + n);
        }
        children[i] = node;
    }

    //SEARCH

    /**
     * Searches for a given key in the tree.
     *
     * @param key the key to search for.
     * @return an ordered pair of a pointer to this node
     * and the index of the key in it's keys array.
     */
    public OrderedPair search(String key) {
        int i = findExpectedIndexOfKey(key);
        if (i < getN() && getKey(i).equals(key)) {
            return new OrderedPair(this, i);
        }
        else if (isLeaf()) {
            return null;
        }
        else {
            return getChild(i).search(key);
        }
    }

    // INSERT METHODS

    /**
     * Inserts given key to it's place in the subtree of this node.
     *
     * @param key the key to insert.
     */
    public void insert(String key) {
        // If this node is a leaf then the key is inserted in the right place
        // in the keys array.
        if (isLeaf()) {
            insertToKeysArray(key);
            return;
        }
        int i = findExpectedIndexOfKey(key);
        // If the node is full we split it.
        if (getChild(i).getN() == 2 * T_VAR - 1) {
            splitChild(i);
        }
        insertToCorrectChild(key, i);
    }

    /**
     * Finds the expected index of the keys if it was in the current node's keys array.
     * This helps to find the index of the child in which the key should be.
     *
     * @param key the key this searches for.
     * @return the index of key, or the one it would have if it was in the keys array.
     */
    private int findExpectedIndexOfKey(String key) {
        int i = 0;
        while (i < getN() && getKey(i).compareTo(key) < 0) {
            i++;
        }
        return i;
    }

    /**
     * Splits the child in given index to 2 children, and puts the middle key
     * of the child between the 2 pointers to the new children.
     *
     * @param index the index of the child to split.
     */
    private void splitChild(int index) {
        BTreeNode splitChild = getChild(index);
        BTreeNode newChild = createNodeForSplit(splitChild);

        if (!splitChild.isLeaf()) {
            transferChildren(splitChild, newChild);
        }
        insertMedianKey(index, this, splitChild);
        insertNewChild(index, this, newChild);
        splitChild.setN(T_VAR - 1);
    }

    /**
     * Sub method of insert that inserts the median key of the split child
     * to it's right place in the father node
     *
     * @param index      the index to insert the key to
     * @param father     the father node of the split child
     * @param splitChild the split child.
     */
    private void insertMedianKey(int index, BTreeNode father, BTreeNode splitChild) {
        for (int i = father.getN(); i > index; i--) {
            father.setKey(i, this.getKey(i - 1));
        }
        setKey(index, splitChild.getKey(T_VAR - 1));
        setN(getN() + 1);
    }

    /**
     * Creates the new right node made from splitting the child, and then copies all
     * keys that come after the median from the split node to the new one.
     *
     * @param splitChild the child node to create the new node from.
     * @return the new right node.
     */
    public BTreeNode createNodeForSplit(BTreeNode splitChild) {
        BTreeNode newChild = new BTreeNode(T_VAR);
        newChild.setLeaf(splitChild.isLeaf());
        newChild.setN(T_VAR - 1);
        int index2 = 0; //the index of the keys array of the new node
        for (int i = T_VAR; i < splitChild.getN(); i++) {
            newChild.setKey(index2, splitChild.getKey(i));
            splitChild.setKey(i, null);
            index2++;
        }
        return newChild;
    }

    /**
     * Copies the children's list of the split child from it's median,
     * to the new child's empty children's list.
     *
     * @param splitChild the child being split.
     * @param newChild   the new child created from the split.
     */
    void transferChildren(BTreeNode splitChild, BTreeNode newChild) {
        int indexOther = 0;
        for (int i = T_VAR; i <= 2 * T_VAR - 1; i++) {
            newChild.setChild(indexOther, splitChild.getChild(i));
            splitChild.setChild(i, null);
            indexOther++;
        }
    }

    /**
     * Sub method of insert that clears a place for the new child in the father
     * node's children array, right after the split child.
     *
     * @param index    the index of the split child.
     * @param father   the father node of the new child and the split child.
     * @param newChild the new child to insert.
     */
    private void insertNewChild(int index, BTreeNode father, BTreeNode newChild) {
        for (int i = father.getN(); i > index; i--) {
            father.setChild(i, father.getChild(i - 1));
        }
        father.setChild(index + 1, newChild);
    }

    /**
     * Sub method of insert that inserts a key to this nodes keys array.
     *
     * @param key the key to insert.
     */
    private void insertToKeysArray(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        int i = getN() - 1;
        while (i >= 0 && getKey(i).compareTo(key) > 0) {
            setKey(i + 1, getKey(i));
            i--;
        }
        setKey(i + 1, key);
        setN(getN() + 1);
    }

    /**
     * Sub method of insert used to decide on which of the 2 new children
     * to call the recursive insertion on.
     *
     * @param key the key to insert.
     * @param i   the index of the key between the 2 new children.
     */
    private void insertToCorrectChild(String key, int i) {
        if (i == getN() || key.compareTo(getKey(i)) < 0) {
            getChild(i).insert(key);
        }
        else {
            getChild(i + 1).insert(key);
        }
    }

    //DELETE

    /**
     * Deletes given key from the sub tree of this node, and while climbing down the tree
     * makes sure that the deletion would be possible by manipulating the tree's nodes.
     *
     * @param key        to remove.
     * @param childIndex the index of the current node in the father's children array.
     * @param father     the father of the current node.
     */
    public void delete(String key, int childIndex, BTreeNode father) {
        if (getN() < T_VAR) {
            handleCase1(childIndex, father);

            //Sends delete back to the father to find the new child
            father.handleCase4(key);
        }
        else {
            boolean keyExist = keyExist(key);
            if (keyExist) {
                if (!isLeaf()) {
                    handleCase2(key);
                }

                else {
                    deleteKey(key); //CASE 3
                }
            }
            else if (isLeaf()) {
                System.out.println("Key '" + key + "' is not in the tree.");
            }

            else { //Key not in the node
                handleCase4(key);
            }
        }

    }

    /**
     * Checks if a key is in the node
     *
     * @param key to check if the key exists in the node
     * @return true - if it exists in the node and false otherwise.
     */
    public boolean keyExist(String key) {
        int index = -1;
        for (int i = 0; i < getN() && getKey(i).compareTo(key) <= 0; i++) {
            if (getKey(i).equals(key)) {
                index = i;
            }
        }
        return index != -1;
    }

    //CASE 1

    /**
     * Handles case 1 of the algorithm: if a node has less than t-1 keys.
     * Checks if the child in childIndex have a sibling with more than t-1 keys and re-arranges the tree.
     * If both of the child's siblings have t-1 keys, the function merges the child with one of its siblings.
     *
     * @param childIndex the index of the node in his father's array with less than t-1 keys.
     * @param father     the father of the node.
     */
    public void handleCase1(int childIndex, BTreeNode father) {
        int siblingIndex = checkSiblings(childIndex, father);
        if (siblingIndex != -1) {
            handleCase1a(childIndex, siblingIndex, father);
        }
        else { //Its siblings have t-1 keys
            int siblingToMerge = chooseASibling(father, childIndex);
            handleCase1b(siblingToMerge, father, childIndex);
        }
    }

    //CASE 1a

    /**
     * Handles case 1b of the algorithm - merges 2 siblings into one node.
     * Finds the median key in the father's node and merges the key,the child and the sibling to one node.
     *
     * @param siblingToMerge the index of the sibling.
     * @param father         the father of the child and the sibling.
     * @param childIndex     the index of the child.
     */
    private void handleCase1b(int siblingToMerge, BTreeNode father, int childIndex) {
        BTreeNode sibling = father.getChild(siblingToMerge);
        BTreeNode child = father.getChild(childIndex);
        String key = determineMedianKey(father, siblingToMerge, childIndex);
        if (siblingToMerge > childIndex) {
            father.mergeChildrenWithKeyAndPlaceMerged(key, childIndex, child, sibling);
        }
        else {
            father.mergeChildrenWithKeyAndPlaceMerged(key, siblingToMerge, sibling, child);
        }
    }

    /**
     * Determines the index of the median key in the father's node to be merged with the child and a sibling.
     *
     * @param father       the father node
     * @param siblingIndex the index of the sibling in the father's node.
     * @param childIndex   the index of the child in the father's node.
     * @return the median key in the correct index.
     */
    private String determineMedianKey(BTreeNode father, int siblingIndex, int childIndex) {
        if (siblingIndex > childIndex) return father.getKey(childIndex);
        else return father.getKey(siblingIndex);
    }

    /**
     * Handles the case in which at least on of a node's siblings has more than t-1 keys
     *
     * @param childIndex   the node of the child who has t-1 keys
     * @param siblingIndex the index of the sibling with more than t-1 keys
     * @param father       father of each nodes
     */
    public void handleCase1a(int childIndex, int siblingIndex, BTreeNode father) {
        BTreeNode child = father.getChild(childIndex);
        BTreeNode sibling = father.getChild(siblingIndex);
        int keyIndexToChange = extractIndex(childIndex, siblingIndex);
        changeKeysAndChild(keyIndexToChange, father, sibling, child, siblingIndex, childIndex);
    }

    /**
     * Makes the various changes to the keys and children to be made in case 1a.
     * Calculates the location of the median key in the father's keys array and insert it to the child.
     * Adds a child of the sibling to the child's children array and deletes what we transferred to the father and
     * the child from the sibling.
     *
     * @param keyIndexToChange the index in the father's array keys array.
     * @param father           the father of the sibling and the child.
     * @param sibling          the sibling of the child.
     * @param child            the child to change the keys in.
     */
    public void changeKeysAndChild(int keyIndexToChange, BTreeNode father, BTreeNode sibling, BTreeNode child, int siblingIndex, int childIndex) {
        String median;
        if (siblingIndex > childIndex) {
            median = sibling.getKey(0); //min
        }
        else {
            median = sibling.getKey(sibling.getN() - 1);//max
        }

        String moveToChild = father.getKey(keyIndexToChange);
        father.setKey(keyIndexToChange, median);
        addAndDeleteOne(sibling, child, siblingIndex, childIndex, moveToChild);
    }

    /**
     * Adds a key a child of the sibling and deletes a key and the child from the sibling.
     * Adds one key from the father and insert it to the child, and adds a child if the child or the sibling ar not leafs.
     * Deletes the key we added to the father node in the sibling's keys array.
     * Also deletes the child we added to the child's children array.
     *
     * @param sibling      the sibling of the child to delete a key and a child from.
     * @param child        the child to add a key and a child to
     * @param siblingIndex the index of the sibling
     * @param childIndex   the index of the child
     * @param moveToChild  the string to insert into the child.
     */

    private void addAndDeleteOne(BTreeNode sibling, BTreeNode child, int siblingIndex, int childIndex, String moveToChild) {
        addOneKey(child, moveToChild, childIndex, siblingIndex);
        addOneChild(child, sibling, childIndex, siblingIndex);
        deleteOne(sibling, siblingIndex, childIndex);

    }

    /**
     * Adds the median key from the father node to the child's keys array in the correct position (last or first)
     *
     * @param child        the child we'll add the key to
     * @param toAdd        the median key we will add to the child
     * @param childIndex   the index of the child
     * @param siblingIndex the index of the sibling
     */
    private void addOneKey(BTreeNode child, String toAdd, int childIndex, int siblingIndex) {
        if (siblingIndex > childIndex) {
            child.setKey(child.getN(), toAdd);
        }
        else {
            for (int i = child.getN(); i >= 1; i--) {
                child.setKey(i, child.getKey(i - 1));
            }
            child.setKey(0, toAdd);
        }
        child.setN(child.getN() + 1);

    }

    /**
     * Adds one child to the child's children array in the correct position.
     * *
     *
     * @param child        the child we will transfer the child to.
     * @param sibling      the sibling of the child we add.
     * @param childIndex   the index of the child
     * @param siblingIndex the index of the sibling.
     */
    private void addOneChild(BTreeNode child, BTreeNode sibling, int childIndex, int siblingIndex) {
        if (!sibling.isLeaf()) {
            if (siblingIndex > childIndex) {
                child.setChild(child.getN(), sibling.getChild(0));
            }
            else {
                for (int i = child.getN() + 1; i >= 1; i--) {
                    child.setChild(i, child.getChild(i - 1));
                }
                child.setChild(0, sibling.getChild(sibling.getN()));
            }
        }
    }

    /**
     * Determines which key index in the father's array need to be changed.
     *
     * @param childIndex   the index of the child.
     * @param siblingIndex the index of the sibling.
     * @return the index of the key needed to change.
     */
    public int extractIndex(int childIndex, int siblingIndex) {
        int keyIndexToChange;
        if (siblingIndex > childIndex) keyIndexToChange = childIndex;
        else keyIndexToChange = siblingIndex;
        return keyIndexToChange;

    }

    /**
     * Deletes one key and child from the sibling, depending on its position.
     *
     * @param sibling the sibling to delete the first key and the first child from.
     */
    public void deleteOne(BTreeNode sibling, int siblingIndex, int childIndex) {
        if (siblingIndex > childIndex) {
            deleteOneCase1(sibling);
        }
        else {
            deleteOneCase2(sibling);
        }
        sibling.setN(sibling.getN() - 1);
    }

    /**
     * Deletes a key and a child of a sibling if its index is larger than the child.
     * Moves the keys and children left and deletes the last ones.
     *
     * @param node the sibling in Case1a
     */
    private void deleteOneCase1(BTreeNode node) {
        for (int i = 1; i < node.getN(); i++) {
            node.setKey(i - 1, node.getKey(i));
        }
        node.setKey(node.getN() - 1, null);
        if (!node.isLeaf()) {
            for (int i = 1; i <= node.getN(); i++) {
                node.setChild(i - 1, node.getChild(i));
            }
            node.setChild(node.getN(), null);
        }
    }

    /**
     * Deletes a key and a child of a sibling if its index is smaller than the child's.
     *
     * @param node the sibling in Case1a.
     */
    private void deleteOneCase2(BTreeNode node) {
        if (!node.isLeaf()) {
            node.setChild(node.getN(), null);

        }
        node.setKey(node.getN() - 1, null);
    }

    /**
     * Checks if a siblings of a node can lend an element to it.
     * Checks if the index of the child we check is the first index or last index, and if so we check only one sibling.
     * if it is in the middle we have to check two of its siblings.
     *
     * @param father the father of the node in index.
     * @param index  the index of the node in the children array to check its siblings of.
     * @return the index of a sibling with T_VAR keys at the least or -1.
     */
    private int checkSiblings(int index, BTreeNode father) {
        if (index == 0 && father.getChild(1).getN() > T_VAR - 1) return index + 1;

        if (index == father.getN() && father.getChild(index - 1).getN() > T_VAR - 1) return index - 1;

        return checkSiblingInMiddle(index, father);
    }

    /**
     * Checks both siblings of the child at position index in the father's node.
     *
     * @param father the father of the node in index.
     * @param index  of the node in the children array to check its siblings from.
     * @return the index of a sibling with T_VAR keys at the least or -1.
     */
    private int checkSiblingInMiddle(int index, BTreeNode father) {
        if (index != 0 && index != father.getN()) {
            int rightChildN = father.getChild(index + 1).getN();
            if (rightChildN > T_VAR - 1) {
                return index + 1;
            }
            int leftChildN = father.getChild(index - 1).getN();
            if (leftChildN > T_VAR - 1) {
                return index - 1;
            }
        }
        return -1;
    }

    //CASE 1b

    /**
     * Chooses a sibling if all of the child's siblings have t-1 keys.
     *
     * @param father     the father of the child.
     * @param childIndex the index of the child in the father's children array.
     * @return an index of some sibling of the child in childIndex.
     */
    private int chooseASibling(BTreeNode father, int childIndex) {
        int siblingIndex;
        if (childIndex == 0) {
            siblingIndex = 1;
        }
        else if (childIndex == father.getN()) {
            siblingIndex = childIndex - 1;
        }
        else siblingIndex = childIndex + 1;
        return siblingIndex;
    }

    //CASE 2

    /**
     * Handles the delete scenario when the key is in the current node but it is an
     * internal node.
     *
     * @param key the key to delete.
     */
    public void handleCase2(String key) {
        int index = UsefulFunctions.binarySearch(getKeys(), key, getN());
        BTreeNode leftChild = getChild(index);
        BTreeNode rightChild = getChild(index + 1);

        if (leftChild.getN() >= T_VAR) {
            replaceKeyWithMaxKey(index, leftChild);
        }
        else if (rightChild.getN() >= T_VAR) {
            replaceKeyWithMinKey(index, rightChild);
        }
        else {
            mergeChildrenWithKeyAndPlaceMerged(key, index, leftChild, rightChild);
            getChild(index).delete(key, index, this);
        }
    }

    //MERGE

    /**
     * Creates a new node and merges the two given sibling children into it,
     * with the key between them as the median. Then places the new node in
     * place of the left child merged, and deletes the key from the father.
     *
     * @param key        the key to put in the median.
     * @param index      the index of the key between the children.
     * @param leftChild  the child left of the key.
     * @param rightChild the child right of the key.
     */
    private void mergeChildrenWithKeyAndPlaceMerged(String key, int index, BTreeNode leftChild, BTreeNode rightChild) {
        BTreeNode merged = merge(leftChild, rightChild, key);
        setChild(index, merged);
        deleteOldChild(index);
        deleteKey(key);
    }

    /**
     * Deletes the old child left from the merge.
     *
     * @param index the index of the merged child.
     */
    private void deleteOldChild(int index) {
        for (int i = index + 1; i < getN(); i++) {
            setChild(i, getChild(i + 1));
        }
        setChild(getN(), null);
    }

    /**
     * Merges two given nodes with a given key, so that the key would be the median,
     * the keys and children of the left node would be to the left of the median,
     * and the keys and children of the right node would be to the right of the median.
     *
     * @param leftNode  the node to merge left.
     * @param rightNode the node to merge right.
     * @param key       the key to put as median.
     * @return the merged node.
     */
    public BTreeNode merge(BTreeNode leftNode, BTreeNode rightNode, String key) {
        BTreeNode merged = new BTreeNode(T_VAR);
        merged.setLeaf(leftNode.isLeaf());
        // We set the n value of the node to the expected n so that we could add to it keys simultaneously
        // on different places, and also add the given key as median.
        merged.setN(rightNode.getN() + leftNode.getN() + 1);
        merged.setKey(T_VAR - 1, key);
        copyKeysToMerged(leftNode, rightNode, merged);
        copyChildrenToMerged(leftNode, rightNode, merged);
        return merged;
    }

    /**
     * Copies the children from the left an right node to the merged.
     *
     * @param leftNode  the left node in the merge.
     * @param rightNode the right node in the merge
     * @param merged    the new node created in the merge.
     */
    private void copyChildrenToMerged(BTreeNode leftNode, BTreeNode rightNode, BTreeNode merged) {
        for (int i = 0; i < T_VAR; i++) {
            merged.setChild(i, leftNode.getChild(i));
            merged.setChild(i + T_VAR, rightNode.getChild(i));
        }
    }

    /**
     * Copies the keys of the left and right nodes in the merge to the merged node.
     *
     * @param leftNode  the left node in the merge.
     * @param rightNode the right key in the merge.
     * @param merged    the new node created in the merge.
     */
    private void copyKeysToMerged(BTreeNode leftNode, BTreeNode rightNode, BTreeNode merged) {
        for (int i = 0; i < T_VAR - 1; i++) {
            merged.setKey(i, leftNode.getKey(i));
            merged.setKey(i + T_VAR, rightNode.getKey(i));
        }
    }

    // FIND PREDECESSOR AND SUCCESSOR

    /**
     * Searches for the max key in the the child's subtree, replaces the key in the node with it,
     * then deletes the max key in the subtree of the child.
     *
     * @param index the index of the child in the children array.
     * @param child the child to search the max key in.
     */
    private void replaceKeyWithMaxKey(int index, BTreeNode child) {
        String max = findMaxKeyInChild(child);
        setKey(index, max);
        child.delete(max, index, this);
    }

    /**
     * Finds the max key in give childes sub tree.
     *
     * @param child the child to search the max key in.
     * @return the max key in the subtree.
     */
    private String findMaxKeyInChild(BTreeNode child) {
        BTreeNode current = child;
        int currentN = current.getN();

        while (!current.isLeaf()) {
            current = current.getChild(currentN);
            currentN = current.getN();
        }
        currentN = current.getN();
        return current.getKey(currentN - 1);
    }

    /**
     * Searches for the min key in the the child's subtree, replaces the key in the node with it,
     * then deletes the min key in the child's subtree.
     *
     * @param index the index of the child in the children array.
     * @param child the child to search the min key in.
     */
    private void replaceKeyWithMinKey(int index, BTreeNode child) {
        String min = findMinKeyInChild(child);
        setKey(index, min);
        child.delete(min, index, this);
    }

    /**
     * Finds the min key in give childes sub tree.
     *
     * @param child the child to search the min key in.
     * @return the min key in the subtree.
     */
    private String findMinKeyInChild(BTreeNode child) {
        BTreeNode current = child;

        while (!current.isLeaf()) {
            current = current.getChild(0);
        }
        return current.getKey(0);
    }

    //CASE3 & GENERAL USE

    /**
     * Deletes given key form current nodes keys array.
     *
     * @param key the key to delete.
     */
    public void deleteKey(String key) {
        int index = UsefulFunctions.binarySearch(getKeys(), key, getN());
        for (int i = index; i < getN() - 1; i++) {
            setKey(i, getKey(i + 1));
        }
        setN(getN() - 1);
    }

    //CASE 4

    /**
     * This functions handles the case in which the key is not in the current node.
     * We check where to continue searching for it.
     *
     * @param key to check in which child it might be in
     */
    public void handleCase4(String key) {

        for (int i = 0; i < getN(); i++) {
            String keyCheck = getKey(i);
            if (keyCheck.compareTo(key) > 0) //keyCheck > key
            {
                getChild(i).delete(key, i, this);
                return;
            }
        }
        getChild(getN()).delete(key, getN(), this);
    }

    //toString
    @Override
    public String toString() {
        String toString = toString(new StringBuilder(), 0).toString();
        return toString.substring(0, toString.length() - 1);
    }

    /**
     * Sub method of toString that builds the string representation
     * of the subtree recursively.
     *
     * @param sb    an accumulator that collects the subtree's toStrings
     * @param depth the depth of the current subtree.
     * @return a string visually representing the subtree.
     */
    private StringBuilder toString(StringBuilder sb, int depth) {
        if (isLeaf()) {
            return sb.append(addKeysToString(depth));
        }
        //Gets the string representation of the sub tree of this node.
        for (int i = 0; i <= getN(); i++) {
            sb = getChild(i).toString(sb, depth + 1);
            if (i < getN()) {
                sb.append(getKey(i)).append("_").append(depth).append(",");
            }
        }
        return sb;
    }

    /**
     * Sub method of toString used to addFirst the keys held in the current node.
     *
     * @param depth the depth of the current node.
     * @return a string with the keys and the depth they are from.
     */
    private String addKeysToString(int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < getN(); i++) {
            sb.append(getKey(i)).append("_").append(depth).append(",");
        }
        return sb.toString();
    }
}
