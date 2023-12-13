import Persistence.Persistence;

import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainMenu {
    private static JFrame frame;
    private JPanel BasePanel;
    private JPanel GreetingPanel;
    private JPanel ProjectsPanel;
    private JButton ProjectsAddButton;
    private JScrollPane ProjectsScrollPane;
    private JLabel ProjectsLabel;
    private JPanel ProjectsSubPanel;

    private JLabel GreetingLabel;
    private JLabel TimeLabel;
    private JLabel DateLabel;
    private JPanel projectjpanel;
    private JPanel ProjectsScrollPaneBasePanel;

    public MainMenu() {
        GridLayout layout0x2 = new GridLayout(0, 1);

        //JPanel for Project Fields
        JPanel projectFieldsPanel = new JPanel();
        JTextField projectTitleField = new JTextField(5);


        projectFieldsPanel.setLayout(layout0x2);
        projectFieldsPanel.add(new JLabel("Project Title:"));
        projectFieldsPanel.add(projectTitleField);

        ProjectsAddButton.addActionListener(e -> {

            int result = JOptionPane.showConfirmDialog(frame, projectFieldsPanel, "New Project", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {

                    Project p = new Project(projectTitleField.getText().trim());

                    // Open project view
                    new Projects(frame, p);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            }

        });

    }

    public MainMenu(JFrame mainFrame) {
        frame = mainFrame;
        frame.setContentPane(new MainMenu().BasePanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }


    private void createUIComponents() {
        TimeLabel = new JLabel("");
        DateLabel = new JLabel("");

        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");

        Timer timer = new Timer(1000, e -> {
            Date date = new Date();
            TimeLabel.setText("Time: " + timeFormat.format(date));
            DateLabel.setText("Date: " + dateFormat.format(date));
        });
        timer.setInitialDelay(0);
        timer.start();


//       //PROJECTS SCROLL PANEL
        ProjectsScrollPaneBasePanel = new JPanel();
        ProjectsScrollPaneBasePanel.setLayout(new GridLayout(5, 1));


        for (Project project : Persistence.INSTANCE.getProjects()) {
            JButton viewProjectBtn = new JButton("View");
            viewProjectBtn.addActionListener(e -> new Projects(frame, project));

            JButton editProjectBtn = new JButton("Edit");
            editProjectBtn.addActionListener(e -> editProject(project));

            JButton deleteProjectBtn = new JButton("Delete");
            deleteProjectBtn.addActionListener(e -> deleteProject(project));

            JPanel projectTile = new JPanel();
            projectTile.setLayout(new GridLayout(1, 4));
            projectTile.setBorder(BorderFactory.createLineBorder(Color.black));

            ProjectsScrollPaneBasePanel.add(projectTile);
            projectTile.add(new JLabel( project.getName()));

            projectTile.add(viewProjectBtn);
            projectTile.add(editProjectBtn);
            projectTile.add(deleteProjectBtn);
        }
        ProjectsScrollPane = new JScrollPane(ProjectsScrollPaneBasePanel);
    }

    private void deleteProject(Project p){
        if(showDeleteConfirmation(p.getName())){
            p.deleteProject();
            new MainMenu(frame);
        }
    }

    private void editProject(Project p){
        p.editProject(JOptionPane.showInputDialog(null, "Enter the new project name:", "New Project Name", JOptionPane.QUESTION_MESSAGE));
        new MainMenu(frame);
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
}


