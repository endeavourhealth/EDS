package org.endeavourhealth.fhir.cron.utilities;

public class FHIRAuditConstants {
    //CRON Constants
    public static final String SCHEDULER_TIME_HOUR = "fhir.scheduler.hour";
    public static final String SCHEDULER_TIME_MINUTE = "fhir.scheduler.minute";

    //AWS Constants
    public static final String ENVIRONMENT_AWS_FLAG = "environment.aws.flag";
    public static final String S3_PATH = "s3.path";
    public static final String S3_FILE_PREFIX = "s3.file.prefix";
    public static final String LOCAL_TMP_PATH = "local.tmp.path";
    public static final String FILE_NAME = "file.name";
}
