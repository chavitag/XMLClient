
import importclientecsvtoxml.Client;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author ASIR\xavi
 */
public class ImportClienteCSVToXML {

    /**
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
                cl.toXMLDocument();
            } catch (Exception ex) {
                System.out.println("ERROR: "+ex.getMessage());
            }
        }
    }
    
}
