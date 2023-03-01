package de.unibremen.swp2.persistence.Interceptors;

import de.unibremen.swp2.persistence.Interceptors.LockUserDAO;
import de.unibremen.swp2.persistence.UserDAO;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;

@Interceptor
@LockUserDAO
public class LockUserDAOInterceptor implements Serializable {
    @Inject
    private UserDAO userDAO;

    @AroundInvoke
    public Object check(InvocationContext ctx) throws Exception {
        userDAO.lock();
        try {
            return ctx.proceed();
        } finally {
            userDAO.unlock();
        }
    }
}
