// 
// 
// 
// Source File Name:   StringUtil.java

package fr.pixware.util;

import java.io.PrintStream;

public class StringUtil
{

    public StringUtil()
    {
    }

    public static int indexOf(String strings[], String string)
    {
        for(int i = 0; i < strings.length; i++)
            if(strings[i].equals(string))
                return i;

        return -1;
    }

    public static boolean contains(String strings[], String string)
    {
        for(int i = 0; i < strings.length; i++)
            if(strings[i].equals(string))
                return true;

        return false;
    }

    public static String[] insertAt(String strings[], String string, int index)
    {
        String newStrings[] = new String[strings.length + 1];
        if(index > 0)
            System.arraycopy(strings, 0, newStrings, 0, index);
        int tail = strings.length - index;
        if(tail > 0)
            System.arraycopy(strings, index, newStrings, index + 1, tail);
        newStrings[index] = string;
        return newStrings;
    }

    public static String[] prepend(String strings[], String string)
    {
        String newStrings[] = new String[strings.length + 1];
        newStrings[0] = string;
        System.arraycopy(strings, 0, newStrings, 1, strings.length);
        return newStrings;
    }

    public static String[] append(String strings[], String string)
    {
        String newStrings[] = new String[strings.length + 1];
        System.arraycopy(strings, 0, newStrings, 0, strings.length);
        newStrings[strings.length] = string;
        return newStrings;
    }

    public static String[] remove(String strings[], String string)
    {
        int index = indexOf(strings, string);
        if(index < 0)
            return strings;
        else
            return removeAt(strings, index);
    }

    public static String[] removeAt(String strings[], int index)
    {
        String string = strings[index];
        String newStrings[] = new String[strings.length - 1];
        if(index > 0)
            System.arraycopy(strings, 0, newStrings, 0, index);
        int first = index + 1;
        int tail = strings.length - first;
        if(tail > 0)
            System.arraycopy(strings, first, newStrings, index, tail);
        return newStrings;
    }

    public static String protect(String string)
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append('"');
        escape(string, buffer);
        buffer.append('"');
        return buffer.toString();
    }

    public static String escape(String string)
    {
        StringBuffer buffer = new StringBuffer();
        escape(string, buffer);
        return buffer.toString();
    }

    private static void escape(String string, StringBuffer buffer)
    {
        int length = string.length();
        for(int i = 0; i < length; i++)
        {
            char c = string.charAt(i);
            switch(c)
            {
            case 8: // '\b'
                buffer.append('\\');
                buffer.append('b');
                break;

            case 9: // '\t'
                buffer.append('\\');
                buffer.append('t');
                break;

            case 10: // '\n'
                buffer.append('\\');
                buffer.append('n');
                break;

            case 12: // '\f'
                buffer.append('\\');
                buffer.append('f');
                break;

            case 13: // '\r'
                buffer.append('\\');
                buffer.append('r');
                break;

            case 34: // '"'
                buffer.append('\\');
                buffer.append('"');
                break;

            case 39: // '\''
                buffer.append('\\');
                buffer.append('\'');
                break;

            case 92: // '\\'
                buffer.append('\\');
                buffer.append('\\');
                break;

            default:
                if(c >= ' ' && c <= '~')
                {
                    buffer.append(c);
                    break;
                }
                buffer.append("\\u");
                String hex = Integer.toString(c, 16);
                for(int hexLength = hex.length(); hexLength < 4; hexLength++)
                    buffer.append('0');

                buffer.append(hex);
                break;
            }
        }

    }

    public static String unprotect(String string)
    {
        int length = string.length();
        if(length >= 2 && string.charAt(0) == '"' && string.charAt(length - 1) == '"')
            return unescape(string, 1, length - 2);
        else
            return unescape(string, 0, length);
    }

    public static String unescape(String string)
    {
        return unescape(string, 0, string.length());
    }

    private static String unescape(String string, int offset, int length)
    {
        StringBuffer buffer = new StringBuffer();
        int end = offset + length;
        for(int i = offset; i < end; i++)
        {
            char c = string.charAt(i);
label0:
            switch(c)
            {
            case 92: // '\\'
                if(i + 1 == end)
                {
                    buffer.append(c);
                    break;
                }
                switch(string.charAt(i + 1))
                {
                case 98: // 'b'
                    buffer.append('\b');
                    i++;
                    break label0;

                case 116: // 't'
                    buffer.append('\t');
                    i++;
                    break label0;

                case 110: // 'n'
                    buffer.append('\n');
                    i++;
                    break label0;

                case 102: // 'f'
                    buffer.append('\f');
                    i++;
                    break label0;

                case 114: // 'r'
                    buffer.append('\r');
                    i++;
                    break label0;

                case 34: // '"'
                    buffer.append('"');
                    i++;
                    break label0;

                case 39: // '\''
                    buffer.append('\'');
                    i++;
                    break label0;

                case 92: // '\\'
                    buffer.append('\\');
                    i++;
                    break label0;

                case 117: // 'u'
                    if(i + 5 >= end)
                        break label0;
                    try
                    {
                        int escaped = Integer.parseInt(string.substring(i + 2, i + 6), 16);
                        buffer.append((char)escaped);
                        i += 5;
                    }
                    catch(NumberFormatException numberformatexception)
                    {
                        buffer.append(c);
                    }
                    break;

                default:
                    buffer.append(c);
                    break;
                }
                break;

            default:
                buffer.append(c);
                break;
            }
        }

        return buffer.toString();
    }

    public static final void main(String args[])
    {
        for(int i = 0; i < args.length; i++)
        {
            String arg = args[i];
            String arg2 = protect(arg);
            System.out.print("'");
            System.out.print(arg);
            System.out.print("' = ");
            System.out.print(arg2);
            System.out.print(" = ");
            System.out.println(unprotect(arg2));
        }

    }

    public static String capitalize(String string)
    {
        int length = string.length();
        if(length == 0)
            return string;
        if(length == 1)
            return string.toUpperCase();
        else
            return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }

    public static String uncapitalize(String string)
    {
        int length = string.length();
        if(length == 0)
            return string;
        if(length == 1)
            return string.toLowerCase();
        else
            return Character.toLowerCase(string.charAt(0)) + string.substring(1);
    }

    public static String[] split(String string, char separatorChar)
    {
        int elementCount = 0;
        int sep;
        for(sep = 0; (sep = string.indexOf(separatorChar, sep)) >= 0; sep++)
            elementCount++;

        String elements[] = new String[++elementCount];
        elementCount = 0;
        int i;
        for(sep = 0; (i = string.indexOf(separatorChar, sep)) >= 0; sep = i + 1)
            elements[elementCount++] = sep != i ? string.substring(sep, i) : "";

        elements[elementCount++] = string.substring(sep);
        return elements;
    }

    public static String join(String strings[], char separatorChar)
    {
        StringBuffer buffer = new StringBuffer();
        int stringCount = strings.length;
        if(stringCount > 0)
        {
            buffer.append(strings[0]);
            for(int i = 1; i < stringCount; i++)
            {
                buffer.append(separatorChar);
                buffer.append(strings[i]);
            }

        }
        return buffer.toString();
    }

    public static String join(String strings[], String separator)
    {
        StringBuffer buffer = new StringBuffer();
        int stringCount = strings.length;
        if(stringCount > 0)
        {
            buffer.append(strings[0]);
            for(int i = 1; i < stringCount; i++)
            {
                buffer.append(separator);
                buffer.append(strings[i]);
            }

        }
        return buffer.toString();
    }

    public static String replaceAll(String string, String oldSub, String newSub)
    {
        StringBuffer replaced = new StringBuffer();
        int oldSubLength = oldSub.length();
        int begin;
        int i;
        for(begin = 0; (i = string.indexOf(oldSub, begin)) >= 0; begin = i + oldSubLength)
        {
            if(i > begin)
                replaced.append(string.substring(begin, i));
            replaced.append(newSub);
        }

        if(begin < string.length())
            replaced.append(string.substring(begin));
        return replaced.toString();
    }

    public static final String EMPTY_LIST[] = new String[0];

}
