import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TableDataBean extends JPanel implements Serializable {
    private JTable table;
    private DefaultTableModel tableModel;
    private List<DataPoint> dataPoints;
    private PropertyChangeSupport support;

    public TableDataBean() {
        support = new PropertyChangeSupport(this);
        dataPoints = new ArrayList<>();

        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new Object[]{"X", "Y"}, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return Double.class;
            }
        };

        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        tableModel.addTableModelListener(e -> {
            updateDataPointsFromTable();
        });
    }

    public void setData(List<DataPoint> newData) {
        List<DataPoint> oldData = this.dataPoints;
        this.dataPoints = newData;

        tableModel.setRowCount(0);
        for (DataPoint dp : dataPoints) {
            tableModel.addRow(new Object[]{dp.getX(), dp.getY()});
        }

        support.firePropertyChange("data", oldData, this.dataPoints);
    }

    public List<DataPoint> getData() {
        return dataPoints;
    }

    private void updateDataPointsFromTable() {
        List<DataPoint> updatedList = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            try {
                double x = Double.parseDouble(tableModel.getValueAt(i, 0).toString());
                double y = Double.parseDouble(tableModel.getValueAt(i, 1).toString());
                updatedList.add(new DataPoint(x, y));
            } catch (Exception ex) {

            }
        }
        List<DataPoint> oldData = this.dataPoints;
        this.dataPoints = updatedList;
        support.firePropertyChange("data", oldData, this.dataPoints);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}