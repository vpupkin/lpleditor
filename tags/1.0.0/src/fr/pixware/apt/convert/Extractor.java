// 
// 
// 
// Source File Name:   Extractor.java

package fr.pixware.apt.convert;


public interface Extractor
{

    public abstract String getExtractorInfo();

    public abstract void extract(String s, String s1)
        throws Exception;
}
