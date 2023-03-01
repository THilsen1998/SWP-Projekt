package de.unibremen.swp2.persistence.Interceptors;

import de.unibremen.swp2.persistence.TaskDAO;

import javax.inject.Inject;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@LockTaskDAO
public class LockTaskDAOInterceptor {

    @Inject
    private TaskDAO taskDAO;

    public Object check(InvocationContext ctx) throws Exception {
        taskDAO.lock();
        try {
            return ctx.proceed();
        } finally {
            taskDAO.unlock();
        }
    }
}
