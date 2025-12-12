package sit;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.sql.PreparedStatement;
public class LibraryAssistant extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final String URL = "jdbc:mysql://localhost:3306/project";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private JTable table;
    private DefaultTableModel model;

    public LibraryAssistant() {

        setTitle("Library Management System");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        Color bgColor = new Color(30, 30, 30);
        Color panelColor = new Color(45, 45, 45);
        Color buttonColor = new Color(65, 105, 225);

        getContentPane().setBackground(bgColor);

        JLabel header = new JLabel("📚 Library Management System", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 28));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(header, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"Book ID", "Title", "Author", "Price"}, 0);
        table = new JTable(model);
        table.setBackground(panelColor);
        table.setForeground(Color.WHITE);
        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.getTableHeader().setBackground(Color.DARK_GRAY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(bgColor);
        add(scroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(bgColor);
        buttonPanel.setLayout(new FlowLayout());

        JButton addBtn = createButton("Add Book", buttonColor);
        JButton viewBtn = createButton("View All", buttonColor);
        JButton updateBtn = createButton("Update Book", buttonColor);
        JButton deleteBtn = createButton("Delete Book", buttonColor);

        buttonPanel.add(addBtn);
        buttonPanel.add(viewBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> addBookDialog());
        viewBtn.addActionListener(e -> loadBooks());
        updateBtn.addActionListener(e -> updateBookDialog());
        deleteBtn.addActionListener(e -> deleteBookDialog());

        loadBooks();
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setPreferredSize(new Dimension(150, 40));
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) { button.setBackground(color.brighter()); }
            public void mouseExited(MouseEvent evt) { button.setBackground(color); }
        });

        return button;
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private void loadBooks() {
        model.setRowCount(0);

        String query = "SELECT * FROM librarymanagement";

        try (Connection con = connect();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("BookId"),
                        rs.getString("BookTitle"),
                        rs.getString("BookAuthor"),
                        rs.getDouble("BookPrice")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading books: " + e.getMessage());
        }
    }

    private void addBookDialog() {
        JTextField id = new JTextField();
        JTextField title = new JTextField();
        JTextField author = new JTextField();
        JTextField price = new JTextField();

        Object[] inputs = {"Book ID:", id, "Title:", title, "Author:", author, "Price:", price};

        int option = JOptionPane.showConfirmDialog(this, inputs, "Add Book", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String query = "INSERT INTO librarymanagement VALUES (?, ?, ?, ?)";

            try (Connection con = connect();
                 PreparedStatement pst = (PreparedStatement) con.prepareStatement(query)) {

                pst.setInt(1, Integer.parseInt(id.getText()));
                pst.setString(2, title.getText());
                pst.setString(3, author.getText());
                pst.setDouble(4, Double.parseDouble(price.getText()));

                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Book Added Successfully!");
                loadBooks();

            } catch ( SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    private void updateBookDialog() {
        String id = JOptionPane.showInputDialog(this, "Enter Book ID to Update:");

        if (id == null) return;

        JTextField title = new JTextField();
        JTextField author = new JTextField();
        JTextField price = new JTextField();

        Object[] inputs = {"New Title:", title, "New Author:", author, "New Price:", price};

        int option = JOptionPane.showConfirmDialog(this, inputs, "Update Book", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String query = "UPDATE librarymanagement SET BookTitle=?, BookAuthor=?, BookPrice=? WHERE BookId=?";

            try (Connection con = connect();
            		PreparedStatement pst = (PreparedStatement) con.prepareStatement(query)) {

                pst.setString(1, title.getText());
                pst.setString(2, author.getText());
                pst.setDouble(3, Double.parseDouble(price.getText()));
                pst.setInt(4, Integer.parseInt(id));

                int rows = pst.executeUpdate();

                if (rows > 0)
                    JOptionPane.showMessageDialog(this, "Book Updated Successfully!");
                else
                    JOptionPane.showMessageDialog(this, "Book ID Not Found!");

                loadBooks();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    private void deleteBookDialog() {
        String id = JOptionPane.showInputDialog(this, "Enter Book ID to Delete:");

        if (id == null) return;

        String query = "DELETE FROM librarymanagement WHERE BookId=?";

        try (Connection con = connect();
        		PreparedStatement pst = (PreparedStatement) con.prepareStatement(query)) {

            pst.setInt(1, Integer.parseInt(id));

            int rows = pst.executeUpdate();

            if (rows > 0)
                JOptionPane.showMessageDialog(this, "Book Deleted Successfully!");
            else
                JOptionPane.showMessageDialog(this, "Book ID Not Found!");

            loadBooks();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibraryAssistant().setVisible(true));
    }
}
