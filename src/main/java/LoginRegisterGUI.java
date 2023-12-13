import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.HashMap;
import java.util.Map;


public class LoginRegisterGUI {
    private final JFrame frame;
    private final JPanel panel;
    private final JTextField emailField;
    private final JPasswordField passwordField;
    private final JButton loginButton;
    private final JButton registerButton;

    private final Map<String, String> users;

    private static final String USER_FILE_PATH = "users.txt";

    public LoginRegisterGUI() {
        frame = new JFrame("Login/Register");
        panel = new JPanel();
        emailField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");

        users = loadUsersFromFile();

        initializeUI();
    }

    private void initializeUI() {
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(registerButton);

        loginButton.addActionListener(e -> loginUser());

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerUser();
            }
        });

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(panel);
        frame.setSize(300, 150);
        frame.setVisible(true);
    }

    private void loginUser() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        if (users.containsKey(email) && users.get(email).equals(password)) {
            JOptionPane.showMessageDialog(frame, "Login successful");
            frame.dispose(); // Close the login/register window
            new MainMenu(new JFrame("Project Management"));
        } else {
            JOptionPane.showMessageDialog(frame, "Invalid email or password");
        }
    }

    private void registerUser() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        if (!users.containsKey(email)) {
            users.put(email, password);
            saveUsersToFile(users);
            JOptionPane.showMessageDialog(frame, "Registration successful");
        } else {
            JOptionPane.showMessageDialog(frame, "User already exists");
        }
    }

    private Map<String, String> loadUsersFromFile() {
        Map<String, String> loadedUsers = new HashMap<>();
        try {
            File file = new File(USER_FILE_PATH);
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    loadedUsers.put(parts[0], parts[1]);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return loadedUsers;
    }

    private void saveUsersToFile(Map<String, String> users) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE_PATH))) {
            for (Map.Entry<String, String> entry : users.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

