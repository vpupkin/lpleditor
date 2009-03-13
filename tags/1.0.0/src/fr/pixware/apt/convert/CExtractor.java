// 
// 
// 
// Source File Name:   CExtractor.java

package fr.pixware.apt.convert;

import java.io.PrintStream;

// Referenced classes of package fr.pixware.apt.convert:
//            SimpleExtractor, Driver

public class CExtractor extends SimpleExtractor
{

    public CExtractor()
    {
        this(null);
    }

    public CExtractor(Driver driver)
    {
        super(driver, new String[] {
            "/*x", "/**", "//x"
        }, new String[] {
            "", "*", "//"
        }, new String[] {
            "*/", "*/", "//"
        });
    }

    public String getExtractorInfo()
    {
        StringBuffer info = new StringBuffer();
        info.append(super.getExtractorInfo());
        info.append("Supported processing instructions (PI):\n");
        info.append("  * c.verbatim=plain|box. Default: plain.\n");
        return info.toString();
    }

    protected boolean isVerbatimBox()
    {
        Driver driver = getDriver();
        if(driver != null)
        {
            String pi = driver.getPI("c", "verbatim");
            if(pi != null && pi.equals("box"))
                return true;
        }
        return false;
    }

    public static void main(String args[])
    {
        if(args.length != 2)
        {
            System.out.println("usage: java fr.pixware.apt.convert.CExtractor c_file apt_file");
            System.exit(1);
        }
        CExtractor extractor = new CExtractor();
        try
        {
            extractor.extract(args[0], args[1]);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.err.println("extraction error: " + e.getMessage());
            System.exit(2);
        }
    }
}
