import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;


public class MainApp extends JFrame {
    private TableDataBean tableBean;
    private ChartDataBean chartBean;
    private JTextField nameFilterField;
    private JFileChooser fileChooser;

    public MainApp() {

        setTitle("Аналіз популярності імен (XML SAX Parser)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);


        tableBean = new TableDataBean();
        chartBean = new ChartDataBean();
        fileChooser = new JFileChooser();


        fileChooser.setCurrentDirectory(new File("."));


        tableBean.addPropertyChangeListener(evt -> {
            if ("data".equals(evt.getPropertyName())) {
                List<DataPoint> newData = (List<DataPoint>) evt.getNewValue();
                chartBean.setData(newData);
            }
        });


        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        toolBar.setBorder(BorderFactory.createEtchedBorder());

        nameFilterField = new JTextField("Chloe", 10);
        JButton btnOpen = new JButton("Завантажити та фільтрувати");
        JButton btnSave = new JButton("Зберегти результат");
        JButton btnClear = new JButton("Очистити");

        toolBar.add(new JLabel("Ім'я для аналізу:"));
        toolBar.add(nameFilterField);
        toolBar.add(btnOpen);
        toolBar.add(btnSave);
        toolBar.add(new JSeparator(SwingConstants.VERTICAL));
        toolBar.add(btnClear);

        btnOpen.addActionListener(e -> openAndParseFile());
        btnSave.addActionListener(e -> saveEditedFile());
        btnClear.addActionListener(e -> {
            tableBean.setData(java.util.Collections.emptyList());
            nameFilterField.setText("");
        });


        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableBean, chartBean);
        splitPane.setDividerLocation(450);

        add(toolBar, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);


        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.add(new JLabel("Готово. Використовуйте SAX для великих файлів."));
        add(statusBar, BorderLayout.SOUTH);
    }


    private void openAndParseFile() {
        String targetName = nameFilterField.getText().trim();
        if (targetName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Будь ласка, введіть ім'я для пошуку (наприклад, Chloe).");
            return;
        }

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            new SwingWorker<List<DataPoint>, Void>() {
                @Override
                protected List<DataPoint> doInBackground() throws Exception {
                    return XMLHandler.readWithSAX(selectedFile, targetName);
                }

                @Override
                protected void done() {
                    try {
                        List<DataPoint> data = get();
                        if (data.isEmpty()) {
                            JOptionPane.showMessageDialog(MainApp.this,
                                    "Нічого не знайдено для імені: " + targetName);
                        } else {
                            tableBean.setData(data); // Це автоматично оновить і графік
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MainApp.this,
                                "Помилка парсингу: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }.execute();
        }
    }


    private void saveEditedFile() {
        List<DataPoint> currentData = tableBean.getData();
        if (currentData.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Немає даних для збереження.");
            return;
        }

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File fileToSave = fileChooser.getSelectedFile();
                if (!fileToSave.getName().toLowerCase().endsWith(".xml")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".xml");
                }

                XMLHandler.writeWithDOM(fileToSave, currentData);
                JOptionPane.showMessageDialog(this, "Дані збережено успішно!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Помилка при збереженні: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new MainApp().setVisible(true);
        });
    }
}