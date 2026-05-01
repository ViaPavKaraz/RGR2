import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChartDataBean extends JPanel implements Serializable {
    private List<DataPoint> dataPoints;

    public ChartDataBean() {
        this.dataPoints = new ArrayList<>();
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(400, 300));
    }


    public void setData(List<DataPoint> dataPoints) {
        this.dataPoints = dataPoints;
        repaint();
    }

    public List<DataPoint> getData() {
        return dataPoints;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (dataPoints == null || dataPoints.isEmpty()) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int padding = 40;


        double minX = dataPoints.stream().mapToDouble(DataPoint::getX).min().orElse(0);
        double maxX = dataPoints.stream().mapToDouble(DataPoint::getX).max().orElse(0);
        double minY = dataPoints.stream().mapToDouble(DataPoint::getY).min().orElse(0);
        double maxY = dataPoints.stream().mapToDouble(DataPoint::getY).max().orElse(0);


        g2d.setColor(Color.BLACK);
        g2d.drawLine(padding, height - padding, width - padding, height - padding); // Вісь X
        g2d.drawLine(padding, height - padding, padding, padding); // Вісь Y


        g2d.setColor(Color.BLUE);
        Point prevPoint = null;

        for (DataPoint dp : dataPoints) {
            int x = (int) (padding + ((dp.getX() - minX) / (maxX - minX == 0 ? 1 : maxX - minX)) * (width - 2 * padding));
            int y = (int) ((height - padding) - ((dp.getY() - minY) / (maxY - minY == 0 ? 1 : maxY - minY)) * (height - 2 * padding));

            if (prevPoint != null) {
                g2d.draw(new Line2D.Double(prevPoint.x, prevPoint.y, x, y));
            }
            g2d.fill(new Ellipse2D.Double(x - 3, y - 3, 6, 6));
            prevPoint = new Point(x, y);
        }
    }
}