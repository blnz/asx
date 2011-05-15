package com.blnz.asx;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.NamespaceSupport;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.Locator;

import java.io.CharConversionException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.OutputStream;
import java.io.Writer;
import java.io.OutputStreamWriter;

import java.util.Hashtable;
import java.util.Vector;

/**
 * a SAX2 content Handler that writes characters
 */
public class XMLSerializer implements ContentHandler
{

    Locator _locator = null;
    Writer _out = null;
    private AttributesImpl _atts = null;
    
    private boolean		recreatedAttrs;
    private AttributesImpl	attributes = new AttributesImpl ();

    private NamespaceSupport _nsSupport;

    //    private OutputStream out = null;
    private boolean keepOpen;
    protected boolean inStartTag = false;
    private boolean omitXmlDeclaration = true;
    private String standalone;
    private static final int DEFAULT_BUF_LENGTH = 8*1024;
    private char[] buf = new char[DEFAULT_BUF_LENGTH];
    private String[] _nameParts = new String[3];
    private int bufUsed = 0;
    private String lineSeparator = "\n";
    private byte minimize = MINIMIZE_EMPTY_ELEMENTS;

    private boolean htmlCharEntities = false;

    private Hashtable _pending = new Hashtable();

    static final public byte MINIMIZE_NONE = 0;
    static final public byte MINIMIZE_EMPTY_ELEMENTS = 1;
    static final public byte MINIMIZE_EMPTY_ELEMENTS_HTML = 2;


    static private final String charEntities[] = {
        "\u00A0nbsp",
        "\u00A1iexcl",
        "\u00A2cent",
        "\u00A3pound",
        "\u00A4curren",
        "\u00A5yen",
        "\u00A6brvbar",
        "\u00A7sect",
        "\u00A8uml",
        "\u00A9copy",
        "\u00AAordf",
        "\u00ABlaquo",
        "\u00ACnot",
        "\u00ADshy",
        "\u00AEreg",
        "\u00AFmacr",
        "\u00B0deg",
        "\u00B1plusmn",
        "\u00B2sup2",
        "\u00B3sup3",
        "\u00B4acute",
        "\u00B5micro",
        "\u00B6para",
        "\u00B7middot",
        "\u00B8cedil",
        "\u00B9sup1",
        "\u00BAordm",
        "\u00BBraquo",
        "\u00BCfrac14",
        "\u00BDfrac12",
        "\u00BEfrac34",
        "\u00BFiquest",
        "\u00C0Agrave",
        "\u00C1Aacute",
        "\u00C2Acirc",
        "\u00C3Atilde",
        "\u00C4Auml",
        "\u00C5Aring",
        "\u00C6AElig",
        "\u00C7Ccedil",
        "\u00C8Egrave",
        "\u00C9Eacute",
        "\u00CAEcirc",
        "\u00CBEuml",
        "\u00CCIgrave",
        "\u00CDIacute",
        "\u00CEIcirc",
        "\u00CFIuml",
        "\u00D0ETH",
        "\u00D1Ntilde",
        "\u00D2Ograve",
        "\u00D3Oacute",
        "\u00D4Ocirc",
        "\u00D5Otilde",
        "\u00D6Ouml",
        "\u00D7times",
        "\u00D8Oslash",
        "\u00D9Ugrave",
        "\u00DAUacute",
        "\u00DBUcirc",
        "\u00DCUuml",
        "\u00DDYacute",
        "\u00DETHORN",
        "\u00DFszlig",
        "\u00E0agrave",
        "\u00E1aacute",
        "\u00E2acirc",
        "\u00E3atilde",
        "\u00E4auml",
        "\u00E5aring",
        "\u00E6aelig",
        "\u00E7ccedil",
        "\u00E8egrave",
        "\u00E9eacute",
        "\u00EAecirc",
        "\u00EBeuml",
        "\u00ECigrave",
        "\u00EDiacute",
        "\u00EEicirc",
        "\u00EFiuml",
        "\u00F0eth",
        "\u00F1ntilde",
        "\u00F2ograve",
        "\u00F3oacute",
        "\u00F4ocirc",
        "\u00F5otilde",
        "\u00F6ouml",
        "\u00F7divide",
        "\u00F8oslash",
        "\u00F9ugrave",
        "\u00FAuacute",
        "\u00FBucirc",
        "\u00FCuuml",
        "\u00FDyacute",
        "\u00FEthorn",
        "\u00FFyuml",
        "\u0152OElig",
        "\u0153oelig",
        "\u0160Scaron",
        "\u0161scaron",
        "\u0178Yuml",
        "\u0192fnof",
        "\u02C6circ",
        "\u02DCtilde",
        "\u0391Alpha",
        "\u0392Beta",
        "\u0393Gamma",
        "\u0394Delta",
        "\u0395Epsilon",
        "\u0396Zeta",
        "\u0397Eta",
        "\u0398Theta",
        "\u0399Iota",
        "\u039AKappa",
        "\u039BLambda",
        "\u039CMu",
        "\u039DNu",
        "\u039EXi",
        "\u039FOmicron",
        "\u03A0Pi",
        "\u03A1Rho",
        "\u03A3Sigma",
        "\u03A4Tau",
        "\u03A5Upsilon",
        "\u03A6Phi",
        "\u03A7Chi",
        "\u03A8Psi",
        "\u03A9Omega",
        "\u03B1alpha",
        "\u03B2beta",
        "\u03B3gamma",
        "\u03B4delta",
        "\u03B5epsilon",
        "\u03B6zeta",
        "\u03B7eta",
        "\u03B8theta",
        "\u03B9iota",
        "\u03BAkappa",
        "\u03BBlambda",
        "\u03BCmu",
        "\u03BDnu",
        "\u03BExi",
        "\u03BFomicron",
        "\u03C0pi",
        "\u03C1rho",
        "\u03C2sigmaf",
        "\u03C3sigma",
        "\u03C4tau",
        "\u03C5upsilon",
        "\u03C6phi",
        "\u03C7chi",
        "\u03C8psi",
        "\u03C9omega",
        "\u03D1thetasym",
        "\u03D2upsih",
        "\u03D6piv",
        "\u2002ensp",
        "\u2003emsp",
        "\u2009thinsp",
        "\u200Czwnj",
        "\u200Dzwj",
        "\u200Elrm",
        "\u200Frlm",
        "\u2013ndash",
        "\u2014mdash",
        "\u2018lsquo",
        "\u2019rsquo",
        "\u201Asbquo",
        "\u201Cldquo",
        "\u201Drdquo",
        "\u201Ebdquo",
        "\u2020dagger",
        "\u2021Dagger",
        "\u2022bull",
        "\u2026hellip",
        "\u2030permil",
        "\u2032prime",
        "\u2033Prime",
        "\u2039lsaquo",
        "\u203Arsaquo",
        "\u203Eoline",
        "\u2044frasl",
        "\u20ACeuro",
        "\u2111image",
        "\u2118weierp",
        "\u211Creal",
        "\u2122trade",
        "\u2135alefsym",
        "\u2190larr",
        "\u2191uarr",
        "\u2192rarr",
        "\u2193darr",
        "\u2194harr",
        "\u21B5crarr",
        "\u21D0lArr",
        "\u21D1uArr",
        "\u21D2rArr",
        "\u21D3dArr",
        "\u21D4hArr",
        "\u2200forall",
        "\u2202part",
        "\u2203exist",
        "\u2205empty",
        "\u2207nabla",
        "\u2208isin",
        "\u2209notin",
        "\u220Bni",
        "\u220Fprod",
        "\u2211sum",
        "\u2212minus",
        "\u2217lowast",
        "\u221Aradic",
        "\u221Dprop",
        "\u221Einfin",
        "\u2220ang",
        "\u2227and",
        "\u2228or",
        "\u2229cap",
        "\u222Acup",
        "\u222Bint",
        "\u2234there4",
        "\u223Csim",
        "\u2245cong",
        "\u2248asymp",
        "\u2260ne",
        "\u2261equiv",
        "\u2264le",
        "\u2265ge",
        "\u2282sub",
        "\u2283sup",
        "\u2284nsub",
        "\u2286sube",
        "\u2287supe",
        "\u2295oplus",
        "\u2297otimes",
        "\u22A5perp",
        "\u22C5sdot",
        "\u2308lceil",
        "\u2309rceil",
        "\u230Alfloor",
        "\u230Brfloor",
        "\u2329lang",
        "\u232Arang",
        "\u25CAloz",
        "\u2660spades",
        "\u2663clubs",
        "\u2665hearts",
        "\u2666diams"
    };

    static private String[][] charMap = new String[256][];

    static {
        for (int i = 0; i < charEntities.length; i++) {
            int c = charEntities[i].charAt(0);
            int lo = c & 0xff;
            int hi = c >> 8;
            if (charMap[hi] == null)
                charMap[hi] = new String[256];
            charMap[hi][lo] = "&" + charEntities[i].substring(1) + ";";
        }
        char[] charBuf = new char[1];
        for (int i = 0; i < 128; i++) {
            if (charMap[0][i] == null) {
                charBuf[0] = (char)i;
                charMap[0][i] = new String(charBuf);
            }
        }
    }


    protected  String getCharString(char c) 
    {
        if (htmlCharEntities) {
            String[] v = charMap[c >> 8];
            if (v == null) {
                v = new String[256];
                charMap[c >> 8] = v;
            }
            String name = v[c & 0xFF];
            if (name == null) {
                name = "&#" + Integer.toString(c) + ";";
                v[c & 0xFF] = name;
            }
            return name;
        } else {
            return "&#" + Integer.toString(c) + ";";
        }
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
    public XMLSerializer(Writer os)
    {
        _nsSupport = new NamespaceSupport();
        _atts = new AttributesImpl();

        this._out = os;
    }

    /**
     * Filter a new document locator event.
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
        if (!omitXmlDeclaration) {
            writeRaw("<?xml version=\"1.0\" encoding=\"utf-8\"");
            if (standalone != null) {
                writeRaw(" standalone=\"");
                writeRaw(standalone);
                put('"');
            }
            writeRaw("?>\n");
        }
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
    {
        //System.out.println("got map event for {" + uri + "} as {" + prefix + "}");
        _pending.put(prefix, uri);
    
        // // reconstruct "xmlns" attributes deleted by all
        // // SAX2 parsers without "namespace-prefixes" = true
        // if ("".equals (prefix))
        //     attributes.addAttribute ("", "", "xmlns",
        //                              "CDATA", uri);
        // else
        //     attributes.addAttribute ("", "", "xmlns:" + prefix,
        //                              "CDATA", uri);
        // recreatedAttrs = true;
    }
    

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
    {
        // FIXME do something ??
    }


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
        if (inStartTag) {
            finishStartTag();
        }

        atts = addNamespaces(uri, localName, qName, atts);

        put('<');
        
        // FIXME use prefix map?
        writeRaw(qName);

        // ... then any recreated ones (DOM deletes duplicates)
        if (recreatedAttrs) {
            recreatedAttrs = false;

            int len = attributes.getLength();
            for (int i = 0; i < len; ++i) {
                String qname = attributes.getQName(i);
                if (atts.getValue(qname) == null) {
                    put(' ');
                    
                    writeRaw(attributes.getQName(i));
                    put('=');
                    put('"');
                    attributeValue(attributes.getValue(i));
                    put('"');
                }
            } 
            attributes.clear ();
        }

        // now the attributes
        int n = atts.getLength();
        for (int i = 0; i < n; i++) {
            put(' ');

            writeRaw(atts.getQName(i));
            put('=');
            put('"');
            attributeValue(atts.getValue(i));
            put('"');
        }
        inStartTag = true;
    }


    /**
     * <p>If necessary, perform Namespace processing.</p>
     *
     * @param qName The qualified (prefixed) name.
     * @param qAtts The XML 1.0 attribute list (with qnames).
     * @exception SAXException The client may raise a
     *            processing exception.
     */
    public Attributes addNamespaces(String nsURI, String localName, 
                                    String qName, Attributes qAtts)
	throws SAXException
    {
        // These are exceptions from the
        // first pass; they should be
        // ignored if there's a second pass,
        // but reported otherwise.
	Vector exceptions = null;
        
        // OK, we're doing Namespace processing.
	_nsSupport.pushContext();
	int length = qAtts.getLength();
	
        // First pass:  handle NS decls by recording them in the
        // namespace support object, and firing a startPrefixMapping event
	for (int i = 0; i < length; i++) {
            
	    String attQName = qAtts.getQName(i);

	    if (!attQName.startsWith("xmlns")) {
		continue;
            }
            // Could be a declaration...
	    String prefix;
	    int n = attQName.indexOf(':');
            
            // xmlns=...
	    if (n == -1 && attQName.length () == 5) {
		prefix = "";
	    } else if (n != 5) {
		// XML namespaces spec doesn't discuss "xmlnsf:oo"
		// (and similarly named) attributes ... at most, warn
                //       System.out.println("4-- don't know what to do with {" + attQName + "}");
		continue;
	    } else {		// xmlns:foo=...
		prefix = attQName.substring(n+1);
            }
            
            String value = qAtts.getValue(i);
	    if (!_nsSupport.declarePrefix(prefix, value)) {
                throw new SAXException("nsSupport says Illegal Namespace prefix: " + prefix);
	    } 
	}

 	_atts.clear();
        
        String prefix;
        int n = qName.indexOf(':');
        
        // 
        if (n == -1) {
            prefix = "";
        } else {
            prefix = qName.substring(0, n);
        }
        
        // next, has the prefix binding been declared yet for the element 
        //  name?
        String currentNamespace = _nsSupport.getURI(prefix);
        if (currentNamespace == null || ! currentNamespace.equals(nsURI)) {
            // no?, fake a declaration
            if ((nsURI != null) && (!"".equals(nsURI))) {
                if (!_nsSupport.declarePrefix(prefix, nsURI)) {
                    throw new SAXException("Illegal Namespace prefix: " + prefix);
                } 
                if ("".equals(prefix)) {
                    addOrReplaceAttribute(_atts, "", "", "xmlns", "CDATA", nsURI);
                } else {
                    addOrReplaceAttribute(_atts, "", "", "xmlns:" + prefix, "CDATA", nsURI);
                }
            }
        } else {

        }

        // now lets check each attribute name, and see if we need to
        // fake a namespace decl for it
        for (int i = 0; i < length; i++) {
	    String attQName = qAtts.getQName(i);
            String attURI = qAtts.getURI(i);

            n = attQName.indexOf(':');
            if (n == -1) {
                prefix = "";
            } else {
                prefix = attQName.substring(0, n);
            }

            // skip over any xmlns decls
            if ("xmlns".equals(prefix) || "xmlns".equals(attQName)) {
                continue;
            }

            // next, has the prefix binding been declared yet?
            currentNamespace = _nsSupport.getURI(prefix);

            if (currentNamespace == null || ! currentNamespace.equals(nsURI)) {
                // no?, fake a declaration
                if ((attURI != null) && (!"".equals(attURI))) {
                    if (! "xml".equals(prefix) && !_nsSupport.declarePrefix(prefix, attURI)) {
                        throw new SAXException("Illegal Namespace prefix: " + prefix);
                    }
                    if ("".equals(prefix)) {
                        addOrReplaceAttribute(_atts, "", "", "xmlns", 
                                              "CDATA", attURI);
                    } else {
                        addOrReplaceAttribute(_atts, "", "", "xmlns:" + prefix, 
                                              "CDATA", attURI);
                    }
                }
            }
        }
	
        // final pass: copy all relevant
        // attributes into the SAX2 AttributeList
        // using updated prefix bindings
	for (int i = 0; i < length; i++) {
	    String attQName = qAtts.getQName(i);
	    String type = qAtts.getType(i);
	    String value = qAtts.getValue(i);

            // Declaration?
	    if (attQName.startsWith("xmlns")) {
		n = attQName.indexOf(':');

		if (n == -1 && attQName.length () == 5) {
		    prefix = "";
		} else if (n != 5) {
		    // XML namespaces spec doesn't discuss "xmlnsf:oo"
		    // (and similarly named) attributes ... ignore

		    prefix = null;
		} else {
		    prefix = attQName.substring(n + 1);
		}
                // Yes, decl:  report or prune
		if (prefix != null) {
                    
                    if (_atts.getValue(attQName.intern()) == null) {
                        addOrReplaceAttribute(_atts, "", "", attQName.intern(),
                                              type, value);
                    }
                    
		    continue;
		}
	    } 

            String attName[] = processName(attQName, true, true);
            addOrReplaceAttribute(_atts, attName[0], attName[1], attName[2],
                                  type, value);

	}

        return _atts;
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
        if (inStartTag) {
            inStartTag = false;
            if (minimize != MINIMIZE_NONE) {
                if (minimize == MINIMIZE_EMPTY_ELEMENTS_HTML)
                    put(' ');
                put('/');
                put('>');
                return;
            }
            put('>');
        }
        put('<');
        put('/');
        writeRaw(qName);
        put('>');
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
        if (inStartTag) {
            finishStartTag();
        }
        do {
            char c = cbuf[off++];
            if (c > 127) {
                //                String s = "&#x" + Integer.toHexString(c) + ";";
                writeRaw(getCharString(c));
            } else {
                
                switch (c) {
                case '&':
                    writeRaw("&amp;");
                    break;
                case '<':
                    writeRaw("&lt;");
                    break;
                case  '>':
                    writeRaw("&gt;");
                    break;
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
        throws SAXException {
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
    {
        if (target == null) {
            comment(data);
            return;
        } else if ("xfyfs".equals(target)) {
            // do not propogate xfyfs target
            return;
        }
        if (inStartTag) {
            finishStartTag();
        }
        put('<');
        put('?');
        writeRaw(target);
        if (data.length() > 0) {
            put(' ');
            writeMarkup(data);
        }
        put('?');
        put('>');
    }


    /**
     * Filter a skipped entity event.
     *
     * @param name The name of the skipped entity.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#skippedEntity
     */
    public void skippedEntity (String name)
	throws SAXException
    {
        // FIXME do something

    }
    ///////////////////////////////

    public void setMinimize(byte minimize) 
    {
        this.minimize = minimize;
    }

    /**
     * set a flag indicating if we should be emitting html's character
     * entities instead of unicode code point entities
     */
    public void setSendHTMLCharEntities(boolean flag)
    {
        htmlCharEntities = flag;
    }
    
    public void rawCharacters(String chars) throws SAXException
    {
        if (inStartTag) {
            finishStartTag();
        }
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


    protected void attributeValue(String value) throws SAXException 
    {
	if (value != null){
	    int valueLength = value.length();
	    for (int j = 0; j < valueLength; j++) {
		char c = value.charAt(j);
		if (c > 127) {
                    //		    String s = "&#x" + Integer.toHexString(c) + ";";
		    writeRaw(getCharString(c));
		} else {
		    switch (c) {
		    case '\n':
			writeRaw("&#10;");
			break;
		    case '&':
			writeRaw("&amp;");
			break;
		    case '<':
			writeRaw("&lt;");
			break;
		    case '"':
			writeRaw("&quot;");
			break;
		    case '\r':
			writeRaw("&#13;");
			break;
		    case '\t':
			writeRaw("&#9;");
			break;
		    default:
			put(c);
			break;
		    }
		}
	    }
        }
    }
    
    protected final void finishStartTag() throws SAXException 
    {
        inStartTag = false;
        put('>');
    }

    public void markup(String chars) throws SAXException 
    {
        if (inStartTag) {
            finishStartTag();
        }
        writeMarkup(chars);
    }

    public void comment(String body) throws SAXException 
    {
        if (inStartTag) {
            finishStartTag();
        }
        writeRaw("<!--");
        writeMarkup(body);
        writeRaw("-->");
    }

    private void writeMarkup(String str) throws SAXException 
    {
        int len = str.length();
        for (int i = 0; i < len; i++) {
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

    //
    private void addOrReplaceAttribute(AttributesImpl atts, String nsURI, 
                                       String attLocalName, String attQName, 
                                       String type, String value)
    {

        int index = atts.getIndex(attQName);
        if (index < 0) {
            atts.addAttribute(nsURI, attLocalName, attQName, type, value);
        } else {
            atts.setAttribute(index, nsURI, attLocalName, attQName, type, value);
        }
    }

    /**
     * Process a qualified (prefixed) name.
     *
     * <p>If the name has an undeclared prefix, use only the qname
     * and make an ErrorHandler.error callback in case the app is
     * interested.</p>
     *
     * @param qName The qualified (prefixed) name.
     * @param isAttribute true if this is an attribute name.
     * @return The name split into three parts.
     * @exception SAXException The client may throw
     *            an exception if there is an error callback.
     */
    private String[] processName (String qName, boolean isAttribute,
				   boolean useException)
	throws SAXException
    {
	String parts[] = _nsSupport.processName(qName, _nameParts,
					       isAttribute);
	if (parts == null) {
	    if (useException) {
		throw new SAXException("Undeclared prefix: " + qName);
            }
	    parts = new String[3];
	    parts[0] = parts[1] = "";
	    parts[2] = qName.intern();
	}
	return parts;
    }
 
}
