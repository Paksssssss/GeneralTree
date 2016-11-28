package generaltree;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position;

/**
 *
 * @author user
 */
public class runProgram {

    public static void main(String[] args) throws IOException {
        FileSystem fs = new FileSystem();
        fs.readCommandsFromFile();
    }
}

class FileSystem {

    private static GeneralTree system;
    JTextField textField;
    JTextArea textArea;
    KeyListener kl;
    JPanel panel;
    JFrame frame;
    Action action;
    String fileContent = "", writeThisToFile = "";
    NavigationFilterPrefixWithBackspace nf;
    boolean writeMode = false, readingFile = false;
    static boolean serReadSuccess;
    int editMode, lineCtr = 0;
    URL inputFilePath;

    public static void serialize() {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
            objectOut.writeObject(system.root);
            FileOutputStream outFile = new FileOutputStream("./tree.ser");
            outFile.write(byteOut.toByteArray());
            outFile.close();
        } catch (IOException ex) {
            Logger.getLogger(FileSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void deSerialize() {
        TreeNode home = new TreeNode("root", true);
        system = new GeneralTree(home);
        try {
            ByteArrayInputStream byteIn = new ByteArrayInputStream(Files.readAllBytes(new File("./tree.ser").toPath()));
            ObjectInputStream objectIn = new ObjectInputStream(byteIn);
            system.root = (TreeNode) objectIn.readObject();
        } catch (IOException i) {
            System.out.println("Serializable file not found");
            i.printStackTrace();
        } catch (ClassNotFoundException c) {
            System.out.println("GeneralTree class not found");
            c.printStackTrace();
        }
    }

    public FileSystem() {
        deSerialize();
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

        kl = new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                //do nothing
            }

            @Override
            public void keyPressed(KeyEvent e) {
                Component source = (Component) e.getSource();
                if (source instanceof JTextField && e.getKeyCode() == KeyEvent.VK_ESCAPE && writeMode) {
                    system.currentNode.setContent(fileContent);
                    system.currentNode = system.currentNode.getParent();
                    writeMode = false;
                    textArea.setText(textArea.getText().concat("File Edited\n"));
                    JTextField f = (JTextField) source;
                    if (readingFile) {
                        try {
                            readingFile = false;
                            readCommandsFromFile();
                        } catch (IOException ex) {
                            Logger.getLogger(FileSystem.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                //do nothing
            }
        };

        textField = new JTextField("/root:");
        textField.setNavigationFilter(nf);
        textField.setBackground(c);
        textField.setForeground(Color.WHITE);
        textField.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, textField.getPreferredSize().height + 1));
        textField.setFont(f);
        textField.setCaretColor(Color.WHITE);
        textField.addActionListener(action);
        textField.addKeyListener(kl);
        nf = new NavigationFilterPrefixWithBackspace(6, textField);
        panel.add(scrollPane);
        panel.add(textField);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                serialize();
                try {
                    writeToFile();
                } catch (IOException ex) {
                    Logger.getLogger(FileSystem.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        frame = new JFrame("File System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void readCommandsFromFile() throws IOException {
        readingFile = true;
        List<String> lines = Files.readAllLines(Paths.get("./mp3.in"), StandardCharsets.UTF_8);
        for (; lineCtr < lines.size(); lineCtr++) {
            textField.setText(system.pathToCurrent + ":" + lines.get(lineCtr));
            if (textField.getText().contains(">") || textField.getText().contains("edit")) {
                lineCtr++;
                break;
            }
            commandListener();
        }
    }

    public void writeToFile() throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter("./mp3.out"));
        pw.write(writeThisToFile);

        pw.close();
    }

    //this parses user input to determine command
    private void commandListener() {
        String userInput = textField.getText();
        String neededInput = userInput.split(":")[1];
        if (!writeMode) {
            String parsedNeededInput[] = neededInput.split("\\s+");
            String command = "", actualInput = "", otherInput = "";
            textField.setText(system.pathToCurrent + ":");
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
                    textArea.setText(textArea.getText().concat(system.pathToCurrent + ">usage: mkdir <directory name>\n"));
                    writeThisToFile += (">usage: mkdir <directory name>\n");
                } else {
                    this.mkdir(actualInput);
                }

            } else if (command.equals("rmdir")) {
                if (actualInput.isEmpty()) {
                    textArea.setText(textArea.getText().concat(system.pathToCurrent + ">usage: rmdir <directory name>\n"));
                    writeThisToFile += (">usage: mkdir <directory name>\n");
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
                this.whereIs(actualInput);
            } else if (command.equals(">") || command.equals(">>") || command.equals("edit")) {
                this.setEditMode(command, actualInput);
            } else if (command.equals("show")) {
                this.show(actualInput);
            } else if (command.equals("rn")) {
                if (actualInput.isEmpty() && otherInput.isEmpty()) {
                    writeThisToFile += "usage: cp source_file/source_directory target_file/target_directory";
                } else {
                    this.rename(actualInput, otherInput);
                }
            } else if (command.equals("rm")) {
                this.deleteFile(actualInput);
            } else if (command.equals("mv")) {
                if (actualInput.isEmpty() && otherInput.isEmpty()) {
                    writeThisToFile += "usage: cp source_file/source_directory target_file/target_directory";
                } else {
                    this.move(actualInput, otherInput);
                }
            } else if (command.equals("cp")) {
                if (actualInput.isEmpty() && otherInput.isEmpty()) {
                    writeThisToFile += "usage: cp source_file/source_directory target_file/target_directory";
                } else {
                    this.copy(actualInput, otherInput);
                }
            } else if (command.equals("clear")) {
                textArea.setText("");
            } else {
                textArea.setText(textArea.getText().concat(">" + command + "\nCommand '" + command + "' not found.\n"));
            }
        } else {
            edit(neededInput);
            textField.setText(system.pathToCurrent + ":");
        }

    }

    public void edit(String input) {
        fileContent += input + "\n";
        textArea.setText(textArea.getText().concat(">" + input + "\n"));
    }

    public void setEditMode(String command, String path) {
        TreeNode tempNode, tmp;
        fileContent = "";

        textArea.setText(textArea.getText() + "<Edit Mode>\n");
        if (path.contains("/")) {
            tempNode = system.goToPath(path);
            if (tempNode != null) {
                if (tempNode.getFileDescriptor().isDir) {
                    textArea.setText(textArea.getText() + path + " is a Directory\n");
                } else {
                    system.currentNode = tempNode;
                    writeMode = true;
                    if (command.equals(">")) {
                        editMode = 0;
                        fileContent = "";
                    } else if (command.equals(">>")) {
                        editMode = 1;
                        fileContent = system.currentNode.getContent();
                    } else if (command.equals("edit")) {
                        editMode = 2;
                        fileContent = system.currentNode.getContent();
                        textArea.setText(textArea.getText() + system.currentNode.getContent() + "\n");
                    }
                }
            } else {
                String parsePath[] = path.split("/");
                String fileName = parsePath[parsePath.length - 1];
                String tempPath = path.replace("/" + fileName, "");
                system.currentNode = system.goToPath(tempPath);
                tmp = new TreeNode(fileName, false);
                tmp.setParent(system.currentNode);
                system.insert(tmp);
                system.currentNode = system.goToLocalPath(fileName);
                writeMode = true;
                if (command.equals(">")) {
                    editMode = 0;
                    fileContent = "";
                } else if (command.equals(">>")) {
                    editMode = 1;
                    fileContent = system.currentNode.getContent();
                } else if (command.equals("edit")) {
                    editMode = 2;
                    fileContent = system.currentNode.getContent();
                    textArea.setText(textArea.getText() + system.currentNode.getContent() + "\n");
                }
            }
        } else {
            tempNode = system.goToLocalPath(path);
            if (tempNode != null) {
                if (tempNode.getFileDescriptor().isDir) {
                    textArea.setText(textArea.getText() + path + " is a Directory\n");
                } else {
                    system.currentNode = tempNode;
                    writeMode = true;
                    if (command.equals(">")) {
                        editMode = 0;
                    } else if (command.equals(">>")) {
                        editMode = 1;
                        fileContent = system.currentNode.getContent();
                    } else if (command.equals("edit")) {
                        editMode = 2;
                        fileContent = system.currentNode.getContent();
                        textArea.setText(textArea.getText() + system.currentNode.getContent());
                    }
                }
            } else {
                system.insert(new TreeNode(path, false));
                system.currentNode = system.goToLocalPath(path);
                writeMode = true;
                if (command.equals(">")) {
                    editMode = 0;
                } else if (command.equals(">>")) {
                    editMode = 1;
                    fileContent = system.currentNode.getContent();
                } else if (command.equals("edit")) {
                    editMode = 2;
                    fileContent = system.currentNode.getContent();
                    textArea.setText(textArea.getText() + system.currentNode.getContent());
                }
            }
        }
    }

    public void copy(String fileName, String copyName) {
        TreeNode tmp = system.goToLocalPath(fileName);
        if (tmp != null) {
            if (copyName.contains("/")) {
                String copyNameParsed[] = copyName.split("/");
                String newCopyName = copyNameParsed[copyNameParsed.length - 1];
                String tempPath = copyName.replace("/" + newCopyName, "");
                TreeNode tempNode = system.goToPath(tempPath), tempNode1 = new TreeNode();
                if (tempNode != null) {
                    tempNode1.setParent(tempNode);
                    tempNode1.setChildren(tmp.getChildren());
                    tempNode1.setContent(tmp.getContent());
                    if (copyName.contains(".")) {
                        tempNode1.setFileDescriptor(new Descriptor(newCopyName.split("\\.")[0], newCopyName.split("\\.")[1]));
                    } else if (!copyName.isEmpty()) {
                        tempNode1.setFileDescriptor(new Descriptor(newCopyName, ""));
                    }
                    tmp = system.currentNode;
                    system.currentNode = tempNode;
                    system.insert(tempNode1);
                    system.currentNode = tmp;
                } else {
                    textArea.setText(textArea.getText().concat(">" + tempPath + ": No such file or directory.\n"));
                    writeThisToFile += (tempPath + ": No such file or directory.\n");
                }
            } else {
                TreeNode tempNode = system.currentNode, tempNode1 = new TreeNode();
                tempNode1.setParent(tempNode);
                tempNode1.setChildren(tmp.getChildren());
                tempNode1.setContent(tmp.getContent());
                tempNode1.setFileDescriptor(tmp.getFileDescriptor());
                if (copyName.contains(".")) {
                    tempNode1.setFileDescriptor(new Descriptor(copyName.split("\\.")[0], copyName.split("\\.")[1]));
                } else if (!copyName.isEmpty()) {
                    tempNode1.setFileDescriptor(new Descriptor(copyName, ""));
                }
                system.insert(tempNode1);
            }
        } else {
            textArea.setText(textArea.getText().concat(">" + fileName + ": No such file or directory.\n"));
            writeThisToFile += (fileName + ": No such file or directory.\n");
        }
    }

    public void show(String fileName) {
        TreeNode tempNode = system.goToLocalPath(fileName);
        if (tempNode != null) {
            if (tempNode.getFileDescriptor().isDir) {
                textArea.setText(textArea.getText().concat(">" + fileName + ": is a directory.\n"));
                writeThisToFile += (fileName + ": is a directory.\n");
            } else {
                textArea.setText(textArea.getText().concat(tempNode.getContent() + "\n"));
                writeThisToFile += tempNode.getContent();
            }
        } else {
            textArea.setText(textArea.getText().concat(">" + fileName + ": No such file or directory.\n"));
            writeThisToFile += (fileName + ": is a directory.\n");
        }
    }

    public void move(String fileName, String path) {
        TreeNode tmp = system.goToLocalPath(fileName);
        if (tmp != null) {
            if (path.contains("/")) {
                String copyNameParsed[] = path.split("/");
                String newCopyName = copyNameParsed[copyNameParsed.length - 1];
                String tempPath = path.replace("/" + newCopyName, "");
                TreeNode tempNode = system.goToPath(tempPath), tempNode1 = new TreeNode();
                if (tempNode != null) {
                    tempNode1.setParent(tempNode);
                    tempNode1.setChildren(tmp.getChildren());
                    tempNode1.setContent(tmp.getContent());
                    if (path.contains(".")) {
                        tempNode1.setFileDescriptor(new Descriptor(newCopyName.split("\\.")[0], newCopyName.split("\\.")[1]));
                    } else if (!path.isEmpty()) {
                        tempNode1.setFileDescriptor(new Descriptor(newCopyName, ""));
                    }
                    tmp = system.currentNode;
                    system.currentNode = tempNode;
                    system.insert(tempNode1);
                    system.currentNode = tmp;
                    system.delete(system.goToLocalPath(fileName));
                } else {
                    textArea.setText(textArea.getText().concat(">" + tempPath + ": No such file or directory.\n"));
                    writeThisToFile += (tempPath + ": No such file or directory.\n");
                }
            } else {
                TreeNode tempNode = system.currentNode, tempNode1 = new TreeNode();
                tempNode1.setParent(tempNode);
                tempNode1.setChildren(tmp.getChildren());
                tempNode1.setContent(tmp.getContent());
                tempNode1.setFileDescriptor(tmp.getFileDescriptor());
                if (path.contains(".")) {
                    tempNode1.setFileDescriptor(new Descriptor(path.split("\\.")[0], path.split("\\.")[1]));
                } else if (!path.isEmpty()) {
                    tempNode1.setFileDescriptor(new Descriptor(path, ""));
                }
                system.insert(tempNode1);
                system.delete(tmp);
            }
        } else {
            textArea.setText(textArea.getText().concat(">" + fileName + ": No such file or directory.\n"));
            writeThisToFile += (fileName + ": No such file or directory.\n");
        }
    }

    public void whereIs(String fileName) {
        TreeNode tempNode;
        PriorityQueue<TreeNode> files = new PriorityQueue();
        ArrayList<TreeNode> hits = new ArrayList();
        files.add(system.root);
        while (!files.isEmpty()) {
            tempNode = files.poll();
            if (tempNode.getShortName().equals(fileName)) {
                textArea.setText(textArea.getText().concat(">" + system.returnPath(tempNode) + "\n"));
                writeThisToFile += (system.returnPath(tempNode) + "\n");
            }
            files.addAll(tempNode.getChildren());
        }
    }

    public void rename(String original, String newName) {
        if (system.goToLocalPath(newName) != null) {
            textArea.setText(textArea.getText().concat(">" + newName + " already exists.\n"));
        }
        TreeNode temp = system.goToLocalPath(original);
        if (temp != null) {
            if (newName.contains(".")) {
                String parsedName[] = newName.split("\\.");
                temp.getFileDescriptor().fileName = parsedName[0];
                temp.getFileDescriptor().fileType = parsedName[1];
                temp.getFileDescriptor().setDate();
                System.out.println(temp.getShortName());
                textArea.setText(textArea.getText().concat(">" + original + " renamed to " + newName + "\n"));
            } else {
                temp.getFileDescriptor().fileName = newName;
                temp.getFileDescriptor().setDate();
                textArea.setText(textArea.getText().concat(">" + original + " renamed to " + newName + "\n"));
            }
        } else {
            textArea.setText(textArea.getText().concat(">" + original + ": No such file or directory.\n"));
            writeThisToFile += (original + ": No such file or directory.\n");
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
                textArea.setText(textArea.getText().concat(">" + path + ": No such file or directory.\n"));
                writeThisToFile += (path + ": No such file or directory.\n");
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
                    if (tempNode1.getFileDescriptor().isDir) {
                        textArea.setText(textArea.getText().concat(">File: '" + path + "' is a Directory.\n"));
                    } else {
                        system.delete(tempNode1);
                        textArea.setText(textArea.getText().concat(">File: '" + path + "' deleted.\n"));
                    }
                } else {
                    textArea.setText(textArea.getText().concat(">" + path + ": No such file or directory.\n"));
                    writeThisToFile += (path + ": No such file or directory.\n");
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
                    writeThisToFile += node.getShortName() + "\n";
                }
            } else {
                textArea.setText(textArea.getText().concat(">" + path + ": No such file or directory.\n"));
                writeThisToFile += (path + ": No such file or directory.\n");
            }
        } else if (path.isEmpty()) {
            for (TreeNode node : system.currentNode.getChildren()) {
                //should print out contents of current directory
                textArea.setText(textArea.getText().concat(node.getDesc()));

                writeThisToFile += node.getShortName() + "\n";
            }
        } else {
            String fix = "";
            if (path.contains("*")) {
                tempNode = system.currentNode;
                fix = path;
                fix = fix.replaceAll("\\.", "\\\\.");
                fix = fix.replaceAll("\\*", ".*");
            } else {
                tempNode = this.system.goToLocalPath(path);
            }
            if (tempNode != null) {
                for (TreeNode node : tempNode.getChildren()) {
                    if (node.getShortName().matches(fix)) {
                        textArea.setText(textArea.getText().concat(node.getDesc()));

                        writeThisToFile += node.getShortName() + "\n";
                    }
                }
            } else {
                textArea.setText(textArea.getText().concat(">" + path + ": No such file or directory.\n"));
            }
        }
    }

    public void navigate(String path) {
        TreeNode temp;
        if (path.contains("/")) {
            temp = system.goToPath(path);
            if (temp != null) {
                if (temp.getFileDescriptor().isDir) {
                    system.currentNode = temp;
                } else {
                    textArea.setText(textArea.getText().concat(">" + temp.getShortName() + " is not a Directory.\n"));
                }
            } else {
                textArea.setText(textArea.getText().concat(">" + path + ": No such file or directory.\n"));
                writeThisToFile += path + ": No such file or directory.\n";
            }
        } else if (path.isEmpty()) {
            system.currentNode = system.root;
        } else if (path.equals("..") && system.currentNode.compareTo(system.root) == 0) {
            system.currentNode = system.currentNode.getParent();
        } else {
            temp = system.goToLocalPath(path);
            if (temp != null) {
                if (temp.getFileDescriptor().isDir) {
                    system.currentNode = temp;
                } else {
                    textArea.setText(textArea.getText().concat(">" + temp.getShortName() + " is not a Directory.\n"));
                }
            } else {
                writeThisToFile += path + ": No such file or directory.\n";
                textArea.setText(textArea.getText().concat(">" + path + ": No such file or directory.\n"));
            }
        }
        system.setPathToCurrent();
        nf.prefixLength = system.pathToCurrent.length() + 1;
        textField.setText(system.pathToCurrent + ":");
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
                if (overwritting) {
                    textArea.setText(textArea.getText().concat("> Overwritting Redundant Folder. Directory Created.\n"));
                    writeThisToFile += dirName + ": Already exists\n";
                } else {
                    textArea.setText(textArea.getText().concat(">Directory Created.\n"));
                }
                system.currentNode = tempNode;
            } else {
                textArea.setText(textArea.getText().concat(">" + tempPath + ": No such file or directory.\n"));
                writeThisToFile += tempPath + ": No such file or directory.\n";
            }
        } else {
            overwritting = system.insert(new TreeNode(dirName, true));
            if (overwritting) {
                textArea.setText(textArea.getText().concat(">Overwritting Redundant Folder. Directory Created.\n"));
                writeThisToFile += path + ": Already exists\n";
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
                textArea.setText(textArea.getText().concat(">" + path + ": No such file or directory.\n"));
            }
        } else {
            tempNode = system.goToLocalPath(tempPath);
            if (!tempNode.getFileDescriptor().isDir) {
                textArea.setText(textArea.getText().concat(">" + dirName + " is not a directory. You can use rm to delete files.\n"));
            } else if (tempNode != null) {
                system.delete(tempNode);
                textArea.setText(textArea.getText().concat(">Directory Deleted.\n"));
            } else {
                textArea.setText(textArea.getText().concat(">" + path + ": No such file or directory.\n"));
            }
        }
    }

}

class GeneralTree implements Serializable {

    TreeNode root;
    TreeNode currentNode;
    String pathToCurrent;

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
            if (node.getShortName().matches(path)) {
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
            while (ctr < tempPath.length && tempPath[ctr].equals("..")) {
                if (temp.compareTo(root) == 0) {
                    temp = temp.getParent();
                }
                ctr++;
            }

            if (ctr >= tempPath.length) {
                error = false;
                break;
            }
            for (TreeNode node : temp.getChildren()) {
                if (node.getShortName().equals(tempPath[ctr])) {
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

    public String returnPath(TreeNode node) {
        String path = "";
        TreeNode temp = node;
        do {
            path = "/" + temp.getShortName() + path;
            temp = temp.getParent();
        } while (temp.compareTo(root) == 0);
        return path;
    }
}

class Descriptor implements Comparable<Descriptor>, Serializable {

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

class TreeNode implements Comparable<TreeNode>, Serializable {

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
        if (this.getFileDescriptor().isDir) {
            return "\n" + getShortName() + "\nDate Created: " + this.getFileDescriptor().dateCreated + "\nDate Modified: "
                    + this.getFileDescriptor().dateModified + "\n" + "DIR\n";
        }
        return "\n" + getShortName() + "\nDate Created: " + this.getFileDescriptor().dateCreated + "\nDate Modified: "
                + this.getFileDescriptor().dateModified + "\n" + "FILE\n";
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

    public int prefixLength;
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
