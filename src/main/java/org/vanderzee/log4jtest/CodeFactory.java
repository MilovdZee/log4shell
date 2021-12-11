package org.vanderzee.log4jtest;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import java.io.Serializable;
import java.util.Hashtable;

public class CodeFactory implements ObjectFactory, Serializable {
    @Override
    public Object getObjectInstance(Object object, Name name, Context context, Hashtable<?, ?> hashtable) {
        return "Remote factory created...";
    }
}
