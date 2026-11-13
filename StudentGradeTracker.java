import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * StudentGradeTracker
 * A complete GUI-based Java Swing application to input, manage, and analyze student grades.
 * 
 * Features:
 * - Add and remove student records using an ArrayList.
 * - Real-time calculation of class average, highest score, and lowest score.
 * - Automatic letter grade and status assignment.
 * - Interactive data table and detailed text summary report modal.
 */
public class StudentGradeTracker extends JFrame {

    // 5 Core Subjects
    public static final String[] SUBJECTS = {
            "Mathematics", "Science", "Social Studies", "Hindi", "English"
    };

    public static final Color[] SUBJECT_COLORS = {
            new Color(59, 130, 246),   // Mathematics - Blue
            new Color(16, 185, 129),   // Science - Emerald
            new Color(245, 158, 11),   // Social Studies - Amber
            new Color(236, 72, 153),   // Hindi - Pink
            new Color(139, 92, 246)    // English - Violet
    };

    // Data model class representing a single Student with 5 subject scores
    static class Student {
        private final String id;
        private final String name;
        private final double[] subjectGrades; // Order matches SUBJECTS

        public Student(String id, String name, double[] subjectGrades) {
            this.id = id;
            this.name = name;
            this.subjectGrades = subjectGrades.clone();
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public double[] getSubjectGrades() { return subjectGrades; }
        public double getSubjectGrade(int index) { return subjectGrades[index]; }

        // Overall average percentage across all 5 subjects
        public double getGrade() {
            double sum = 0;
            for (double g : subjectGrades) sum += g;
            return sum / subjectGrades.length;
        }

        public String getLetterGrade() {
            double avg = getGrade();
            if (avg >= 90) return "A";
            if (avg >= 80) return "B";
            if (avg >= 70) return "C";
            if (avg >= 60) return "D";
            return "F";
        }

        public String getStatus() {
            return getGrade() >= 60.0 ? "Pass" : "Fail";
        }
    }

    // Data storage using ArrayList as required
    private final ArrayList<Student> studentList = new ArrayList<>();

    // GUI Components
    private JTextField idField;
    private JTextField nameField;
    private final JTextField[] subjectFields = new JTextField[SUBJECTS.length];
    private JLabel feedbackLabel;
    private DefaultTableModel tableModel;
    private JTable studentTable;

    // Statistics Labels
    private JLabel totalStudentsValueLabel;
    private JLabel averageValueLabel;
    private JLabel highestValueLabel;
    private JLabel lowestValueLabel;

    public StudentGradeTracker() {
        // Frame initialization
        setTitle("Student Grade Tracker - 5 Subject Analytics Edition");
        setSize(1180, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));

        // Main container with colorful soft indigo-tinted background
        JPanel mainContainer = new JPanel(new BorderLayout(15, 15));
        mainContainer.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainContainer.setBackground(new Color(238, 242, 255));
        setContentPane(mainContainer);

        // 1. Header Panel
        mainContainer.add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Center Split (Left: Form & Actions, Right: Student Table)
        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setOpaque(false);
        centerPanel.add(createInputPanel(), BorderLayout.WEST);
        centerPanel.add(createTablePanel(), BorderLayout.CENTER);
        mainContainer.add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom Statistics Dashboard
        mainContainer.add(createStatsDashboard(), BorderLayout.SOUTH);

        // Populate with initial sample data so the user can test immediately
        loadSampleData();
    }

    /**
     * Creates the top header banner with a vibrant gradient background.
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(30, 27, 75),
                        getWidth(), getHeight(), new Color(67, 56, 202)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(16, 22, 16, 22));

        JLabel titleLabel = new JLabel("Student Multi-Subject Grade Tracker");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Track Mathematics, Science, Social Studies, Hindi & English with visual performance charts");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(199, 210, 254));

        JPanel textGroup = new JPanel(new GridLayout(2, 1, 0, 4));
        textGroup.setOpaque(false);
        textGroup.add(titleLabel);
        textGroup.add(subtitleLabel);

        JButton reportBtn = createStyledButton("View Charts & Summary Report", new Color(16, 185, 129), Color.WHITE);
        reportBtn.addActionListener(this::showSummaryReportDialog);

        headerPanel.add(textGroup, BorderLayout.WEST);
        headerPanel.add(reportBtn, BorderLayout.EAST);

        return headerPanel;
    }

    /**
     * Creates the left-side form panel for adding and managing student records across 5 subjects.
     */
    private JPanel createInputPanel() {
        JPanel inputCard = new JPanel(new BorderLayout(10, 10));
        inputCard.setPreferredSize(new Dimension(320, 0));
        inputCard.setBackground(Color.WHITE);
        inputCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(4, 0, 0, 0, new Color(79, 70, 229)),
                new EmptyBorder(14, 15, 14, 15)
        ));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.weightx = 1.0;

        JLabel sectionTitle = new JLabel("Add Student & Subject Marks");
        sectionTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        sectionTitle.setForeground(new Color(49, 46, 129));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(sectionTitle, gbc);

        // Student ID Input
        gbc.gridy++;
        JLabel idLabel = new JLabel("Student ID (e.g., S1007):");
        idLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        idLabel.setForeground(new Color(51, 65, 85));
        formPanel.add(idLabel, gbc);

        gbc.gridy++;
        idField = new JTextField();
        idField.setPreferredSize(new Dimension(0, 30));
        idField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        formPanel.add(idField, gbc);

        // Student Name Input
        gbc.gridy++;
        JLabel nameLabel = new JLabel("Student Full Name:");
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        nameLabel.setForeground(new Color(51, 65, 85));
        formPanel.add(nameLabel, gbc);

        gbc.gridy++;
        nameField = new JTextField();
        nameField.setPreferredSize(new Dimension(0, 30));
        nameField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        formPanel.add(nameField, gbc);

        // 5 Subject Inputs in a clean 2-column label/input layout
        gbc.gridy++;
        gbc.insets = new Insets(10, 4, 4, 4);
        JLabel subjHeader = new JLabel("Subject Scores (0 - 100):");
        subjHeader.setFont(new Font("SansSerif", Font.BOLD, 12));
        subjHeader.setForeground(new Color(79, 70, 229));
        formPanel.add(subjHeader, gbc);

        gbc.insets = new Insets(4, 4, 4, 4);
        for (int i = 0; i < SUBJECTS.length; i++) {
            gbc.gridy++;
            gbc.gridwidth = 1;
            gbc.gridx = 0;
            gbc.weightx = 0.55;

            JLabel sLabel = new JLabel("● " + SUBJECTS[i] + ":");
            sLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            sLabel.setForeground(SUBJECT_COLORS[i]);
            formPanel.add(sLabel, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.45;
            subjectFields[i] = new JTextField();
            subjectFields[i].setPreferredSize(new Dimension(0, 28));
            subjectFields[i].setFont(new Font("SansSerif", Font.PLAIN, 13));
            subjectFields[i].addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        addStudentAction();
                    }
                }
            });
            formPanel.add(subjectFields[i], gbc);
        }

        // Inline Validation / Feedback message
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(8, 4, 4, 4);
        feedbackLabel = new JLabel("Enter marks for all 5 subjects.");
        feedbackLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        feedbackLabel.setForeground(new Color(100, 116, 139));
        formPanel.add(feedbackLabel, gbc);

        // Add Button
        gbc.gridy++;
        gbc.insets = new Insets(8, 4, 4, 4);
        JButton addButton = createStyledButton("Add Student Record", new Color(22, 163, 74), Color.WHITE);
        addButton.addActionListener(e -> addStudentAction());
        formPanel.add(addButton, gbc);

        // Divider and Management Actions
        gbc.gridy++;
        gbc.insets = new Insets(10, 4, 6, 4);
        formPanel.add(new JSeparator(), gbc);

        // Delete Selected Button
        gbc.gridy++;
        gbc.insets = new Insets(4, 4, 4, 4);
        JButton deleteBtn = createStyledButton("Remove Selected Student", new Color(225, 29, 72), Color.WHITE);
        deleteBtn.addActionListener(e -> removeSelectedStudent());
        formPanel.add(deleteBtn, gbc);

        // Clear All & Reset Sample in a 2-button row
        gbc.gridy++;
        JPanel bottomBtns = new JPanel(new GridLayout(1, 2, 8, 0));
        bottomBtns.setOpaque(false);

        JButton clearBtn = createStyledButton("Clear All", new Color(100, 116, 139), Color.WHITE);
        clearBtn.addActionListener(e -> clearAllStudents());

        JButton sampleBtn = createStyledButton("Reset Sample", new Color(224, 231, 255), new Color(49, 46, 129));
        sampleBtn.addActionListener(e -> {
            studentList.clear();
            loadSampleData();
            setFeedback("Sample data reloaded.", new Color(67, 56, 202));
        });

        bottomBtns.add(clearBtn);
        bottomBtns.add(sampleBtn);
        formPanel.add(bottomBtns, gbc);

        // Push everything to the top of the card
        inputCard.add(formPanel, BorderLayout.NORTH);
        return inputCard;
    }

    /**
     * Creates the center table panel displaying all students, their 5 subject grades, and overall stats.
     */
    private JPanel createTablePanel() {
        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(4, 0, 0, 0, new Color(59, 130, 246)),
                new EmptyBorder(14, 15, 14, 15)
        ));

        JLabel tableTitle = new JLabel("Enrolled Students & 5-Subject Grade Roster");
        tableTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        tableTitle.setForeground(new Color(30, 41, 59));
        tableCard.add(tableTitle, BorderLayout.NORTH);

        String[] columns = {
                "ID", "Student Name", "Maths", "Science", "Social", "Hindi", "English", "Avg (%)", "Grade", "Status"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };

        studentTable = new JTable(tableModel);
        studentTable.setRowHeight(32);
        studentTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        studentTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        studentTable.getTableHeader().setBackground(new Color(224, 231, 255));
        studentTable.getTableHeader().setForeground(new Color(30, 27, 75));
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentTable.setSelectionBackground(new Color(199, 210, 254));
        studentTable.setSelectionForeground(new Color(30, 27, 75));
        studentTable.setGridColor(new Color(226, 232, 240));

        // Custom colorful cell renderer for grades, averages, and status badges
        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(col == 1 ? JLabel.LEFT : JLabel.CENTER);
                setFont(new Font("SansSerif", (col >= 7) ? Font.BOLD : Font.PLAIN, 13));

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    c.setForeground(new Color(30, 41, 59));

                    if (col == 8 && value != null) { // Grade column
                        String g = value.toString();
                        switch (g) {
                            case "A": c.setForeground(new Color(22, 163, 74)); break;
                            case "B": c.setForeground(new Color(37, 99, 235)); break;
                            case "C": c.setForeground(new Color(202, 138, 4)); break;
                            case "D": c.setForeground(new Color(234, 88, 12)); break;
                            default:  c.setForeground(new Color(220, 38, 38)); break;
                        }
                    } else if (col == 9 && value != null) { // Status column
                        boolean pass = "Pass".equals(value.toString());
                        c.setBackground(pass ? new Color(220, 252, 231) : new Color(254, 226, 226));
                        c.setForeground(pass ? new Color(21, 128, 61) : new Color(185, 28, 28));
                    }
                }
                return c;
            }
        };

        for (int i = 0; i < columns.length; i++) {
            studentTable.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
        }

        // Adjust column widths
        studentTable.getColumnModel().getColumn(0).setPreferredWidth(55);
        studentTable.getColumnModel().getColumn(1).setPreferredWidth(155);
        for (int i = 2; i <= 7; i++) {
            studentTable.getColumnModel().getColumn(i).setPreferredWidth(68);
        }
        studentTable.getColumnModel().getColumn(8).setPreferredWidth(55);
        studentTable.getColumnModel().getColumn(9).setPreferredWidth(65);

        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        return tableCard;
    }

    /**
     * Creates the bottom dashboard with vibrant gradient cards for real-time statistics.
     */
    private JPanel createStatsDashboard() {
        JPanel dashboardPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        dashboardPanel.setOpaque(false);
        dashboardPanel.setPreferredSize(new Dimension(0, 100));

        totalStudentsValueLabel = new JLabel("0");
        averageValueLabel = new JLabel("0.00%");
        highestValueLabel = new JLabel("N/A");
        lowestValueLabel = new JLabel("N/A");

        dashboardPanel.add(createStatCard("Total Students", totalStudentsValueLabel,
                new Color(37, 99, 235), new Color(29, 78, 216)));
        dashboardPanel.add(createStatCard("Overall Class Average", averageValueLabel,
                new Color(124, 58, 237), new Color(109, 40, 217)));
        dashboardPanel.add(createStatCard("Highest Overall Score", highestValueLabel,
                new Color(16, 185, 129), new Color(5, 150, 105)));
        dashboardPanel.add(createStatCard("Lowest Overall Score", lowestValueLabel,
                new Color(244, 63, 94), new Color(225, 29, 72)));

        return dashboardPanel;
    }

    /**
     * Helper method to build a colorful gradient statistics metric card.
     */
    private JPanel createStatCard(String title, JLabel valueLabel, Color colorStart, Color colorEnd) {
        JPanel card = new JPanel(new BorderLayout(5, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, colorStart, getWidth(), getHeight(), colorEnd);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        titleLabel.setForeground(new Color(224, 242, 254));

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 19));
        valueLabel.setForeground(Color.WHITE);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    /**
     * Validates user input for Student ID, Name, and all 5 subjects, then adds a new Student.
     */
    private void addStudentAction() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();

        if (id.isEmpty()) {
            setFeedback("Error: Please enter a Student ID.", new Color(220, 38, 38));
            idField.requestFocus();
            return;
        }

        // Ensure Student ID is unique
        for (Student s : studentList) {
            if (s.getId().equalsIgnoreCase(id)) {
                setFeedback("Error: Student ID '" + id + "' already exists.", new Color(220, 38, 38));
                idField.requestFocus();
                return;
            }
        }

        if (name.isEmpty()) {
            setFeedback("Error: Please enter a student name.", new Color(220, 38, 38));
            nameField.requestFocus();
            return;
        }

        double[] scores = new double[SUBJECTS.length];
        for (int i = 0; i < SUBJECTS.length; i++) {
            String text = subjectFields[i].getText().trim();
            if (text.isEmpty()) {
                setFeedback("Error: Enter score for " + SUBJECTS[i] + ".", new Color(220, 38, 38));
                subjectFields[i].requestFocus();
                return;
            }
            try {
                double val = Double.parseDouble(text);
                if (val < 0.0 || val > 100.0) {
                    setFeedback("Error: " + SUBJECTS[i] + " must be 0-100.", new Color(220, 38, 38));
                    subjectFields[i].requestFocus();
                    return;
                }
                scores[i] = val;
            } catch (NumberFormatException ex) {
                setFeedback("Error: Invalid score in " + SUBJECTS[i] + ".", new Color(220, 38, 38));
                subjectFields[i].requestFocus();
                return;
            }
        }

        // Create and store student
        Student newStudent = new Student(id, name, scores);
        studentList.add(newStudent);

        // Reset inputs
        idField.setText("");
        nameField.setText("");
        for (JTextField tf : subjectFields) tf.setText("");
        idField.requestFocus();

        // Refresh UI
        refreshTableAndStats();
        setFeedback("Added " + name + " [" + id + "] (Avg: " + String.format("%.1f%%", newStudent.getGrade()) + ")", new Color(22, 163, 74));
    }

    /**
     * Removes the currently selected student from the ArrayList and table.
     */
    private void removeSelectedStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            setFeedback("Select a student row in the table first.", new Color(217, 119, 6));
            return;
        }

        Student removed = studentList.remove(selectedRow);
        refreshTableAndStats();
        setFeedback("Removed student: " + removed.getName(), new Color(100, 116, 139));
    }

    /**
     * Clears all student records from the tracker.
     */
    private void clearAllStudents() {
        if (studentList.isEmpty()) {
            setFeedback("The roster is already empty.", new Color(100, 116, 139));
            return;
        }
        studentList.clear();
        refreshTableAndStats();
        setFeedback("Cleared all student records.", new Color(220, 38, 38));
    }

    /**
     * Synchronizes the JTable and recalculates average, highest, and lowest scores.
     */
    private void refreshTableAndStats() {
        // 1. Update Table Rows
        tableModel.setRowCount(0);
        for (Student s : studentList) {
            double[] sg = s.getSubjectGrades();
            tableModel.addRow(new Object[]{
                    s.getId(),
                    s.getName(),
                    String.format("%.1f", sg[0]),
                    String.format("%.1f", sg[1]),
                    String.format("%.1f", sg[2]),
                    String.format("%.1f", sg[3]),
                    String.format("%.1f", sg[4]),
                    String.format("%.2f", s.getGrade()),
                    s.getLetterGrade(),
                    s.getStatus()
            });
        }

        // 2. Recalculate Statistics
        int count = studentList.size();
        totalStudentsValueLabel.setText(String.valueOf(count));

        if (count == 0) {
            averageValueLabel.setText("0.00%");
            highestValueLabel.setText("N/A");
            lowestValueLabel.setText("N/A");
            return;
        }

        double sum = 0.0;
        Student highestStudent = studentList.get(0);
        Student lowestStudent = studentList.get(0);

        for (Student s : studentList) {
            double g = s.getGrade();
            sum += g;
            if (g > highestStudent.getGrade()) {
                highestStudent = s;
            }
            if (g < lowestStudent.getGrade()) {
                lowestStudent = s;
            }
        }

        double average = sum / count;

        averageValueLabel.setText(String.format("%.2f%%", average));
        highestValueLabel.setText(String.format("%.1f%% (%s)", highestStudent.getGrade(), highestStudent.getName()));
        lowestValueLabel.setText(String.format("%.1f%% (%s)", lowestStudent.getGrade(), lowestStudent.getName()));
    }

    /**
     * Opens a custom modal dialog displaying visual subject charts, grade distribution charts, and text report.
     */
    private void showSummaryReportDialog(ActionEvent e) {
        JDialog dialog = new JDialog(this, "5-Subject Analytics & Student Summary Report", true);
        dialog.setSize(960, 660);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabbedPane.setBorder(new EmptyBorder(10, 10, 0, 10));

        // Tab 1: Dedicated 5-Subject Performance Charts (Average Bar Chart + High/Avg/Low Breakdown)
        JPanel subjectChartsPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        subjectChartsPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        subjectChartsPanel.setBackground(new Color(238, 242, 255));
        subjectChartsPanel.add(new SubjectAverageBarChartPanel());
        subjectChartsPanel.add(new SubjectMinMaxComparisonChartPanel());
        tabbedPane.addTab("5-Subject Performance Charts", subjectChartsPanel);

        // Tab 2: Bar Chart & Pie Chart (Overall Grade Distribution)
        JPanel distributionChartsPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        distributionChartsPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        distributionChartsPanel.setBackground(new Color(238, 242, 255));
        distributionChartsPanel.add(new GradeBarChartPanel());
        distributionChartsPanel.add(new GradePieChartPanel());
        tabbedPane.addTab("Grade Distribution (Bar & Pie)", distributionChartsPanel);

        // Tab 3: Student Scores Graph vs Class Average
        JPanel scoreGraphWrapper = new JPanel(new BorderLayout());
        scoreGraphWrapper.setBorder(new EmptyBorder(12, 12, 12, 12));
        scoreGraphWrapper.setBackground(new Color(238, 242, 255));
        scoreGraphWrapper.add(new StudentScoreGraphPanel(), BorderLayout.CENTER);
        tabbedPane.addTab("Student Overall Trend Graph", scoreGraphWrapper);

        // Tab 4: Detailed Text Report
        JTextArea reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportArea.setMargin(new Insets(15, 15, 15, 15));
        reportArea.setBackground(new Color(248, 250, 252));
        reportArea.setText(generateSummaryReportText());
        reportArea.setCaretPosition(0);

        JScrollPane textScrollPane = new JScrollPane(reportArea);
        textScrollPane.setBorder(new EmptyBorder(12, 12, 12, 12));
        tabbedPane.addTab("Detailed Text Report", textScrollPane);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBorder(new EmptyBorder(0, 10, 10, 10));
        JButton closeBtn = createStyledButton("Close Report", new Color(49, 46, 129), Color.WHITE);
        closeBtn.addActionListener(ev -> dialog.dispose());
        footer.add(closeBtn);

        dialog.add(tabbedPane, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * Helper method to compute letter grade counts [A, B, C, D, F].
     */
    private int[] calculateGradeDistribution() {
        int[] dist = new int[5];
        for (Student s : studentList) {
            switch (s.getLetterGrade()) {
                case "A": dist[0]++; break;
                case "B": dist[1]++; break;
                case "C": dist[2]++; break;
                case "D": dist[3]++; break;
                default:  dist[4]++; break;
            }
        }
        return dist;
    }

    /**
     * Dedicated Chart 1: Subject-Wise Class Average Bar Chart for the 5 Subjects.
     */
    private class SubjectAverageBarChartPanel extends JPanel {
        public SubjectAverageBarChartPanel() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.setColor(new Color(30, 27, 75));
            g2.drawString("Subject-Wise Average Score (Bar Chart)", 20, 32);

            if (studentList.isEmpty()) {
                g2.setFont(new Font("SansSerif", Font.ITALIC, 14));
                g2.setColor(new Color(100, 116, 139));
                g2.drawString("No student records available to plot.", 20, h / 2);
                return;
            }

            int leftPad = 50;
            int rightPad = 25;
            int topPad = 65;
            int bottomPad = 60;
            int chartW = w - leftPad - rightPad;
            int chartH = h - topPad - bottomPad;

            // Draw Y-Axis Grid (0% to 100%)
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            for (int pct = 0; pct <= 100; pct += 20) {
                int y = topPad + chartH - (int) ((pct / 100.0) * chartH);
                g2.setColor(new Color(226, 232, 240));
                g2.drawLine(leftPad, y, leftPad + chartW, y);
                g2.setColor(new Color(100, 116, 139));
                g2.drawString(pct + "%", leftPad - 35, y + 4);
            }

            // Draw Axes
            g2.setColor(new Color(148, 163, 184));
            g2.drawLine(leftPad, topPad, leftPad, topPad + chartH);
            g2.drawLine(leftPad, topPad + chartH, leftPad + chartW, topPad + chartH);

            int numSubjects = SUBJECTS.length;
            int slotWidth = chartW / numSubjects;
            int barWidth = Math.min(50, (int) (slotWidth * 0.6));

            for (int i = 0; i < numSubjects; i++) {
                double sum = 0;
                for (Student s : studentList) sum += s.getSubjectGrade(i);
                double avg = sum / studentList.size();

                int barHeight = (int) ((avg / 100.0) * chartH);
                int x = leftPad + i * slotWidth + (slotWidth - barWidth) / 2;
                int y = topPad + chartH - barHeight;

                g2.setColor(SUBJECT_COLORS[i]);
                g2.fillRoundRect(x, y, barWidth, barHeight, 8, 8);

                // Average Percentage Label Above Bar
                g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                g2.setColor(new Color(30, 41, 59));
                String valStr = String.format("%.1f%%", avg);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(valStr, x + (barWidth - fm.stringWidth(valStr)) / 2, y - 6);

                // Subject Name Label
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                g2.setColor(new Color(51, 65, 85));
                String shortName = SUBJECTS[i].equals("Social Studies") ? "Social" : SUBJECTS[i];
                FontMetrics lfm = g2.getFontMetrics();
                g2.drawString(shortName, x + (barWidth - lfm.stringWidth(shortName)) / 2, topPad + chartH + 22);
            }
        }
    }

    /**
     * Dedicated Chart 2: Grouped High / Average / Low Comparison Graph for each of the 5 Subjects.
     */
    private class SubjectMinMaxComparisonChartPanel extends JPanel {
        public SubjectMinMaxComparisonChartPanel() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.setColor(new Color(30, 27, 75));
            g2.drawString("Subject Highest / Average / Lowest Breakdown", 20, 32);

            if (studentList.isEmpty()) {
                g2.setFont(new Font("SansSerif", Font.ITALIC, 14));
                g2.setColor(new Color(100, 116, 139));
                g2.drawString("No student records available to plot.", 20, h / 2);
                return;
            }

            // Mini Legend for Highest, Average, Lowest
            Color highCol = new Color(22, 163, 74);
            Color avgCol = new Color(59, 130, 246);
            Color lowCol = new Color(244, 63, 94);

            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.setColor(highCol); g2.fillRoundRect(20, 42, 10, 10, 3, 3);
            g2.setColor(new Color(51, 65, 85)); g2.drawString("Highest", 35, 51);

            g2.setColor(avgCol); g2.fillRoundRect(95, 42, 10, 10, 3, 3);
            g2.setColor(new Color(51, 65, 85)); g2.drawString("Average", 110, 51);

            g2.setColor(lowCol); g2.fillRoundRect(175, 42, 10, 10, 3, 3);
            g2.setColor(new Color(51, 65, 85)); g2.drawString("Lowest", 190, 51);

            int leftPad = 50;
            int rightPad = 25;
            int topPad = 72;
            int bottomPad = 60;
            int chartW = w - leftPad - rightPad;
            int chartH = h - topPad - bottomPad;

            // Grid
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            for (int pct = 0; pct <= 100; pct += 20) {
                int y = topPad + chartH - (int) ((pct / 100.0) * chartH);
                g2.setColor(new Color(226, 232, 240));
                g2.drawLine(leftPad, y, leftPad + chartW, y);
                g2.setColor(new Color(100, 116, 139));
                g2.drawString(pct + "%", leftPad - 35, y + 4);
            }

            int numSubjects = SUBJECTS.length;
            int slotWidth = chartW / numSubjects;
            int singleBarW = Math.max(8, Math.min(16, (slotWidth - 20) / 3));

            for (int i = 0; i < numSubjects; i++) {
                double sum = 0;
                double high = studentList.get(0).getSubjectGrade(i);
                double low = studentList.get(0).getSubjectGrade(i);

                for (Student s : studentList) {
                    double val = s.getSubjectGrade(i);
                    sum += val;
                    if (val > high) high = val;
                    if (val < low) low = val;
                }
                double avg = sum / studentList.size();

                double[] metrics = {high, avg, low};
                Color[] mColors = {highCol, avgCol, lowCol};

                int groupW = singleBarW * 3 + 6;
                int startX = leftPad + i * slotWidth + (slotWidth - groupW) / 2;

                for (int m = 0; m < 3; m++) {
                    int bH = (int) ((metrics[m] / 100.0) * chartH);
                    int bx = startX + m * (singleBarW + 3);
                    int by = topPad + chartH - bH;

                    g2.setColor(mColors[m]);
                    g2.fillRoundRect(bx, by, singleBarW, bH, 4, 4);
                }

                // Subject label on X-axis
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                g2.setColor(new Color(51, 65, 85));
                String shortName = SUBJECTS[i].equals("Social Studies") ? "Social" : SUBJECTS[i];
                FontMetrics lfm = g2.getFontMetrics();
                int centerSlot = leftPad + i * slotWidth + slotWidth / 2;
                g2.drawString(shortName, centerSlot - lfm.stringWidth(shortName) / 2, topPad + chartH + 22);
            }
        }
    }

    /**
     * Custom Bar Chart Panel showing student counts per Letter Grade (A - F).
     */
    private class GradeBarChartPanel extends JPanel {
        private final String[] labels = {"A (90-100)", "B (80-89)", "C (70-79)", "D (60-69)", "F (<60)"};
        private final Color[] colors = {
                new Color(22, 163, 74),   // A - Green
                new Color(59, 130, 246),  // B - Blue
                new Color(234, 179, 8),   // C - Yellow
                new Color(249, 115, 22),  // D - Orange
                new Color(220, 38, 38)    // F - Red
        };

        public GradeBarChartPanel() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Chart Title
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.setColor(new Color(30, 41, 59));
            g2.drawString("Grade Distribution (Bar Chart)", 20, 32);

            if (studentList.isEmpty()) {
                g2.setFont(new Font("SansSerif", Font.ITALIC, 14));
                g2.setColor(new Color(100, 116, 139));
                g2.drawString("No student records available to plot.", 20, h / 2);
                return;
            }

            int[] dist = calculateGradeDistribution();
            int maxCount = 1;
            for (int c : dist) {
                if (c > maxCount) maxCount = c;
            }

            int leftPad = 50;
            int rightPad = 25;
            int topPad = 65;
            int bottomPad = 55;
            int chartW = w - leftPad - rightPad;
            int chartH = h - topPad - bottomPad;

            // Draw horizontal grid lines and Y-axis ticks
            int gridSteps = Math.min(maxCount, 5);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            for (int i = 0; i <= gridSteps; i++) {
                int y = topPad + chartH - (int) ((double) i / gridSteps * chartH);
                g2.setColor(new Color(226, 232, 240));
                g2.drawLine(leftPad, y, leftPad + chartW, y);

                String tickVal = String.format("%.0f", (double) i / gridSteps * maxCount);
                g2.setColor(new Color(100, 116, 139));
                g2.drawString(tickVal, leftPad - 25, y + 4);
            }

            // Draw Axes
            g2.setColor(new Color(148, 163, 184));
            g2.drawLine(leftPad, topPad, leftPad, topPad + chartH);
            g2.drawLine(leftPad, topPad + chartH, leftPad + chartW, topPad + chartH);

            // Draw Bars
            int numBars = dist.length;
            int slotWidth = chartW / numBars;
            int barWidth = Math.min(48, (int) (slotWidth * 0.6));

            for (int i = 0; i < numBars; i++) {
                int count = dist[i];
                int barHeight = (int) (((double) count / maxCount) * (chartH - 15));
                int x = leftPad + i * slotWidth + (slotWidth - barWidth) / 2;
                int y = topPad + chartH - barHeight;

                // Bar Fill
                g2.setColor(colors[i]);
                g2.fillRoundRect(x, y, barWidth, barHeight, 6, 6);

                // Count Value Above Bar
                g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                g2.setColor(new Color(30, 41, 59));
                String valStr = String.valueOf(count);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(valStr, x + (barWidth - fm.stringWidth(valStr)) / 2, y - 6);

                // X-axis Category Label
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                g2.setColor(new Color(71, 85, 105));
                FontMetrics lfm = g2.getFontMetrics();
                g2.drawString(labels[i], x + (barWidth - lfm.stringWidth(labels[i])) / 2, topPad + chartH + 22);
            }
        }
    }

    /**
     * Custom Pie / Donut Chart Panel showing proportional percentage of Letter Grades (A - F).
     */
    private class GradePieChartPanel extends JPanel {
        private final String[] labels = {"Grade A (90-100%)", "Grade B (80-89%)", "Grade C (70-79%)", "Grade D (60-69%)", "Grade F (<60%)"};
        private final Color[] colors = {
                new Color(22, 163, 74),   // A - Green
                new Color(59, 130, 246),  // B - Blue
                new Color(234, 179, 8),   // C - Yellow
                new Color(249, 115, 22),  // D - Orange
                new Color(220, 38, 38)    // F - Red
        };

        public GradePieChartPanel() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.setColor(new Color(30, 41, 59));
            g2.drawString("Grade Share (Pie / Donut Chart)", 20, 32);

            if (studentList.isEmpty()) {
                g2.setFont(new Font("SansSerif", Font.ITALIC, 14));
                g2.setColor(new Color(100, 116, 139));
                g2.drawString("No student records available to plot.", 20, h / 2);
                return;
            }

            int[] dist = calculateGradeDistribution();
            int total = studentList.size();

            int diameter = Math.min(w - 200, h - 110);
            diameter = Math.max(130, diameter);
            int pieX = 25;
            int pieY = (h - diameter) / 2 + 15;

            double currentAngle = 90.0;
            for (int i = 0; i < dist.length; i++) {
                if (dist[i] == 0) continue;
                double arcAngle = (dist[i] * 360.0) / total;
                g2.setColor(colors[i]);
                g2.fillArc(pieX, pieY, diameter, diameter, (int) Math.round(currentAngle), (int) -Math.round(arcAngle));
                currentAngle -= arcAngle;
            }

            // Inner donut cutout showing total student count
            int holeSize = diameter / 2;
            int holeX = pieX + (diameter - holeSize) / 2;
            int holeY = pieY + (diameter - holeSize) / 2;
            g2.setColor(Color.WHITE);
            g2.fillOval(holeX, holeY, holeSize, holeSize);

            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.setColor(new Color(30, 41, 59));
            String centerTitle = total + " Students";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(centerTitle, pieX + (diameter - fm.stringWidth(centerTitle)) / 2, pieY + diameter / 2 + 5);

            // Draw Color Legend on the right side
            int legendX = pieX + diameter + 25;
            int legendY = pieY + 15;
            for (int i = 0; i < labels.length; i++) {
                g2.setColor(colors[i]);
                g2.fillRoundRect(legendX, legendY + i * 32, 14, 14, 4, 4);

                double pct = (dist[i] * 100.0) / total;
                String legText = String.format("%s: %d (%.1f%%)", labels[i], dist[i], pct);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
                g2.setColor(new Color(51, 65, 85));
                g2.drawString(legText, legendX + 22, legendY + i * 32 + 12);
            }
        }
    }

    /**
     * Custom Student Overall Score Comparison & Trend Line Graph Panel.
     */
    private class StudentScoreGraphPanel extends JPanel {
        public StudentScoreGraphPanel() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.setColor(new Color(30, 41, 59));
            g2.drawString("Individual Student Overall Averages vs. Class Average Benchmark", 20, 32);

            if (studentList.isEmpty()) {
                g2.setFont(new Font("SansSerif", Font.ITALIC, 14));
                g2.setColor(new Color(100, 116, 139));
                g2.drawString("No student records available to plot.", 20, h / 2);
                return;
            }

            int leftPad = 55;
            int rightPad = 35;
            int topPad = 65;
            int bottomPad = 65;
            int chartW = w - leftPad - rightPad;
            int chartH = h - topPad - bottomPad;

            // Horizontal percentage grid lines
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            for (int pct = 0; pct <= 100; pct += 20) {
                int y = topPad + chartH - (int) ((pct / 100.0) * chartH);
                g2.setColor(new Color(226, 232, 240));
                g2.drawLine(leftPad, y, leftPad + chartW, y);
                g2.setColor(new Color(100, 116, 139));
                g2.drawString(pct + "%", leftPad - 35, y + 4);
            }

            // Axes
            g2.setColor(new Color(148, 163, 184));
            g2.drawLine(leftPad, topPad, leftPad, topPad + chartH);
            g2.drawLine(leftPad, topPad + chartH, leftPad + chartW, topPad + chartH);

            int n = studentList.size();
            int slotW = Math.max(1, chartW / n);
            int barW = Math.min(44, Math.max(12, (int) (slotW * 0.5)));

            double totalSum = 0;
            int[] ptX = new int[n];
            int[] ptY = new int[n];

            for (int i = 0; i < n; i++) {
                Student s = studentList.get(i);
                double score = s.getGrade();
                totalSum += score;

                int barH = (int) ((score / 100.0) * chartH);
                int x = leftPad + i * slotW + (slotW - barW) / 2;
                int y = topPad + chartH - barH;

                ptX[i] = x + barW / 2;
                ptY[i] = y;

                // Pass/Fail color-coded bar
                g2.setColor(score >= 60.0 ? new Color(147, 197, 253) : new Color(252, 165, 165));
                g2.fillRoundRect(x, y, barW, barH, 6, 6);

                // Student Name Label
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                g2.setColor(new Color(51, 65, 85));
                String shortName = truncate(s.getName(), 10);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(shortName, ptX[i] - fm.stringWidth(shortName) / 2, topPad + chartH + 22);
            }

            // Draw Class Average Dashed Reference Line
            double classAvg = totalSum / n;
            int avgY = topPad + chartH - (int) ((classAvg / 100.0) * chartH);
            Stroke oldStroke = g2.getStroke();
            g2.setColor(new Color(217, 119, 6));
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{6, 6}, 0));
            g2.drawLine(leftPad, avgY, leftPad + chartW, avgY);

            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString(String.format("Class Avg: %.1f%%", classAvg), leftPad + 10, Math.max(topPad + 12, avgY - 6));

            // Draw Trend Line connecting student scores
            g2.setColor(new Color(67, 56, 202));
            g2.setStroke(new BasicStroke(2.5f));
            for (int i = 0; i < n - 1; i++) {
                g2.drawLine(ptX[i], ptY[i], ptX[i + 1], ptY[i + 1]);
            }
            g2.setStroke(oldStroke);

            // Draw Points and Score Labels
            for (int i = 0; i < n; i++) {
                g2.setColor(new Color(67, 56, 202));
                g2.fillOval(ptX[i] - 5, ptY[i] - 5, 10, 10);
                g2.setColor(Color.WHITE);
                g2.fillOval(ptX[i] - 2, ptY[i] - 2, 4, 4);

                String scoreStr = String.format("%.1f%%", studentList.get(i).getGrade());
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                g2.setColor(new Color(30, 27, 75));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(scoreStr, ptX[i] - fm.stringWidth(scoreStr) / 2, ptY[i] - 10);
            }
        }
    }

    /**
     * Builds the plain-text summary report string with overall statistics, 5-subject breakdown, and student roster.
     */
    private String generateSummaryReportText() {
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================================================================\n");
        sb.append("                            STUDENT 5-SUBJECT GRADE SUMMARY REPORT                                  \n");
        sb.append("====================================================================================================\n\n");

        if (studentList.isEmpty()) {
            sb.append("No student data available. Please add students to generate metrics.\n");
            return sb.toString();
        }

        double sum = 0;
        Student highest = studentList.get(0);
        Student lowest = studentList.get(0);
        int passCount = 0;
        int[] gradeDist = new int[5]; // A, B, C, D, F

        for (Student s : studentList) {
            double g = s.getGrade();
            sum += g;
            if (g > highest.getGrade()) highest = s;
            if (g < lowest.getGrade()) lowest = s;
            if (g >= 60.0) passCount++;

            switch (s.getLetterGrade()) {
                case "A": gradeDist[0]++; break;
                case "B": gradeDist[1]++; break;
                case "C": gradeDist[2]++; break;
                case "D": gradeDist[3]++; break;
                default:  gradeDist[4]++; break;
            }
        }

        double avg = sum / studentList.size();
        double passRate = (passCount * 100.0) / studentList.size();

        sb.append("1. CLASS PERFORMANCE OVERVIEW\n");
        sb.append("----------------------------------------------------------------------------------------------------\n");
        sb.append(String.format("   Total Enrolled Students : %d\n", studentList.size()));
        sb.append(String.format("   Overall Class Average   : %.2f%%\n", avg));
        sb.append(String.format("   Highest Overall Score   : %.2f%% (%s - %s)\n", highest.getGrade(), highest.getName(), highest.getId()));
        sb.append(String.format("   Lowest Overall Score    : %.2f%% (%s - %s)\n", lowest.getGrade(), lowest.getName(), lowest.getId()));
        sb.append(String.format("   Passing Rate (>=60%%)    : %.1f%% (%d Passed, %d Failed)\n\n", passRate, passCount, studentList.size() - passCount));

        sb.append("2. SUBJECT-WISE ANALYTICS (Mathematics, Science, Social Studies, Hindi, English)\n");
        sb.append("----------------------------------------------------------------------------------------------------\n");
        sb.append(String.format("   %-18s %-14s %-28s %-28s\n", "Subject", "Average", "Highest Score", "Lowest Score"));
        sb.append("----------------------------------------------------------------------------------------------------\n");
        for (int i = 0; i < SUBJECTS.length; i++) {
            double sSum = 0;
            Student sHigh = studentList.get(0);
            Student sLow = studentList.get(0);
            for (Student s : studentList) {
                double val = s.getSubjectGrade(i);
                sSum += val;
                if (val > sHigh.getSubjectGrade(i)) sHigh = s;
                if (val < sLow.getSubjectGrade(i)) sLow = s;
            }
            double sAvg = sSum / studentList.size();
            sb.append(String.format("   %-18s %6.2f%%        %5.1f%% (%-16s)   %5.1f%% (%-16s)\n",
                    SUBJECTS[i], sAvg,
                    sHigh.getSubjectGrade(i), truncate(sHigh.getName(), 16),
                    sLow.getSubjectGrade(i), truncate(sLow.getName(), 16)));
        }
        sb.append("\n");

        sb.append("3. INDIVIDUAL STUDENT ROSTER & SUBJECT MARKS\n");
        sb.append("----------------------------------------------------------------------------------------------------\n");
        sb.append(String.format("%-6s %-18s %7s %7s %7s %7s %7s %9s %-6s %-6s\n",
                "ID", "Student Name", "Maths", "Sci", "Social", "Hindi", "Eng", "Avg(%)", "Grade", "Status"));
        sb.append("----------------------------------------------------------------------------------------------------\n");

        for (Student s : studentList) {
            double[] sg = s.getSubjectGrades();
            sb.append(String.format("%-6s %-18s %7.1f %7.1f %7.1f %7.1f %7.1f %8.2f%%   %-6s %-6s\n",
                    s.getId(),
                    truncate(s.getName(), 17),
                    sg[0], sg[1], sg[2], sg[3], sg[4],
                    s.getGrade(),
                    s.getLetterGrade(),
                    s.getStatus()));
        }
        sb.append("====================================================================================================\n");
        return sb.toString();
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Populates the tracker with initial sample students across all 5 subjects.
     */
    private void loadSampleData() {
        studentList.add(new Student("S1001", "Aarav Sharma",  new double[]{96, 94, 91, 92, 95}));
        studentList.add(new Student("S1002", "Priya Patel",   new double[]{88, 91, 84, 89, 86}));
        studentList.add(new Student("S1003", "Rohan Verma",   new double[]{74, 78, 72, 80, 75}));
        studentList.add(new Student("S1004", "Ananya Nair",   new double[]{99, 97, 96, 95, 98}));
        studentList.add(new Student("S1005", "Vikram Singh",  new double[]{52, 58, 55, 61, 49}));
        studentList.add(new Student("S1006", "Meera Reddy",   new double[]{85, 82, 79, 84, 88}));
        refreshTableAndStats();
    }

    /**
     * Updates the inline status message in the form panel.
     */
    private void setFeedback(String message, Color color) {
        feedbackLabel.setText(message);
        feedbackLabel.setForeground(color);
    }

    /**
     * Utility method to create consistently styled flat buttons.
     */
    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 16, 10, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        return btn;
    }

    /**
     * Application Entry Point.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fallback to default Swing look and feel
        }

        SwingUtilities.invokeLater(() -> {
            StudentGradeTracker app = new StudentGradeTracker();
            app.setVisible(true);
        });
    }
}