package de.unibremen.swp2.persistence.Interceptors;


import de.unibremen.swp2.persistence.GroupDAO;
import de.unibremen.swp2.persistence.Interceptors.LockGroupDAO;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;

@Interceptor
@LockGroupDAO
public class LockGroupDAOInterceptor implements Serializable {

    @Inject
    private GroupDAO groupDAO;

    @AroundInvoke
    public Object check(InvocationContext ctx) throws Exception {
        groupDAO.lock();
        try {
            return ctx.proceed();
        } finally {
            groupDAO.unlock();
        }
    }

}
