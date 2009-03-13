// 
// 
// 
// Source File Name:   FontMetrics.java

package fr.pixware.apt.convert.rtf;


class FontMetrics
{
    static class CharMetrics
    {

        short wx;
        short wy;
        short llx;
        short lly;
        short urx;
        short ury;

        CharMetrics(int wx, int wy, int llx, int lly, int urx, int ury)
        {
            this.wx = (short)wx;
            this.wy = (short)wy;
            this.llx = (short)llx;
            this.lly = (short)lly;
            this.urx = (short)urx;
            this.ury = (short)ury;
        }
    }


    FontMetrics(boolean fixedPitch, int ascent, int descent, CharMetrics bounds, CharMetrics metrics[])
    {
        this.fixedPitch = fixedPitch;
        this.ascent = (short)ascent;
        this.descent = (short)descent;
        this.bounds = bounds;
        charMetrics = metrics;
    }

    static FontMetrics find(int style)
        throws Exception
    {
        String s = (fr.pixware.apt.convert.rtf.FontMetrics.class).getName();
        String packageName = s.substring(0, s.lastIndexOf('.'));
        StringBuffer buf = new StringBuffer(packageName + ".");
        switch(style)
        {
        case 0: // '\0'
        default:
            buf.append("Serif");
            break;

        case 1: // '\001'
            buf.append("SerifItalic");
            break;

        case 2: // '\002'
            buf.append("SerifBold");
            break;

        case 3: // '\003'
            buf.append("Monospace");
            break;
        }
        String className = buf.toString();
        Class classObject = Class.forName(className);
        return (FontMetrics)classObject.newInstance();
    }

    boolean fixedPitch;
    short ascent;
    short descent;
    CharMetrics bounds;
    CharMetrics charMetrics[];
}
