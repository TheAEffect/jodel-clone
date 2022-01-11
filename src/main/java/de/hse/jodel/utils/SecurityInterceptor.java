package de.hse.jodel.utils;

import de.hse.jodel.model.Session;
import org.jboss.logging.Logger;
import org.jboss.resteasy.core.ResourceMethodInvoker;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This interceptor verifys the access permissions for a user
 * based on username and passowrd provided in request
 */
@Provider
public class SecurityInterceptor implements ContainerRequestFilter {
    @Inject
    AuthUser authUser;

    private static final Logger LOGGER = Logger.getLogger(SecurityInterceptor.class);
    private static final Response ACCESS_DENIED = Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();
    private static final Response ACCESS_FORBIDDEN = Response.status(Response.Status.FORBIDDEN).entity("Forbidden").build();
    private static final Response SERVER_ERROR = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal server error").build();

    @Override
    public void filter(ContainerRequestContext requestContext) {

        LOGGER.debug("Interceptor");

        ResourceMethodInvoker methodInvoker = (ResourceMethodInvoker) requestContext.getProperty("org.jboss.resteasy.core.ResourceMethodInvoker");
        Method method = methodInvoker.getMethod();

        //Access allowed for all
        if (!method.isAnnotationPresent(PermitAll.class)) {

            //Access denied for all
            if (method.isAnnotationPresent(DenyAll.class)) {
                requestContext.abortWith(ACCESS_FORBIDDEN);
                return;
            }

            //Check for session cookie
            try {
                String token = requestContext.getCookies().get("jodel-session").getValue();

                Session session = Session.findByToken(token);

                if (session == null) {
                    LOGGER.debug("Session null");
                    LOGGER.debug(requestContext.getMethod());
                    LOGGER.debug(requestContext.getUriInfo().getRequestUri().getPath());
                    requestContext.abortWith(ACCESS_DENIED);
                } else {
                    LOGGER.debug("Session not null");
                    LOGGER.debug(requestContext.getMethod());
                    LOGGER.debug(requestContext.getUriInfo().getRequestUri().getPath());

                    //Verify user access
                    if (method.isAnnotationPresent(RolesAllowed.class)) {
                        RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
                        Set<String> rolesSet = new HashSet<String>(Arrays.asList(rolesAnnotation.value()));

                        //Is user valid?
                        if (!rolesSet.contains(session.user.role)) {
                            requestContext.abortWith(ACCESS_DENIED);
                        }

                        authUser.setUser(session.user);
                    }
                }

            } catch (Exception exception) {
                LOGGER.error("SecurityInterceptor");
                requestContext.abortWith(ACCESS_DENIED);
            }
        }
    }
}