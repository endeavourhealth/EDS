package org.endeavourhealth.transform.ceg.models;

import java.util.Date;

public class Appointment {

    private long serviceProviderId;
    private long patientId;
    private Date appointmentDate;
    private Date appointmentTime;
    private Date appointmentEndTime;
    private boolean isCancelled;
    private String currentStatus;
    private Date arrivalTime;
    private Date seenTime;
    private Date dateAppointmentBooked;
    private Date timeAppointmentBooked;
    private String sessionHolder;
    private String sessionType;
    private String sessionLocation;


    public long getServiceProviderId() {
        return serviceProviderId;
    }

    public void setServiceProviderId(long serviceProviderId) {
        this.serviceProviderId = serviceProviderId;
    }

    public long getPatientId() {
        return patientId;
    }

    public void setPatientId(long patientId) {
        this.patientId = patientId;
    }

    public Date getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(Date appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public Date getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(Date appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public Date getAppointmentEndTime() {
        return appointmentEndTime;
    }

    public void setAppointmentEndTime(Date appointmentEndTime) {
        this.appointmentEndTime = appointmentEndTime;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean cancelled) {
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

    public Date getTimeAppointmentBooked() {
        return timeAppointmentBooked;
    }

    public void setTimeAppointmentBooked(Date timeAppointmentBooked) {
        this.timeAppointmentBooked = timeAppointmentBooked;
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
