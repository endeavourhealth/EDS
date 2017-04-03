
package org.endeavourhealth.core.xml.transformError;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.endeavourhealth.core.xml.transformError package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _TransformError_QNAME = new QName("", "transformError");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.endeavourhealth.core.xml.transformError
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link TransformError }
     * 
     */
    public TransformError createTransformError() {
        return new TransformError();
    }

    /**
     * Create an instance of {@link Exception }
     * 
     */
    public Exception createException() {
        return new Exception();
    }

    /**
     * Create an instance of {@link ExceptionLine }
     * 
     */
    public ExceptionLine createExceptionLine() {
        return new ExceptionLine();
    }

    /**
     * Create an instance of {@link Arg }
     * 
     */
    public Arg createArg() {
        return new Arg();
    }

    /**
     * Create an instance of {@link Error }
     * 
     */
    public Error createError() {
        return new Error();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TransformError }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "transformError")
    public JAXBElement<TransformError> createTransformError(TransformError value) {
        return new JAXBElement<TransformError>(_TransformError_QNAME, TransformError.class, null, value);
    }

}
