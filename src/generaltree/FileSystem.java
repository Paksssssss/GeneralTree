package generaltree;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.PriorityQueue;
import java.util.Queue;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position;

/**
 *
 * @author user
 */
public class FileSystem {

    private GeneralTree system;
    JTextField textField;
    JTextArea textArea;
    JPanel panel;
    Action action;
    static JFrame frame;
    String textAreaLogs;

    public static void main(String[] args) {
        FileSystem fs = new FileSystem();
        TreeNode sample = new TreeNode("Sample");
        fs.system.insert(sample);

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                showUI(fs);
            }
        });

    }

    public FileSystem() {
        TreeNode home = new TreeNode("home");
        home.getFileDescriptor().isDir = true;
        system = new GeneralTree(home);
        Color c = new Color(0, 0, 0);
        panel = new JPanel();
        Font f = new Font("Roboto", Font.PLAIN, 13);
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        textArea = new JTextArea(20, 60);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(false);
        textArea.setEditable(false);
        textArea.setFont(f);
        textArea.setBackground(c);
        textArea.setForeground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(textArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);;
        //scrollPane.setMaximumSize( scrollPane.getPreferredSize() );

        action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commandListener();
            }
        };
        textField = new JTextField("/home>");
        textField.setNavigationFilter(new NavigationFilterPrefixWithBackspace(6, textField));
        textField.setBackground(c);
        textField.setForeground(Color.WHITE);
        textField.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, textField.getPreferredSize().height + 1));
        textField.setFont(f);
        textField.setCaretColor(Color.WHITE);
        textField.addActionListener(action);
        frame = new JFrame("FileSystem");
        frame.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                textField.requestFocus();
            }
        });
        panel.add(scrollPane);
        panel.add(textField);
    }

    public static void showUI(FileSystem fs) {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(fs.panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    //this parses user input to determine command
    private void commandListener() {
        String userInput = textField.getText();
        String neededInput = userInput.split(">")[1];
        String parsedNeededInput[] = neededInput.split("\\s+");
        String command = "", actualInput = "", otherInput = "";
        textField.setText(system.pathToCurrent + ">");
        if (parsedNeededInput[0].isEmpty() && parsedNeededInput.length > 0) {
            command = parsedNeededInput[1].replaceAll("\\s+", "");
            actualInput = parsedNeededInput[2];
        } else if (parsedNeededInput.length == 1) {
            command = parsedNeededInput[0].replaceAll("\\s+", "");
        } else if (parsedNeededInput.length == 2) {
            command = parsedNeededInput[0].replaceAll("\\s+", "");
            actualInput = parsedNeededInput[1];
        } else if (parsedNeededInput.length == 3) {
            command = parsedNeededInput[0].replaceAll("\\s+", "");
            actualInput = parsedNeededInput[1];
            otherInput = parsedNeededInput[2];
        }
        textArea.setText(textArea.getText().concat(userInput + "\n"));
        if (command.equals("mkdir")) {
            if (actualInput.isEmpty()) {
                actualInput = "untitled";
            }
            this.mkdir(actualInput);

        } else if (command.equals("rmdir")) {
            if (actualInput.isEmpty()) {
                textArea.setText(textArea.getText().concat(system.pathToCurrent + "Please add and Argument.\n"));
            } else {
                this.rmdir(actualInput);
            }
        } else if (command.equals("cd")) {
            if (actualInput.isEmpty()) {
                actualInput = "";
            }
            this.navigate(actualInput);
        } else if (command.equals("ls")) {
            this.list(actualInput);
        } else if (command.equals("whereis")) {

        } else if (command.equals("edit")) {

        } else if (command.equals("rn")) {
            this.rename(actualInput, otherInput);
        } else if (command.equals("rm")) {
            this.deleteFile(actualInput);
        } else {
            textArea.setText(textArea.getText().concat(">" + command + "\nCommand '" + command + "' not found.\n"));
        }
    }

    public void move(String fileName, String path) {

    }

    public void rename(String original, String newName) {
        if (system.goToLocalPath(newName) != null) {
            textArea.setText(textArea.getText().concat(">" + newName + " already exists."));
        }
        TreeNode temp = system.goToLocalPath(original);
        if (temp != null) {
            if (newName.contains(".")) {
                String parsedName[] = newName.split(".");
                temp.getFileDescriptor().fileName = parsedName[0];
                temp.getFileDescriptor().fileType = parsedName[1];
                temp.getFileDescriptor().setDate();
                textArea.setText(textArea.getText().concat(">" + original + " renamed to " + newName));
            } else {
                temp.getFileDescriptor().fileName = newName;
                temp.getFileDescriptor().setDate();
                textArea.setText(textArea.getText().concat(">" + original + " renamed to " + newName));
            }
        } else {
            textArea.setText(textArea.getText().concat(">" + original + " not found"));
        }
    }

    public void deleteFile(String path) {
        TreeNode tempNode1, tempNode2;
        String tempString[] = path.split("/");
        String dirName = tempString[tempString.length - 1];
        if (path.contains("/")) {
            tempNode1 = system.goToPath(path);
            if (tempNode1 != null) {
                if (!tempNode1.getFileDescriptor().isDir) {
                    tempNode2 = tempNode1.getParent();
                    tempNode1 = system.currentNode;
                    system.currentNode = tempNode2;
                    system.delete(system.goToLocalPath(dirName));
                    system.currentNode = tempNode1;
                    textArea.setText(textArea.getText().concat(">File: '" + dirName + "' deleted.\n"));
                } else {
                    textArea.setText(textArea.getText().concat(">File: '" + dirName + "' is a Directory.\n"));
                }
            } else {
                textArea.setText(textArea.getText().concat("Path '" + path + "' not found.\n"));
            }
        } else {
            String suffix;
            if (path.startsWith("*")) {
                ArrayList<TreeNode> toDelete = new ArrayList();
                if (path.length() == 1) {
                    suffix = "";
                } else {
                    suffix = path.substring(1);
                }
                for (TreeNode node : system.currentNode.getChildren()) {
                    System.out.println(node.getShortName());
                    if (node.getShortName().endsWith(suffix)) {
                        toDelete.add(node);
                    }
                }
                for (TreeNode node : toDelete) {
                    system.delete(node);
                    textArea.setText(textArea.getText().concat(">File: '" + node.getShortName() + "' deleted.\n"));
                }
            } else {
                tempNode1 = this.system.goToLocalPath(path);
                if (tempNode1 != null) {
                    system.delete(tempNode1);
                    textArea.setText(textArea.getText().concat(">File: '" + path + "' deleted.\n"));
                } else {
                    textArea.setText(textArea.getText().concat("File '" + path + "' not found.\n"));
                }
            }
        }
    }

    public void list(String path) {
        TreeNode tempNode;
        if (path.contains("/")) {
            tempNode = system.goToPath(path);
            if (tempNode != null) {
                for (TreeNode node : tempNode.getChildren()) {
                    //should print out contents of current director
                    textArea.setText(textArea.getText().concat(node.getDesc()));
                }
            } else {
                textArea.setText(textArea.getText().concat("Path '" + path + "' not found.\n"));
            }
        } else if (path.isEmpty()) {
            for (TreeNode node : system.currentNode.getChildren()) {
                //should print out contents of current directory
                textArea.setText(textArea.getText().concat(node.getDesc()));
            }
        } else {
            tempNode = this.system.goToLocalPath(path);
            boolean hasSuf = false;
            String suffix = "";
            if (path.startsWith("*")) {
                hasSuf = true;
                if (path.length() == 1) {
                    suffix = "";
                } else {
                    suffix = path.substring(1);
                }
            }
            if (tempNode != null) {
                for (TreeNode node : tempNode.getChildren()) {
                    if (hasSuf) {
                        if (node.getShortName().endsWith(suffix)) {
                            textArea.setText(textArea.getText().concat(system.pathToCurrent + node.getDesc()));
                        }
                    } else {
                        textArea.setText(textArea.getText().concat(system.pathToCurrent + node.getDesc()));
                    }
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
            } else {
                System.out.println("Path not found!");
            }
        } else if (path.isEmpty()) {
            system.currentNode = system.root;
        } else {
            temp = system.goToLocalPath(path);
            if (temp != null) {
                system.currentNode = temp;
            } else {
                System.out.println("Path not found!");
            }
        }
        this.system.setPathToCurrent();
        textField.setText(this.system.pathToCurrent + ">");
        textField.setNavigationFilter(new NavigationFilterPrefixWithBackspace(system.pathToCurrent.length() + 1, textField));
    }

    public void mkdir(String path) {
        TreeNode tempNode;
        String tempString[] = path.split("/");
        String dirName = tempString[tempString.length - 1];
        String tempPath = path.replace("/" + dirName, "");
        boolean overwritting;
        if (path.contains("/")) {
            tempNode = system.goToPath(tempPath);
            if (tempNode != null) {
                tempNode = system.currentNode;
                system.currentNode = system.goToPath(tempPath);
                overwritting = system.insert(new TreeNode(dirName, true));
                System.out.println(overwritting + "dasd");
                if (overwritting) {
                    textArea.setText(textArea.getText().concat("> Overwritting Redundant Folder. Directory Created.\n"));
                } else {
                    textArea.setText(textArea.getText().concat(">Directory Created.\n"));
                }
                system.currentNode = tempNode;
            } else {
                textArea.setText(textArea.getText().concat(">Path " + tempPath + " not found.\n"));
            }
        } else {
            overwritting = system.insert(new TreeNode(dirName, true));
            System.out.println(overwritting + "dasd");
            if (overwritting) {
                textArea.setText(textArea.getText().concat(">Overwritting Redundant Folder. Directory Created.\n"));
            } else {
                textArea.setText(textArea.getText().concat(">Directory Created.\n"));
            }
        }
    }

    public void rmdir(String path) {
        TreeNode tempNode, tempNode2;
        String tempString[] = path.split("/");
        String dirName = tempString[tempString.length - 1];
        String tempPath = path.replace("/" + dirName, "");
        if (path.contains("/")) {
            tempNode = system.goToPath(path);
            if (!tempNode.getFileDescriptor().isDir) {
                textArea.setText(textArea.getText().concat(">" + dirName + " is not a directory. You can use rm to delete files.\n"));
            } else if (tempNode != null) {
                if (tempNode.compareTo(system.currentNode) == 1) {
                    textArea.setText(textArea.getText().concat(">Directory in use.\n"));
                } else {
                    tempNode2 = system.currentNode;
                    system.currentNode = tempNode.getParent();
                    system.delete(tempNode);
                    system.currentNode = tempNode2;
                    textArea.setText(textArea.getText().concat(">Directory Deleted.\n"));
                }

            } else {
                textArea.setText(textArea.getText().concat(">" + path + " not found.\n"));
            }
        } else {
            tempNode = system.goToLocalPath(tempPath);
            if (!tempNode.getFileDescriptor().isDir) {
                textArea.setText(textArea.getText().concat(">" + dirName + " is not a directory. You can use rm to delete files.\n"));
            } else if (tempNode != null) {
                system.delete(tempNode);
                textArea.setText(textArea.getText().concat(">Directory Deleted.\n"));
            } else {
                textArea.setText(textArea.getText().concat(">" + path + " not found.\n"));
            }
        }
    }

}

class GeneralTree {

    TreeNode root;
    TreeNode currentNode;
    int height;
    String pathToCurrent;

    public GeneralTree() {
        root = null;
    }

    public GeneralTree(TreeNode root) {
        this.root = root;
        this.currentNode = root;
        setPathToCurrent();
    }

    public void setPathToCurrent() {
        if (currentNode.compareTo(root) == 1) {
            pathToCurrent = "/" + root.getShortName();
        } else {
            TreeNode temp = currentNode;
            pathToCurrent = "";
            while (temp != null) {
                pathToCurrent = "/" + temp.getShortName() + pathToCurrent;
                temp = temp.getParent();
            }
        }
        System.out.println(pathToCurrent);
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

    public boolean insert(TreeNode node) {
        TreeNode redundantNode = getRedundantNode(node);
        if (redundantNode == null) {
            node.setParent(this.currentNode);
            this.currentNode.getChildren().add(node);
            return false;
        } else {
            System.out.println("overwritting");
            node.setParent(this.currentNode);
            this.currentNode.getChildren().remove(redundantNode);
            this.currentNode.getChildren().add(node);
            return true;
        }
    }

    public void delete(TreeNode node) {
        //this implementation is for delete that looks for the node in all directories
//        PriorityQueue<TreeNode> nodeQueue = new PriorityQueue();
//        nodeQueue.add(root);
//        boolean isFound = false;
//        TreeNode temp;
//        do {
//            temp = nodeQueue.poll();
//            if (temp.compareTo(node) == 0) {
//                isFound = true;
//                break;
//            } else if (!temp.getChildren().isEmpty()) {
//                nodeQueue.addAll(temp.getChildren());
//            }
//        } while (!nodeQueue.isEmpty());
//        if (isFound) {
//            temp = temp.getParent();
//            temp.getChildren().remove(node);
//        }
        //this implementation is for the deletion of the node from the children of the currentNode;
        TreeNode temp = getRedundantNode(node);
        if (temp != null) {
            currentNode.getChildren().remove(temp);
        }

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
            if (childNode.getFileDescriptor().compareTo(node.getFileDescriptor()) == 1) {
                return childNode;
            }
        }
        return null;
    }

    public TreeNode goToLocalPath(String path) {
        for (TreeNode node : this.currentNode.getChildren()) {
            if (node.getFileDescriptor().fileName.matches(path)) {
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

        this.dateCreated = new Date();
    }

    public Descriptor(String fileName, String fileType) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.dateCreated = new Date();
        setDate();
    }

    @Override
    public int compareTo(Descriptor o) {
        if (this.isDir == o.isDir && this.fileName.equals(o.fileName) && this.fileType.equals(o.fileType)) {
            return 1;
        }
        return 0;
    }

    public void setDate() {
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

    public String getShortName() {
        if (this.fileDescriptor.isDir || this.fileDescriptor.fileType.isEmpty()) {
            return this.fileDescriptor.fileName;
        } else {
            return this.fileDescriptor.fileName + "." + this.fileDescriptor.fileType;
        }
    }

    public String getDesc() {
        return "\n" + getShortName() + "\nDate Created: " + this.getFileDescriptor().dateCreated + "\nDate Modified: "
                + this.getFileDescriptor().dateModified + "\n";
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
        if ((this.parent == null || node.getParent() == null) && this.fileDescriptor.compareTo(node.getFileDescriptor()) == 1) {
            return 1;
        } else if ((this.parent != null && node.getParent() != null) && this.parent.compareTo(node.getParent()) == 1 && this.fileDescriptor.compareTo(node.getFileDescriptor()) == 1) {
            return 1;
        }
        return 0;
    }
}

class NavigationFilterPrefixWithBackspace extends NavigationFilter {

    private int prefixLength;
    private Action deletePrevious;

    public NavigationFilterPrefixWithBackspace(int prefixLength, JTextComponent component) {
        this.prefixLength = prefixLength;
        deletePrevious = component.getActionMap().get("delete-previous");
        component.getActionMap().put("delete-previous", new BackspaceAction());
        component.setCaretPosition(prefixLength);
    }

    public void setDot(NavigationFilter.FilterBypass fb, int dot, Position.Bias bias) {
        fb.setDot(Math.max(dot, prefixLength), bias);
    }

    public void moveDot(NavigationFilter.FilterBypass fb, int dot, Position.Bias bias) {
        fb.moveDot(Math.max(dot, prefixLength), bias);
    }

    class BackspaceAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            JTextComponent component = (JTextComponent) e.getSource();

            if (component.getCaretPosition() > prefixLength) {
                deletePrevious.actionPerformed(e);
            }
        }
    }
}
