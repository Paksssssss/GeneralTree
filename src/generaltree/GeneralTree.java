/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 *
 * @author user
 */
public class GeneralTree {

    TreeNode root;
    TreeNode currentNode = root;
    int height;

    public GeneralTree() {
        root = null;
    }

    public GeneralTree(TreeNode root) {
        this.root = root;
    }

    public String search(String name) {
        PriorityQueue<TreeNode> nodeQueue = new PriorityQueue();
        nodeQueue.add(root);
        boolean isFound = false;
        TreeNode temp;
        do {
            temp = nodeQueue.poll();
            if (temp.getName().compareTo(name) == 0) {
                isFound = true;
                break;
            } else if (!temp.getChildren().isEmpty()) {
                nodeQueue.addAll(temp.getChildren());
            }
        } while (!nodeQueue.isEmpty());
        String path = "";
        if (isFound) {
            path = "\\" + temp.getName();
            while (!temp.equals(root)) {
                temp = temp.getParent();
                path = "\\" + temp.getName() + path;
            }
        }
        return path;
    }

    public void insert(TreeNode node) {
        if (getRedundantNode(node)!=null) {
            if (!currentNode.isDir) {
                currentNode.getChildren().add(node);
            } else {
                System.out.println("This Node is a File.");
            }
        } else {
            System.out.println("Node already exists. Overwriting...");
            currentNode.getChildren().remove(getRedundantNode(node));
            currentNode.getChildren().add(node);
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
            if (temp.getName().compareTo(node.getName()) == 0) {
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
            if (childNode.getName().compareTo(node.getName()) == 0) {
                return childNode;
            }
        }
        return null;
    }
}

class TreeNode {
    private TreeNode parent;
    private String content;
    private String name;
    boolean isDir;
    private ArrayList<TreeNode> children;
    public TreeNode(){
        parent = null;
        isDir = false;
        content = "";
        children = new ArrayList();
    }
    public TreeNode(TreeNode parent){
        this.parent = parent;
        isDir = false;
        content = "";
        children = new ArrayList();
    }
    public TreeNode(String name, TreeNode parent){
        this.name = name;
        this.parent = parent;
        isDir = false;
        content = "";
        children = new ArrayList();
    }
    public TreeNode(String name){
        this.name = name;
        parent = null;
        isDir = false;
        content = "";
        children = new ArrayList();
    }
    public TreeNode(TreeNode parent, ArrayList<TreeNode> children){
        this.parent = parent;
        this.children = children;
        content = "";
        isDir = true;
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
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
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
}
