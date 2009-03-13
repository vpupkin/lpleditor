// 
// 
// 
// Source File Name:   Font.java

package fr.pixware.apt.convert.rtf;


// Referenced classes of package fr.pixware.apt.convert.rtf:
//            FontMetrics

public class Font
{
    static class TextExtents
    {

        int width;
        int height;
        int ascent;

        TextExtents(int width, int height, int ascent)
        {
            this.width = width;
            this.height = height;
            this.ascent = ascent;
        }
    }


    public Font(int style, int size)
        throws Exception
    {
        this.style = style;
        this.size = size;
        metrics = FontMetrics.find(style);
    }

    public int ascent()
    {
        return toTwips(metrics.ascent);
    }

    public int descent()
    {
        return toTwips(metrics.descent);
    }

    public TextExtents textExtents(String text)
    {
        int width = 0;
        int ascent = 0;
        int descent = 0;
        int i = 0;
        for(int n = text.length(); i < n; i++)
        {
            char c = text.charAt(i);
            if(c > '\377')
                c = ' ';
            FontMetrics.CharMetrics metrics = this.metrics.charMetrics[c];
            width += metrics.wx;
            if(metrics.ury > ascent)
                ascent = metrics.ury;
            if(metrics.lly < descent)
                descent = metrics.lly;
        }

        int height = ascent + Math.abs(descent);
        return new TextExtents(toTwips(width), toTwips(height), toTwips(ascent));
    }

    private int toTwips(int length)
    {
        return (int)Math.rint(((double)length * (double)size) / 50D);
    }

    private int style;
    private int size;
    private FontMetrics metrics;
}
