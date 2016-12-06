package org.endeavourhealth.patientexplorer.utility;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SearchTermsParser {
    private String nhsNumber;
    private String emisNumber;
    private List<String> names = new ArrayList<>();

    public SearchTermsParser(String searchTerms) {
        if (StringUtils.isEmpty(searchTerms))
            return;

        String[] tokens = searchTerms.split(" ");

        for (String token : tokens) {
            if (StringUtils.isEmpty(token))
                continue;

            token = token.trim();

            if (StringUtils.isNumeric(token)) {
                if (token.length() == 10)
                    this.nhsNumber = token;
                else
                    this.emisNumber = token;
            }

            this.names.add(token);
        }
    }

    public boolean hasNhsNumber() {
        return StringUtils.isNotBlank(this.nhsNumber);
    }

    public String getNhsNumber() {
        return nhsNumber;
    }

    public SearchTermsParser setNhsNumber(String nhsNumber) {
        this.nhsNumber = nhsNumber;
        return this;
    }

    public boolean hasEmisNumber() {
        return StringUtils.isNotBlank(this.emisNumber);
    }

    public String getEmisNumber() {
        return emisNumber;
    }

    public SearchTermsParser setEmisNumber(String emisNumber) {
        this.emisNumber = emisNumber;
        return this;
    }

    public List<String> getNames() {
        return names;
    }

    public SearchTermsParser setNames(List<String> names) {
        this.names = names;
        return this;
    }
}
