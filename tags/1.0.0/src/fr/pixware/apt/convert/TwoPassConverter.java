// 
// 
// 
// Source File Name:   TwoPassConverter.java

package fr.pixware.apt.convert;

import fr.pixware.apt.parse.*;

// Referenced classes of package fr.pixware.apt.convert:
//            OnePassConverter, StructureSink, Driver, Structure

public abstract class TwoPassConverter extends OnePassConverter
{

    public TwoPassConverter(Driver driver)
    {
        super(driver);
        structure = null;
    }

    public void convert(String inFileNames[], String outFileName)
        throws Exception
    {
        String enc = getDriver().getEncoding();
        MultiFileSource doc = new MultiFileSource(inFileNames, enc);
        structure = new StructureSink();
        Parser parser = new Parser();
        try
        {
            parser.parse(doc, structure);
        }
        catch(Exception e)
        {
            throw new ParseException(e, parser.getSourceName(), parser.getSourceLineNumber());
        }
        finally
        {
            doc.close();
            doc = null;
        }
        super.convert(inFileNames, outFileName);
    }

    protected Structure getStructure()
    {
        return structure;
    }

    private StructureSink structure;
}
