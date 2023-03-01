package de.unibremen.swp2.persistence.Interceptors;

import de.unibremen.swp2.persistence.FinalGradeDAO;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@LockFinalGradeDAO
public class LockFinalGradeDAOInterceptor {

    @Inject
    private FinalGradeDAO finalGradeDAO;

    @AroundInvoke
    public Object check(InvocationContext ctx) throws Exception {
        finalGradeDAO.lock();
        try {
            return ctx.proceed();
        } finally {
            finalGradeDAO.unlock();
        }
    }
}
