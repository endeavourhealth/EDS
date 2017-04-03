package org.endeavourhealth.transform.terminology;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Test script at https://gitlab.com/noesisinformatica/termlex-tutorial/blob/master/src/etc/termlex-test.sh
 */
public class Snomed {

    private static final Logger LOG = LoggerFactory.getLogger(Snomed.class);
    private static final String TERMLEX = "http://termlex.org/";

    private static LinkedList<Client> webClients = new LinkedList<>(); //cache of web clients
    private static JCS cachedTermsForConceptAndDescription = null;
    private static JCS cachedDescendantForConcept = null;

    /**
     * initialise terminology caches
     */
    static {

        try {

            //by default the Java Caching System has a load of logging enabled, which is really slow, so turn it off
            //not longer required, since it no longer uses log4J and the new default doesn't have debug enabled
            /*org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("org.apache.jcs");
            logger.setLevel(org.apache.log4j.Level.OFF);*/

            cachedTermsForConceptAndDescription = JCS.getInstance("SnomedTermsForConceptAndDescription");
            cachedDescendantForConcept = JCS.getInstance("SnomedDescendantsForConcept");
        } catch (CacheException ex) {
            throw new RuntimeException("Error initialising caches", ex);
        }
    }

    public static String getTerm(long conceptId, long descriptionId) throws Exception {

        String cacheKey = conceptId + "/" + descriptionId;
        String term = (String)cachedTermsForConceptAndDescription.get(cacheKey);
        if (term == null) {
            JsonElement json = executeTermlexGet("concepts/" + conceptId + "?flavour=ID_DESCRIPTIONS_RELATIONSHIPS");
            JsonObject jsonObj = json.getAsJsonObject();

            JsonArray jsonArr = jsonObj.get("descriptions").getAsJsonArray();
            for (JsonElement jsonElement : jsonArr) {
                JsonObject jsonDescriptionObj = jsonElement.getAsJsonObject();
                long jsonDescriptionId = jsonDescriptionObj.get("id").getAsLong();
                if (jsonDescriptionId == descriptionId) {
                    term = jsonDescriptionObj.get("term").getAsString();
                    cachedTermsForConceptAndDescription.put(cacheKey, term);
                    break;
                }
            }
        }

        //if still null, then something is wrong
        if (term == null) {
            throw new RuntimeException("Failed to find Snomed term for concept " + conceptId + " and description " + descriptionId);
        }

        return term;
    }

    /*ublic static HashSet<String> enumerateConcepts(CodeSet codeSet) {

        HashSet<String> includedConcepts = new HashSet<>();
        HashSet<String> excludedConcepts = new HashSet<>();

        List<CodeSetValue> values = codeSet.getCodeSetValue();
        for (CodeSetValue value: values) {
            enumerateCodeSetValue(value, includedConcepts, excludedConcepts);
        }

        includedConcepts.removeAll(excludedConcepts);
        return includedConcepts;
    }

    private static void enumerateCodeSetValue(CodeSetValue value, HashSet<String> included, HashSet<String> excluded) {
        String concept = value.getCode();
        if (value.isIncludeChildren()) {
            List<String> descendants = getDescendantsUsingCache(concept);
            included.addAll(descendants);
        }
        else {
            included.add(concept);
        }

        HashSet<String> exclusionsOfExclusions = new HashSet<>();

        List<CodeSetValue> exclusions = value.getExclusion();
        for (CodeSetValue exclusion: exclusions) {
            enumerateCodeSetValue(exclusion, excluded, exclusionsOfExclusions);
        }

        if (exclusionsOfExclusions.size() > 0) {
            throw new RuntimeException("CodeSet exclusions of exclusions not supports");
        }
    }*/

    /**
     * returns all descendant codes
     */
    public static List<Long> getDescendants(long conceptCode) throws Exception {
        List<Long> descendants = (List<Long>)cachedDescendantForConcept.get(Long.valueOf(conceptCode));
        if (descendants == null) {
            JsonElement json = executeTermlexGet("hierarchy/" + conceptCode + "/descendants");
            descendants = getCodesFromJsonArray(json);
            cachedDescendantForConcept.put(Long.valueOf(conceptCode), descendants);
        }

        return descendants;

    }

    /**
     * returns the direct child concepts
     */
    public static List<Long> getChildren(Long conceptCode) {
        JsonElement json = executeTermlexGet("hierarchy/" + conceptCode + "/children");
        return getCodesFromJsonArray(json);
    }

    /**
     * returns the preferred term for the concept
     */
    public static String getPreferredTerm(String conceptCode) {
        JsonElement json = executeTermlexGet("concepts/" + conceptCode);
        if (json == null) {
            return null;
        }
        JsonObject obj = json.getAsJsonObject();
        JsonElement termObj = obj.get("preferredTerm");
        return termObj.getAsString();
    }

    private static List<Long> getCodesFromJsonArray(JsonElement jsonElement) {
        List<Long> ret = new ArrayList<>();

        JsonArray array = jsonElement.getAsJsonArray();
        for (int i=0; i<array.size(); i++) {
            JsonElement child = array.get(i);
            Long childCode = child.getAsLong();
            ret.add(childCode);
        }

        return ret;
    }

    private synchronized static Client borrowClient() {
        try {
            return webClients.pop();
        } catch (NoSuchElementException nsee) {
        }

        return ClientBuilder.newClient();
    }
    private synchronized static void returnClient(Client client) {
        webClients.push(client);
    }

    private static JsonElement executeTermlexGet(String path) {

        String fullPath = TERMLEX + path;
        Client client = borrowClient();
        WebTarget target = client.target(fullPath);

        Invocation.Builder request = target.request();
        request.accept(MediaType.APPLICATION_JSON);

        Response response = null;
        try {
            response = request.get();
        } catch (Exception e) {
            throw new RuntimeException("Error performing termlex query to " + fullPath, e);
        }

        if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            String s = response.readEntity(String.class);
            returnClient(client); //only return on HTTP success

            JsonParser parser = new JsonParser();
            return parser.parse(s);
        } else {
            throw new RuntimeException("Error performing termlex query to " + fullPath + " - status code " + response.getStatus());
        }
    }

    public static boolean isProcedureCode(Long snomedConceptId) {
        //TODO - work out if Snomed concept is procedure
        return false;
    }

    /**
     * for quick testing
     */
/*public static void main(String[] args) {

    String concept = null;
    if (args.length > 0) {
        concept = args[0];
    } else {
        concept = javax.swing.JOptionPane.showInputDialog(null, "Concept ID", "195967001"); //asthma
        if (concept == null) {
            return;
        }
    }

    //test basic Termlex functions
    LOG.debug("Preferred term of " + concept + ": " + getPreferredTerm(concept));

    LOG.debug("Children of " + concept);
    List<String> v = getChildren(concept);
    dumpWithDesc(v);

    LOG.debug("Descendants of " + concept);
    v = getDescendants(concept);
    dumpWithDesc(v);

    //test processing of QueryDocument objects
    CodeSet cs = new CodeSet();

    CodeSetValue value = new CodeSetValue();
    value.setCode(concept);
    value.setIncludeChildren(true);
    cs.getCodeSetValue().add(value);

    CodeSetValue exclusion = new CodeSetValue();
    exclusion.setCode("57607007"); //Occupational asthma
    exclusion.setIncludeChildren(true);
    value.getExclusion().add(exclusion);

    exclusion = new CodeSetValue();
    exclusion.setCode("41553006"); //Detergent asthma
    exclusion.setIncludeChildren(false);
    value.getExclusion().add(exclusion);

    value = new CodeSetValue();
    value.setCode("22298006"); //MI
    value.setIncludeChildren(false);
    cs.getCodeSetValue().add(value);

    HashSet<String> hs = enumerateConcepts(cs);

    LOG.debug("Enumeration of " + concept);
    dumpWithDesc(hs);
}
private static void dumpWithDesc(Iterable<? extends CharSequence> elements) {

    if (elements == null) {
        LOG.debug("\tNULL");
        return;
    }

    List<String> v = new ArrayList<>();
    Iterator it = elements.iterator();
    while (it.hasNext()) {
        String concept = (String)it.next();
        String term = getPreferredTerm(concept);
        v.add("\t" + term + ": " + concept);
    }

    LOG.debug("\t==" + v.size() + " results");

    String[] arr = v.toArray(new String[0]);
    Arrays.sort(arr);

    for (String s: arr) {
        LOG.debug(s);
    }
}*/

}


