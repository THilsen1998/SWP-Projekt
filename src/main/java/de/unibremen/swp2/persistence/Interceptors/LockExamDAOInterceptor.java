package de.unibremen.swp2.persistence.Interceptors;

import de.unibremen.swp2.persistence.ExamDAO;
import de.unibremen.swp2.persistence.Interceptors.LockExamDAO;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;

@Interceptor
@LockExamDAO
public class LockExamDAOInterceptor implements Serializable {

    @Inject
    private ExamDAO examDAO;

    @AroundInvoke
    public Object check(InvocationContext ctx) throws Exception {
        examDAO.lock();
        try {
            return ctx.proceed();
        } finally {
            examDAO.unlock();
        }
    }
}
