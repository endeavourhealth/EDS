package org.endeavourhealth.core.security;

import org.endeavourhealth.core.data.admin.models.EndUser;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.SecurityContext;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class provides security utilities for Keycloak integration.
 */
public class SecurityUtils {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityUtils.class);

    public static UUID getCurrentUserId(SecurityContext sc) {
        if(sc != null) {
            if(sc.getUserPrincipal() != null) {
                return UUID.fromString(sc.getUserPrincipal().getName());
            }
        }
        LOG.warn("Something appears to be wrong with the security configuration, SecurityContext is null.");
        return null;
    }

    public static KeycloakSecurityContext getKeycloakSecurityContext(SecurityContext sc) {
        if (sc != null && sc.getUserPrincipal() != null) {
            KeycloakPrincipal kp = (KeycloakPrincipal)sc.getUserPrincipal();
            return kp.getKeycloakSecurityContext();
        }
        LOG.warn("Something appears to be wrong with the security configuration, UserPrincipal is not as expected.");
        return null;
    }

    public static AccessToken getToken(SecurityContext sc) {

        if (sc != null && sc.getUserPrincipal() != null) {
            try {
                KeycloakPrincipal kp = (KeycloakPrincipal)sc.getUserPrincipal();
                return kp.getKeycloakSecurityContext().getToken();
            } catch(Exception e) {
                LOG.warn("Something appears to be wrong with the security configuration, UserPrincipal is not as expected.", e);
            }
        }
        return null;
    }

    public static boolean hasRole(SecurityContext sc, String role) {

        AccessToken token = getToken(sc);

        if (token != null) {
            for (String userRole : token.getRealmAccess().getRoles()) {
                if (userRole.equalsIgnoreCase(role)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean hasOrganisationRole(SecurityContext sc, String organisationId, String role) {
        return hasOrganisationRole(sc, organisationId, new String[] { role });
    }

    public static boolean hasOrganisationRole(SecurityContext sc, String organisationId, String[] roles) {

        // get roles for an organisation
        List<String> orgGroupRoles = getOrganisationRoles(sc, organisationId);
        if(orgGroupRoles != null) {
            for (String orgGroupRole : orgGroupRoles) {
                // check for matches
                for (String role : roles) {
                    if (orgGroupRole.equalsIgnoreCase(role)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static Map<String, List<String>> getOrganisationRoles(SecurityContext sc) {

        AccessToken token = getToken(sc);

        if (token != null) {

            // get organisation group roles claim
            List<Map<String, Object>> orgGroups = (List<Map<String, Object>>) token.getOtherClaims().getOrDefault(OrgRoles.OTHER_CLAIMS_ORG_GROUPS, null);
            if(orgGroups != null) {
                Map<String, List<String>> orgRoles = new HashMap<>();

                for(Map<String, Object> orgGroup : orgGroups) {
                    List<String> orgGroupRoles = new ArrayList<>();

                    // ignore everything except organisations matching organisationId
                    String orgGroupOrganisationId = (String) orgGroup.getOrDefault(OrgRoles.OTHER_CLAIMS_ORG_GROUPS_ORG_ID, null);
                    // get roles
                    List<String> roles = (List<String>) orgGroup.getOrDefault(OrgRoles.OTHER_CLAIMS_ORG_GROUPS_ROLES, null);
                    if(roles != null && roles.size() > 0) {
                        orgGroupRoles.addAll(roles);
                    }

                    if(!orgRoles.containsKey(orgGroupOrganisationId)) {
                        orgRoles.put(orgGroupOrganisationId, new ArrayList<>());
                    }
                    orgRoles.get(orgGroupOrganisationId).addAll(orgGroupRoles);
                }

                Map<String, List<String>> orgRolesOut = new HashMap<>();

                orgRoles.forEach((orgId, roles) ->
                    orgRolesOut.put(orgId, roles.stream().distinct().collect(Collectors.toList()))
                );

                return orgRolesOut;

            }
        }

        return null;
    }


    public static List<String> getOrganisationRoles(SecurityContext sc, String organisationId) {

        AccessToken token = getToken(sc);

        if (token != null) {

            // get organisation group roles claim
            List<Map<String, Object>> orgGroups = (List<Map<String, Object>>) token.getOtherClaims().getOrDefault(OrgRoles.OTHER_CLAIMS_ORG_GROUPS, null);
            if(orgGroups != null) {
                List<String> orgGroupRoles = new ArrayList<>();

                for(Map<String, Object> orgGroup : orgGroups) {
                    // ignore everything except organisations matching organisationId
                    String orgGroupOrganisationId = (String) orgGroup.getOrDefault(OrgRoles.OTHER_CLAIMS_ORG_GROUPS_ORG_ID, null);
                    if(orgGroupOrganisationId != null && orgGroupOrganisationId.equalsIgnoreCase(organisationId)) {
                        // get roles
                        List<String> roles = (List<String>) orgGroup.getOrDefault(OrgRoles.OTHER_CLAIMS_ORG_GROUPS_ROLES, null);
                        if(roles != null && roles.size() > 0) {
                            orgGroupRoles.addAll(roles);
                        }
                    }
                }

                return orgGroupRoles;
            }
        }

        return null;
    }

    public static String getCurrentUserOrganisationId(ContainerRequestContext requestContext) {
        String organisationId = requestContext.getHeaderString(OrgRoles.HEADER_ORGANISATION_ID);
        if(requestContext.getUriInfo().getQueryParameters().containsKey("organisationId")) {
            organisationId = requestContext.getUriInfo().getQueryParameters().getFirst("organisationId");
        }
        return organisationId;
    }

    public static EndUser getCurrentUser(SecurityContext sc) {
        EndUser endUser = null;
        AccessToken token = getToken(sc);
        if(token != null) {
            endUser = new EndUser(
                    UUID.fromString(token.getSubject()),   // subject == keycloak user id
                    null,   // no title attribute, TODO: add custom user attribute to Keylcloak??
                    token.getGivenName(),
                    token.getFamilyName(),
                    token.getEmail(),
                    RoleUtils.isEDSSuperUser(sc)
            );
        }
        return endUser;
    }
}
