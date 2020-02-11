/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package importclientecsvtoxml;

import java.io.IOException;
import java.io.StringReader;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Client  {
    private final String dni;
    private final String firstname;
    private final String lastname;
    TreeMap <Long,String> localPhones;  // O TreeMap permanece ordeado
    TreeMap <Long,String> internationalPhones;
    HashMap <String,String> emails; // Os emails non necesitan estar ordeados, pero deben ser únicos
    
    public Client(String dni,String firstname,String lastname) throws Exception {
        //if (!verificaDNI(dni)) throw new Exception("DNI number not valid");
        Comparator comparePhones=new PhoneComparator();
        this.dni=dni;
        this.firstname=firstname;
        this.lastname=lastname;
        localPhones=new TreeMap <>(comparePhones);  // comparePhones ordea de maior a menor.
        internationalPhones=new TreeMap <>(comparePhones);
        emails=new HashMap <> ();
    }
    
    public void addLocalPhone(String phone) throws Exception {
        try { 
            localPhones.put(Long.parseLong(phone),phone);
        } catch(NumberFormatException e) {
            throw new Exception("The Phone number "+phone+" is not valid");
        }
    }
    
    public void addInternationalPhone(String phone) throws Exception {
        String number=phone.substring(1);
        if (phone.charAt(0)!='+') 
                throw new Exception("The Phone number "+phone+" is not valid");
        try {
            internationalPhones.put(Long.parseLong(number),phone);
        } catch(NumberFormatException e) {
            throw new Exception("The Phone number "+phone+" is not valid");
        }
    }
    
    public void addEmail(String email) {
        emails.put(email.toLowerCase(),email);
    }
    
    public static boolean verificaDNI(String dni) {
        char[] l={'T','R','W','A','G','M','Y','F','P','D','X','B','N','J','Z','S','Q','V','H','L','C','K','E'};
        char firstchar,lastchar;
        String number;
        int length=dni.length();
        int val;
        
        if (length!=9) return false;
        firstchar=dni.charAt(0);
        lastchar=dni.charAt(length-1);
        // NIE
        if (!Character.isDigit(firstchar)) {
            val=0;
            switch(Character.toUpperCase(firstchar)) {
                case 'Z': val++; 
                case 'Y': val++;
                case 'X': break;
                default: return false;
            }
            dni=val+dni.substring(1);
        } 
        number=dni.substring(0,length-1);
        try {
            val=Integer.parseInt(number);
            if (l[val%23]!=lastchar) return false;
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
    
    public static Client scanCSVLine(String line) throws Exception, ScanException {
        Client cl;
        String[] fields=line.split(",");
        String f;
        
        if (fields.length < 3) throw new ScanException("Faltan datos");
        cl=new Client(fields[0].trim(),cleanQuotes(fields[1]),cleanQuotes(fields[2]));
        for(int idx=3;idx<fields.length;idx++) {
            f=fields[idx].trim();
            if (isEmail(f)) {
                cl.addEmail(f);
            } else {
                if (isPhoneNumber(f)) {
                    f=f.replace("(","").replace(")","");
                    if (isLocalPhoneNumber(f)) {
                        cl.addLocalPhone(f);
                    } else 
                        cl.addInternationalPhone(f);
                }
                else throw new ScanException("Unknown field "+f+" scanning "+line);
            }
        }
        return cl;
    }
    
    public String toString() {
        return dni+": "+firstname+" "+lastname;
    }
    
    /**
     * Crea un texto XML --- Non un DOCUMENTO ---
     * @return 
     */
    public String toXMLString() {
        StringBuilder xml=new StringBuilder("<datos_cliente>");
        xml.append("<id>").append(dni).append("</id>");
        xml.append("<nombre>").append(firstname).append("</nombre>");
        xml.append("<apellidos>").append(lastname).append("</apellidos>");
        xml.append("<telefonos>");
        for(String tel:localPhones.values()) {
            xml.append("<telefono>").append(tel).append("</telefono>");
        }
        for(String tel:internationalPhones.values()) {
            xml.append("<telefono>").append(tel).append("</telefono>");
        }
        xml.append("</telefonos>");
        xml.append("<mails>");
        for(String mail:emails.values()) {
            xml.append("<mail>").append(mail).append("</mail>");
        }
        xml.append("</mails>");
        xml.append("</datos_cliente>");
        return xml.toString();
    }
    
    /**
     * Crea o documento XML a partir do texto XML
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException 
     */
    public Document toXMLDocument() throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder doc=DocumentBuilderFactory.newInstance().newDocumentBuilder();
        StringReader sr=new StringReader(toXMLString());
        return doc.parse(new InputSource(sr));
    }
    
    
    /**
     * Crea o documento XML nodo por nodo
     * @return
     * @throws ParserConfigurationException 
     */
    public Document buildXMLDocument() throws ParserConfigurationException {
        DocumentBuilder db=DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc=db.newDocument();
        Element raiz;
        Element node;
        
        doc.setXmlVersion("1.0");
        raiz=doc.createElement("datos_cliente");
        doc.appendChild(raiz);
        
        raiz.appendChild(createXMLNode(doc,"id",dni));
        raiz.appendChild(createXMLNode(doc,"nombre",firstname));
        raiz.appendChild(createXMLNode(doc,"apellidos",lastname));
        
        node=doc.createElement("telefonos");
        raiz.appendChild(node);
        
        for(String tel:localPhones.values()) {
            node.appendChild(createXMLNode(doc,"telefono",tel));
        }
      
        for(String tel:internationalPhones.values()) {
            node.appendChild(createXMLNode(doc,"telefono",tel));
        }
        
        node=doc.createElement("mails");
        raiz.appendChild(node);
        
        for(String mail:emails.values()) {
            node.appendChild(createXMLNode(doc,"mail",mail));
        }
        
        return doc;
    }
    
    // Métodos Auxiliares
        
    private static String cleanQuotes(String str) throws ScanException {
        // O patrón captura todo o que esté entre comiñas, e devolve a primeira captura.
        // --> captura todo o que esté despois de abrir comiñas, que non sexa unha comiña e teña detrás outras comiñas
        Pattern pattern = Pattern.compile("\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(str);
        if (!matcher.find()) throw new ScanException("Error in quoted String "+str);
        return matcher.group(1).trim();
    }
    
    private static boolean isEmail(String field) {
        // Analiza que no comenzo teña unha ou varias letras A-Z ou números 0-9 ou punto, seguido dunha @, letras
        // A-Z ou números 0-9 ou puntos, seguido dun punto e dunha extensión alfabética A-Z de 2 a 6 letras.
        // NON diferencia entre minúsculas e maiúsculas
        Pattern pattern=Pattern.compile("^[A-Z0-9.]+@[A-Z0-9.]+\\.[A-Z]{2,6}$", 
                                         Pattern.CASE_INSENSITIVE);
        Matcher matcher=pattern.matcher(field);
        return matcher.find();
    }
        
    private static boolean isPhoneNumber(String field) {
        // Comenzo de liña pode levar ou non un +.  (?: indica un grupo, pero non o "captura", como
        // no caso de coller o que está entre as comiñas en cleanQuotes. polo tanto indico que pode existir
        // ou non un conxunto de números entre paréntese. O seguinte grupo, que tampouco captura, sería un número 
        // entre 6 e 14 díxitos, e logo un díxito mais antes de rematar a cadea.
        // 
        Pattern pattern=Pattern.compile("^\\+?(?:\\([0-9]+\\))?(?:[0-9]?){6,14}[0-9]$");
        Matcher matcher=pattern.matcher(field);
        return matcher.find();
    }

    private static boolean isLocalPhoneNumber(String field) {
        // Si comenza por +, non é un número local...
        if (field.charAt(0)=='+') return false;
        return true;
    }

    private static Element createXMLNode(Document doc, String name,String content) {
        Element n=doc.createElement(name);
        n.setTextContent(content);
        return n;
    }
}
