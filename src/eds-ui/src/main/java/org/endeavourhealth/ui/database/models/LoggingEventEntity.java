package org.endeavourhealth.ui.database.models;

import javax.persistence.*;

/**
 * Created by darren on 04/08/16.
 */
@Entity
@Table(name = "logging_event", schema = "\"public\"", catalog = "logback")
public class LoggingEventEntity {
    private long timestmp;
    private String formattedMessage;
    private String loggerName;
    private String levelString;
    private String threadName;
    private Short referenceFlag;
    private String arg0;
    private String arg1;
    private String arg2;
    private String arg3;
    private String callerFilename;
    private String callerClass;
    private String callerMethod;
    private String callerLine;
    private long eventId;

    @Basic
    @Column(name = "timestmp")
    public long getTimestmp() {
        return timestmp;
    }

    public void setTimestmp(long timestmp) {
        this.timestmp = timestmp;
    }

    @Basic
    @Column(name = "formatted_message")
    public String getFormattedMessage() {
        return formattedMessage;
    }

    public void setFormattedMessage(String formattedMessage) {
        this.formattedMessage = formattedMessage;
    }

    @Basic
    @Column(name = "logger_name")
    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    @Basic
    @Column(name = "level_string")
    public String getLevelString() {
        return levelString;
    }

    public void setLevelString(String levelString) {
        this.levelString = levelString;
    }

    @Basic
    @Column(name = "thread_name")
    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    @Basic
    @Column(name = "reference_flag")
    public Short getReferenceFlag() {
        return referenceFlag;
    }

    public void setReferenceFlag(Short referenceFlag) {
        this.referenceFlag = referenceFlag;
    }

    @Basic
    @Column(name = "arg0")
    public String getArg0() {
        return arg0;
    }

    public void setArg0(String arg0) {
        this.arg0 = arg0;
    }

    @Basic
    @Column(name = "arg1")
    public String getArg1() {
        return arg1;
    }

    public void setArg1(String arg1) {
        this.arg1 = arg1;
    }

    @Basic
    @Column(name = "arg2")
    public String getArg2() {
        return arg2;
    }

    public void setArg2(String arg2) {
        this.arg2 = arg2;
    }

    @Basic
    @Column(name = "arg3")
    public String getArg3() {
        return arg3;
    }

    public void setArg3(String arg3) {
        this.arg3 = arg3;
    }

    @Basic
    @Column(name = "caller_filename")
    public String getCallerFilename() {
        return callerFilename;
    }

    public void setCallerFilename(String callerFilename) {
        this.callerFilename = callerFilename;
    }

    @Basic
    @Column(name = "caller_class")
    public String getCallerClass() {
        return callerClass;
    }

    public void setCallerClass(String callerClass) {
        this.callerClass = callerClass;
    }

    @Basic
    @Column(name = "caller_method")
    public String getCallerMethod() {
        return callerMethod;
    }

    public void setCallerMethod(String callerMethod) {
        this.callerMethod = callerMethod;
    }

    @Basic
    @Column(name = "caller_line")
    public String getCallerLine() {
        return callerLine;
    }

    public void setCallerLine(String callerLine) {
        this.callerLine = callerLine;
    }

    @Id
    @Column(name = "event_id")
    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LoggingEventEntity that = (LoggingEventEntity) o;

        if (timestmp != that.timestmp) return false;
        if (eventId != that.eventId) return false;
        if (formattedMessage != null ? !formattedMessage.equals(that.formattedMessage) : that.formattedMessage != null)
            return false;
        if (loggerName != null ? !loggerName.equals(that.loggerName) : that.loggerName != null) return false;
        if (levelString != null ? !levelString.equals(that.levelString) : that.levelString != null) return false;
        if (threadName != null ? !threadName.equals(that.threadName) : that.threadName != null) return false;
        if (referenceFlag != null ? !referenceFlag.equals(that.referenceFlag) : that.referenceFlag != null)
            return false;
        if (arg0 != null ? !arg0.equals(that.arg0) : that.arg0 != null) return false;
        if (arg1 != null ? !arg1.equals(that.arg1) : that.arg1 != null) return false;
        if (arg2 != null ? !arg2.equals(that.arg2) : that.arg2 != null) return false;
        if (arg3 != null ? !arg3.equals(that.arg3) : that.arg3 != null) return false;
        if (callerFilename != null ? !callerFilename.equals(that.callerFilename) : that.callerFilename != null)
            return false;
        if (callerClass != null ? !callerClass.equals(that.callerClass) : that.callerClass != null) return false;
        if (callerMethod != null ? !callerMethod.equals(that.callerMethod) : that.callerMethod != null) return false;
        if (callerLine != null ? !callerLine.equals(that.callerLine) : that.callerLine != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (timestmp ^ (timestmp >>> 32));
        result = 31 * result + (formattedMessage != null ? formattedMessage.hashCode() : 0);
        result = 31 * result + (loggerName != null ? loggerName.hashCode() : 0);
        result = 31 * result + (levelString != null ? levelString.hashCode() : 0);
        result = 31 * result + (threadName != null ? threadName.hashCode() : 0);
        result = 31 * result + (referenceFlag != null ? referenceFlag.hashCode() : 0);
        result = 31 * result + (arg0 != null ? arg0.hashCode() : 0);
        result = 31 * result + (arg1 != null ? arg1.hashCode() : 0);
        result = 31 * result + (arg2 != null ? arg2.hashCode() : 0);
        result = 31 * result + (arg3 != null ? arg3.hashCode() : 0);
        result = 31 * result + (callerFilename != null ? callerFilename.hashCode() : 0);
        result = 31 * result + (callerClass != null ? callerClass.hashCode() : 0);
        result = 31 * result + (callerMethod != null ? callerMethod.hashCode() : 0);
        result = 31 * result + (callerLine != null ? callerLine.hashCode() : 0);
        result = 31 * result + (int) (eventId ^ (eventId >>> 32));
        return result;
    }
}
