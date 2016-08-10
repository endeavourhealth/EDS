package org.endeavourhealth.core.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Provider
public class CustomRolesAllowedFeature implements DynamicFeature {

    private static final Logger LOG = LoggerFactory.getLogger(CustomRolesAllowedFeature.class);

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext configuration) {

        Method resourceMethod = resourceInfo.getResourceMethod();

        if (resourceMethod.isAnnotationPresent(DenyAll.class)) {
            configuration.register(new RolesAllowedRequestFilter());
            return;
        }

        RolesAllowed ra = resourceMethod.getAnnotation(RolesAllowed.class);
        if(ra == null) {
            for(Annotation a : resourceMethod.getDeclaredAnnotations()) {
                String pckg = "org.endeavourhealth.core.security.annotations";
                if(a.getClass().getName().contains(pckg) || a.toString().contains(pckg)) {    // sometimes proxies are created, so can't use simple reflection
                    ra = a.getClass().getAnnotation(RolesAllowed.class);
                    if(ra == null) {
                        // handle the proxy class
                        ra = a.annotationType().getAnnotation(RolesAllowed.class);
                    }
                    break;
                }
            }
        }

        if (ra != null) {
            configuration.register(new RolesAllowedRequestFilter(ra.value()));
            return;
        }

        if (resourceMethod.isAnnotationPresent(PermitAll.class)) {
            return;
        }


        ra = resourceInfo.getResourceClass().getAnnotation(RolesAllowed.class);
        if (ra != null) {
            configuration.register(new RolesAllowedRequestFilter(ra.value()));
        }
    }

    @Priority(Priorities.AUTHORIZATION) // authorization filter - should go after any authentication filters
    private static class RolesAllowedRequestFilter implements ContainerRequestFilter {

        private final boolean denyAll;
        private final String[] rolesAllowed;

        RolesAllowedRequestFilter() {
            this.denyAll = true;
            this.rolesAllowed = null;
        }

        RolesAllowedRequestFilter(final String[] rolesAllowed) {
            this.denyAll = false;
            this.rolesAllowed = (rolesAllowed != null) ? rolesAllowed : new String[]{};
        }

        @Override
        public void filter(final ContainerRequestContext requestContext) throws IOException {
            if (!denyAll) {
                if (rolesAllowed.length > 0 && !isAuthenticated(requestContext)) {
                    throw new ForbiddenException("Not Authorized");
                }

                for (final String role : rolesAllowed) {
                    if (SecurityUtils.hasRole(requestContext.getSecurityContext(), role)) {
                        return;
                    }
                }
            }

            throw new ForbiddenException("Not Authorized");
        }

        private static boolean isAuthenticated(final ContainerRequestContext requestContext) {
            return requestContext.getSecurityContext().getUserPrincipal() != null;
        }
    }
}
