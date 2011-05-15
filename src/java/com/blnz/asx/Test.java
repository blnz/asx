package com.blnz.asx;

import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;

class Test
{

    public static void main(String[] args)
    {
        System.out.println("hello world");
        Test test = new Test();
        test.test1();
        System.out.println("bye world");
    }

    private void test1()
    {

        XMLReader parser = new JSONReader();

        String json = 
            " { \n" +
            "       'name': 'John Smith' , \n" +
            "       'address': { \n" +
            "           'streetAddress': '21 2nd Street', \n" +
            "           'city': 'New York', \n" +
            "           'state': 'NY', \n" +
            "           'postalCode': 10021, \n" +
            "       }, \n" +
            "       'phoneNumbers': [ \n" +
            "           '212 555-1111', \n" +
            "           '212 555-2222' \n" +
            "       ], \n" +
            "       'additionalInfo': null, \n" +
            "       'remote': false, \n" +
            "       'height': 62.4, \n" +
            "       'ficoScore': '> 640' \n" +
            "   } \n" ;
        

        StringReader sr = new StringReader(json);
        InputSource is = new InputSource(sr);
        is.setSystemId("dummy");

        StringWriter sw = new StringWriter();
        ContentHandler dest = new XMLSerializer(sw);

        try {
            parser.setContentHandler(dest);
            parser.parse(is);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println(sw.toString());
        System.out.println("-------");

        sr = new StringReader(sw.toString());
        is = new InputSource(sr);
        is.setSystemId("dummy");

        sw = new StringWriter();
        dest = new JSONSerializer(sw);
        
        try {
            parser = XMLReaderFactory.createXMLReader();
            parser.setContentHandler(dest);
            parser.parse(is);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println(sw.toString());
        System.out.println("-------");


    }
}