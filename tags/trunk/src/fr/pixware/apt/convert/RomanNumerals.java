// 
// 
// 
// Source File Name:   RomanNumerals.java

package fr.pixware.apt.convert;

import java.io.PrintStream;

public class RomanNumerals
{

    public RomanNumerals()
    {
    }

    public static String toString(int n)
    {
        return toString(n, false);
    }

    public static String toString(int n, boolean lowerCase)
    {
        StringBuffer roman = new StringBuffer();
        String letters[] = lowerCase ? LOWER_CASE_LETTERS : UPPER_CASE_LETTERS;
        for(int i = 0; i < NUMBERS.length; i++)
            while(n >= NUMBERS[i]) 
            {
                roman.append(letters[i]);
                n -= NUMBERS[i];
            }

        return roman.toString();
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

    private static final int NUMBERS[] = {
        1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 
        5, 4, 1
    };
    private static final String UPPER_CASE_LETTERS[] = {
        "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", 
        "V", "IV", "I"
    };
    private static final String LOWER_CASE_LETTERS[] = {
        "m", "cm", "d", "cd", "c", "xc", "l", "xl", "x", "ix", 
        "v", "iv", "i"
    };

}
