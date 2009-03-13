// 
// 
// 
// Source File Name:   PropertySet.java

package fr.pixware.util;

import java.util.Enumeration;

// Referenced classes of package fr.pixware.util:
//            LinearHashtable

public abstract class PropertySet
{
    private static class PropertyEnumeration
        implements Enumeration
    {

        public boolean hasMoreElements()
        {
            return keys.hasMoreElements();
        }

        public Object nextElement()
        {
            String key = (String)keys.nextElement();
            keyValuePair[0] = key;
            keyValuePair[1] = properties.get(key);
            return ((Object) (keyValuePair));
        }

        private LinearHashtable properties;
        private Enumeration keys;
        private Object keyValuePair[];

        public PropertyEnumeration(LinearHashtable properties)
        {
            keyValuePair = new Object[2];
            this.properties = properties;
            keys = properties.keys();
        }
    }


    public PropertySet()
    {
        properties = null;
    }

    public Object putProperty(Object key, Object value)
    {
        if(properties == null)
            properties = new LinearHashtable();
        return properties.put(key, value);
    }

    public Object removeProperty(Object key)
    {
        if(properties == null)
            return null;
        Object value = properties.remove(key);
        if(properties.size() == 0)
            properties = null;
        return value;
    }

    public void removeAllProperties()
    {
        properties = null;
    }

    public boolean hasProperty(Object key)
    {
        return properties != null ? properties.get(key) != null : false;
    }

    public Object getProperty(Object key)
    {
        return properties != null ? properties.get(key) : null;
    }

    public int getPropertyCount()
    {
        return properties != null ? properties.size() : 0;
    }

    public Enumeration getProperties()
    {
        if(properties == null)
            return NO_PROPERTY_ENUMERATION;
        else
            return new PropertyEnumeration(properties);
    }

    private static final Enumeration NO_PROPERTY_ENUMERATION = new PropertyEnumeration(new LinearHashtable());
    protected LinearHashtable properties;

}
