package star;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ActorParser extends DefaultHandler {
    Set<Star> stars;

    private String tempVal;
    private Star tempStar;
    private Integer starId;

    public ActorParser() {
        stars = new HashSet<>();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql:///moviedb?autoReconnect=true&allowPublicKeyRetrieval=true&useSSL=false", "mytestuser", "My6$Password");
            if (conn != null) {
                PreparedStatement preparedStatement = conn.prepareStatement("select max(substring(id, 3)) as id from stars");
                ResultSet rs = preparedStatement.executeQuery();
                rs.next();
                starId = Integer.parseInt(rs.getString("id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        parseDocument();
        writeToActorFile();


        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql:///moviedb?autoReconnect=true&allowPublicKeyRetrieval=true&allowLoadLocalInfile=true&useSSL=false", "mytestuser", "My6$Password");
            if (conn != null) {
                Statement statement = conn.createStatement();

                String actorFilePath = "src/actors.txt";
                String loadActorsStatement = "LOAD DATA LOCAL INFILE '" + actorFilePath + "' INTO TABLE stars fields terminated by '|' lines terminated by '\\n'";
                statement.execute(loadActorsStatement);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseDocument() {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();
            sp.parse("../actors63.xml", this);
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    private void printData() {
        System.out.println("Number of Actors '" + stars.size() + "'.");
    }

    private void writeToActorFile() {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter("src/actors.txt", false));

            Iterator<Star> it = stars.iterator();
            while (it.hasNext()) {
                Star s = it.next();
                ++starId;
                s.setId(String.format("nm%07d", starId));
                writer.printf("%s|%s%s\n", s.getId(), s.getName(), (s.getBirthYear() == 0 ? "|" : "|" + s.getBirthYear()));
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = "";
        if (qName.equalsIgnoreCase("actor")) {
            tempStar = new Star();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("actor")) {
            stars.add(tempStar);
        } else if (qName.equalsIgnoreCase("stagename")) {
            tempStar.setName(tempVal);
        } else if (qName.equalsIgnoreCase("dob")) {
            try {
                tempStar.setYear(Integer.parseInt(tempVal));
            } catch (Exception e) {
                tempStar.setYear(0);
            }
        }
    }
}
