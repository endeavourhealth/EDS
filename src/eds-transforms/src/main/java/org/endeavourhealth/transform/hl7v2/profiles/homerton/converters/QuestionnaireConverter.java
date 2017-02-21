package org.endeavourhealth.transform.hl7v2.profiles.homerton.converters;

import org.endeavourhealth.transform.hl7v2.profiles.homerton.datatypes.Zqa;
import org.endeavourhealth.transform.hl7v2.transform.TransformException;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.List;

public class QuestionnaireConverter {

    public static List<Questionnaire.QuestionComponent> convert(List<Zqa> questionField) throws TransformException {
        List<Questionnaire.QuestionComponent> questions = new ArrayList<>();

        for (Zqa zqa : questionField)
            if (zqa != null) {
                Questionnaire.QuestionComponent qc = new Questionnaire.QuestionComponent();
                qc.setId(zqa.getQuestionIdentifier());
                qc.setText(zqa.getQuestionValue());
                qc.setType(Questionnaire.AnswerFormat.CHOICE);
                qc.setLinkId(zqa.getQuestionLabel());
                questions.add(qc);
            }

        return questions;
    }

}
