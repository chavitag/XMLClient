
import importclientecsvtoxml.Client;
import java.io.StringWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

/**
 *
 * @author ASIR\xavi
 */
public class ImportClienteCSVToXML {

    /** TEST
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String[] clients= {
            "X12345678F,\"nombreA\",\"apellidosA \",(91)23456789 ,+(82)12345678, 612345678,test@TEST.com,prueba@prueba.com",
            "12345678Z,\"nombreB \",\"apellidosB \", prueba@prueba.com,(952)333333,test@test.com ,952333333,test@TEST.com",
        };
        for(String str: clients) {
            Client cl;
            try {
                cl = Client.scanCSVLine(str);
                System.out.println(cl.toXMLString());
                writeXmlDocumentToConsole(cl.toXMLDocument());
                writeXmlDocumentToConsole(cl.buildXMLDocument());

            } catch (Exception ex) {
                System.out.println("ERROR: "+ex.getMessage());
            }
        }
    }
    
    public static void writeXmlDocumentToConsole(Document xmlDocument) {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
            StringWriter writer = new StringWriter();
 
            //transform document to string 
            transformer.transform(new DOMSource(xmlDocument), new StreamResult(writer));
 
            String xmlString = writer.getBuffer().toString();   
            System.out.println(xmlString);                      //Print to console or logs
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
