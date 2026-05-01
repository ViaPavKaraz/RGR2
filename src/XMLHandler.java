import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class XMLHandler {

    public static List<DataPoint> readWithSAX(File file, String nameToFilter) throws Exception {
        List<DataPoint> points = new ArrayList<>();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();

        DefaultHandler handler = new DefaultHandler() {
            private String currentElement = "";
            private double year, count;
            private String currentName = "";
            private boolean isTargetName = false;

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                currentElement = qName;
            }

            @Override
            public void characters(char[] ch, int start, int length) {
                String value = new String(ch, start, length).trim();
                if (value.isEmpty()) return;

                if (currentElement.equalsIgnoreCase("nm")) {
                    if (value.equalsIgnoreCase(nameToFilter)) {
                        isTargetName = true;
                    }
                } else if (currentElement.equalsIgnoreCase("brth_yr")) {
                    year = Double.parseDouble(value);
                } else if (currentElement.equalsIgnoreCase("cnt")) {
                    count = Double.parseDouble(value);
                }
            }

            @Override
            public void endElement(String uri, String localName, String qName) {
                if (qName.equalsIgnoreCase("row")) {
                    if (isTargetName) {
                        points.add(new DataPoint(year, count));
                    }
                    isTargetName = false;
                }
                currentElement = "";
            }
        };

        saxParser.parse(file, handler);

        points.sort((p1, p2) -> Double.compare(p1.getX(), p2.getX()));
        return points;
    }

    public static void writeWithDOM(File file, List<DataPoint> points) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();

        Element rootElement = doc.createElement("dataset");
        doc.appendChild(rootElement);

        for (DataPoint p : points) {
            Element point = doc.createElement("point");

            Element x = doc.createElement("x");
            x.appendChild(doc.createTextNode(String.valueOf(p.getX())));
            point.appendChild(x);

            Element y = doc.createElement("y");
            y.appendChild(doc.createTextNode(String.valueOf(p.getY())));
            point.appendChild(y);

            rootElement.appendChild(point);
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }
}