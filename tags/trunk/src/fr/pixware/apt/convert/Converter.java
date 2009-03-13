// 
// 
// 
// Source File Name:   Converter.java

package fr.pixware.apt.convert;


public interface Converter
{

    public abstract String getConverterInfo();

    public abstract void convert(String as[], String s)
        throws Exception;
}
