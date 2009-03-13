// 
// 
// 
// Source File Name:   QuickSort.java

package fr.pixware.util;

import java.io.PrintStream;

public class QuickSort
{
    public static class StringCompare
        implements Compare
    {

        public int compare(Object o1, Object o2)
        {
            return ((String)o1).compareTo((String)o2);
        }

        public StringCompare()
        {
        }
    }

    public static interface Compare
    {

        public abstract int compare(Object obj, Object obj1);
    }


    public QuickSort()
    {
    }

    public static void sort(String strings[])
    {
        sort(((Object []) (strings)), 0, strings.length - 1, ((Compare) (stringCompare)));
    }

    public static void sort(Object values[], Compare compare)
    {
        sort(values, 0, values.length - 1, compare);
    }

    public static void sort(Object values[], int first, int last, Compare compare)
    {
        int f = first;
        int l = last;
        if(last > first)
        {
            Object pivot = values[(first + last) / 2];
            while(f <= l) 
            {
                while(f < last && compare.compare(values[f], pivot) < 0) 
                    f++;
                for(; l > first && compare.compare(values[l], pivot) > 0; l--);
                if(f <= l)
                {
                    swap(values, f, l);
                    f++;
                    l--;
                }
            }
            if(first < l)
                sort(values, first, l, compare);
            if(f < last)
                sort(values, f, last, compare);
        }
    }

    private static void swap(Object values[], int i, int j)
    {
        Object temp = values[i];
        values[i] = values[j];
        values[j] = temp;
    }

    public static void main(String args[])
    {
        sort(args);
        for(int i = 0; i < args.length; i++)
            System.out.println(args[i]);

    }

    public static StringCompare stringCompare = new StringCompare();

}
