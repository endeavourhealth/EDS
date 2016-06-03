package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.ui.database.administration.DbEndUser;
import org.endeavourhealth.ui.database.execution.DbJob;
import org.endeavourhealth.ui.database.execution.DbRequest;
import org.endeavourhealth.ui.requestParameters.RequestParametersSerializer;
import org.endeavourhealth.ui.requestParameters.models.RequestParameters;

import java.util.Date;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonReportRequest {

    private UUID uuid = null;
    private Date date = null;
    private String status = null;
    private UUID endUserUuid = null;
    private String endUserName = null;
    private RequestParameters parameters = null;

    public JsonReportRequest() { }

    public JsonReportRequest(DbRequest request, DbJob job, DbEndUser user) throws Exception {

        String xml = request.getParameters();
        String status = "Scheduled";
        if (job != null) {
            status = job.getStatusId().toString();
        }
        String userName = user.getForename() + " " + user.getSurname();

        this.uuid = request.getRequestUuid();
        this.date = new Date(request.getTimeStamp().toEpochMilli());
        this.status = status;
        this.endUserUuid = request.getEndUserUuid();
        this.endUserName = userName;
        this.parameters = RequestParametersSerializer.readFromXml(xml);
    }

    /**
     * gets/sets
     */
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getEndUserName() {
        return endUserName;
    }

    public void setEndUserName(String endUserName) {
        this.endUserName = endUserName;
    }

    public UUID getEndUserUuid() {
        return endUserUuid;
    }

    public void setEndUserUuid(UUID endUserUuid) {
        this.endUserUuid = endUserUuid;
    }

    public RequestParameters getParameters() {
        return parameters;
    }

    public void setParameters(RequestParameters parameters) {
        this.parameters = parameters;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
