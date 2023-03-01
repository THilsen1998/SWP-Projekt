package de.unibremen.swp2.persistence.Interceptors;

import de.unibremen.swp2.persistence.EvaluationDAO;
import de.unibremen.swp2.persistence.Interceptors.LockEvaluationDAO;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@LockEvaluationDAO
@Interceptor
public class LockEvaluationDAOInterceptor {

    @Inject
    private EvaluationDAO evaluationDAO;

    @AroundInvoke
    public Object check(InvocationContext ctx) throws Exception {
        evaluationDAO.lock();
        try {
            return ctx.proceed();
        } finally {
            evaluationDAO.unlock();
        }
    }
}
