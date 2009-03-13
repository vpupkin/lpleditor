// 
// 
// 
// Source File Name:   Driver.java

package fr.pixware.apt.convert;

import fr.pixware.apt.parse.ParseException;
import fr.pixware.util.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;

// Referenced classes of package fr.pixware.apt.convert:
//            CExtractor, TclExtractor, LatexConverter, PSConverter, 
//            PDFConverter, HTMLConverter, DocBookConverter, Extractor, 
//            Converter, Warning

public class Driver
{
    public static final class Paper
    {

        public String name;
        public String description;
        public int unit;
        public double width;
        public double height;

        public Paper(String name, String description, int unit, double width, double height)
        {
            this.name = name;
            this.description = description;
            this.unit = unit;
            this.width = width;
            this.height = height;
        }
    }


    public Driver()
    {
        meta = new Hashtable();
        rules = new Hashtable();
        pi = new Hashtable();
        converters = new Hashtable();
        extractors = new Hashtable();
        warnings = new Vector();
        setVerbose(false);
        setTOC(false);
        setIndex(false);
        setSectionsNumbered(true);
        setPaper("a4");
        setLanguage("en");
        setEncoding(FileUtil.defaultEncoding());
        CExtractor cExtractor = new CExtractor(this);
        registerExtractor("h", cExtractor);
        registerExtractor("c", cExtractor);
        registerExtractor("hh", cExtractor);
        registerExtractor("cc", cExtractor);
        registerExtractor("hpp", cExtractor);
        registerExtractor("cpp", cExtractor);
        registerExtractor("hxx", cExtractor);
        registerExtractor("cxx", cExtractor);
        registerExtractor("tcl", new TclExtractor(this));
        registerConverter("tex", new LatexConverter(this));
        registerConverter("ps", new PSConverter(this));
        registerConverter("pdf", new PDFConverter(this));
        HTMLConverter htmlConverter = new HTMLConverter(this);
        registerConverter("html", htmlConverter);
        registerConverter("htm", htmlConverter);
        registerConverter("xhtml", htmlConverter);
        registerConverter("xhtm", htmlConverter);
        DocBookConverter sgmlConverter = new DocBookConverter(this);
        registerConverter("sgml", sgmlConverter);
        registerConverter("sgm", sgmlConverter);
        registerConverter("xml", new DocBookConverter(this, true));
        registerConverter("rtf", "fr.pixware.apt.convert.rtf.RTFConverter");
    }

    public void setVerbose(boolean verbose)
    {
        this.verbose = verbose;
    }

    public boolean isVerbose()
    {
        return verbose;
    }

    public void setTOC(boolean toc)
    {
        this.toc = toc;
    }

    public boolean isTOC()
    {
        return toc;
    }

    public void setIndex(boolean index)
    {
        this.index = index;
    }

    public boolean isIndex()
    {
        return index;
    }

    public void setSectionsNumbered(boolean sectionsNumbered)
    {
        this.sectionsNumbered = sectionsNumbered;
    }

    public boolean isSectionsNumbered()
    {
        return sectionsNumbered;
    }

    public void putMeta(String key, String value)
    {
        meta.put(key, value);
    }

    public void removeMeta(String key)
    {
        meta.remove(key);
    }

    public void removeAllMeta()
    {
        meta.clear();
    }

    public String getMeta(String key)
    {
        return (String)meta.get(key);
    }

    public String[] getAllMeta()
    {
        String allMeta[] = new String[2 * meta.size()];
        Enumeration iter = meta.keys();
        for(int i = 0; iter.hasMoreElements(); i += 2)
        {
            String key = (String)iter.nextElement();
            allMeta[i] = key;
            allMeta[i + 1] = (String)meta.get(key);
        }

        return allMeta;
    }

    public void putPI(String format, String key, String value)
    {
        String compoundKey = format + "." + key;
        pi.put(compoundKey, value);
    }

    public void removePI(String format, String key)
    {
        pi.remove(format + "." + key);
    }

    public void removeAllPI(String format)
    {
        for(Enumeration keys = pi.keys(); keys.hasMoreElements();)
        {
            String key = (String)keys.nextElement();
            if(key.startsWith(format))
                pi.remove(key);
        }

    }

    public void removeAllPI()
    {
        pi.clear();
    }

    public String getPI(String format, String key)
    {
        return (String)pi.get(format + "." + key);
    }

    public void putRule(String srcExt, String dstExt, String value)
    {
        String compoundKey = "." + srcExt + "." + dstExt;
        rules.put(compoundKey, value);
    }

    public void removeRule(String srcExt, String dstExt)
    {
        rules.remove("." + srcExt + "." + dstExt);
    }

    public void removeAllRules(String dstExt)
    {
        String pattern = "." + dstExt;
        for(Enumeration keys = rules.keys(); keys.hasMoreElements();)
        {
            String key = (String)keys.nextElement();
            if(key.endsWith(pattern))
                rules.remove(key);
        }

    }

    public void removeAllRules()
    {
        rules.clear();
    }

    public String getRule(String srcExt, String dstExt)
    {
        return (String)rules.get("." + srcExt + "." + dstExt);
    }

    public String[] getAllRules()
    {
        String allRules[] = new String[3 * rules.size()];
        Enumeration iter = rules.keys();
        for(int i = 0; iter.hasMoreElements(); i += 3)
        {
            String key = (String)iter.nextElement();
            int split = key.lastIndexOf('.');
            allRules[i] = key.substring(1, split);
            allRules[i + 1] = key.substring(split + 1);
            allRules[i + 2] = (String)rules.get(key);
        }

        return allRules;
    }

    public void setPaper(String paper)
    {
        this.paper = paper;
    }

    public String getPaper()
    {
        return paper;
    }

    public void setLanguage(String language)
    {
        this.language = language;
    }

    public String getLanguage()
    {
        return language;
    }

    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    public String getEncoding()
    {
        return encoding;
    }

    public void loadProperties(String fileName)
        throws IOException
    {
        Properties prop = new Properties();
        FileInputStream in = new FileInputStream(fileName);
        prop.load(in);
        in.close();
        setProperties(prop);
    }

    public void setProperties(Properties properties)
    {
        Properties prop = (Properties)properties.clone();
        String value = prop.getProperty("v");
        if(value != null)
        {
            setVerbose(true);
            prop.remove("v");
        }
        value = prop.getProperty("paper");
        if(value != null)
        {
            setPaper(value);
            prop.remove("paper");
        }
        value = prop.getProperty("lang");
        if(value != null)
        {
            setLanguage(value);
            prop.remove("lang");
        }
        value = prop.getProperty("enc");
        if(value != null)
        {
            setEncoding(value);
            prop.remove("encoding");
        }
        value = prop.getProperty("toc");
        if(value != null)
        {
            setTOC(true);
            prop.remove("toc");
        }
        value = prop.getProperty("index");
        if(value != null)
        {
            setIndex(true);
            prop.remove("index");
        }
        String key;
        Hashtable map;
        for(Enumeration keys = prop.keys(); keys.hasMoreElements(); map.put(key, value))
        {
            key = (String)keys.nextElement();
            value = prop.getProperty(key);
            int i = key.indexOf('.');
            map = i != 0 ? i <= 0 ? meta : pi : rules;
        }

    }

    public void registerExtractor(String extension, Extractor extractor)
    {
        extractors.put(extension, extractor);
    }

    public Extractor getExtractor(String extension)
    {
        return (Extractor)extractors.get(extension.toLowerCase());
    }

    public String[] getRegisteredExtractors()
    {
        String extensions[] = new String[extractors.size()];
        Enumeration iter = extractors.keys();
        int i = 0;
        while(iter.hasMoreElements()) 
            extensions[i++] = (String)iter.nextElement();
        return extensions;
    }

    public void registerConverter(String extension, String className)
    {
        Converter converter;
        try
        {
            Class converterclass = Class.forName(className);
            Constructor constructor = converterclass.getDeclaredConstructor(driverClassParam);
            converter = (Converter)constructor.newInstance(new Object[] {
                this
            });
        }
        catch(Exception exception)
        {
            converter = null;
        }
        if(converter != null)
            registerConverter(extension, converter);
    }

    public void registerConverter(String extension, Converter converter)
    {
        converters.put(extension, converter);
    }

    public Converter getConverter(String extension)
    {
        return (Converter)converters.get(extension.toLowerCase());
    }

    public String[] getRegisteredConverters()
    {
        String extensions[] = new String[converters.size()];
        Enumeration iter = converters.keys();
        int i = 0;
        while(iter.hasMoreElements()) 
            extensions[i++] = (String)iter.nextElement();
        return extensions;
    }

    public void convert(String inFileNames[], String outFileName)
        throws Exception
    {
        String outExt = FileUtil.fileExtension(outFileName);
        if(outExt == null || outExt.length() == 0)
            throw new IllegalArgumentException("missing format extension");
        Converter converter = getConverter(outExt);
        if(converter == null)
            throw new IllegalArgumentException("unkown format extension '" + outExt + "'");
        String aptFileNames[] = new String[inFileNames.length];
        for(int i = 0; i < inFileNames.length; i++)
            aptFileNames[i] = inFileNames[i];

        removeAllWarnings();
        try
        {
            String suffix = Long.toString(System.currentTimeMillis(), 36);
            for(int i = 0; i < inFileNames.length; i++)
            {
                String inExt = FileUtil.fileExtension(inFileNames[i]);
                Extractor extractor;
                if(inExt != null && inExt.length() != 0 && (extractor = getExtractor(inExt)) != null)
                {
                    aptFileNames[i] = inFileNames[i] + suffix;
                    extractor.extract(inFileNames[i], aptFileNames[i]);
                }
            }

            converter.convert(aptFileNames, outFileName);
        }
        finally
        {
            for(int i = 0; i < inFileNames.length; i++)
                if(!aptFileNames[i].equals(inFileNames[i]))
                    (new File(aptFileNames[i])).delete();

        }
    }

    public void addWarning(Warning warning)
    {
        warnings.addElement(warning);
    }

    public void removeAllWarnings()
    {
        warnings.removeAllElements();
    }

    public Warning[] getWarnings()
    {
        Warning list[] = new Warning[warnings.size()];
        warnings.copyInto(list);
        return list;
    }

    public static void main(String args[])
    {
        Driver driver = new Driver();
        String propFileNames[] = {
            System.getProperty("aptconvertrc"), PlatformUtil.rcFileName("aptconvert")
        };
        int i;
        for(i = 0; i < propFileNames.length; i++)
        {
            String propFileName = propFileNames[i];
            if(propFileName != null && (new File(propFileName)).isFile())
                try
                {
                    driver.loadProperties(propFileName);
                }
                catch(IOException e)
                {
                    System.err.println("cannot load '" + propFileName + "': " + e.getMessage());
                    System.exit(1);
                }
        }

        boolean help = false;
        for(i = 0; i < args.length; i++)
        {
            String arg = args[i];
            if(arg.indexOf('-') != 0)
                break;
            if(arg.equals("-v"))
                driver.setVerbose(true);
            else
            if(arg.equals("-toc"))
                driver.setTOC(true);
            else
            if(arg.equals("-index"))
                driver.setIndex(true);
            else
            if(arg.equals("-nonum"))
                driver.setSectionsNumbered(false);
            else
            if(arg.equals("-meta"))
            {
                if(i + 2 >= args.length)
                    usage(driver);
                driver.putMeta(args[i + 1], args[i + 2]);
                i += 2;
            } else
            if(arg.equals("-pi"))
            {
                if(i + 3 >= args.length)
                    usage(driver);
                driver.putPI(args[i + 1], args[i + 2], args[i + 3]);
                i += 3;
            } else
            if(arg.equals("-rule"))
            {
                if(i + 3 >= args.length)
                    usage(driver);
                driver.putRule(args[i + 1], args[i + 2], args[i + 3]);
                i += 3;
            } else
            if(arg.equals("-paper"))
            {
                if(i + 1 >= args.length)
                    usage(driver);
                driver.setPaper(args[++i]);
            } else
            if(arg.equals("-lang"))
            {
                if(i + 1 >= args.length)
                    usage(driver);
                driver.setLanguage(args[++i]);
            } else
            if(arg.equals("-enc"))
            {
                if(i + 1 >= args.length)
                    usage(driver);
                driver.setEncoding(args[++i]);
            } else
            if(arg.equals("-?"))
            {
                if(i + 1 >= args.length)
                    usage(driver);
                usage(driver, args[++i]);
            } else
            {
                usage(driver);
            }
        }

        if(args.length - i < 2)
            usage(driver);
        String outFileName = args[i++];
        String inFileNames[] = new String[args.length - i];
        System.arraycopy(args, i, inFileNames, 0, args.length - i);
        for(i = 0; i < inFileNames.length; i++)
            if(!(new File(inFileNames[i])).isFile())
            {
                System.err.println("'" + inFileNames[i] + "' is not a file");
                System.exit(1);
            }

        try
        {
            driver.convert(inFileNames, outFileName);
        }
        catch(Exception e)
        {
            String msg = e.getMessage();
            if(msg == null)
                msg = e.getClass().getName();
            if(e instanceof ParseException)
            {
                String fileName = ((ParseException)e).getFileName();
                if(fileName != null)
                    msg = "file '" + fileName + "', near line " + ((ParseException)e).getLineNumber() + ": " + msg;
            }
            System.err.println("conversion error: " + msg);
            System.exit(2);
        }
        Warning warnings[] = driver.getWarnings();
        for(i = 0; i < warnings.length; i++)
            System.err.println("conversion warning: " + warnings[i].getMessage());

    }

    private static void usage(Driver driver)
    {
        System.out.println("Usage: java fr.pixware.apt.convert.Driver ?options?\n       out_file in_file ?in_file? ...\nOptions are:\n  -v. Default: not verbose.\n  -toc. Default: no table of contents.\n  -index. Default: no index.\n  -nonum. Default: sections are numbered.\n  -meta <meta_key> <meta_value>. Default: none.\n  -pi <format> <pi_key> <pi_value>. Default: none.\n  -rule <src_ext> <dst_ext> <src_to_dst_rule>. Default: None.\n  -paper <paper>. Default: a4.\n  -lang <language>. Default: en.\n  -enc <encoding>. Default: " + FileUtil.defaultEncoding() + ".\n" + "  -? <ext>. Print info about converter or extractor\n" + "            associated to file extension <ext>.");
        String extensions[] = driver.getRegisteredExtractors();
        if(extensions.length > 0)
        {
            System.out.print("\nExtractors for: ");
            printExtensions(extensions);
        }
        extensions = driver.getRegisteredConverters();
        if(extensions.length > 0)
        {
            System.out.print("\nConverters for: ");
            printExtensions(extensions);
        }
        System.exit(1);
    }

    private static void printExtensions(String extensions[])
    {
        QuickSort.sort(extensions);
        for(int i = 0; i < extensions.length; i++)
        {
            if(i > 0)
                System.out.print(", ");
            System.out.print(extensions[i]);
        }

        System.out.print(".\n");
    }

    private static void usage(Driver driver, String extension)
    {
        String help = null;
        Converter converter = driver.getConverter(extension);
        if(converter != null)
        {
            help = converter.getConverterInfo();
        } else
        {
            Extractor extractor = driver.getExtractor(extension);
            if(extractor != null)
                help = extractor.getExtractorInfo();
        }
        if(help == null)
            help = "No info available about converter or extractor associated to file extension '" + extension + "'";
        System.out.println(help);
        System.exit(1);
    }

    static Class _mthclass$(String x0)
    {
        try
        {
            return Class.forName(x0);
        }
        catch(ClassNotFoundException x1)
        {
            throw new NoClassDefFoundError(x1.getMessage());
        }
    }

    public static final int UNIT_MM = 0;
    public static final int UNIT_INCH = 1;
    public static final Paper standardPapers[] = {
        new Paper("a0", "ISO A0", 0, 841D, 1189D), new Paper("a1", "ISO A1", 0, 594D, 841D), new Paper("a2", "ISO A2", 0, 420D, 594D), new Paper("a3", "ISO A3", 0, 297D, 420D), new Paper("a4", "ISO A4", 0, 210D, 297D), new Paper("a5", "ISO A5", 0, 148D, 210D), new Paper("a6", "ISO A6", 0, 105D, 148D), new Paper("a7", "ISO A7", 0, 74D, 105D), new Paper("a8", "ISO A8", 0, 52D, 114D), new Paper("a9", "ISO A9", 0, 37D, 52D), 
        new Paper("a10", "ISO A10", 0, 26D, 37D), new Paper("b0", "ISO B0", 0, 1000D, 1414D), new Paper("b1", "ISO B1", 0, 707D, 1000D), new Paper("b2", "ISO B2", 0, 500D, 707D), new Paper("b3", "ISO B3", 0, 353D, 500D), new Paper("b4", "ISO B4", 0, 250D, 353D), new Paper("b5", "ISO B5", 0, 176D, 250D), new Paper("b6", "ISO B6", 0, 125D, 176D), new Paper("b7", "ISO B7", 0, 88D, 127D), new Paper("b8", "ISO B8", 0, 62D, 88D), 
        new Paper("b9", "ISO B9", 0, 44D, 62D), new Paper("b10", "ISO B10", 0, 31D, 44D), new Paper("jisb0", "JIS B0", 0, 1030D, 1456D), new Paper("jisb1", "JIS B1", 0, 728D, 1030D), new Paper("jisb2", "JIS B2", 0, 515D, 728D), new Paper("jisb3", "JIS B3", 0, 364D, 515D), new Paper("jisb4", "JIS B4", 0, 257D, 364D), new Paper("jisb5", "JIS B5", 0, 182D, 257D), new Paper("jisb6", "JIS B6", 0, 128D, 182D), new Paper("jisb7", "JIS B7", 0, 91D, 128D), 
        new Paper("jisb8", "JIS B8", 0, 64D, 91D), new Paper("jisb9", "JIS B9", 0, 45D, 64D), new Paper("jisb10", "JIS B10", 0, 32D, 45D), new Paper("c0", "ISO C0", 0, 917D, 1297D), new Paper("c1", "ISO C1", 0, 648D, 917D), new Paper("c2", "ISO C2", 0, 458D, 648D), new Paper("c3", "ISO C3", 0, 324D, 458D), new Paper("c4", "ISO C4", 0, 229D, 324D), new Paper("c5", "ISO C5", 0, 162D, 229D), new Paper("c6", "ISO C6", 0, 114D, 162D), 
        new Paper("c7", "ISO C7", 0, 81D, 114D), new Paper("dl", "ISO DL", 0, 110D, 220D), new Paper("letter", "US Letter", 1, 8.5D, 11D), new Paper("legal", "US Legal", 1, 8.5D, 14D), new Paper("statement", "US Statement", 1, 5.5D, 8.5D), new Paper("tabloid", "US Tabloid", 1, 11D, 17D), new Paper("ledger", "US Ledger", 1, 17D, 11D), new Paper("7x9", "US 7x9", 1, 7D, 9D), new Paper("9x11", "US 9x11", 1, 9D, 11D), new Paper("9x12", "US 9x12", 1, 9D, 12D), 
        new Paper("10x13", "US 10x13", 1, 10D, 13D), new Paper("10x14", "US 10x14", 1, 10D, 14D), new Paper("executive", "US Executive 1", 1, 7.5D, 10D), new Paper("executive2", "US Executive 2", 1, 7.25D, 10.5D), new Paper("executive3", "US Executive 3", 1, 7.5D, 10.5D)
    };
    private Hashtable meta;
    private Hashtable rules;
    private Hashtable pi;
    private boolean verbose;
    private boolean toc;
    private boolean index;
    private boolean sectionsNumbered;
    private String paper;
    private String language;
    private String encoding;
    private Hashtable converters;
    private Hashtable extractors;
    private Vector warnings;
    private static final Class driverClassParam[];

    static 
    {
        driverClassParam = (new Class[] {
            fr.pixware.apt.convert.Driver.class
        });
    }
}
