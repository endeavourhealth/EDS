package org.endeavourhealth.transform.terminology;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.OriginatorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Test script at https://gitlab.com/noesisinformatica/termlex-tutorial/blob/master/src/etc/termlex-test.sh
 */
public class Snomed {

    private static final Logger LOG = LoggerFactory.getLogger(Snomed.class);
    private static final String TERMLEX = "http://termlex.org/";

    private static LinkedList<Client> webClients = new LinkedList<>(); //cache of web clients
    private static Cache<String, String> cachedTermsForConceptAndDescription = null;
    private static Cache<Long, List> cachedDescendantForConcept = null;

    /**
     * initialise terminology caches
     */
    static {

        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
        cacheManager.init();

        cachedTermsForConceptAndDescription = cacheManager.createCache("TermsForConceptAndDescription",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class, ResourcePoolsBuilder.heap(Integer.MAX_VALUE)));

        cachedDescendantForConcept = cacheManager.createCache("DescendantsForConcept",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, List.class, ResourcePoolsBuilder.heap(Integer.MAX_VALUE)));
    }

    public static String getTerm(long conceptId, long descriptionId) {

        String cacheKey = conceptId + "/" + descriptionId;
        String term = cachedTermsForConceptAndDescription.get(cacheKey);
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
    public static List<Long> getDescendants(Long conceptCode) {
        List<Long> descendants = cachedDescendantForConcept.get(conceptCode);
        if (descendants == null) {
            JsonElement json = executeTermlexGet("hierarchy/" + conceptCode + "/descendants");
            descendants = getCodesFromJsonArray(json);
            cachedDescendantForConcept.put(conceptCode, descendants);
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

        Client client = borrowClient();
        WebTarget target = client.target(TERMLEX + path);
        //target.path(path);

        Invocation.Builder request = target.request();
        request.accept(MediaType.APPLICATION_JSON);

        Response response = request.get();

        if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            String s = response.readEntity(String.class);
            returnClient(client); //only return on HTTP success

            JsonParser parser = new JsonParser();
            return parser.parse(s);
        } else {
            throw new RuntimeException("Error performing termlex query to " + path + " - status code " + response.getStatus());
        }
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


