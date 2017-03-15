package org.endeavourhealth.patientexplorer.utility;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SearchTermsParser {
    private String nhsNumber;
    private String emisNumber;
    private Date dateOfBirth;
    private List<String> names = new ArrayList<>();

    public SearchTermsParser(String searchTerms) {
        if (StringUtils.isEmpty(searchTerms))
            return;

        String[] tokens = searchTerms.split(" ");

        for (String token : tokens) {
            if (StringUtils.isEmpty(token))
                continue;

            token = token.trim();

            //removes need to check for empty tokens when using the results of the parsed search term
            if (StringUtils.isEmpty(token)) {
                continue;
            }

            if (StringUtils.isNumeric(token)) {
                if (token.length() == 10)
                    this.nhsNumber = token;
                else
                    this.emisNumber = token;
            } else if ((token.length() == 10 || token.length() == 11) && StringUtils.countMatches(token,'-')==2) {
                // Assume its a date dd-MMM-yyyy & attempt to parse
                SimpleDateFormat sf = new SimpleDateFormat("dd-MMM-yyyy");
                try {
                    dateOfBirth = sf.parse(token);
                } catch (ParseException e) {
                    // Not a valid date, continue and treat as regular token
                }
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

    public boolean hasDateOfBirth() {
        return this.dateOfBirth != null;
    }

    public Date getDateOfBirth() {
        return this.dateOfBirth;
    }

    public List<String> getNames() {
        return names;
    }

    public SearchTermsParser setNames(List<String> names) {
        this.names = names;
        return this;
    }
}
