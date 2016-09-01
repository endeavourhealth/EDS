package org.endeavourhealth.transform.ceg.models;

import org.apache.commons.csv.CSVPrinter;

import java.math.BigInteger;
import java.util.Date;

public class Appointment extends AbstractModel {

    private BigInteger serviceProviderId;
    private BigInteger patientId;
    private Date appointmentDate;
    private Date appointmentEndTime;
    private Boolean isCancelled;
    private String currentStatus;
    private Date arrivalTime;
    private Date seenTime;
    private Date dateAppointmentBooked;
    private String sessionHolder;
    private String sessionType;
    private String sessionLocation;


    @Override
    public void writeHeaderToCsv(CSVPrinter csvPrinter) throws Exception {
        printString("SK_ServiceProviderID", csvPrinter);
        printString("SK_PatientID", csvPrinter);
        printString("AppointmentDate", csvPrinter);
        printString("AppointmentStartTime", csvPrinter);
        printString("AppointmentEndTime", csvPrinter);
        printString("IsCancelled", csvPrinter);
        printString("CurrentStatus", csvPrinter);
        printString("ArrivalTime", csvPrinter);
        printString("SeenTime", csvPrinter);
        printString("DateAppointmentBooked", csvPrinter);
        printString("TimeAppointmentBooked", csvPrinter);
        printString("SessionHolder", csvPrinter);
        printString("SessionType", csvPrinter);
        printString("SessionLocation", csvPrinter);
    }

    @Override
    public void writeRecordToCsv(CSVPrinter csvPrinter) throws Exception {

        printBigInt(serviceProviderId, csvPrinter);
        printBigInt(patientId, csvPrinter);
        printDate(appointmentDate, csvPrinter);
        printTime(appointmentDate, csvPrinter);
        printTime(appointmentEndTime, csvPrinter);
        printBoolean(isCancelled, csvPrinter);
        printString(currentStatus, csvPrinter);
        printTime(arrivalTime, csvPrinter);
        printTime(seenTime, csvPrinter);
        printDate(dateAppointmentBooked, csvPrinter);
        printTime(dateAppointmentBooked, csvPrinter);
        printString(sessionHolder, csvPrinter);
        printString(sessionType, csvPrinter);
        printString(sessionLocation, csvPrinter);
    }

    @Override
    public BigInteger getServiceProviderId() {
        return serviceProviderId;
    }

    public void setServiceProviderId(BigInteger serviceProviderId) {
        this.serviceProviderId = serviceProviderId;
    }

    public BigInteger getPatientId() {
        return patientId;
    }

    public void setPatientId(BigInteger patientId) {
        this.patientId = patientId;
    }

    public Date getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(Date appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public Date getAppointmentEndTime() {
        return appointmentEndTime;
    }

    public void setAppointmentEndTime(Date appointmentEndTime) {
        this.appointmentEndTime = appointmentEndTime;
    }

    public Boolean getCancelled() {
        return isCancelled;
    }

    public void setCancelled(Boolean cancelled) {
        isCancelled = cancelled;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public Date getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Date arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Date getSeenTime() {
        return seenTime;
    }

    public void setSeenTime(Date seenTime) {
        this.seenTime = seenTime;
    }

    public Date getDateAppointmentBooked() {
        return dateAppointmentBooked;
    }

    public void setDateAppointmentBooked(Date dateAppointmentBooked) {
        this.dateAppointmentBooked = dateAppointmentBooked;
    }

    public String getSessionHolder() {
        return sessionHolder;
    }

    public void setSessionHolder(String sessionHolder) {
        this.sessionHolder = sessionHolder;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public String getSessionLocation() {
        return sessionLocation;
    }

    public void setSessionLocation(String sessionLocation) {
        this.sessionLocation = sessionLocation;
    }
}
