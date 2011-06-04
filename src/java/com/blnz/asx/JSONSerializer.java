package com.blnz.asx;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;

import java.io.IOException;
import java.io.Writer;

import java.util.Stack;

/**
 * a SAX2 content Handler that writes JSON
 */
public class JSONSerializer implements ContentHandler
{

    Locator _locator = null;
    Writer _out = null;
    
    private boolean keepOpen;
    protected boolean inStartTag = false;

    private static final int DEFAULT_BUF_LENGTH = 8*1024;
    private char[] buf = new char[DEFAULT_BUF_LENGTH];

    private int bufUsed = 0;

    private Stack<Integer> elementStack = new Stack<Integer>();

    protected  String getCharString(char c) 
    {
        return Integer.toString(c);
    }

    public boolean keepOpen() 
    {
        return keepOpen;
    }
    
    public void setKeepOpen(boolean ko)
    {
        this.keepOpen = ko;
    }

    /**
     * construct with Writer to write to
     */
    public JSONSerializer(Writer os)
    {
        this._out = os;
    }

    /**
     * become provisioned with a Locator
     *
     * @param locator The document locator.
     * @see org.xml.sax.ContentHandler#setDocumentLocator
     */
    public void setDocumentLocator (Locator locator)
    {
	this._locator = locator;
    }

    /**
     * Filter a start document event.
     *
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#startDocument
     */
    public void startDocument ()
	throws SAXException
    {
        elementStack.push(new Integer(0));
    }


    /**
     * Filter an end document event.
     *
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#endDocument
     */
    public void endDocument ()
	throws SAXException
    {
        if (bufUsed != 0)
            flushBuf();
        try {
            if (_out != null) {
                if (keepOpen)
                    _out.flush();
                else
                    _out.close();
                _out = null;
            }
        }
        catch (java.io.IOException e) {
            throw new SAXException(e);
        }
        _out = null;
        buf = null;
    }


    /**
     * Filter a start Namespace prefix mapping event.
     *
     * @param prefix The Namespace prefix.
     * @param uri The Namespace URI.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#startPrefixMapping
     */
    
    public void startPrefixMapping(String prefix, String uri)
    { }
    

    /**
     * Filter an end Namespace prefix mapping event.
     *
     * @param prefix The Namespace prefix.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#endPrefixMapping
     */
    public void endPrefixMapping (String prefix)
	throws SAXException
    { }


    /**
     * Filter a start element event.
     *
     * @param uri The element's Namespace URI, or the empty string.
     * @param localName The element's local name, or the empty string.
     * @param qName The element's qualified (prefixed) name, or the empty
     *        string.
     * @param atts The element's attributes.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#startElement
     */
    public void startElement (String uri, String localName, String qName,
			      Attributes atts)
	throws SAXException
    {

        if (elementStack.peek() > 0) {
            put(',');
        }
        elementStack.push(elementStack.pop() + 1);
        elementStack.push(0);

        String name = atts.getValue("name");
        if (name != null) {
            put('"');
            writeRaw(name);
            put('"');
            put(':');
        }
        if ("object".equals(localName)) {
            put('{');
        } else if ("array".equals(localName)) {
            put('[');
        } else if ("string".equals(localName)) {
            put('"');
        } else if ("number".equals(localName)) {

        } else if ("boolean".equals(localName)) {

        } else if ("null".equals(localName)) {
            writeRaw("null");
        } else {

        }
    }


    /**
     * Filter an end element event.
     *
     * @param uri The element's Namespace URI, or the empty string.
     * @param localName The element's local name, or the empty string.
     * @param qName The element's qualified (prefixed) name, or the empty
     *        string.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#endElement
     */
    public void endElement (String uri, String localName, String qName)
	throws SAXException
    {
        elementStack.pop();
        if ("object".equals(localName)) {
            put('}');
        } else if ("array".equals(localName)) {
            put(']');
        } else if ("string".equals(localName)) {
            put('"');
        } else if ("number".equals(localName)) {

        } else if ("boolean".equals(localName)) {

        } else if ("null".equals(localName)) {

        } else {

        }
    }

    /**
     * Filter a character data event.
     *
     * @param cbuf An array of characters.
     * @param off The starting position in the array.
     * @param len The number of characters to use from the array.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#characters
     */
    public void characters (char cbuf[], int off, int len)
	throws SAXException
    {
        if (len == 0) {
            return;
        }

        do {
            char c = cbuf[off++];
            if (c > 127) {
                writeRaw(getCharString(c));
            } else {
                
                switch (c) {
                default:
                    put(c);
                }
            }
        } while (--len > 0);
    }


    /**
     * Filter an ignorable whitespace event.
     *
     * @param ch An array of characters.
     * @param start The starting position in the array.
     * @param length The number of characters to use from the array.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#ignorableWhitespace
     */

    public void ignorableWhitespace (char ch[], int start, int length)
        throws SAXException 
    {
        for (; length > 0; length--, start++)
            put(ch[start]);
    }

    /**
     * Filter a processing instruction event.
     *
     * @param target The processing instruction target.
     * @param data The text following the target.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#processingInstruction
     */
    public void processingInstruction(String target, String data)
        throws SAXException 
    { }


    /**
     * Filter a skipped entity event.
     *
     * @param name The name of the skipped entity.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
g     * @see org.xml.sax.ContentHandler#skippedEntity
     */
    public void skippedEntity (String name)
	throws SAXException
    { }
    
    public void markup(String chars) throws SAXException 
    { }

    public void comment(String body) throws SAXException 
    { }


    public void rawCharacters(String chars) throws SAXException
    {
        writeRaw(chars);
    }

    protected void writeRaw(String str) throws SAXException 
    {
        final int n = str.length();
        for (int i = 0; i < n; i++) {
            char c = str.charAt(i);
            put(c);
        }
    }

    protected final void put(char c) throws SAXException 
    {
        if (bufUsed == buf.length) {
            flushBuf();
        }
        buf[bufUsed++] = c;
    }

    private final void flushBuf() throws SAXException 
    {
        try {
            _out.write(buf, 0, bufUsed);
            bufUsed = 0;
        }
        catch (java.io.IOException e) {
            throw new SAXException(e);
        }
    }

}
