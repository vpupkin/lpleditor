// 
// 
// 
// Source File Name:   LinearHashtable.java

package fr.pixware.util;

import java.io.*;
import java.util.Enumeration;

public final class LinearHashtable
    implements Cloneable, Serializable
{
    private class ElementEnumeration
        implements Enumeration
    {

        public boolean hasMoreElements()
        {
            for(; index < table.length; index += 2)
                if(table[index] != null)
                    return true;

            return false;
        }

        public Object nextElement()
        {
            Object next;
            if(index < table.length)
            {
                next = table[index + 1];
                index += 2;
            } else
            {
                next = null;
            }
            return next;
        }

        private int index;

        private ElementEnumeration()
        {
            index = 0;
        }

    }

    private class KeyEnumeration
        implements Enumeration
    {

        public boolean hasMoreElements()
        {
            for(; index < table.length; index += 2)
                if(table[index] != null)
                    return true;

            return false;
        }

        public Object nextElement()
        {
            Object next;
            if(index < table.length)
            {
                next = table[index];
                index += 2;
            } else
            {
                next = null;
            }
            return next;
        }

        private int index;

        private KeyEnumeration()
        {
            index = 0;
        }

    }


    public LinearHashtable()
    {
        this(3);
    }

    public LinearHashtable(int capacity)
    {
        table = new Object[2 * capacity];
    }

    public int size()
    {
        int size = 0;
        for(int i = 0; i < table.length; i += 2)
            if(table[i] != null)
                size++;

        return size;
    }

    public boolean isEmpty()
    {
        return size() == 0;
    }

    public boolean contains(Object value)
    {
        for(int i = 1; i < table.length; i += 2)
            if(table[i] == value)
                return true;

        return false;
    }

    public boolean containsKey(Object key)
    {
        return get(key) != null;
    }

    public Object get(Object key)
    {
        int i = indexOf(table, key);
        return i >= 0 ? table[i + 1] : null;
    }

    private static int indexOf(Object table[], Object key)
    {
        int hash = key.hashCode();
        if(hash < 0)
            hash = -hash;
        int i0 = 2 * (hash % (table.length / 2));
        int i = i0;
        do
        {
            if(table[i] == null || table[i].equals(key))
                return i;
            if((i += 2) == table.length)
                i = 0;
        } while(i != i0);
        return -1;
    }

    public Object put(Object key, Object value)
    {
        int i = indexOf(table, key);
        if(i < 0)
        {
            Object newTable[] = new Object[2 * table.length];
            for(int j = 0; j < table.length; j += 2)
            {
                Object curKey = table[j];
                if(curKey != null)
                {
                    int k = indexOf(newTable, curKey);
                    newTable[k] = curKey;
                    newTable[k + 1] = table[j + 1];
                }
            }

            table = newTable;
            i = indexOf(table, key);
        }
        Object oldValue = table[i + 1];
        table[i] = key;
        table[i + 1] = value;
        return oldValue;
    }

    public Object remove(Object key)
    {
        int i = indexOf(table, key);
        if(i < 0 || table[i] == null)
            return null;
        Object oldValue = table[i + 1];
        table[i] = null;
        table[i + 1] = null;
        do
        {
            if((i += 2) == table.length)
                i = 0;
            if(table[i] == null)
                break;
            Object curKey = table[i];
            int j = indexOf(table, curKey);
            if(table[j] == null)
            {
                table[j] = curKey;
                table[j + 1] = table[i + 1];
                table[i] = null;
                table[i + 1] = null;
            }
        } while(true);
        return oldValue;
    }

    public void clear()
    {
        for(int i = 0; i < table.length; i++)
            table[i] = null;

    }

    public Object clone()
    {
        LinearHashtable copy;
        try
        {
            copy = (LinearHashtable)super.clone();
        }
        catch(CloneNotSupportedException cannotHappen)
        {
            cannotHappen.printStackTrace();
            return null;
        }
        copy.table = new Object[table.length];
        System.arraycopy(((Object) (table)), 0, ((Object) (copy.table)), 0, table.length);
        return copy;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getClass().getName());
        buffer.append('[');
        for(int i = 0; i < table.length; i += 2)
            if(table[i] != null)
            {
                if(i > 0)
                    buffer.append(',');
                buffer.append(table[i]);
                buffer.append('=');
                buffer.append(table[i + 1]);
            }

        buffer.append(']');
        return buffer.toString();
    }

    public void copyKeysInto(Object array[])
    {
        int count = 0;
        for(int i = 0; i < table.length; i += 2)
            if(table[i] != null)
                array[count++] = table[i];

    }

    public void copyElementsInto(Object array[])
    {
        int count = 0;
        for(int i = 0; i < table.length; i += 2)
            if(table[i] != null)
                array[count++] = table[i + 1];

    }

    public Enumeration keys()
    {
        return new KeyEnumeration();
    }

    public Enumeration elements()
    {
        return new ElementEnumeration();
    }

    private void writeObject(ObjectOutputStream stream)
        throws IOException
    {
        int length = table.length;
        stream.writeInt(length);
        stream.writeInt(2 * size());
        for(int i = 0; i < length; i += 2)
            if(table[i] != null)
            {
                stream.writeObject(table[i]);
                stream.writeObject(table[i + 1]);
            }

    }

    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException
    {
        table = new Object[stream.readInt()];
        int count = stream.readInt();
        for(int i = 0; i < count; i += 2)
        {
            Object key = stream.readObject();
            Object value = stream.readObject();
            int index = indexOf(table, key);
            table[index] = key;
            table[index + 1] = value;
        }

    }

    private transient Object table[];

}
