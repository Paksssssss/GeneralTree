package generaltree;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 *
 * @author user
 */


public class FileSystem{
    private GeneralTree system;
    public static void main(String[] args) {
        
    }
    public FileSystem(){
        TreeNode home = new TreeNode("home");
        home.getFileDescriptor().isDir = true;
        system = new GeneralTree(home);
    }
}

class GeneralTree {
    TreeNode root;
    TreeNode currentNode = root;
    int height;

    public GeneralTree() {
        root = null;
    }

    public GeneralTree(TreeNode root) {
        this.root = root;
    }

    public String search(TreeNode node) {
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
        String path = "";
        if (isFound) {
            path = "\\" + temp.getFileDescriptor().fileName;
            while (!temp.equals(root)) {
                temp = temp.getParent();
                path = "\\" + temp.getFileDescriptor().fileName;
            }
        }
        return path;
    }

    public void insert(TreeNode node) {
        TreeNode redundantNode = getRedundantNode(node);
        if (redundantNode==null) {
            this.currentNode.getChildren().add(node);
        } else {
            this.currentNode.getChildren().remove(node);
            this.currentNode.getChildren().add(node);
        }
    }

    public void delete(TreeNode node){
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
        /*TreeNode temp = getRedundantNode(node);
        if (temp!=null) {
            currentNode.getChildren().remove(temp);
        } */

    }

    private TreeNode getRedundantNode(TreeNode node) {
        for (TreeNode childNode : currentNode.getChildren()) {
            if (childNode.getFileDescriptor().compareTo(node.getFileDescriptor()) == 0) {
                return childNode;
            }
        }
        return null;
    }

//    private TreeNode goToPath(String path){
//        String tempPath[] = path.split("\\\\");
//        boolean error = false;
//    }
}

class Descriptor implements Comparable<Descriptor>{
    boolean isDir;
    String fileType;
    String fileName;
    Date dateCreated;
    Date dateModified;

    public Descriptor(){
        this.isDir = false;
        this.fileType = "";
        this.fileName = "";
        this.setDate();
    }

    public Descriptor(String fileName,String fileType){
        this.fileName = fileName;
        this.fileType = fileType;
        this.setDate();
    }

    @Override
    public int compareTo(Descriptor o) {
        if (this.isDir == o.isDir && this.fileName == o.fileName && this.fileType == o.fileType) {
            return 1;
        }
        return 0;
    }
    
    private void setDate(){
        this.dateCreated = new Date();
        this.dateModified = new Date();
    }
}

class TreeNode implements Comparable<TreeNode>{
    private TreeNode parent;
    private String content;
    private Descriptor fileDescriptor;
    private ArrayList<TreeNode> children;
    public TreeNode(){
        parent = null;
        content = "";
        children = new ArrayList();
    }
    public TreeNode(TreeNode parent){
        this.parent = parent;
        this.fileDescriptor.isDir = false;
        content = "";
        children = new ArrayList();
    }
    public TreeNode(String name, TreeNode parent){
        this.fileDescriptor.fileName = name;
        this.parent = parent;
        this.fileDescriptor.isDir = false;
        content = "";
        children = new ArrayList();
    }
    public TreeNode(String name){
        this.fileDescriptor.fileName = name;
        parent = null;
        this.fileDescriptor.isDir = false;
        content = "";
        children = new ArrayList();
    }
    public TreeNode(TreeNode parent, ArrayList<TreeNode> children){
        this.parent = parent;
        this.children = children;
        content = "";
        this.fileDescriptor.isDir = true;
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
        if (this.parent.compareTo(node.getParent())==1 && this.fileDescriptor.compareTo(node.getFileDescriptor())==1 && this.children.equals(node.getChildren())) {
            return 1;
        }
        return 0;
    }
}