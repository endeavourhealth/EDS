package org.endeavourhealth.transform.hl7v2.profiles.homerton;


import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.profiles.homerton.converters.QuestionnaireConverter;
import org.endeavourhealth.transform.hl7v2.profiles.homerton.segments.ZqaSegment;
import org.endeavourhealth.transform.hl7v2.transform.TransformException;
import org.hl7.fhir.instance.model.*;

public class QuestionnaireTransform {

    public static Questionnaire fromHl7v2(ZqaSegment source) throws ParseException, TransformException {
        Questionnaire questionnaire = new Questionnaire();

        questionnaire.addIdentifier(new Identifier().setValue(source.getQuestionnaireId()));
        Questionnaire.GroupComponent group = new Questionnaire.GroupComponent();

        for (Questionnaire.QuestionComponent question : QuestionnaireConverter.convert(source.getQuestionAndAnswer())) {
            group.addQuestion(question);
        }

        questionnaire.setGroup(group);

        return questionnaire;

    }

}
