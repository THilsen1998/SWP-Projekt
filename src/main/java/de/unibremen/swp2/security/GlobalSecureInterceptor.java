package de.unibremen.swp2.security;

import de.unibremen.swp2.model.Role;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.security.enterprise.SecurityContext;
import java.io.Serializable;

/**
 * Implements the security checks specified by {@link GlobalSecure}.
 */
@Interceptor
@GlobalSecure
public class GlobalSecureInterceptor implements Serializable {

    @Inject
    private SecurityContext securityContext;

    /**
     * Used to check a users role.
     */
    @AroundInvoke
    public Object check(final InvocationContext ctx) throws Exception {
        GlobalSecure annotation = ctx.getMethod().getAnnotation(GlobalSecure.class);
        if (annotation == null) {
            annotation = ctx.getMethod().getDeclaringClass().getAnnotation(GlobalSecure.class);
        }
        if (annotation == null) {
            throw new IllegalStateException("Cannot find annotation 'GlobalSecure'");
        }
        for (final Role role : annotation.roles()) {
            if (securityContext.isCallerInRole(role.toString())) {
                return ctx.proceed();
            }
        }
        throw new SecurityException("Principal is not allowed");
    }
}
