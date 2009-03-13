// 
// 
// 
// Source File Name:   AnchorNotFoundWarning.java

package fr.pixware.apt.convert;


// Referenced classes of package fr.pixware.apt.convert:
//            Warning

public class AnchorNotFoundWarning
    implements Warning
{

    public AnchorNotFoundWarning(String linkText, String fileName, int lineNumber)
    {
        this.linkText = linkText;
        this.fileName = fileName;
        this.lineNumber = lineNumber;
    }

    public String getLinkText()
    {
        return linkText;
    }

    public String getSourceFileName()
    {
        return fileName;
    }

    public int getSourceLineNumber()
    {
        return lineNumber;
    }

    public String getMessage()
    {
        return "file " + fileName + ", near line " + lineNumber + ": anchor '" + linkText + "' not found";
    }

    private String linkText;
    private String fileName;
    private int lineNumber;
}
