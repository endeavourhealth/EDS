package org.endeavourhealth.core.data.logging.models;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.Date;
import java.util.UUID;

@Table(keyspace = "logging", name = "logging_event")
public class LoggingEvent {


    @Column(name = "timestmp")
    private Date timestmp = null;
    @Column(name = "formatted_message")
    private String formattedMessage = null;
    @Column(name = "logger_name")
    private String loggerName = null;
    @Column(name = "level_string")
    private String level = null;
    @Column(name = "thread_name")
    private String threadName = null;
    @Column(name = "reference_flag")
    private Integer referenceFlag = null;
    @Column(name = "arg0")
    private String arg0 = null;
    @Column(name = "arg1")
    private String arg1 = null;
    @Column(name = "arg2")
    private String arg2 = null;
    @Column(name = "arg3")
    private String arg3 = null;
    @Column(name = "caller_filename")
    private String callerFilename = null;
    @Column(name = "caller_class")
    private String callerClass = null;
    @Column(name = "caller_method")
    private String callerMethod = null;
    @Column(name = "caller_line")
    private Integer callerLine = null;
    @PartitionKey
    @Column(name = "event_id")
    private UUID eventId;


    public Date getTimestmp() {
        return timestmp;
    }

    public void setTimestmp(Date timestmp) {
        this.timestmp = timestmp;
    }

    public String getFormattedMessage() {
        return formattedMessage;
    }

    public void setFormattedMessage(String formattedMessage) {
        this.formattedMessage = formattedMessage;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public Integer getReferenceFlag() {
        return referenceFlag;
    }

    public void setReferenceFlag(Integer referenceFlag) {
        this.referenceFlag = referenceFlag;
    }

    public String getArg0() {
        return arg0;
    }

    public void setArg0(String arg0) {
        this.arg0 = arg0;
    }

    public String getArg1() {
        return arg1;
    }

    public void setArg1(String arg1) {
        this.arg1 = arg1;
    }

    public String getArg2() {
        return arg2;
    }

    public void setArg2(String arg2) {
        this.arg2 = arg2;
    }

    public String getArg3() {
        return arg3;
    }

    public void setArg3(String arg3) {
        this.arg3 = arg3;
    }

    public String getCallerFilename() {
        return callerFilename;
    }

    public void setCallerFilename(String callerFilename) {
        this.callerFilename = callerFilename;
    }

    public String getCallerClass() {
        return callerClass;
    }

    public void setCallerClass(String callerClass) {
        this.callerClass = callerClass;
    }

    public String getCallerMethod() {
        return callerMethod;
    }

    public void setCallerMethod(String callerMethod) {
        this.callerMethod = callerMethod;
    }

    public Integer getCallerLine() {
        return callerLine;
    }

    public void setCallerLine(Integer callerLine) {
        this.callerLine = callerLine;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }
}
