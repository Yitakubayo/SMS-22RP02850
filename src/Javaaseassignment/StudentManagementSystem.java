import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class StudentManagementSystem extends JFrame {
    // Database connection details
    private static final String URL = "jdbc:mariadb://localhost:3306/iprc_tumba";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    // Model for the table displaying student data
    private DefaultTableModel studentTableModel;
    // Database connection and prepared statement
    private Connection connection;
    private PreparedStatement insertStatement;

    // Input fields for student data
    private JTextField nameField, regNoField, mathMarksField, javaMarksField, phpMarksField;

    // Constructor to initialize the UI and database connection
    public StudentManagementSystem() {
        initializeUI();
        initializeDatabase();
        loadExistingStudents();
    }

    // Method to initialize the user interface
    private void initializeUI() {
        setTitle("Student Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Input Panel for student details
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        inputPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Add input fields to the panel
        nameField = addLabelAndTextField(inputPanel, "Name:");
        regNoField = addLabelAndTextField(inputPanel, "Reg Number:");
        mathMarksField = addLabelAndTextField(inputPanel, "Math Marks:");
        javaMarksField = addLabelAndTextField(inputPanel, "Java Marks:");
        phpMarksField = addLabelAndTextField(inputPanel, "PHP Marks:");

        // Button Panel for actions
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBorder(new EmptyBorder(0, 20, 20, 20));

        // Add buttons to the panel
        JButton addButton = createButton("Add Student", Color.GREEN, e -> addStudent());
        JButton exitButton = createButton("Exit", Color.RED, e -> System.exit(0));

        buttonPanel.add(addButton);
        buttonPanel.add(exitButton);

        // Table setup to display student data
        String[] columnNames = {"Name", "Reg Number", "Average Marks"};
        studentTableModel = new DefaultTableModel(columnNames, 0);
        JTable studentTable = new JTable(studentTableModel);

        // Set custom cell renderer for highlighting marks greater than 85
        studentTable.setDefaultRenderer(Object.class, new CustomCellRenderer());

        JScrollPane scrollPane = new JScrollPane(studentTable);

        // Add components to the main frame
        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null); // Center the window on screen
    }

    // Helper method to create labels and text fields
    private JTextField addLabelAndTextField(JPanel panel, String labelText) {
        JLabel label = new JLabel(labelText);
        JTextField textField = new JTextField(20); // Adjust the width of text fields as needed
        panel.add(label);
        panel.add(textField);
        return textField;
    }

    // Helper method to create buttons
    private JButton createButton(String text, Color color, ActionListener listener) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.addActionListener(listener);
        return button;
    }

    // Method to initialize database connection and prepare statements
    private void initializeDatabase() {
        try {
            // Load the MariaDB driver and establish a connection
            Class.forName("org.mariadb.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            // Prepare the SQL statement for inserting student data
            String insertSql = "INSERT INTO students (name, reg_number, math_marks, java_marks, php_marks) VALUES (?, ?, ?, ?, ?)";
            insertStatement = connection.prepareStatement(insertSql);
        } catch (Exception e) {
            // Handle exceptions related to database connection
            JOptionPane.showMessageDialog(this, "Failed to connect to the database.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
    }

    // Method to load existing students from the database
    private void loadExistingStudents() {
        try {
            // Execute the SQL query to fetch existing students
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT name, reg_number, math_marks, java_marks, php_marks FROM students");
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String regNumber = resultSet.getString("reg_number");
                int mathMarks = resultSet.getInt("math_marks");
                int javaMarks = resultSet.getInt("java_marks");
                int phpMarks = resultSet.getInt("php_marks");
                double averageMarks = (mathMarks + javaMarks + phpMarks) / 3.0;

                // Add student data to the table model
                Object[] rowData = {name, regNumber, averageMarks};
                studentTableModel.addRow(rowData);
            }
        } catch (SQLException e) {
            // Handle SQL exceptions
            JOptionPane.showMessageDialog(this, "Error loading existing students.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Method to add a new student
    private void addStudent() {
        String name = nameField.getText().trim();
        String regNo = regNoField.getText().trim();
        String mathMarksStr = mathMarksField.getText().trim();
        String javaMarksStr = javaMarksField.getText().trim();
        String phpMarksStr = phpMarksField.getText().trim();

        // Validate input fields
        if (name.isEmpty() || regNo.isEmpty() || mathMarksStr.isEmpty() || javaMarksStr.isEmpty() || phpMarksStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Validate numeric fields
            int mathMarks = Integer.parseInt(mathMarksStr);
            int javaMarks = Integer.parseInt(javaMarksStr);
            int phpMarks = Integer.parseInt(phpMarksStr);

            // Validate marks range (0 to 100)
            if (mathMarks < 0 || mathMarks > 100 || javaMarks < 0 || javaMarks > 100 || phpMarks < 0 || phpMarks > 100) {
                JOptionPane.showMessageDialog(this, "Marks should be between 0 and 100.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Insert student data into database
            insertStatement.setString(1, name);
            insertStatement.setString(2, regNo);
            insertStatement.setInt(3, mathMarks);
            insertStatement.setInt(4, javaMarks);
            insertStatement.setInt(5, phpMarks);
            insertStatement.executeUpdate();

            // Calculate average marks
            double averageMarks = (mathMarks + javaMarks + phpMarks) / 3.0;

            // Update table
            Object[] rowData = {name, regNo, averageMarks};
            studentTableModel.addRow(rowData);

            // Clear fields after adding
            clearFields();
        } catch (NumberFormatException ex) {
            // Handle exceptions related to invalid numeric input
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter valid numbers for marks.", "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (SQLException ex) {
            // Handle SQL exceptions
            JOptionPane.showMessageDialog(this, "Error adding student. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // Method to clear input fields
    private void clearFields() {
        nameField.setText("");
        regNoField.setText("");
        mathMarksField.setText("");
        javaMarksField.setText("");
        phpMarksField.setText("");
    }

    // Main method to launch the application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StudentManagementSystem sms = new StudentManagementSystem();
            sms.setVisible(true);
        });
    }

    // Custom cell renderer class to highlight marks greater than 85
    private static class CustomCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof Number) {
                double marks = ((Number) value).doubleValue();
                if (marks > 85) {
                    cell.setBackground(Color.YELLOW); // Highlight cells with marks greater than 85
                } else {
                    cell.setBackground(Color.WHITE); // Default background color for other cells
                }
            } else {
                cell.setBackground(Color.WHITE); // Default background color for non-numeric cells
            }
            return cell;
        }
    }
}
