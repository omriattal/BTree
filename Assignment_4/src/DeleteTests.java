public class DeleteTests {
    public static void main(String[] args) {
        BTree tree = new BTree("2");
        tree.createFullTree(System.getProperty("user.dir") + "/badPasswords.txt");
        System.out.println(tree);
        LinkedList<String> deletes = UsefulFunctions.createStringListFromFile(System.getProperty("user.dir")
                + "/delete_keys.txt");
        for (String delete : deletes) {
            tree.delete(delete);
        }
        System.out.println(tree);
    }

}
