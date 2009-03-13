// 
// 
// 
// Source File Name:   AlphaNumerals.java

package fr.pixware.apt.convert;

import java.io.PrintStream;

public class AlphaNumerals
{

    public AlphaNumerals()
    {
    }

    public static String toString(int n)
    {
        return toString(n, false);
    }

    public static String toString(int n, boolean lowerCase)
    {
        StringBuffer alpha = new StringBuffer();
        char zeroLetter = lowerCase ? '`' : '@';
        for(; n > 0; n /= 27)
        {
            char letter = (char)(zeroLetter + n % 27);
            if(letter == zeroLetter)
                letter = '0';
            alpha.insert(0, letter);
        }

        return alpha.toString();
    }

    public static void main(String args[])
        throws NumberFormatException
    {
        for(int i = 0; i < args.length; i++)
        {
            String arg = args[i];
            System.out.println(arg + " = " + toString(Integer.parseInt(arg)));
        }

    }
}
