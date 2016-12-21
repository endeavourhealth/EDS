package org.endeavourhealth.keycloak.providers;

import org.keycloak.models.*;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.IDToken;

import java.util.*;

/**
 * Maps user group membership
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class OrgGroupMembershipMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper {

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        ProviderConfigProperty property;
        ProviderConfigProperty property1;
        property1 = new ProviderConfigProperty();
        property1.setName(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME);
        property1.setLabel(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME_LABEL);
        property1.setType(ProviderConfigProperty.STRING_TYPE);
        property1.setDefaultValue("groups");
        property1.setHelpText(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME_TOOLTIP);
        configProperties.add(property1);
        property1 = new ProviderConfigProperty();
        property1.setName("full.path");
        property1.setLabel("Full group path");
        property1.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        property1.setDefaultValue("true");
        property1.setHelpText("Include full path to group i.e. /top/level1/level2, false will just specify the group name");
        configProperties.add(property1);

        property1 = new ProviderConfigProperty();
        property1.setName(OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN);
        property1.setLabel(OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN_LABEL);
        property1.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        property1.setDefaultValue("true");
        property1.setHelpText(OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN_HELP_TEXT);
        configProperties.add(property1);
        property1 = new ProviderConfigProperty();
        property1.setName(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN);
        property1.setLabel(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN_LABEL);
        property1.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        property1.setDefaultValue("true");
        property1.setHelpText(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN_HELP_TEXT);
        configProperties.add(property1);



    }

    public static final String PROVIDER_ID = "endeavourhealth-org-group-membership-mapper";

    public static final String ORG_ATTRIBUTE_LABEL = "organisation-id";


    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Endeavour Organisation Group Membership";
    }

    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    @Override
    public String getHelpText() {
        return "Map user organisation group membership";
    }

    public static boolean useFullPath(ProtocolMapperModel mappingModel) {
        return "true".equals(mappingModel.getConfig().get("full.path"));
    }


    @Override
    public AccessToken transformAccessToken(AccessToken token, ProtocolMapperModel mappingModel, KeycloakSession session,
                                            UserSessionModel userSession, ClientSessionModel clientSession) {
        if (!OIDCAttributeMapperHelper.includeInAccessToken(mappingModel)) return token;
        buildMembership(token, mappingModel, userSession);
        return token;
    }

    public void buildMembership(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession) {
        LinkedList<OrgGroupMembership> membershipMap = new LinkedList<>();

        boolean fullPath = useFullPath(mappingModel);

        Set<GroupModel> groupModels = userSession.getUser().getGroups();
        addOrganisationIds(fullPath, groupModels, membershipMap);

        String protocolClaim = mappingModel.getConfig().get(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME);
        token.getOtherClaims().put(protocolClaim, membershipMap);
    }

    private void addOrganisationIds(boolean fullPath, Set<GroupModel> groupModels, LinkedList<OrgGroupMembership> membershipMap) {
        if (groupModels == null || groupModels.size() == 0)
            return;

        for (GroupModel group : groupModels) {
            OrgGroupMembership orgGroupMembership = new OrgGroupMembership();
            if (fullPath) {
                orgGroupMembership.setGroup(ModelToRepresentation.buildGroupPath(group));
            } else {
                orgGroupMembership.setGroup(group.getName());
            }
            orgGroupMembership.setGroupId(group.getId());

            String organisationId = group.getFirstAttribute(ORG_ATTRIBUTE_LABEL);
            if(organisationId != null && organisationId.trim().length() > 0) {
                orgGroupMembership.setOrganisationId(organisationId.trim());
            }

            membershipMap.add(orgGroupMembership);

            buildRoles(orgGroupMembership.getRoles(), group.getRoleMappings());

            addOrganisationIds(fullPath, group.getSubGroups(), membershipMap);
        }
    }

    private void buildRoles(List<String> orgGroupMembership, Set<RoleModel> roles) {
        for(RoleModel role : roles) {
            orgGroupMembership.add(role.getName());
            if(role.isComposite()) {
                buildRoles(orgGroupMembership, role.getComposites());
            }
        }
    }

    @Override
    public IDToken transformIDToken(IDToken token, ProtocolMapperModel mappingModel, KeycloakSession session, UserSessionModel userSession, ClientSessionModel clientSession) {
        if (!OIDCAttributeMapperHelper.includeInIDToken(mappingModel)) return token;
        buildMembership(token, mappingModel, userSession);
        return token;
    }

    public static ProtocolMapperModel create(String name,
                                             String tokenClaimName,
                                             boolean consentRequired, String consentText,
                                             boolean accessToken, boolean idToken) {
        ProtocolMapperModel mapper = new ProtocolMapperModel();
        mapper.setName(name);
        mapper.setProtocolMapper(PROVIDER_ID);
        mapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        mapper.setConsentRequired(consentRequired);
        mapper.setConsentText(consentText);
        Map<String, String> config = new HashMap<String, String>();
        config.put(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME, tokenClaimName);
        if (accessToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, "true");
        if (idToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN, "true");
        mapper.setConfig(config);

        return mapper;
    }


}
