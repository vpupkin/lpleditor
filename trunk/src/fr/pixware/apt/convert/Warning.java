// 
// 
// 
// Source File Name:   Warning.java

package fr.pixware.apt.convert;


public interface Warning
{

    public abstract String getSourceFileName();

    public abstract int getSourceLineNumber();

    public abstract String getMessage();
}
