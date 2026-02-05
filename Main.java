import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.util.ArrayList;
import java.util.List;
import java.net.*;
import java.io.*;


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
    
    public static List<String> getPMIDsFromPubMed(String title) throws Exception {

        List<String> pmids = new ArrayList<>();

        // 1. Encode title for URL- so that the spaces doesn't throw it off
        String encodedTitle = URLEncoder.encode(title, "UTF-8");

        // 2. Build PubMed ESearch URL- API usage
        String urlString =
            "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi"
            + "?db=pubmed"
            + "&term=" + encodedTitle
            + "&retmode=xml"
            + "&retmax=1"
            + "&api_key=2c8cc4bf9d613680e142a2a2870562f24308";

        URL url = new URL(urlString);

        // 3. Open connection: using YorkU email address, to verify that we are not spamming PubMed for the requests we make. Also, added a 10 second time out to let the program pass when stuck.
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "ITEC4020-Assignment/1.0 (brian13@my.yorku.ca)");
        conn.setConnectTimeout(10000); // 10 seconds
        conn.setReadTimeout(10000);

        // 4. Read XML response
        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
        	throw new IOException ("Bad HTTP Response: "+conn.getResponseCode());
        }
        InputStream inputStream = conn.getInputStream();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inputStream);

        // 5. Extract PMID(s)
        NodeList idList = doc.getElementsByTagName("Id");
        for (int i = 0; i < idList.getLength(); i++) {
            pmids.add(idList.item(i).getTextContent());
        }

        return pmids;
    }
    public static void writeResultsToXML(List<PubMedData> results, String fileName) throws Exception{
    	PrintWriter writer= new PrintWriter(new FileWriter(fileName));
        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
    	writer.println("<PubmedArticleSet>");
    	for(PubMedData entry:results) {
    		writer.println("<PubmedArticle>");
    		writer.println("     <PMID>"+entry.pmid + "</PMID>");
    		writer.println("     <ArticleTitle>"+entry.title +"</ArticleTitle>");
    		writer.println("</PubmedArticle>");
    	}
    	writer.println("</PubmedArticleSet>");
    	writer.close();
    }
    public static void main(String[] args) throws Exception {
        List<String> titles = readTitles("4020a1-datasets.xml");
        List<PubMedData> results = new ArrayList<>();
        int counter = 0;
        // creating a counter to count through the list, and a try-catch to report any item that may crash the program.
        for(String t : titles) {
        	counter ++;
        	System.out.println(
                    "[" + counter + "/" + titles.size() + "] Querying: " + t
                );
        	try {
        		List<String> pmids = getPMIDsFromPubMed(t);
        		for (String id : pmids) {
                    results.add(new PubMedData(id, t));
                }
        		Thread.sleep(100);
        	} catch (Exception e){
        		System.out.println("SKIPPED ITEM!"+counter+"due to error: "+e.getClass().getSimpleName());
        		Thread.sleep(500);
        	}
            System.out.println(t);
        }
        
        
        writeResultsToXML(results,"group3_result.xml");
    }
}
