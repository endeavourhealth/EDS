package org.endeavourhealth.transform.ui.models.resources.clinicial;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.transform.ui.models.resources.admin.UIPractitioner;
import org.endeavourhealth.transform.ui.models.types.UICode;
import org.endeavourhealth.transform.ui.models.types.UIDate;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UIProblem extends UICondition {
    private Integer expectedDuration;
    private UIDate lastReviewDate;
    private UIPractitioner lastReviewer;
    private UICode significance;
    private UIProblem relatedProblem;
    private String relationshipType;

    public UICode getSignificance() {
        return significance;
    }

    public UIProblem setSignificance(UICode significance) {
        this.significance = significance;
        return this;
    }

    public Integer getExpectedDuration() {
        return expectedDuration;
    }

    public UIProblem setExpectedDuration(Integer expectedDuration) {
        this.expectedDuration = expectedDuration;
        return this;
    }

    public UIDate getLastReviewDate() {
        return lastReviewDate;
    }

    public UIProblem setLastReviewDate(UIDate lastReviewDate) {
        this.lastReviewDate = lastReviewDate;
        return this;
    }

    public UIPractitioner getLastReviewer() {
        return lastReviewer;
    }

    public UIProblem setLastReviewer(UIPractitioner lastReviewer) {
        this.lastReviewer = lastReviewer;
        return this;
    }

    public UIProblem getRelatedProblem() {
        return relatedProblem;
    }

    public UIProblem setRelatedProblem(UIProblem relatedProblem) {
        this.relatedProblem = relatedProblem;
        return this;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public UIProblem setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
        return this;
    }
}
