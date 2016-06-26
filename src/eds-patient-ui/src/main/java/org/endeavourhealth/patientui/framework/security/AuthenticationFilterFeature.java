package org.endeavourhealth.patientui.framework.security;

import org.glassfish.jersey.server.ServerProperties;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;

@Provider
public class AuthenticationFilterFeature implements DynamicFeature
{
    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext featureContext) {

        String packageName = (String) featureContext.getConfiguration().getProperties().get(ServerProperties.PROVIDER_PACKAGES);

        boolean usePackageFilter = true;
        if ((packageName == null) || (packageName.trim().equals(""))) {
            usePackageFilter = false;
        }

        if ((!usePackageFilter) || (resourceInfo.getResourceClass().getName().startsWith(packageName))) {
            if (!containsUnsecuredAnnotation(resourceInfo.getResourceMethod().getAnnotations())) {
                AuthenticationFilter authenticationFilter = new AuthenticationFilter();
                featureContext.register(authenticationFilter);
            }
        }
    }

    private static boolean containsUnsecuredAnnotation(Annotation[] annotations) {
        for (Annotation annotation : annotations)
            if (annotation.annotationType().equals(Unsecured.class))
                return true;

        return false;
    }
}
