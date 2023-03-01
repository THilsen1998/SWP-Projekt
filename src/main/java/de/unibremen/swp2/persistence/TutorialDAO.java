package de.unibremen.swp2.persistence;
import de.unibremen.swp2.model.*;
import de.unibremen.swp2.model.Meeting;
import de.unibremen.swp2.model.Tutorial;
import de.unibremen.swp2.model.User;
import de.unibremen.swp2.persistence.Exceptions.*;
import de.unibremen.swp2.persistence.Interceptors.LockTutorialDAOInterceptor;

import javax.enterprise.context.RequestScoped;
import javax.interceptor.Interceptors;
import javax.persistence.*;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/*
@Author Martin, Tommy
 * Process date from and into the data base
 */
@RequestScoped
public class TutorialDAO implements DAO<Tutorial> {

    private final static ReentrantLock lock = new ReentrantLock();

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    @PersistenceContext(name = "IGradeBook")
    private EntityManager entityManager;


    /**
     * Inserts the given tutorial.
     *
     * @param tutorial Tutorial to insert.
     * @throws NullPointerException If {@code tutorial} is {@code null}.
     */
    public void insert(final Tutorial tutorial) throws NullPointerException, MeetingNotFoundException {
        final Meeting meeting = entityManager.find(Meeting.class, tutorial.getMeeting().getId(), LockModeType.PESSIMISTIC_WRITE);
        if (meeting != null) {
            entityManager.persist(tutorial);
        } else {
            throw new MeetingNotFoundException();
        }
    }

    /**
     * Updates the given tutorial.
     *
     * @param tutorial Tutorial to update.
     * @throws NullPointerException If {@code tutorial} is {@code null}.
     */
    public void update(final Tutorial tutorial) throws NullPointerException, OutdatedException, TutorialNotFoundException, ParticipantNotFoundException {
        final Tutorial current = entityManager.find(Tutorial.class, tutorial.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (current == null) {
            throw new TutorialNotFoundException();
        } else if (current.getVersion() == null || !tutorial.getVersion().equals(current.getVersion())) {
            throw new OutdatedException();
        }
        try {
            entityManager.merge(tutorial);
        } catch (OptimisticLockException e) {
            throw new OutdatedException();
        }

    }

    /**
     * Deletes the given tutorial. Does not fail if {@code tutorial} is {@code null}
     * or unknown.
     *
     * @param tutorial Tutorial to delete.
     */
    public void delete(final Tutorial tutorial) {
        final Tutorial current = entityManager.find(Tutorial.class, tutorial.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (current != null) {
            entityManager.remove(current);
        }
    }

    /**
     * Returns the Tutorial with given id.
     *
     * @param id Id of the tutorial in question.
     * @return Tutorial with given id.
     */
    public Tutorial getById(final String id) {
        return entityManager.find(Tutorial.class, id);
    }

    /**
     * Returns the tutorials with given meeting.
     *
     * @param meeting Meeting of the tutorials in question.
     * @return Tutorials with given meeting.
     */
    public List<Tutorial> getTutorialsByMeeting(final Meeting meeting) {
        return entityManager.createQuery("SELECT t FROM Tutorial t WHERE t.meeting =:meeting ", Tutorial.class).
                setParameter("meeting", meeting).getResultList();
    }

    /**
     * Returns the tutorials with given meeting and user.
     *
     * @param meeting Meeting of the tutorials in question.
     * @param user    User of the tutorials in question.
     * @return Tutorials with given meeting.
     */
    public List<Tutorial> getTutorialsByUserAndMeeting(final Meeting meeting, final User user) {
        return entityManager.
                createQuery("SELECT t FROM Tutorial t JOIN t.userMeetingRoles r WHERE t.meeting = :meeting AND r.user = :user", Tutorial.class).
                setParameter("meeting", meeting).
                setParameter("user", user).
                getResultList();
    }
    /**
     * Adds the a tutor to a specific tutorial .
     *
     * @param userMeetingRole
     * tutor to add
     * @param tutorial
     * the wanted tutorial
     */
    public void addTutorToTutorial(final UserMeetingRole userMeetingRole, final Tutorial tutorial) throws TutorialNotFoundException, OutdatedException {
        final Tutorial current = entityManager.find(Tutorial.class, tutorial.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (current == null) {
            throw new TutorialNotFoundException();
        }
        final UserMeetingRole currentRole = entityManager.find(UserMeetingRole.class, userMeetingRole.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (currentRole != null) {
            if (currentRole.getVersion() == null || !userMeetingRole.getVersion().equals(currentRole.getVersion())) {
                throw new OutdatedException();
            }
        }
        try {
            entityManager.merge(userMeetingRole);
        } catch (OptimisticLockException e) {
            throw new OutdatedException();
        }
    }
    /**
     * Adds the a Participant to a specific tutorial .
     *
     * @param status
     * Participant to add
     */
    public void addParticipantToTutorial(final ParticipantStatus status) throws ParticipantNotInMeetingException, OutdatedException {
        final ParticipantStatus current = entityManager.find(ParticipantStatus.class, status.getId(), LockModeType.PESSIMISTIC_WRITE);
        final Tutorial tutorial = entityManager.find(Tutorial.class, status.getTutorial().getId(), LockModeType.PESSIMISTIC_WRITE);
        if (current == null || tutorial == null) {
            throw new ParticipantNotInMeetingException();
        } else if (!status.getVersion().equals(current.getVersion())) {
            throw new OutdatedException();
        }
        try {
            entityManager.merge(status);
        } catch (OptimisticLockException e) {
            throw new OutdatedException();
        }
    }
    /**
     * Returns the tutorials by giving group.
     *
     * @param tGroup the given group .
     * @return Tutorials with given group.
     */
    public Tutorial getTutorialByGroup(TGroup tGroup){
        return entityManager.createQuery("SELECT t FROM Tutorial t WHERE EXISTS (SELECT g FROM TGroup g WHERE g = :tGroup AND g.tutorial = t)", Tutorial.class).
                setParameter("tGroup", tGroup).getSingleResult();

    }

    public void flush() {
        entityManager.flush();
    }
}
