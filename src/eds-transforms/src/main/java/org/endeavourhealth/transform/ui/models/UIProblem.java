package org.endeavourhealth.transform.ui.models;

public class UIProblem extends UICondition {
    private UICode significance;

    public UICode getSignificance() {
        return significance;
    }

    public UIProblem setSignificance(UICode significance) {
        this.significance = significance;
        return this;
    }
}
