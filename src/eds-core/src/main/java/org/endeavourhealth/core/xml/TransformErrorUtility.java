package org.endeavourhealth.core.xml;

import com.google.common.base.Strings;
import org.endeavourhealth.core.data.audit.AuditRepository;
import org.endeavourhealth.core.data.audit.models.ExchangeTransformAudit;
import org.endeavourhealth.core.xml.transformError.Arg;
import org.endeavourhealth.core.xml.transformError.Error;
import org.endeavourhealth.core.xml.transformError.ExceptionLine;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;
import java.util.Map;

public class TransformErrorUtility {

    private static final Logger LOG = LoggerFactory.getLogger(TransformErrorUtility.class);

    //general-purpose arguments
    public static final String ARG_FATAL_ERROR = "FatalError";
    public static final String ARG_WAITING = "WaitingForPreviousErrorToClear";

    //transform-specific arguments
    //public static final String ARG_EMIS_CSV_DIRECTORY= "CsvDirectory";
    public static final String ARG_EMIS_CSV_FILE = "CsvFile";
    public static final String ARG_EMIS_CSV_RECORD_NUMBER = "Record";


    public static void addTransformError(TransformError transformError, Throwable ex, Map<String, String> args) {
        transformError.getError().add(createError(ex, args));
    }

    /*public static void addTransformError(ExchangeTransformAudit transformAudit, Throwable ex, Map<String, String> args) {

        TransformError container = null;
        if (transformAudit.getErrorXml() == null) {
            container = new TransformError();
        } else {
            try {
                container = TransformErrorSerializer.readFromXml(transformAudit.getErrorXml());
            } catch (Exception xmlException) {
                LOG.error("Error parsing XML " + transformAudit.getErrorXml(), xmlException);
            }
        }

        container.getError().add(createError(ex, args));

        transformAudit.setErrorXml(TransformErrorSerializer.writeToXml(container));
    }*/

    public static void save(ExchangeTransformAudit transformAudit, TransformError errors) {

        transformAudit.setErrorXml(TransformErrorSerializer.writeToXml(errors));
        new AuditRepository().save(transformAudit);
    }

    /**
     * utility fn for creataing content to go in the XML
     */
    private static org.endeavourhealth.core.xml.transformError.Error createError(Throwable ex, Map<String, String> args) {
        org.endeavourhealth.core.xml.transformError.Error ret = new org.endeavourhealth.core.xml.transformError.Error();

        try {
            GregorianCalendar c = new GregorianCalendar();
            XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
            ret.setDatetime(xmlDate);
        } catch (DatatypeConfigurationException calendarException) {
            LOG.error("Failed to create XML date time ", calendarException);
        }

        for (Map.Entry<String,String> entry : args.entrySet()) {
            Arg arg = new Arg();
            arg.setName(entry.getKey());
            arg.setValue(entry.getValue());
            ret.getArg().add(arg);
        }

        if (ex != null) {
            ret.setException(createException(ex));
        }

        return ret;
    }

    private static org.endeavourhealth.core.xml.transformError.Exception createException(Throwable ex) {

        org.endeavourhealth.core.xml.transformError.Exception ret = new org.endeavourhealth.core.xml.transformError.Exception();

        String message = ex.getMessage();
        if (!Strings.isNullOrEmpty(message)) {
            ret.setMessage(message);
        }

        for (StackTraceElement element: ex.getStackTrace()) {
            ExceptionLine line = new ExceptionLine();
            ret.getLine().add(line);

            line.setClazz(element.getClassName());
            line.setMethod(element.getMethodName());
            line.setLine(Integer.valueOf(element.getLineNumber()));
        }

        if (ex.getCause() != null) {
            ret.setCause(createException(ex.getCause()));
        }

        return ret;
    }

    public static boolean containsArgument(Error error, String argName) {
        for (Arg arg: error.getArg()) {
            if (arg.getName().equals(argName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsArgument(TransformError errors, String argName) {
        for (Error error: errors.getError()) {
            if (containsArgument(error, argName)) {
                return true;
            }
        }
        return false;
    }

    public static String findArgumentValue(Error error, String argName) {
        for (Arg arg: error.getArg()) {
            if (arg.getName().equals(argName)) {
                return arg.getValue();
            }
        }
        return null;
    }
}
