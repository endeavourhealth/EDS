package org.endeavourhealth.core.messaging.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.transform.subscriber.json.LinkDistributorConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SubscriberConfig {

    private static final int DEFAULT_TRANSFORM_BATCH_SIZE = 50;

    //common properties to subscribers
    private SubscriberType subscriberType;
    private boolean includeDateRecorded;
    private int batchSize = DEFAULT_TRANSFORM_BATCH_SIZE;
    private String excludeNhsNumberRegex;
    private boolean isPseudonymised;
    private List<LinkDistributorConfig> pseudoSalts = new ArrayList<>();
    private CohortType cohortType;
    private Set<String> cohortGpServices = new HashSet<>(); //if cohort is GpRegisteredAt, this gives the ODS codes the patients should be registered at
    private Integer remoteSubscriberId;

    //compass v1 properties

    //compass v2 properties
    private boolean v2HasEncounterEventTable;


    public enum SubscriberType {
        CompassV1,
        CompassV2
    }

    public enum CohortType {
        AllPatients,
        ExplicitPatients,
        GpRegisteredAt
    }

    public SubscriberType getSubscriberType() {
        return subscriberType;
    }

    public boolean isIncludeDateRecorded() {
        return includeDateRecorded;
    }

    public boolean isV2HasEncounterEventTable() {
        return v2HasEncounterEventTable;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public String getExcludeNhsNumberRegex() {
        return excludeNhsNumberRegex;
    }

    public boolean isPseudonymised() {
        return isPseudonymised;
    }

    public List<LinkDistributorConfig> getPseudoSalts() {
        return pseudoSalts;
    }

    public Integer getRemoteSubscriberId() {
        return remoteSubscriberId;
    }

    public CohortType getCohortType() {
        return cohortType;
    }

    public static SubscriberConfig readFromConfig(String subscriberConfigName) throws Exception {
        JsonNode config = ConfigManager.getConfigurationAsJson(subscriberConfigName, "db_subscriber");
        if (config == null) {
            throw new Exception("No config record found for [" + subscriberConfigName + "]");
        }
        return readFromJson(config);
    }

    public static SubscriberConfig readFromJson(JsonNode config) throws Exception {

        SubscriberConfig ret = new SubscriberConfig();
        ret.populateFromJson(config);
        return ret;
    }

    private void populateFromJson(JsonNode config) throws Exception {

        if (!config.has("subscriber_type")) {
            throw new Exception("[subscriber_type] element not found");
        }
        this.subscriberType = parseSubscriberType(config.get("subscriber_type").asText());

        if (config.has("transform_batch_size")) {
            this.batchSize = config.get("transform_batch_size").asInt();
        }

        if (config.has("excluded_nhs_number_regex")) {
            this.excludeNhsNumberRegex = config.get("excluded_nhs_number_regex").asText();
        }

        this.includeDateRecorded = config.has("include_date_recorded")
                && config.get("include_date_recorded").asBoolean();

        if (config.has("remote_subscriber_id")) {
            this.remoteSubscriberId = new Integer(config.get("remote_subscriber_id").asInt());
        }

        if (!config.has("cohort_type")) {
            throw new Exception("[cohort_type] element not found");
        }
        this.cohortType = parseCohortType(config.get("cohort_type").asText());

        if (cohortType == CohortType.GpRegisteredAt) {
            JsonNode arr = config.get("cohort");
            for (int i=0; i<arr.size(); i++) {
                String odsCode = arr.get(i).asText();
                cohortGpServices.add(odsCode);
            }
        }


        //compass v1-specific config
        if (subscriberType == SubscriberType.CompassV1) {

            //compass v1 config may be stored in an older style or newer one
            if (config.has("pseudonymisation")) { //old style
                this.isPseudonymised = config.has("pseudonymisation");

                //the pseudonymisation node itself contains the primary salt key
                if (this.isPseudonymised) {
                    JsonNode saltNode = config.get("pseudonymisation");
                    String json = convertJsonNodeToString(saltNode);
                    LinkDistributorConfig firstSalt = ObjectMapperPool.getInstance().readValue(json, LinkDistributorConfig.class);
                    this.pseudoSalts.add(firstSalt);
                }

                //subsequent salts will be in this element
                if (config.has("linkedDistributors")) {
                    JsonNode linkDistributorsNode = config.get("linkedDistributors");

                    String linkDistributors = convertJsonNodeToString(linkDistributorsNode);
                    LinkDistributorConfig[] arr = ObjectMapperPool.getInstance().readValue(linkDistributors, LinkDistributorConfig[].class);
                    for (LinkDistributorConfig l : arr) {
                        this.pseudoSalts.add(l);
                    }
                }

            } else { //new style

                this.isPseudonymised = config.has("pseudonymised")
                        && config.get("pseudonymised").asBoolean();

                if (config.has("pseudo_salts")) {

                    JsonNode linkDistributorsNode = config.get("pseudo_salts");

                    if (linkDistributorsNode != null) {
                        String linkDistributors = convertJsonNodeToString(linkDistributorsNode);
                        LinkDistributorConfig[] arr = ObjectMapperPool.getInstance().readValue(linkDistributors, LinkDistributorConfig[].class);

                        for (LinkDistributorConfig l : arr) {
                            this.pseudoSalts.add(l);
                        }
                    }
                }
            }

        } else if (subscriberType == SubscriberType.CompassV2) {

            this.v2HasEncounterEventTable = config.has("include_encounter_event")
                    && config.get("include_encounter_event").asBoolean();

            this.isPseudonymised = config.has("pseudonymised")
                    && config.get("pseudonymised").asBoolean();

            if (config.has("pseudo_salts")) {

                JsonNode linkDistributorsNode = config.get("pseudo_salts");

                if (linkDistributorsNode != null) {
                    String linkDistributors = convertJsonNodeToString(linkDistributorsNode);
                    LinkDistributorConfig[] arr = ObjectMapperPool.getInstance().readValue(linkDistributors, LinkDistributorConfig[].class);

                    for (LinkDistributorConfig l : arr) {
                        this.pseudoSalts.add(l);
                    }
                }
            }

        } else {
                throw new Exception("No handler for subscriber type " + this.subscriberType);
        }

    }

    private CohortType parseCohortType(String type) {

        if (type.equals("all_patients")) {
            return CohortType.AllPatients;

        } else if (type.equals("explicit_patients")) {
            return CohortType.ExplicitPatients;

        } else if (type.equals("gp_registered_at")) {
            return CohortType.GpRegisteredAt;

        } else {
            throw new RuntimeException("Unsupported cohort type [" + type + "]");
        }
    }

    private static String convertJsonNodeToString(JsonNode jsonNode) throws Exception {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Object json = mapper.readValue(jsonNode.toString(), Object.class);
            return mapper.writeValueAsString(json);
        } catch (Exception e) {
            throw new Exception("Error parsing Link Distributor Config");
        }
    }

    private static SubscriberType parseSubscriberType(String type) {
        if (type.equals("compass_v1")) {
            return SubscriberType.CompassV1;

        } else if (type.equals("compass_v2")) {
            return SubscriberType.CompassV2;

        } else {
            throw new RuntimeException("Unsupported subscriber type [" + type + "]");
        }
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("subscriberType = [" + subscriberType + "], ");
        sb.append("isPseudonymised = [" + isPseudonymised + "], ");
        sb.append("cohortType = [" + cohortType + "], ");
        sb.append("cohortGpServices = [" + cohortGpServices.size() + "], ");
        sb.append("pseudoSalts = [" + pseudoSalts.size() + "], ");
        sb.append("excludeNhsNumberRegex = [" + excludeNhsNumberRegex + "], ");
        sb.append("remoteSubscriberId = [" + remoteSubscriberId + "], ");
        sb.append("includeDateRecorded = [" + includeDateRecorded + "], ");
        sb.append("batchSize = [" + batchSize + "], ");
        sb.append("v2HasEncounterEventTable = [" + v2HasEncounterEventTable + "]");

        return sb.toString();
    }
}
