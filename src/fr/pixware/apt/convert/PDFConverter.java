// 
// 
// 
// Source File Name:   PDFConverter.java

package fr.pixware.apt.convert;


// Referenced classes of package fr.pixware.apt.convert:
//            Converter, Driver, PSConverter

public class PDFConverter
    implements Converter
{

    public PDFConverter(Driver driver)
    {
        this.driver = driver;
    }

    public Driver getDriver()
    {
        return driver;
    }

    public String getConverterInfo()
    {
        return "Uses LaTeX as its typesetter: requires a recent TeX\ndistribution with latex, makeindex, dvips.\nAlso requires ps2pdf, Aladdin Ghostscript PostScript\nto PDF translator.\n\nSupported processing instructions (PI):\n  * pdf.pass1=latex to dvi command template.\n    Default: \"latex doc\"\n  * pdf.pass2=dvi to ps command template.\n    Default: \"dvips -o doc.ps doc\"\n  * pdf.pass3=ps to pdf command template.\n    Default: \"ps2pdf doc.ps %O\"\n";
    }

    public void convert(String inFileNames[], String outFileName)
        throws Exception
    {
        String pass1 = driver.getPI("pdf", "pass1");
        if(pass1 == null)
            pass1 = "latex doc";
        String pass2 = driver.getPI("pdf", "pass2");
        if(pass2 == null)
            pass2 = "dvips -o doc.ps doc";
        String pass3 = driver.getPI("pdf", "pass3");
        if(pass3 == null)
            pass3 = "ps2pdf doc.ps %O";
        PSConverter.doConvert(inFileNames, outFileName, driver, pass1, pass2, pass3);
    }

    private Driver driver;
    private static final String DEFAULT_PASS1 = "latex doc";
    private static final String DEFAULT_PASS2 = "dvips -o doc.ps doc";
    private static final String DEFAULT_PASS3 = "ps2pdf doc.ps %O";
}
