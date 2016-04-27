package org.endeavourhealth.messaging.utilities;

public class ReflectionHelper
{
    public static Object instantiateObject(String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        return Thread.currentThread().getContextClassLoader().loadClass(className).newInstance();
    }
}
