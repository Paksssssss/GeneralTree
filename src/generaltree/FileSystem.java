package generaltree;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 *
 * @author user
 */
public class FileSystem {

    private GeneralTree system;

    public static void main(String[] args) {
        FileSystem fs = new FileSystem();
        fs.system.currentNode.getFileDescriptor().displayInfo();
        TreeNode sample = new TreeNode("Sample");
        fs.system.insert(sample);

    }
    

    public FileSystem() {
        TreeNode home = new TreeNode("home");
        home.getFileDescriptor().isDir = true;
        system = new GeneralTree(home);
    }
    
    //this parses user input to determine command
    private void commandListener(String userInput) {
        
    }

    public void list(String path){
        TreeNode tempNode;
        if (path.contains("/")) {
            tempNode = system.goToPath(path);
            if (tempNode != null) {
                for (TreeNode node : tempNode.getChildren()) {
                    //should print out contents of current directory
                    System.out.print(node.getShortName());
                }
            } else {
                System.out.println("Invalid path");
            }
        } else if (path.isEmpty()) {
            for (TreeNode node : system.currentNode.getChildren()) {
                //should print out contents of current directory
                System.out.print(node.getShortName());
            }
        } else {
            tempNode = this.system.goToLocalPath(path);
            if (tempNode != null){
                for (TreeNode node : tempNode.getChildren()) {
                    System.out.println(node.getShortName());
                }
            }
        }
    }
    
    public void navigate(String path) {
        TreeNode temp;
        if (path.contains("/")) {
            temp = system.goToPath(path);
            if (temp != null) {
                system.currentNode = temp;
                System.out.println("Current Node: ");
                system.currentNode.getFileDescriptor().displayInfo();
            } else {
                System.out.println("Path not found!");
            }
        } else {
            temp = system.goToLocalPath(path);
            if (temp != null) {
                system.currentNode = temp;
                System.out.println("Current Node: ");
                system.currentNode.getFileDescriptor().displayInfo();
            } else {
                System.out.println("Path not found!");
            }
        }
    }

    public void mkdir(String path) {
        TreeNode tempNode;
        String tempString[] = path.split("/");
        String dirName = tempString[tempString.length - 1];
        String tempPath = path.replace("/" + dirName, "");
        if (path.contains("/")) {
            tempNode = system.goToPath(tempPath);
            if (tempNode != null) {
                tempNode = system.currentNode;
                system.currentNode = system.goToPath(tempPath);
                system.insert(new TreeNode(dirName, true));
                system.currentNode = tempNode;
            } else {
                System.out.println("Invalid path");
            }
        } else {
            system.insert(new TreeNode(path, true));
        }
    }

    public void rmdir(String path) {
        TreeNode tempNode;
        String tempString[] = path.split("/");
        String dirName = tempString[tempString.length - 1];
        String tempPath = path.replace("/" + dirName, "");
        if (path.contains("/")) {
            tempNode = system.goToPath(tempPath);
            if (tempNode != null) {
                tempNode = system.currentNode;
                system.currentNode = system.goToPath(tempPath);
                system.delete(new TreeNode(dirName, true));
                system.currentNode = tempNode;
            } else {
                System.out.println("Invalid path");
            }
        } else {
            system.delete(new TreeNode(path, true));
        }
    }
}

class GeneralTree {

    TreeNode root;
    TreeNode currentNode;
    int height;

    public GeneralTree() {
        root = null;
    }

    public GeneralTree(TreeNode root) {
        this.root = root;
        this.currentNode = root;
    }

    public TreeNode search(TreeNode node) {
        PriorityQueue<TreeNode> nodeQueue = new PriorityQueue();
        nodeQueue.add(root);
        boolean isFound = false;
        TreeNode temp;
        do {
            temp = nodeQueue.poll();
            if (temp.compareTo(node) == 1) {
                isFound = true;
                break;
            } else if (!temp.getChildren().isEmpty()) {
                nodeQueue.addAll(temp.getChildren());
            }
        } while (!nodeQueue.isEmpty());

        if (isFound) {
            return temp;
        }
        return null;
    }

    public TreeNode search(String fileName) {
        PriorityQueue<TreeNode> nodeQueue = new PriorityQueue();
        nodeQueue.add(root);
        boolean isFound = false;
        TreeNode temp;
        do {
            temp = nodeQueue.poll();
            if (temp.getFileDescriptor().fileName.equals(fileName)) {
                isFound = true;
                break;
            } else if (!temp.getChildren().isEmpty()) {
                nodeQueue.addAll(temp.getChildren());
            }
        } while (!nodeQueue.isEmpty());

        if (isFound) {
            return temp;
        }
        return null;
    }

    public void insert(TreeNode node) {
        TreeNode redundantNode = getRedundantNode(node);
        if (redundantNode == null) {
            this.currentNode.getChildren().add(node);
        } else {
            System.out.println("OVERWRITING");
            this.currentNode.getChildren().remove(node);
            this.currentNode.getChildren().add(node);
        }
    }

    public void delete(TreeNode node) {
        //this implementation is for delete that looks for the node in all directories
        PriorityQueue<TreeNode> nodeQueue = new PriorityQueue();
        nodeQueue.add(root);
        boolean isFound = false;
        TreeNode temp;
        do {
            temp = nodeQueue.poll();
            if (temp.compareTo(node) == 0) {
                isFound = true;
                break;
            } else if (!temp.getChildren().isEmpty()) {
                nodeQueue.addAll(temp.getChildren());
            }
        } while (!nodeQueue.isEmpty());
        if (isFound) {
            temp = temp.getParent();
            temp.getChildren().remove(node);
        }
        //this implementation is for the deletion of the node from the children of the currentNode;
//        TreeNode temp = getRedundantNode(node);
//        if (temp!=null) {
//            currentNode.getChildren().remove(temp);
//        } 

    }

    private void delete(String name) {
        TreeNode temp = null;
        boolean found = false;
        for (TreeNode node : this.currentNode.getChildren()) {
            if (node.getFileDescriptor().fileName.equals(name)) {
                temp = node;
                found = true;
            }
        }
        if (found) {
            this.currentNode.getChildren().remove(temp);
        } else {
            System.out.println("Directory not Found");
        }

    }

    private TreeNode getRedundantNode(TreeNode node) {
        for (TreeNode childNode : currentNode.getChildren()) {
            if (childNode.getFileDescriptor().compareTo(node.getFileDescriptor()) == 0) {
                return childNode;
            }
        }
        return null;
    }

    public TreeNode goToLocalPath(String path) {
        for (TreeNode node : this.currentNode.getChildren()) {
            if (path.equals(node.getFileDescriptor().fileName)) {
                return node;
            }
        }
        return null;
    }

    public TreeNode goToPath(String path) {
        String tempPath[] = path.split("/");
        boolean error;
        TreeNode temp;
        int ctr;
        //still not sure if 1 ba or 0
        if (path.startsWith("/")) {
            temp = root;
            ctr = 2;
        } else {
            temp = currentNode;
            ctr = 0;
        }
        for (; ctr < tempPath.length; ctr++) {
            error = true;
            for (TreeNode node : temp.getChildren()) {
                if (node.getFileDescriptor().fileName.equals(tempPath[ctr])) {
                    temp = node;
                    error = false;
                }
            }
            if (error) {
                return null;
            }
        }
        return temp;
    }
}

class Descriptor implements Comparable<Descriptor> {

    boolean isDir;
    String fileType;
    String fileName;
    Date dateCreated;
    Date dateModified;

    public Descriptor() {
        this.isDir = false;
        this.fileType = "";
        this.fileName = "";
        this.setDate();
    }

    public Descriptor(String fileName, String fileType) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.setDate();
    }

    public void displayInfo() {
        DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        System.out.println("File Name: " + this.fileName + this.fileType);
        System.out.println("Date Created" + df.format(dateCreated));
        System.out.println("Date Modified" + df.format(dateModified));
    }

    @Override
    public int compareTo(Descriptor o) {
        if (this.isDir == o.isDir && this.fileName == o.fileName && this.fileType == o.fileType) {
            return 1;
        }
        return 0;
    }

    private void setDate() {
        this.dateCreated = new Date();
        this.dateModified = new Date();
    }
}

class TreeNode implements Comparable<TreeNode> {

    private TreeNode parent;
    private String content;
    private Descriptor fileDescriptor;
    private ArrayList<TreeNode> children;

    public TreeNode() {
        parent = null;
        content = "";
        children = new ArrayList();
    }

    public TreeNode(TreeNode parent) {
        this.parent = parent;
        this.fileDescriptor.isDir = false;
        content = "";
        children = new ArrayList();
    }

    public TreeNode(String name, TreeNode parent) {
        this.fileDescriptor.fileName = name;
        this.parent = parent;
        this.fileDescriptor.isDir = false;
        content = "";
        children = new ArrayList();
    }

    public TreeNode(String name) {
        this.fileDescriptor = new Descriptor(name, "");
        parent = null;
        this.fileDescriptor.isDir = false;
        content = "";
        children = new ArrayList();
    }

    public TreeNode(String name, boolean isDir) {
        this.fileDescriptor = new Descriptor(name, "");
        parent = null;
        this.fileDescriptor.isDir = isDir;
        content = "";
        children = new ArrayList();
    }

    public TreeNode(TreeNode parent, ArrayList<TreeNode> children) {
        this.parent = parent;
        this.children = children;
        content = "";
        this.fileDescriptor.isDir = true;
    }

    public String getShortName(){
        if (this.fileDescriptor.isDir||this.fileDescriptor.fileType.isEmpty()) {
            return this.fileDescriptor.fileName;
        } else {
            return this.fileDescriptor.fileName + "." +this.fileDescriptor.fileType;
        }
    }
    
    /**
     * @return the parent
     */
    public TreeNode getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * @return the children
     */
    public ArrayList<TreeNode> getChildren() {
        return children;
    }

    /**
     * @param children the children to set
     */
    public void setChildren(ArrayList<TreeNode> children) {
        this.children = children;
    }

    /**
     * @return the fileDescriptor
     */
    public Descriptor getFileDescriptor() {
        return fileDescriptor;
    }

    /**
     * @param fileDescriptor the fileDescriptor to set
     */
    public void setFileDescriptor(Descriptor fileDescriptor) {
        this.fileDescriptor = fileDescriptor;
    }

    @Override
    public int compareTo(TreeNode node) {
        if (this.parent.compareTo(node.getParent()) == 1 && this.fileDescriptor.compareTo(node.getFileDescriptor()) == 1 && this.children.equals(node.getChildren())) {
            return 1;
        }
        return 0;
    }
}
