package com.blnz.asx;

import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import org.xml.sax.ContentHandler;

import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;


/**
 *
 */
class JSONReader extends XMLFilterImpl 
{

    private int  character;
    private boolean eof;
    private int index;
    private int line;
    private char previous;
    private Reader reader;
    private boolean usePrevious;

    private ContentHandler downstream = null;
    private InputSource source = null;
    private AttributesImpl atts =  new AttributesImpl();

    private String JSONNamespace = "http://www.ibm.com/xmlns/prod/2009/jsonx";
    private String JSONPrefix = "json:";


    JSONReader()
    {
        this.eof = false;
        this.usePrevious = false;
        this.previous = 0;
        this.index = 0;
        this.character = 1;
        this.line = 1;
    }


    public void setContentHandler(ContentHandler handler)
    {
        this.downstream = handler;
    }


    public ContentHandler getContentHandler()
    {
        return downstream;
    }


    public void parse(InputSource src) throws SAXException
    {
        this.source = src;
        reader = src.getCharacterStream();
        if (reader == null) {
            InputStream stream = src.getByteStream();
            if (stream != null) {
                reader = new InputStreamReader(stream);
            }
        }
        if ( ! reader.markSupported() ) { 
            reader = new BufferedReader(reader);
        }
        downstream.startDocument();
        nextValue(null);
        downstream.endDocument();
    }

    /**
     * Back up one character. This provides a sort of lookahead capability,
     * so that you can test for a digit or letter before attempting to parse
     * the next number or identifier.
     */
    public void back() throws SAXException 
    {
        if (usePrevious || index <= 0) {
            throw new SAXException("Stepping back two steps is not supported");
        }
        this.index -= 1;
        this.character -= 1;
        this.usePrevious = true;
        this.eof = false;
    }

    public boolean end() {
    	return eof && !usePrevious;    	
    }


    /**
     * Determine if the source string still contains characters that next()
     * can consume.
     * @return true if not yet at the end of the source.
     */
    public boolean more() throws SAXException {
        next();
        if (end()) {
            return false;
        } 
        back();
        return true;
    }


    /**
     * Get the next character in the source string.
     *
     * @return The next character, or 0 if past the end of the source string.
     */
    public char next() throws SAXException {
        int c;
        if (this.usePrevious) {
            this.usePrevious = false;
            c = this.previous;
        } else {
            try {
                c = this.reader.read();
            } catch (IOException exception) {
                throw new SAXException(exception);
            }
	
            if (c <= 0) { // End of stream
                this.eof = true;
                c = 0;
            } 
        }
    	this.index += 1;
    	if (this.previous == '\r') {
            this.line += 1;
            this.character = c == '\n' ? 0 : 1;
    	} else if (c == '\n') {
            this.line += 1;
            this.character = 0;
    	} else {
            this.character += 1;
    	}
    	this.previous = (char) c;
        return this.previous;
    }


    /**
     * Consume the next character, and check that it matches a specified
     * character.
     * @param c The character to match.
     * @return The character.
     * @throws SAXException if the character does not match.
     */
    public char next(char c) throws SAXException {
        char n = next();
        if (n != c) {
            throw syntaxError("Expected '" + c + "' and instead saw '" +
                              n + "'");
        }
        return n;
    }


    /**
     * Get the next n characters.
     *
     * @param n     The number of characters to take.
     * @return      A string of n characters.
     * @throws SAXException
     *   Substring bounds error if there are not
     *   n characters remaining in the source string.
     */
    public String next(int n) throws SAXException {
        if (n == 0) {
            return "";
        }

        char[] chars = new char[n];
        int pos = 0;

        while (pos < n) {
            chars[pos] = next();
            if (end()) {
                throw syntaxError("Substring bounds error");                 
            }
            pos += 1;
        }
        return new String(chars);
    }


    /**
     * Get the next char in the string, skipping whitespace.
     * @throws SAXException
     * @return  A character, or 0 if there are no more characters.
     */
    private char nextClean() throws SAXException {
        for (;;) {
            char c = next();
            if (c == 0 || c > ' ') {
                return c;
            }
        }
    }


    /**
     * Return the characters up to the next close quote character.
     * Backslash processing is done. The formal JSON format does not
     * allow strings in single quotes, but an implementation is allowed to
     * accept them.
     * @param quote The quoting character, either
     *      <code>"</code>&nbsp;<small>(double quote)</small> or
     *      <code>'</code>&nbsp;<small>(single quote)</small>.
     * @return      A String.
     * @throws SAXException Unterminated string.
     */
    private String nextString(char quote) throws SAXException {
        char c;
        StringBuffer sb = new StringBuffer();
        for (;;) {
            c = next();
            switch (c) {
            case 0:
            case '\n':
            case '\r':
                throw syntaxError("Unterminated string");
            case '\\':
                c = next();
                switch (c) {
                case 'b':
                    sb.append('\b');
                    break;
                case 't':
                    sb.append('\t');
                    break;
                case 'n':
                    sb.append('\n');
                    break;
                case 'f':
                    sb.append('\f');
                    break;
                case 'r':
                    sb.append('\r');
                    break;
                case 'u':
                    sb.append((char)Integer.parseInt(next(4), 16));
                    break;
                case '"':
                case '\'':
                case '\\':
                case '/':
                    sb.append(c);
                break;
                default:
                    throw syntaxError("Illegal escape.");
                }
                break;
            default:
                if (c == quote) {
                    return sb.toString();
                }
                sb.append(c);
            }
        }
    }


    /**
     * Get the text up but not including the specified character or the
     * end of line, whichever comes first.
     * @param  delimiter A delimiter character.
     * @return   A string.
     */
    public String nextTo(char delimiter) throws SAXException {
        StringBuffer sb = new StringBuffer();
        for (;;) {
            char c = next();
            if (c == delimiter || c == 0 || c == '\n' || c == '\r') {
                if (c != 0) {
                    back();
                }
                return sb.toString().trim();
            }
            sb.append(c);
        }
    }


    /**
     * Get the text up but not including one of the specified delimiter
     * characters or the end of line, whichever comes first.
     * @param delimiters A set of delimiter characters.
     * @return A string, trimmed.
     */
    public String nextTo(String delimiters) throws SAXException {
        char c;
        StringBuffer sb = new StringBuffer();
        for (;;) {
            c = next();
            if (delimiters.indexOf(c) >= 0 || c == 0 ||
                c == '\n' || c == '\r') {
                if (c != 0) {
                    back();
                }
                return sb.toString().trim();
            }
            sb.append(c);
        }
    }

    
    
    /**
     * Get the next value. The value can be a Boolean, Double, Integer,
     * JSONArray, JSONObject, Long, or String, or the JSONObject.NULL object.
     * Sends the appropriate SAX events to the downstream ContentHandler.
     * @throws SAXException If syntax error.
     *
     * @return An object.
     */
    private String  nextValue(String name) throws SAXException 
    {

        if (name != null) {
            //            System.err.println("nextValue to bind to " + name);
        }

        char c = nextClean();
        String string;
        
        switch (c) {
        case '"':
        case '\'':
            return parseNextString(c, name);
        
        case '{':
            back();
            return parseJSONObject(name);

        case '[':
            back();
            return parseJSONArray(name);
        }
        
        /*
         * Handle unquoted text. This could be the values true, false, or
         * null, or it can be a number. An implementation (such as this one)
         * is allowed to also accept non-standard forms.
         *
         * Accumulate characters until we reach the end of the text or a
         * formatting character.
         */

        StringBuffer sb = new StringBuffer();
        while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
            sb.append(c);
            c = next();
        }
        back();

        string = sb.toString().trim();
        if (string.equals("")) {
            throw syntaxError("Missing value");
        }

        stringToValue(string, name);


        return string;

    }   

    
    /**
     * Get the next key. The value can be parsable as a Boolean, Double, Integer,
     *  Long, or String, or the JSONObject.NULL object.
     * @throws SAXException If syntax error.
     *
     * @return An object.
     */
    private String  nextKey() throws SAXException 
    {
        char c = nextClean();
        String string;
        
        switch (c) {
        case '"':
        case '\'':
            return nextString(c);
        
        case '{':
            back();
            throw syntaxError("object found where key expected");

        case '[':
            back();
            throw syntaxError("array found where key expected");
        }
        
        /*
         * Handle unquoted text. This could be the values true, false, or
         * null, or it can be a number. An implementation (such as this one)
         * is allowed to also accept non-standard forms.
         *
         * Accumulate characters until we reach the end of the text or a
         * formatting character.
         */

        StringBuffer sb = new StringBuffer();
        while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
            sb.append(c);
            c = next();
        }
        back();

        string = sb.toString().trim();
        if (string.equals("")) {
            throw syntaxError("Missing value");
        }

        return string;

    }   


    private String parseNextString(char delimiter, String name) throws SAXException
    {

        atts.clear();
        if (name != null) {
            atts.addAttribute("", "name", "name", "CDATA", name);
        }

        String string = nextString(delimiter);

        downstream.startElement(JSONNamespace, "string", "json:string", atts);
        downstream.characters(string.toCharArray(), 0, string.length());
        downstream.endElement(JSONNamespace, "string", "json:string");

        return string;
    }
    

    /**
     * Try to convert a string into a number, boolean, or null. If the string
     * can't be converted, return the string.
     * @param string A String.
     * @return A simple JSON value.
     */
    private void stringToValue(String string, String name) throws SAXException
    {

        atts.clear();
        if (name != null) {
            atts.addAttribute("", "name", "name", "CDATA", name);
        }

        if (string.equals("")) {
            return;
        }

        if (string.equalsIgnoreCase("true")) {
            downstream.startElement(JSONNamespace, "boolean", "json:boolean", atts);
            downstream.characters(string.toCharArray(), 0, 4);
            downstream.endElement(JSONNamespace, "boolean", "json:boolean");
            return;
        }

        if (string.equalsIgnoreCase("false")) {
            downstream.startElement(JSONNamespace, "boolean", "json:boolean", atts);
            downstream.characters(string.toCharArray(), 0, 5);
            downstream.endElement(JSONNamespace, "boolean", "json:boolean");
            return;
        }

        if (string.equalsIgnoreCase("null")) {
            downstream.startElement(JSONNamespace, "null", "json:null", atts);
            downstream.endElement(JSONNamespace, "null", "json:null");
            return;
        }

        /*
         * If it might be a number, try converting it. 
         * We support the non-standard 0x- convention. 
         * If a number cannot be produced, then the value will just
         * be a string. Note that the 0x-, plus, and implied string
         * conventions are non-standard. A JSON parser may accept
         * non-JSON forms as long as it accepts all correct JSON forms.
         */

        char b = string.charAt(0);
        if ((b >= '0' && b <= '9') || b == '.' || b == '-' || b == '+') {
            if (b == '0' && string.length() > 2 &&
                (string.charAt(1) == 'x' || string.charAt(1) == 'X')) {
                try {
                    //     return new Integer(Integer.parseInt(string.substring(2), 16));
                } catch (Exception ignore) {
                }
            }
            try {

                if (string.indexOf('.') > -1 || 
                    string.indexOf('e') > -1 || string.indexOf('E') > -1) {
                    Double d = Double.valueOf(string);
                    downstream.startElement(JSONNamespace, "number", "json:number", atts);
                    downstream.characters(string.toCharArray(), 0, string.length());
                    downstream.endElement(JSONNamespace, "number", "json:number");
                    return;
                } else {
                    Long myLong = new Long(string);
                    downstream.startElement(JSONNamespace, "number", "json:number", atts);
                    downstream.characters(string.toCharArray(), 0, string.length());
                    downstream.endElement(JSONNamespace, "number", "json:number");
                    return;
                }
            }  catch (Exception ignore) {
            }
        }
        downstream.startElement(JSONNamespace, "string", "json:string", atts);
        downstream.characters(string.toCharArray(), 0, string.length());
        downstream.endElement(JSONNamespace, "string", "json:string");
    }


    private String parseJSONObject(String name)  throws SAXException
    {
        char c;
        String key;

        if (nextClean() != '{') {
            throw syntaxError("A JSONObject text must begin with '{'");
        }


        atts.clear();
        if (name != null) {
            atts.addAttribute("", "name", "name", "CDATA", name);
        }


        downstream.startElement(JSONNamespace, "object", "json:object", atts);


        // FIXME:  start element ?
        for (;;) {
            c = nextClean();
            switch (c) {
            case 0:  // eof
                throw syntaxError("A JSONObject text must end with '}'");
            case '}':
                downstream.endElement(JSONNamespace, "object", "json:object");
                return "{}";
            default:
                back();
                key = nextKey();
            }

            // The key is followed by ':'. We will also tolerate '=' or '=>'.

            c = nextClean();
            if (c == '=') {
                if (next() != '>') {
                    back();
                }
            } else if (c != ':') {
                throw syntaxError("Expected a ':' after a key");
            }

            //            System.err.println( "found " + c + " key is [" + key + "]");

            //            putOnce(key, nextValue());

            nextValue(key);

            // Pairs are separated by ','. We will also tolerate ';'.

            switch (nextClean()) {
            case ';':
            case ',':
                if (nextClean() == '}') {
                    downstream.endElement(JSONNamespace, "object", "json:object");
                    return "{}";
                }
            back();
            break;

            case '}':
                downstream.endElement(JSONNamespace, "object", "json:object");
                return "{}";
            default:
                throw syntaxError("Expected a ',' or '}'");
            }
        }


    }
    
    
    private String parseJSONArray(String name)  throws SAXException
    {


        if (nextClean() != '[') {
            throw syntaxError("A JSONArray text must start with '['");
        }

        atts.clear();
        if (name != null) {
            atts.addAttribute("", "name", "name", "CDATA", name);
        }

        downstream.startElement(JSONNamespace, "array", "json:array", atts);

        atts.clear();

        if (nextClean() == ']') {
            downstream.endElement(JSONNamespace, "array", "json:array");
            return "[]";
        } else {
            back();
            for (;;) {
                if (nextClean() == ',') {
                    back();
                    downstream.startElement(JSONNamespace, "null", "json:null", atts);
                    downstream.endElement(JSONNamespace, "null", "json:null");
                } else {
                    back();
                    nextValue(null);
                }
                switch (nextClean()) {
                case ';':
                case ',':
                    if (nextClean() == ']') {
                        downstream.endElement(JSONNamespace, "array", "json:array");
                        return "[]";
                    }
                back();
                break;
                case ']':
                    downstream.endElement(JSONNamespace, "array", "json:array");
                    return "[]";
                default:
                    throw syntaxError("Expected a ',' or ']'");
                }
            }
        }

    }
    
    /**
     * Make a SAXException to signal a JSON syntax error.
     *
     * @param message The error message.
     * @return  A SAXException object, suitable for throwing
     */
    private SAXException syntaxError(String message) {
        return new SAXException(message + toString());
    }


    /**
     * Make a printable string of this Tokener.
     *
     * @return " at {index} [character {character} line {line}]"
     */
    public String toString() {
        return " at " + index + " [character " + this.character + " line " + 
            this.line + "]";
    }

        
}