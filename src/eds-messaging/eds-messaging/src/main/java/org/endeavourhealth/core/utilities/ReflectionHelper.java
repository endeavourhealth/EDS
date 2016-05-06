package org.endeavourhealth.core.utilities;

public class ReflectionHelper
{
    public static Object instantiateObject(String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        return Thread.currentThread().getContextClassLoader().loadClass(className).newInstance();
    }
}
