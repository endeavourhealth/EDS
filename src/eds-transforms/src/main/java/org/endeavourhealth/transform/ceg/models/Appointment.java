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



    /**
    // * 	[SK_ServiceProviderID] [int] NOT NULL,
    // [SK_PatientID] [int] NOT NULL,
    // [AppointmentDate] [date] NOT NULL,
     [AppointmentStartTime] [time](0) NOT NULL,
     [AppointmentEndTime] [time](0) NOT NULL,
     [IsCancelled] [bit] NOT NULL,
     [CurrentStatus] [varchar](20) NOT NULL,
     [ArrivalTime] [time](0) NULL,
     [SeenTime] [time](0) NULL,
     [DateAppointmentBooked] [date] NULL,
     [TimeAppointmentBooked] [time](0) NULL,
     [SessionHolder] [varchar](100) NULL,
     [SessionType] [varchar](20) NULL,
     [SessionLocation] [varchar](50) NULL
     */
}
