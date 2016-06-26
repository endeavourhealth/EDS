package org.endeavourhealth.patientui.framework.security;

import javax.annotation.Priority;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

@Priority(Priorities.AUTHENTICATION)
public final class AuthenticationFilter implements ContainerRequestFilter {


    public AuthenticationFilter() {
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        try {
            Map<String, Cookie> cookies = containerRequestContext.getCookies();

            if (!cookies.containsKey(SecurityConfig.AUTH_COOKIE_NAME)) {
                throw new NotAuthorizedException("Cookie not found");
            }

            Cookie cookie = cookies.get(SecurityConfig.AUTH_COOKIE_NAME);
            String tokenString = cookie.getValue();
            UserWrapper userWrapper = TokenHelper.validateToken(tokenString);

            containerRequestContext.setSecurityContext(new UserSecurityContext(userWrapper));
        } catch (Exception e) {
            containerRequestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }
}
