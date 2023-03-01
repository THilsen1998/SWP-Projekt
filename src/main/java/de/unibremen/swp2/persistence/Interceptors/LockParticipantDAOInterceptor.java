package de.unibremen.swp2.persistence.Interceptors;

import de.unibremen.swp2.persistence.Interceptors.LockParticipantDAO;
import de.unibremen.swp2.persistence.ParticipantDAO;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;

@Interceptor
@LockParticipantDAO
public class LockParticipantDAOInterceptor implements Serializable {

    @Inject
    private ParticipantDAO participantDAO;

    @AroundInvoke
    public Object check(InvocationContext ctx) throws Exception {
        participantDAO.lock();
        try {
            return ctx.proceed();
        } finally {
            participantDAO.unlock();
        }
    }
}
