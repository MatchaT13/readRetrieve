import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static List<String> readTitles(String filePath) throws Exception {
        List<String> titles = new ArrayList<>();
        File xmlFile = new File(filePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("ArticleTitle");
        for (int i = 0; i < nList.getLength(); i++) {
            titles.add(nList.item(i).getTextContent());
        }
        return titles;
    }

    public static void main(String[] args) throws Exception {
        List<String> titles = readTitles("4020a1-datasets");
        for(String t : titles) {
            System.out.println(t);
        }
    }
}
