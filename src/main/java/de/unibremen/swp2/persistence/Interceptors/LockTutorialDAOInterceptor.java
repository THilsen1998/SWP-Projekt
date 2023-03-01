package de.unibremen.swp2.persistence.Interceptors;

import de.unibremen.swp2.persistence.Interceptors.LockTutorialDAO;
import de.unibremen.swp2.persistence.TutorialDAO;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@LockTutorialDAO
@Interceptor
public class LockTutorialDAOInterceptor {

    @Inject
    private TutorialDAO tutorialDAO;

    @AroundInvoke
    public Object check(InvocationContext ctx) throws Exception {
        tutorialDAO.lock();
        try {
            return ctx.proceed();
        } finally {
            tutorialDAO.unlock();
        }
    }
}
