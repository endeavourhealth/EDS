package org.endeavourhealth.coreui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonDsaBenefit {
    private String uuid = null;
    private String dataSharingAgreementUuid = null;
    private String title = null;
    private String detail = null;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDataSharingAgreementUuid() {
        return dataSharingAgreementUuid;
    }

    public void setDataSharingAgreementUuid(String dataSharingAgreementUuid) {
        this.dataSharingAgreementUuid = dataSharingAgreementUuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
