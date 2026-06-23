package tracker;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;

//  GradeTracker GUI  —  Swing-based Student Grade Management System
public class GradeTrackerGUI extends JFrame {

    // Brand palette (from SVG logo)
    static final Color NAVY     = new Color(0x16, 0x31, 0x4F);   // #16314f
    static final Color NAVY_MID = new Color(0x23, 0x4A, 0x72);   // #234a72
    static final Color TEAL     = new Color(0x2B, 0xB3, 0xA3);   // #2bb3a3
    static final Color GOLD     = new Color(0xF2, 0xC1, 0x4E);   // #f2c14e
    static final Color GOLD_DRK = new Color(0xCA, 0xA2, 0x3E);   // #caa23e
    static final Color GREY_TXT = new Color(0x6B, 0x72, 0x80);   // #6b7280
    static final Color BG       = new Color(0xF4, 0xF7, 0xFA);
    static final Color WHITE    = Color.WHITE;
    static final Color GREEN    = new Color(0x22, 0xC5, 0x5E);
    static final Color ORANGE   = new Color(0xF9, 0x73, 0x16);
    static final Color RED_ERR  = new Color(0xEF, 0x44, 0x44);

    //  Data 
    private final ArrayList<Student> students = new ArrayList<>();
    private DefaultTableModel tableModel;
    private JLabel lblAvg, lblHighest, lblLowest, lblCount;
    private JTable table;

    
    //  Constructor
    
    public GradeTrackerGUI() {
        super("GradeTracker — Student Grade Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(960, 680);
        setMinimumSize(new Dimension(800, 560));
        setLocationRelativeTo(null);
        setBackground(BG);

        // Root panel with a subtle gradient background
        JPanel root = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0xEEF4FB), 0, getHeight(), BG);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        root.setBorder(new EmptyBorder(0, 0, 0, 0));

        root.add(buildHeader(),      BorderLayout.NORTH);
        root.add(buildCenter(),      BorderLayout.CENTER);
        root.add(buildStatsFooter(), BorderLayout.SOUTH);

        setContentPane(root);
        setVisible(true);
    }

    //  HEADER — Logo panel
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(NAVY);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Subtle teal stripe at bottom
                g2.setColor(TEAL);
                g2.fillRect(0, getHeight() - 3, getWidth(), 3);
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(0, 88));
        header.setBorder(new EmptyBorder(0, 24, 3, 24));

        // Logo canvas (left)
        LogoPanel logo = new LogoPanel();
        logo.setPreferredSize(new Dimension(340, 88));
        logo.setOpaque(false);

        // Subtitle label (right)
        JLabel subtitle = new JLabel("v1.0  —  Manage students, scores & grades");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(0xA0, 0xB8, 0xD0));
        subtitle.setHorizontalAlignment(SwingConstants.RIGHT);

        header.add(logo,     BorderLayout.WEST);
        header.add(subtitle, BorderLayout.EAST);
        return header;
    }

    //  CENTER — Table + Form side-by-side
    private JSplitPane buildCenter() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildTablePanel(), buildFormPanel());
        split.setDividerLocation(580);
        split.setDividerSize(6);
        split.setResizeWeight(0.65);
        split.setBorder(null);
        split.setBackground(BG);
        return split;
    }

    // Table Panel
    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 12, 10));

        // Section label
        JLabel title = sectionLabel("All Students");
        panel.add(title, BorderLayout.NORTH);

        // Table
        String[] cols = {"#", "Name", "Score", "Grade", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xD1, 0xDE, 0xEC), 1));
        scroll.setBackground(WHITE);
        scroll.getViewport().setBackground(WHITE);
        panel.add(scroll, BorderLayout.CENTER);

        // Bottom action bar
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);

        JButton btnRemove = iconButton("✕  Remove Selected", RED_ERR);
        JButton btnSearch = iconButton("⌕  Search", NAVY_MID);

        btnRemove.addActionListener(e -> removeSelected());
        btnSearch.addActionListener(e -> searchDialog());

        actions.add(btnRemove);
        actions.add(btnSearch);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    // Form Panel 
    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 10, 12, 20));

        JLabel title = sectionLabel("Add Student");
        panel.add(title, BorderLayout.NORTH);

        // Card
        JPanel card = roundedCard();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 12, 8, 12);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;

        JTextField nameField  = styledField("Enter full name…");
        JTextField scoreField = styledField("0 – 100");

        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        card.add(fieldLabel("Student Name"), gc);
        gc.gridy = 1;
        card.add(nameField, gc);

        gc.gridy = 2;
        card.add(fieldLabel("Score"), gc);
        gc.gridy = 3;
        card.add(scoreField, gc);

        // Grade preview
        JLabel gradePreview = new JLabel("—");
        gradePreview.setFont(new Font("Segoe UI", Font.BOLD, 36));
        gradePreview.setForeground(TEAL);
        gradePreview.setHorizontalAlignment(SwingConstants.CENTER);
        gradePreview.setBorder(new EmptyBorder(4, 0, 4, 0));

        JLabel gradeCaption = new JLabel("GRADE PREVIEW");
        gradeCaption.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        gradeCaption.setForeground(GREY_TXT);
        gradeCaption.setHorizontalAlignment(SwingConstants.CENTER);

        scoreField.addCaretListener(e -> {
            try {
                double sc = Double.parseDouble(scoreField.getText().trim());
                char g = gradeFromScore(sc);
                gradePreview.setText(String.valueOf(g));
                gradePreview.setForeground(gradeColor(g));
            } catch (NumberFormatException ex) {
                gradePreview.setText("—");
                gradePreview.setForeground(TEAL);
            }
        });

        gc.gridy = 4;
        card.add(gradeCaption, gc);
        gc.gridy = 5;
        card.add(gradePreview, gc);

        JButton btnAdd = new JButton("Add Student  ＋");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAdd.setBackground(TEAL);
        btnAdd.setForeground(WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setBorder(new EmptyBorder(12, 16, 12, 16));
        btnAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAdd.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnAdd.setBackground(TEAL.darker()); }
            @Override public void mouseExited(MouseEvent e)  { btnAdd.setBackground(TEAL); }
        });
        btnAdd.addActionListener(e -> {
            String name = nameField.getText().trim();
            String scoreText = scoreField.getText().trim();
            if (name.isEmpty() || name.equals("Enter full name…")) {
                shake(nameField); return;
            }
            double score;
            try { score = Double.parseDouble(scoreText); }
            catch (NumberFormatException ex) { shake(scoreField); return; }
            if (score < 0 || score > 100) { shake(scoreField); return; }

            students.add(new Student(name, score));
            refreshTable();
            updateStats();
            nameField.setText("");
            scoreField.setText("");
            gradePreview.setText("—");
            gradePreview.setForeground(TEAL);
            nameField.requestFocus();
        });

        gc.gridy = 6;
        gc.insets = new Insets(16, 12, 12, 12);
        card.add(btnAdd, gc);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    //  STATS FOOTER
    private JPanel buildStatsFooter() {
        JPanel bar = new JPanel(new GridLayout(1, 4, 1, 0)) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(0xE2, 0xEC, 0xF5));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        bar.setPreferredSize(new Dimension(0, 64));
        bar.setBorder(new MatteBorder(1, 0, 0, 0, new Color(0xD1, 0xDE, 0xEC)));

        lblCount   = statTile("Students",      "0",  NAVY);
        lblAvg     = statTile("Class Average", "—",  TEAL);
        lblHighest = statTile("Highest Score", "—",  GREEN);
        lblLowest  = statTile("Lowest Score",  "—",  ORANGE);

        bar.add(wrapStat("Students",      lblCount,   NAVY));
        bar.add(wrapStat("Class Average", lblAvg,     TEAL));
        bar.add(wrapStat("Highest Score", lblHighest, GREEN));
        bar.add(wrapStat("Lowest Score",  lblLowest,  ORANGE));

        return bar;
    }

    private JLabel statTile(String label, String val, Color col) {
        JLabel l = new JLabel(val);
        l.setFont(new Font("Segoe UI", Font.BOLD, 22));
        l.setForeground(col);
        return l;
    }

    private JPanel wrapStat(String caption, JLabel valueLabel, Color accent) {
        JPanel p = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(WHITE);
                g2.fillRect(4, 4, getWidth() - 8, getHeight() - 8);
                g2.setColor(accent);
                g2.fillRect(4, 4, 4, getHeight() - 8);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(4, 4, 4, 4));

        JPanel inner = new JPanel(new BorderLayout(0, 2));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(6, 14, 6, 10));

        JLabel cap = new JLabel(caption.toUpperCase());
        cap.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        cap.setForeground(GREY_TXT);

        inner.add(cap,        BorderLayout.NORTH);
        inner.add(valueLabel, BorderLayout.CENTER);

        p.add(inner);
        return p;
    }

    //  Logic helpers
    private void refreshTable() {
        tableModel.setRowCount(0);
        for (int i = 0; i < students.size(); i++) {
            Student s = students.get(i);
            char g = gradeFromScore(s.score);
            tableModel.addRow(new Object[]{
                i + 1,
                s.name,
                String.format("%.1f", s.score),
                String.valueOf(g),
                statusLabel(s.score)
            });
        }
        // Re-apply row renderer
        table.repaint();
    }

    private void updateStats() {
        if (students.isEmpty()) {
            lblCount.setText("0");
            lblAvg.setText("—"); lblHighest.setText("—"); lblLowest.setText("—");
            return;
        }
        double total = 0, hi = students.get(0).score, lo = students.get(0).score;
        String hiName = students.get(0).name, loName = students.get(0).name;
        for (Student s : students) {
            total += s.score;
            if (s.score > hi) { hi = s.score; hiName = s.name; }
            if (s.score < lo) { lo = s.score; loName = s.name; }
        }
        lblCount.setText(String.valueOf(students.size()));
        lblAvg.setText(String.format("%.1f", total / students.size()));
        lblHighest.setText(String.format("%.1f  (%s)", hi, truncate(hiName, 10)));
        lblLowest.setText(String.format("%.1f  (%s)", lo, truncate(loName, 10)));
    }

    private String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }

    private void removeSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student row first.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String name = students.get(row).name;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove \"" + name + "\" from the list?", "Confirm Remove",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            students.remove(row);
            refreshTable();
            updateStats();
        }
    }

    private void searchDialog() {
        String query = JOptionPane.showInputDialog(this, "Enter student name to search:", "Search", JOptionPane.QUESTION_MESSAGE);
        if (query == null || query.trim().isEmpty()) return;
        String q = query.trim().toLowerCase();
        StringBuilder result = new StringBuilder();
        for (Student s : students) {
            if (s.name.toLowerCase().contains(q)) {
                char g = gradeFromScore(s.score);
                result.append(String.format("%-20s  Score: %5.1f  Grade: %s%n", s.name, s.score, g));
            }
        }
        if (result.length() == 0) {
            JOptionPane.showMessageDialog(this, "No student found matching \"" + query + "\".",
                    "Not Found", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JTextArea ta = new JTextArea(result.toString());
            ta.setFont(new Font("Monospaced", Font.PLAIN, 13));
            ta.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Search Results", JOptionPane.PLAIN_MESSAGE);
        }
    }

    //  Styling helpers
    private void styleTable(JTable t) {
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setRowHeight(36);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(new Color(0xD6, 0xEE, 0xFB));
        t.setSelectionForeground(NAVY);
        t.setBackground(WHITE);

        // Header
        JTableHeader header = t.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(NAVY);
        header.setForeground(WHITE);
        header.setBorder(BorderFactory.createEmptyBorder());
        header.setPreferredSize(new Dimension(0, 40));

        // Column widths
        int[] widths = {36, 200, 70, 60, 120};
        for (int i = 0; i < widths.length; i++)
            t.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Custom cell renderer
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                setHorizontalAlignment(col == 0 ? CENTER : col >= 2 ? CENTER : LEFT);
                setBorder(new EmptyBorder(0, 10, 0, 10));

                if (!sel) {
                    setBackground(row % 2 == 0 ? WHITE : new Color(0xF8, 0xFB, 0xFF));
                }

                // Color Grade column
                if (col == 3 && val != null) {
                    char g = val.toString().charAt(0);
                    setForeground(gradeColor(g));
                    setFont(getFont().deriveFont(Font.BOLD, 14f));
                } else if (col == 4 && val != null) {
                    String v = val.toString();
                    if (v.startsWith("Pass"))      setForeground(GREEN);
                    else if (v.startsWith("Fair")) setForeground(ORANGE);
                    else                           setForeground(RED_ERR);
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    setForeground(sel ? NAVY : Color.DARK_GRAY);
                    setFont(new Font("Segoe UI", Font.PLAIN, 13));
                }
                return c;
            }
        };
        for (int i = 0; i < t.getColumnCount(); i++)
            t.getColumnModel().getColumn(i).setCellRenderer(renderer);
    }

    private JPanel roundedCard() {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(0xD1, 0xDE, 0xEC));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
            @Override public boolean isOpaque() { return false; }
        };
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 15));
        l.setForeground(NAVY);
        l.setBorder(new EmptyBorder(0, 0, 8, 0));
        return l;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(GREY_TXT);
        return l;
    }

    private JTextField styledField(String placeholder) {
        JTextField f = new JTextField(placeholder) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() || getText().equals(placeholder)) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(new Color(0xB0, 0xBE, 0xCC));
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    Insets ins = getInsets();
                    FontMetrics fm = g2.getFontMetrics();
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString(placeholder, ins.left + 4, y);
                }
            }
        };
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setPreferredSize(new Dimension(0, 40));
        f.setBackground(new Color(0xF4, 0xF7, 0xFA));
        f.setForeground(NAVY);
        f.setCaretColor(TEAL);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD1, 0xDE, 0xEC), 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        // Clear placeholder on focus
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (f.getText().equals(placeholder)) f.setText("");
            }
            @Override public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) f.setText(placeholder);
            }
        });
        return f;
    }

    private JButton iconButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg);
        b.setForeground(WHITE);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 14, 8, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(bg.darker()); }
            @Override public void mouseExited(MouseEvent e)  { b.setBackground(bg); }
        });
        return b;
    }

    private void shake(JComponent c) {
        final int[] offsets = {-8, 8, -6, 6, -4, 4, 0};
        Timer t = new Timer(40, null);
        final int[] idx = {0};
        Point origin = c.getLocation();
        t.addActionListener(e -> {
            if (idx[0] >= offsets.length) { c.setLocation(origin); t.stop(); return; }
            c.setLocation(origin.x + offsets[idx[0]++], origin.y);
        });
        t.start();
    }

    //  Grade helpers 
    static char gradeFromScore(double s) {
        if (s >= 90) return 'A';
        if (s >= 80) return 'B';
        if (s >= 70) return 'C';
        if (s >= 60) return 'D';
        return 'F';
    }

    static Color gradeColor(char g) {
        switch (g) {
            case 'A': return GREEN;
            case 'B': return TEAL;
            case 'C': return GOLD_DRK;
            case 'D': return ORANGE;
            default:  return RED_ERR;
        }
    }

    static String statusLabel(double s) {
        if (s >= 70) return "Pass";
        if (s >= 60) return "Fair";
        return "Fail";
    }

    //  LOGO PANEL  — redraws the SVG logo in pure Java2D
    static class LogoPanel extends JPanel {
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,        RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,   RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,           RenderingHints.VALUE_RENDER_QUALITY);

            // Scale to fit panel height (badge is 120×120 in SVG, offset 40,50)
            float scale = (getHeight() - 12f) / 120f;
            g2.translate(8, 6);
            g2.scale(scale, scale);

            // Badge (rounded rect) 
            g2.setColor(NAVY);
            g2.fillRoundRect(0, 0, 120, 120, 24, 24);

            // Ellipse shadow
            g2.setColor(NAVY_MID);
            g2.fillOval(40, 54, 40, 16);

            // Graduation cap (polygon) 
            int[] capX = {60, 98, 60, 22};
            int[] capY = {26, 44, 62, 44};
            g2.setColor(GOLD);
            g2.fillPolygon(capX, capY, 4);

            // Tassel circle at top-center
            g2.setColor(GOLD_DRK);
            g2.fillOval(55, 39, 10, 10);

            // Tassel string
            g2.setColor(GOLD);
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(60, 44, 84, 66);
            g2.setColor(GOLD);
            g2.fillOval(79, 69, 10, 10);

            // Bar chart (3 ascending bars) 
            g2.setColor(TEAL);
            g2.setStroke(new BasicStroke(1f));
            g2.fillRoundRect(18, 98, 14, 14, 4, 4);
            g2.fillRoundRect(40, 90, 14, 22, 4, 4);
            g2.fillRoundRect(62, 82, 14, 30, 4, 4);

            g2.dispose();

            //  Wordmark text (drawn in screen coords, after scale reset)
            Graphics2D g3 = (Graphics2D) g.create();
            g3.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int textX = (int) (120 * scale + 20);
            int baseY = getHeight() / 2 + 4;

            // "Grade" in white, "Tracker" in teal
            Font wordmark = new Font("Segoe UI", Font.BOLD, 26);
            g3.setFont(wordmark);

            FontMetrics fm = g3.getFontMetrics();
            int gradeW = fm.stringWidth("Grade");

            g3.setColor(WHITE);
            g3.drawString("Grade", textX, baseY);

            g3.setColor(TEAL);
            g3.drawString("Tracker", textX + gradeW, baseY);

            // Tagline
            Font tagFont = new Font("Segoe UI", Font.PLAIN, 11);
            g3.setFont(tagFont);
            g3.setColor(new Color(0xA0, 0xB8, 0xD0));
            g3.drawString("Student grade management system", textX, baseY + 18);

            g3.dispose();
        }
    }

    //  Student model
    static class Student {
        String name;
        double score;
        Student(String n, double s) { name = n; score = s; }
    }

    //  Entry point
    public static void main(String[] args) {
        // Use system look-and-feel as base, then override with custom styling
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}

        UIManager.put("ScrollBar.thumb",         new Color(0xC0, 0xD4, 0xE8));
        UIManager.put("ScrollBar.track",         BG);
        UIManager.put("SplitPane.background",    BG);
        UIManager.put("SplitPaneDivider.border", null);

        SwingUtilities.invokeLater(GradeTrackerGUI::new);
    }
}
