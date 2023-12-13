import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static java.sql.DriverManager.println;


public class Projects extends JFrame {
    private static JFrame frame;
    protected JPanel BasePanel;
    DefaultMutableTreeNode root;
    private JTree TasksTree;
    private JPanel TasksPanel;
    private JPanel TasksSubPanel;
    private JPanel TopPanel;
    private JLabel ProjectProgressLabel;
    private JLabel TimeLeftLabel;
    private JLabel DueLabel;
    private JLabel TasksLabel;
    private JButton AddTask;
    private JButton HomeButton;
    private JPanel NavigationPanel;
    private JTextPane TaskDetails;
    private JButton editButton;
    private JButton deleteButton;
    private JTable adjmatrixTable;
    private JComboBox<String> CriticalPathComboBox;
    private JButton CriticalPathCalculateButton;
    private final JScrollPane BaseScrollPane;
    private final ArrayList<DefaultMutableTreeNode> treeNodes = new ArrayList<>();
    private Map<Task, CriticalCalculations> criticalCalculations;
    private final Project project;
    private CriticalPath criticalPath = CriticalPathKotlin.INSTANCE;


    public Projects(JFrame mainFrame, Project currentProject) {
        System.out.println("projects cons");
        frame = mainFrame;
        project = currentProject;

        ProjectProgressLabel.setText(project.getName());
        DueLabel.setText("");

        editButton.setEnabled(false);
        deleteButton.setEnabled(false);


        // Use Kotlin critical path by default
        criticalCalculations = CriticalPathKotlin.INSTANCE.forwardBackwardPass(project.getTasks());
        int maxTime = 0;
        for (Map.Entry<Task, CriticalCalculations> c : criticalCalculations.entrySet()) {
            int earlyFinish = c.getValue().getEarlyFinish();
            if (earlyFinish > maxTime) maxTime = earlyFinish;
        }
        if (maxTime > 0) TimeLeftLabel.setText("Project duration: " + maxTime + " days");

        // Add tasks to JTree
        populateTree();

        //ADD TASK OPTION
        GridLayout layout0x2 = new GridLayout(0, 2);

        JPanel AddTaskPanel = new JPanel();
        JTextField TaskNameField = new JTextField(5);
        JTextField TaskDurationField = new JTextField(5);
        JCheckBox DependentCheckBox = new JCheckBox("Dependent:");
        System.out.println("Checked? " + DependentCheckBox.isSelected());
        JComboBox<String> ProjectDependentComboBox = new JComboBox<>(new DefaultComboBoxModel<>());
        ProjectDependentComboBox.addItem("Select a task");

        JTextField OptionLagField = new JTextField("0", 5);

        ProjectProgressLabel.setText("Project Name: " + currentProject.getName());

        for (Task t : currentProject.getTasks()) {
            ProjectDependentComboBox.addItem(t.getName());
        }

        JPanel dependentTasks = new JPanel();

        AddTaskPanel.setLayout(layout0x2);

        AddTaskPanel.add(new JLabel("Task Name:"));
        AddTaskPanel.add(TaskNameField);
        AddTaskPanel.add(new JLabel("Task Duration:"));
        AddTaskPanel.add(TaskDurationField);
        AddTaskPanel.add(new JLabel("Lag"));
        AddTaskPanel.add(OptionLagField);
        AddTaskPanel.add(DependentCheckBox);
        AddTaskPanel.add(ProjectDependentComboBox);
        AddTaskPanel.add(dependentTasks);
        ProjectDependentComboBox.setEnabled(false);

        BaseScrollPane = new JScrollPane(AddTaskPanel);
        BaseScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        BaseScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        BaseScrollPane.setPreferredSize(new Dimension(400, 110));

        DependentCheckBox.addActionListener(e -> {
            if (DependentCheckBox.isSelected()) {
                ProjectDependentComboBox.setEnabled(true);
                System.out.println("Project is a Dependent");
            } else {
                ProjectDependentComboBox.setEnabled(false);
                System.out.println("Project is not a Dependent");
            }

        });

        final boolean[] deleting = {false};

        ProjectDependentComboBox.addItemListener(e -> {
            if (DependentCheckBox.isSelected() && !deleting[0] && e.getStateChange() == ItemEvent.SELECTED) {
                String selected = e.getItem().toString();
                System.out.println("selected " + selected);
                if (!selected.equals("Select a task")) {
                    deleting[0] = true;
                    ProjectDependentComboBox.removeItemAt(ProjectDependentComboBox.getSelectedIndex());
                    ProjectDependentComboBox.setSelectedIndex(0);
                    deleting[0] = false;
                    JLabel l = new JLabel(selected);
                    l.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            ProjectDependentComboBox.addItem(l.getText());
                            dependentTasks.remove(l);
                            dependentTasks.repaint();
                        }
                    });
                    dependentTasks.add(l);
                    dependentTasks.repaint();
                }
            }
        });

        AddTask.addActionListener(e -> {

            int result = JOptionPane.showConfirmDialog(frame, BaseScrollPane,
                    "New Task", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    System.out.println("Task Name: " + TaskNameField.getText());
                    System.out.println("Task Duration: " + TaskDurationField.getText());
                    System.out.println("Task Lag: " + OptionLagField.getText());

                    ArrayList<String> dependencies = new ArrayList<>();

                    // If dependencies are selected
                    if (DependentCheckBox.isSelected() && dependentTasks.getComponents().length > 0) {
                        for (Component component : dependentTasks.getComponents()) {
                            if (component.getClass() == JLabel.class) {
                                JLabel l = (JLabel) component;
                                dependencies.add(l.getText());
                            }
                        }
                        System.out.println("Project Dependent of:" + dependencies);
                    }

                    project.addTask(TaskNameField.getText().trim(), Integer.parseInt(TaskDurationField.getText().trim()), Integer.parseInt(OptionLagField.getText().trim()), dependencies.toArray(new String[0]));
                    populateTree();

                    // Reset fields
                    TaskNameField.setText("");
                    TaskDurationField.setText("0");
                    OptionLagField.setText("");
                    ProjectDependentComboBox.setSelectedIndex(0);
                    ProjectDependentComboBox.setEnabled(false);
                    DependentCheckBox.setSelected(false);
                } catch(NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Duration must be a number.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            }
        });



        HomeButton.addActionListener(e -> new MainMenu(frame));


        frame.setContentPane(BasePanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        System.out.println("PROJECT.JAVA PROJECT: " + project);


    }

    private void runCriticalPath() {
        criticalCalculations = criticalPath.forwardBackwardPass(project.getTasks());
        for (Task t : criticalPath.findCriticalPath(project.getTasks())) setNodeAsCritical(t.getName());
        DefaultTreeModel model = (DefaultTreeModel) TasksTree.getModel();
        model.reload(root); // Reload JTree so tasks are updated
    }

    private void setNodeAsCritical(String name) {
        for (DefaultMutableTreeNode n : treeNodes) {
            if (n.getUserObject().getClass() == Node.class) {
                Node node = (Node) n.getUserObject();
                if (node.getTask().getName().equals(name)) {
                    node.setCritical(true);
                }
            }
        }
    }

    private DefaultMutableTreeNode populateTree(Task currentTask, DefaultMutableTreeNode currentNode) {
        currentTask.getNextTasks().forEach(t -> currentNode.add(populateTree(t, new DefaultMutableTreeNode(new Node(t, false)))));
        treeNodes.add(currentNode);
        return currentNode;
    }

    private void populateTree() {
        root.removeAllChildren();
        // Add nodes to JTree
        // Project can have multiple starting nodes, so add each of them to JTree
        project.getTasks().stream().filter(t -> t.getPreviousTasks().isEmpty()).forEach(t -> root.add(populateTree(t, new DefaultMutableTreeNode(new Node(t, false)))));
        DefaultTreeModel model = (DefaultTreeModel) TasksTree.getModel();
        model.reload(root); // Reload JTree so tasks are displayed
        runCriticalPath();
    }

    private void createUIComponents() {
        root = new DefaultMutableTreeNode("Tasks");
        TasksTree = new JTree(root);
        TasksTree.setCellRenderer(new RedNodeRenderer());
        TasksTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) TasksTree.getLastSelectedPathComponent();
                    if (node != null && node.getUserObject().getClass() == Node.class) {
                        Node n = (Node) node.getUserObject();
                        Task t = n.getTask();
                        System.out.println("Double-clicked on task \"" + t.getName() + "\" with duration " + t.getDuration());
                        String criticalDetails = "";
                        if (criticalCalculations != null) {
                            CriticalCalculations c = criticalCalculations.get(t);
                            if (c != null) {
                                criticalDetails = "\n\n" +
                                        "The task can start is day " + c.getEarlyStart() + ", and will finish on day " + c.getEarlyFinish() + "\n" +
                                        "The  task can start is day " + c.getLateStart() + ", and will finish on day " + c.getLateFinish();

                                if (c.getFloat() != null && c.getFloat() > 0)
                                    criticalDetails += "\nThis task can be started " + c.getFloat() + " day(s) late without affecting the overall duration of this project.";
                            }
                        }

                        // Join prevTasks and nextTasks
                        String prevTasks = String.join(", ", t.getPreviousTasks().stream().map(Task::getName).toArray(String[]::new));
                        String nextTasks = String.join(", ", t.getNextTasks().stream().map(Task::getName).toArray(String[]::new));

                        TaskDetails.setText("Name: " + t.getName() + " --- " +
                                "Duration: " + t.getDuration() + " -- " +
                                "Lag: " + t.getLag() + "\n" +
                                "Previous Tasks: " + prevTasks + "\n" +
                                "Next Tasks: " + nextTasks +
                                criticalDetails);

//                        project.printAdjacencyMatrix();
                        createAdjacencyMatrixTable();

                        editButton.setEnabled(true);
                        editTaskButtonRun(t);


                        deleteButton.setEnabled(true);
                        deleteTaskButtonRun(t);

                    }
                }
            }
        });
    }

    private void editTaskButtonRun(Task t){
        editButton.addActionListener(e -> {

            JPanel panel = new JPanel(new GridLayout(3, 2));
            JTextField taskNameField = new JTextField();
            JTextField taskDurationField = new JTextField();
            JTextField optionLagField = new JTextField();

            panel.add(new JLabel("Task Name:"));
            panel.add(taskNameField);
            panel.add(new JLabel("Task Duration:"));
            panel.add(taskDurationField);
            panel.add(new JLabel("Optional Lag:"));
            panel.add(optionLagField);

            int result = JOptionPane.showConfirmDialog(frame, panel, "Edit Task", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    System.out.println("New Task Name: " + taskNameField.getText());
                    System.out.println("New Task Duration: " + taskDurationField.getText());
                    System.out.println("New Task Lag: " + optionLagField.getText());

                    project.editTask(t.getName(),taskNameField.getText().trim(), Integer.parseInt(taskDurationField.getText().trim()), Integer.parseInt(optionLagField.getText().trim()));
                    populateTree();

                    // Reset fields
                    taskNameField.setText("");
                    taskDurationField.setText("0");
                    optionLagField.setText("");
                } catch(NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Duration must be a number.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            }

            new Projects(frame,project);

        });

    }

    private void deleteTaskButtonRun(Task t){
        deleteButton.addActionListener(e -> {
            if(showDeleteConfirmation(t.getName())){
                project.deleteTask(t.getName());
            }
            new Projects(frame,project);
        });

    }

    public boolean showDeleteConfirmation(String itemName) {
        int choice = JOptionPane.showConfirmDialog(
                null,
                "Are you sure you want to delete " + itemName + "?",
                "Delete Confirmation",
                JOptionPane.YES_NO_OPTION
        );

        return choice == JOptionPane.YES_OPTION;
    }

    private void createAdjacencyMatrixTable() {
        Map<String, Integer> map = project.getTaskIndex();
        ArrayList<String> headers = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            headers.add(entry.getKey());
        }
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Get the adjacency matrix
        boolean[][] booleanAdjacencyMatrix = project.createAdjacencyMatrix();

        ArrayList<ArrayList<String>> adjacencyMatrix = convertBooleanArrayToArrayList(booleanAdjacencyMatrix);

        ArrayList<ArrayList<String>> finalMatrix = new ArrayList<>();

        for (int i = 0; i < adjacencyMatrix.size()+1;i++){
            ArrayList<String> row = adjacencyMatrix.get(0);
            ArrayList<String> newRow = new ArrayList<>();
            for (int j = 0; j < row.size()+1; j++) {
                if(i==0 && j==0){
                    newRow.add(" ");
                }
                else if(i==0){
                    newRow.add(headers.get(j-1));
                } else if (j==0) {
                    newRow.add(headers.get(i-1));
                }else{
                    newRow.add(adjacencyMatrix.get(i-1).get(j-1));
                }
            }
            finalMatrix.add(newRow);
        }
        

        DefaultTableModel model = (DefaultTableModel) adjmatrixTable.getModel();

        model.setRowCount(0);
        model.setColumnCount(0);

        for (int i = 0; i < finalMatrix.size(); i++) {
            model.addColumn(Integer.toString(i + 1));
        }

        for (int i = 0; i < finalMatrix.size(); i++) {
            Object[] rowData = new Object[finalMatrix.size()];
            for (int j = 0; j < finalMatrix.size(); j++) {
                rowData[j] = finalMatrix.get(i).get(j);
            }
            model.addRow(rowData);
        }

    }

    public static ArrayList<ArrayList<String>> convertBooleanArrayToArrayList(boolean[][] booleanArray) {
        ArrayList<ArrayList<String>> stringArrayList = new ArrayList<>();

        for (boolean[] row : booleanArray) {
            ArrayList<String> rowStringList = new ArrayList<>();

            for (boolean value : row) {
                rowStringList.add(value ? "1" : "0");
            }

            // Add the row to the 2D ArrayList
            stringArrayList.add(rowStringList);
        }

        return stringArrayList;
    }

}