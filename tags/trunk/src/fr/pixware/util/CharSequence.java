// 
// 
// 
// Source File Name:   CharSequence.java

package fr.pixware.util;


public interface CharSequence
{

    public abstract int length();

    public abstract char charAt(int i);

    public abstract CharSequence subSequence(int i, int j);

    public abstract String toString();
}
