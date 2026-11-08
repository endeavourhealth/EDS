package org.endeavourhealth.queuereader.routines;

import org.endeavourhealth.common.ods.OdsOrganisation;
import org.endeavourhealth.common.ods.OdsWebService;
import org.endeavourhealth.common.utility.ThreadPool;
import org.endeavourhealth.common.utility.ThreadPoolError;
import org.hl7.fhir.instance.model.Resource;

import javax.xml.bind.ValidationException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import static org.endeavourhealth.common.ods.OdsWebService.lookupOrganisationViaRest;
import static org.endeavourhealth.transform.common.AbstractCsvCallable.handleErrors;

public class SD929 {

    public static void testOrganisationLookup(String threads, String odsCode, String runsPerThread) throws Exception {
        int RPT = Integer.parseInt(runsPerThread);
        ThreadPool threadPool = new ThreadPool(Integer.parseInt(threads), 1000);

        for(int i = 0; i < Integer.parseInt(threads); i++) {
            CallableTest callable = new CallableTest(odsCode, RPT);
            List<ThreadPoolError> errors = threadPool.submit(callable);
            handleErrors(errors);
        }
    }
    public static void testMultiOrgLookup(String threads, String runsPerThread) throws Exception{
        int RPT = Integer.parseInt(runsPerThread);
        ThreadPool threadPool = new ThreadPool(Integer.parseInt(threads), 1000);

        for(int i = 0; i < Integer.parseInt(threads); i++) {
            CallableTest callable = new CallableTest(selectRandomOdsCode(), RPT);
            List<ThreadPoolError> errors = threadPool.submit(callable);
            handleErrors(errors);
        }
    }

    private static String selectRandomOdsCode() {
        Random rand = new Random();
        int idx = rand.nextInt(999);
        return odsCodes.orgOdsCodes[idx];
    }

    public static void testOrganisationLookup(String threads, String odsCode) throws Exception {

        ThreadPool threadPool = new ThreadPool(Integer.parseInt(threads), 1000);

        for(int i = 0; i < Integer.parseInt(threads); i++) {
            CallableTest callable = new CallableTest(odsCode, 1);
            List<ThreadPoolError> errors = threadPool.submit(callable);
            handleErrors(errors);
        }
    }

}

class CallableTest extends AbstractRoutine implements Callable<OdsOrganisation> {

    private String odsCode;
    private int callsToMake;

    public CallableTest(String odsCode, int callsToMake){
        this.odsCode = odsCode;
        this.callsToMake = callsToMake;
    }

    @Override
    public OdsOrganisation call() throws Exception {
        for(int i = 0; i < callsToMake; i++) {
            System.out.println("Calling to api");
            OdsOrganisation organisation = OdsWebService.lookupOrganisationViaRest(odsCode);
            validateOrgData(organisation);
            System.out.println("Organisation pulled from API: " + organisation.getOdsCode());
        }
        return null;
    }

    private void validateOrgData(OdsOrganisation organisation) throws ValidationException {
        StringBuilder errorMsg = new StringBuilder("");
        String expectedName = "NHS NORTH EAST LONDON INTEGRATED CARE BOARD";
        if (!organisation.getOrganisationName().equals(expectedName)){
            errorMsg.append("wrong organisation name returned for org: ")
                    .append(odsCode)
                    .append("\n expected: ")
                    .append(expectedName)
                    .append("actual")
                    .append(organisation.getOrganisationName());
        }
        String parentName = "LONDON COMMISSIONING REGION";
            if (!organisation.getParents().get("Y56").getOrganisationName().equals(parentName)) {
                errorMsg.append("\nwrong parent returned for org: ")
                        .append(odsCode)
                        .append("\n expected: ")
                        .append(parentName)
                        .append("actual")
                        .append(organisation.getParents().get("Y56").getOrganisationName());
            }
        int activeRoles = 2;
            if (organisation.getOrganisationTypes().size() != 2){
                errorMsg.append("\norg: ")
                        .append(odsCode)
                        .append("\n had an unexpected number of roles \nexpected number: ")
                        .append(activeRoles)
                        .append("actual")
                        .append(organisation.getOrganisationTypes().size());
            }
            if (!errorMsg.toString().isEmpty()){
                throw new ValidationException(errorMsg.toString());
            }

    }
}
