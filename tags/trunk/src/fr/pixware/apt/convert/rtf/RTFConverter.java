// 
// 
// 
// Source File Name:   RTFConverter.java

package fr.pixware.apt.convert.rtf;

import fr.pixware.apt.convert.Driver;
import fr.pixware.apt.convert.OnePassConverter;
import fr.pixware.apt.parse.ParseException;
import fr.pixware.apt.parse.Sink;
import fr.pixware.util.FileUtil;
import java.io.*;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.StringTokenizer;

// Referenced classes of package fr.pixware.apt.convert.rtf:
//            RTFSink

public class RTFConverter extends OnePassConverter
{
    private class InnerSink extends RTFSink
    {

        public void body()
            throws ParseException
        {
            sectionNumber.reset();
            super.body();
        }

        public void section1()
            throws ParseException
        {
            sectionNumber.increment(1);
            super.section1();
        }

        public void section2()
            throws ParseException
        {
            sectionNumber.increment(2);
            super.section2();
        }

        public void section3()
            throws ParseException
        {
            sectionNumber.increment(3);
            super.section3();
        }

        public void section4()
            throws ParseException
        {
            sectionNumber.increment(4);
            super.section4();
        }

        public void section5()
            throws ParseException
        {
            sectionNumber.increment(5);
            super.section5();
        }

        public void sectionTitle()
            throws ParseException
        {
            super.sectionTitle();
            if(sectionNumbering)
                super.writer.print(sectionNumber.format('.') + "  ");
        }

        public void figureGraphics(String name)
            throws ParseException
        {
            try
            {
                convertGraphics(name, "ppm");
            }
            catch(Exception e)
            {
                e.printStackTrace();
                System.exit(2);
            }
            String dir = FileUtil.fileDirName(outFileName);
            name = FileUtil.fileBaseName(name);
            String path;
            if(dir != null)
                path = dir + File.separatorChar + name;
            else
                path = name;
            super.figureGraphics(path);
        }

        private String outFileName;
        private boolean sectionNumbering;
        private SectionNumber sectionNumber;

        public InnerSink(String outFileName, String encoding)
            throws IOException
        {
            this(((OutputStream) (new FileOutputStream(outFileName))), encoding);
            this.outFileName = outFileName;
        }

        public InnerSink(OutputStream output, String encoding)
            throws IOException
        {
            super(output, encoding);
            sectionNumber = new SectionNumber();
            sectionNumbering = getDriver().isSectionsNumbered();
        }
    }

    private static class SectionNumber
    {

        void reset()
        {
            for(int i = 0; i < number.length; i++)
                number[i] = 0;

        }

        void increment(int level)
        {
            int i = level - 1;
            number[i]++;
            for(int j = i + 1; j < number.length; j++)
                number[j] = 0;

        }

        String format(char separator)
        {
            StringBuffer buf = new StringBuffer();
            for(int i = 0; i < number.length && number[i] > 0; i++)
            {
                if(i > 0)
                    buf.append(separator);
                buf.append(Integer.toString(number[i]));
            }

            return buf.toString();
        }

        private int number[];

        private SectionNumber()
        {
            number = new int[5];
        }

    }

    private static class Encoding
    {

        String name;
        int codePage;
        int charSet;

        Encoding(String name, int codePage, int charSet)
        {
            this.name = name;
            this.codePage = codePage;
            this.charSet = charSet;
        }
    }

    private static class Paper
    {

        String name;
        double width;
        double height;

        Paper(String name, double width, double height)
        {
            this.name = name;
            this.width = width;
            this.height = height;
        }
    }


    public RTFConverter(Driver driver)
    {
        super(driver);
    }

    public String getConverterInfo()
    {
        StringBuffer info = new StringBuffer();
        info.append("Supported Options:\n");
        info.append("  -paper");
        for(int i = 0; i < papers.length; i++)
            info.append(" " + papers[i].name);

        info.append(" <w>x<h> (cm)\n");
        info.append("  -enc");
        for(int i = 0; i < encodings.length; i++)
            info.append(" " + encodings[i].name);

        info.append("\n");
        info.append("\nProcessing Instructions:\n");
        info.append("  rtf.topmargin=<cm>");
        info.append(" (" + format(2D, 1) + ")\n");
        info.append("  rtf.bottommargin=<cm>");
        info.append(" (" + format(2D, 1) + ")\n");
        info.append("  rtf.leftmargin=<cm>");
        info.append(" (" + format(2D, 1) + ")\n");
        info.append("  rtf.rightmargin=<cm>");
        info.append(" (" + format(2D, 1) + ")\n");
        info.append("  rtf.fontsize=<pts>");
        info.append(" (10)\n");
        info.append("  rtf.spacing=<pts>");
        info.append(" (10)\n");
        info.append("  rtf.resolution=<dpi>");
        info.append(" (72)\n");
        info.append("  rtf.imagetype={palette|rgb}");
        info.append(" (palette)\n");
        info.append("  rtf.imagedataformat={ascii|raw}");
        info.append(" (ascii)\n");
        return info.toString();
    }

    private static String format(double v, int n)
    {
        NumberFormat fmt = NumberFormat.getInstance(Locale.ENGLISH);
        fmt.setMinimumFractionDigits(n);
        fmt.setMaximumFractionDigits(n);
        return fmt.format(v);
    }

    protected Sink createSink(String outFileName)
        throws Exception
    {
        Driver driver = getDriver();
        Encoding encoding = getEncoding();
        Paper paper = getPaper();
        InnerSink sink = new InnerSink(outFileName, encoding.name);
        sink.setCodePage(encoding.codePage);
        sink.setCharSet(encoding.charSet);
        sink.setPaperSize(paper.width, paper.height);
        String pi = driver.getPI("rtf", "topmargin");
        if(pi != null)
            try
            {
                double margin = Double.valueOf(pi).doubleValue();
                if(margin >= 0.0D)
                    sink.setTopMargin(margin);
            }
            catch(NumberFormatException numberformatexception) { }
        pi = driver.getPI("rtf", "bottommargin");
        if(pi != null)
            try
            {
                double margin = Double.valueOf(pi).doubleValue();
                if(margin >= 0.0D)
                    sink.setBottomMargin(margin);
            }
            catch(NumberFormatException numberformatexception1) { }
        pi = driver.getPI("rtf", "leftmargin");
        if(pi != null)
            try
            {
                double margin = Double.valueOf(pi).doubleValue();
                if(margin >= 0.0D)
                    sink.setLeftMargin(margin);
            }
            catch(NumberFormatException numberformatexception2) { }
        pi = driver.getPI("rtf", "rightmargin");
        if(pi != null)
            try
            {
                double margin = Double.valueOf(pi).doubleValue();
                if(margin >= 0.0D)
                    sink.setRightMargin(margin);
            }
            catch(NumberFormatException numberformatexception3) { }
        pi = driver.getPI("rtf", "fontsize");
        if(pi != null)
            try
            {
                int size = Integer.valueOf(pi).intValue();
                if(size > 0)
                    sink.setFontSize(size);
            }
            catch(NumberFormatException numberformatexception4) { }
        pi = driver.getPI("rtf", "spacing");
        if(pi != null)
            try
            {
                int spacing = Integer.valueOf(pi).intValue();
                if(spacing >= 0)
                    sink.setSpacing(spacing);
            }
            catch(NumberFormatException numberformatexception5) { }
        pi = driver.getPI("rtf", "resolution");
        if(pi != null)
            try
            {
                int resolution = Integer.valueOf(pi).intValue();
                if(resolution > 0)
                    sink.setResolution(resolution);
            }
            catch(NumberFormatException numberformatexception6) { }
        pi = driver.getPI("rtf", "imagetype");
        if(pi != null)
            sink.setImageType(pi);
        pi = driver.getPI("rtf", "imagedataformat");
        if(pi != null)
            sink.setImageDataFormat(pi);
        return sink;
    }

    private Paper getPaper()
        throws IllegalArgumentException
    {
        String paper = getDriver().getPaper();
        for(int i = 0; i < papers.length; i++)
            if(papers[i].name.equals(paper))
                return papers[i];

        StringTokenizer size = new StringTokenizer(paper, "x", true);
        double width;
        double height;
        try
        {
            width = Double.valueOf(size.nextToken()).doubleValue();
            if(!size.nextToken().equals("x"))
                throw new Exception();
            height = Double.valueOf(size.nextToken()).doubleValue();
        }
        catch(Exception exception)
        {
            throw new IllegalArgumentException("invalid paper size \"" + paper + "\"");
        }
        return new Paper(null, width, height);
    }

    private Encoding getEncoding()
        throws IllegalArgumentException
    {
        String encoding = getDriver().getEncoding();
        for(int i = 0; i < encodings.length; i++)
            if(encodings[i].name.equals(encoding))
                return encodings[i];

        throw new IllegalArgumentException("unsupported encoding \"" + encoding + "\"");
    }

    protected Sink createSink(Writer out)
        throws Exception
    {
        return null;
    }

    protected void convertGraphics(String name, String extension)
        throws Exception
    {
        super.convertGraphics(name, extension);
    }

    private static final Paper papers[] = {
        new Paper("a3", 29.699999999999999D, 42D), new Paper("a4", 21D, 29.699999999999999D), new Paper("a5", 14.800000000000001D, 21D), new Paper("b4", 25D, 35.299999999999997D), new Paper("b5", 17.600000000000001D, 25D), new Paper("executive", 19.050000000000001D, 25.399999999999999D), new Paper("ledger", 43.18D, 27.940000000000001D), new Paper("legal", 21.59D, 35.560000000000002D), new Paper("letter", 21.59D, 27.940000000000001D), new Paper("tabloid", 27.940000000000001D, 43.18D)
    };
    private static final Encoding encodings[] = {
        new Encoding("ASCII", 1252, 0), new Encoding("Cp1250", 1250, 238), new Encoding("Cp1251", 1251, 204), new Encoding("Cp1252", 1252, 0), new Encoding("ISO8859_1", 1252, 0)
    };

}
