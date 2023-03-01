package de.unibremen.swp2.security;

import de.unibremen.swp2.model.Role;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;

@Interceptor
@MeetingRole
public class MeetingRoleInterceptor implements Serializable {

    @AroundInvoke
    public Object check(final InvocationContext ctx) throws Exception {
        MeetingRole annotation = ctx.getMethod().getAnnotation(MeetingRole.class);
        if (annotation == null) {
            throw new IllegalStateException("Cannot find annotation 'MeetingRole'");
        }
        final Role role = (Role) ctx.getMethod().getDeclaringClass().getMethod("getRole").invoke(ctx.getTarget());
        for (final Role r : annotation.allowedRoles()) {
            if (role.equals(r)) {
                return ctx.proceed();
            }
        }
        throw new SecurityException("Principal is not allowed");
    }
}
