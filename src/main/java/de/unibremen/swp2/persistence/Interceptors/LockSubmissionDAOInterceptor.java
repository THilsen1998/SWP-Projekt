package de.unibremen.swp2.persistence.Interceptors;

import de.unibremen.swp2.persistence.SubmissionDAO;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@LockSubmissionDAO
public class LockSubmissionDAOInterceptor {

    @Inject
    private SubmissionDAO submissionDAO;

    @AroundInvoke
    public Object check(InvocationContext ctx) throws Exception {
        submissionDAO.lock();
        try {
            return ctx.proceed();
        } finally {
            submissionDAO.unlock();
        }
    }

}
