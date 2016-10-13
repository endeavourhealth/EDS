package org.endeavourhealth.core.xml;

import com.google.common.base.Strings;
import org.endeavourhealth.core.utility.XmlSerializer;
import org.endeavourhealth.core.xml.transformErrors.Arg;
import org.endeavourhealth.core.xml.transformErrors.ExceptionLine;
import org.endeavourhealth.core.xml.transformErrors.ObjectFactory;
import org.endeavourhealth.core.xml.transformErrors.TransformError;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Map;

public abstract class TransformErrorsSerializer {

    public static final String ARG_EMIS_CSV_FATAL_ERROR = "FatalError";
    public static final String ARG_EMIS_CSV_PROCESSING_ID = "ProcessingId";
    public static final String ARG_EMIS_CSV_FILE = "CsvFile";
    public static final String ARG_EMIS_CSV_RECORD_NUMBER = "Record";


    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    private static final String XSD = "TransformError.xsd";

    public static TransformError readFromXml(String xml) throws ParserConfigurationException, JAXBException, IOException, SAXException {
        return XmlSerializer.deserializeFromString(TransformError.class, xml, XSD);
    }

    public static String writeToXml(TransformError transformError) {
        JAXBElement element = OBJECT_FACTORY.createTransformError(transformError);
        return XmlSerializer.serializeToString(element, XSD);
    }

    public static void addError(TransformError container, Throwable ex, Map<String, String> args) {
        container.getErrors().add(createError(ex, args));
    }

    /**
     * utility fn for creataing content to go in the XML
     */
    private static org.endeavourhealth.core.xml.transformErrors.Error createError(Throwable ex, Map<String, String> args) {
        org.endeavourhealth.core.xml.transformErrors.Error ret = new org.endeavourhealth.core.xml.transformErrors.Error();

        for (String argName: args.keySet()) {
            String argValue = args.get(argName);

            Arg arg = new Arg();
            arg.setName(argName);
            arg.setValue(argValue);
            ret.getArg().add(arg);
        }

        if (ex != null) {
            ret.setException(createException(ex));
        }

        return ret;
    }

    private static org.endeavourhealth.core.xml.transformErrors.Exception createException(Throwable ex) {

        org.endeavourhealth.core.xml.transformErrors.Exception ret = new org.endeavourhealth.core.xml.transformErrors.Exception();

        String message = ex.getMessage();
        if (!Strings.isNullOrEmpty(message)) {
            ret.setMessage(message);
        }

        for (StackTraceElement element: ex.getStackTrace()) {
            ExceptionLine line = new ExceptionLine();
            ret.getLines().add(line);

            line.setClazz(element.getClassName());
            line.setMethod(element.getMethodName());
            line.setLine(new Integer(element.getLineNumber()));
        }

        if (ex.getCause() != null) {
            ret.setCause(createException(ex.getCause()));
        }

        return ret;
    }
}
