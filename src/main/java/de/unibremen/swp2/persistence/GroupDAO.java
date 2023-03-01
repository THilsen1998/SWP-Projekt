package de.unibremen.swp2.persistence;


import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.Exceptions.EntityNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.OutdatedException;
import de.unibremen.swp2.persistence.Exceptions.ParticipantNotInMeetingException;

import javax.enterprise.context.RequestScoped;
import javax.persistence.*;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/*
@Author Martin
 * Process date from and into the data base
 */
@RequestScoped
public class GroupDAO implements DAO<TGroup> {

    private final static ReentrantLock reentrantLock = new ReentrantLock();
    @PersistenceContext(name = "IGradeBook")
    private EntityManager entityManager;

    public void lock() {
        reentrantLock.lock();
    }

    public void unlock() {
        reentrantLock.unlock();
    }

    /**
     * Inserts the given group.
     *
     * @param group Group to insert.
     * @throws NullPointerException If {@code group} is {@code null}.
     */
    public void insert(final TGroup group) throws NullPointerException, EntityNotFoundException {
        final Tutorial tutorial = entityManager.find(Tutorial.class, group.getTutorial().getId(),
                LockModeType.PESSIMISTIC_WRITE);
        if (tutorial != null) {
            entityManager.persist(group);
        } else {
            throw new EntityNotFoundException();
        }
    }

    /**
     * Updates the given group.
     *
     * @param group Group to update.
     * @throws NullPointerException If {@code group} is {@code null}.
     */
    public void update(final TGroup group) throws NullPointerException, EntityNotFoundException, OutdatedException {
        final TGroup current = entityManager.find(TGroup.class, group.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (current == null) {
            throw new EntityNotFoundException();
        } else if (current.getVersion() == null || !group.getVersion().equals(current.getVersion())) {
            throw new OutdatedException();
        }
        try {
            entityManager.merge(group);
        } catch (OptimisticLockException e) {
            throw new OutdatedException();
        }
    }

    /**
     * Deletes the given object. Does not fail if {@code group} is {@code null}
     * or unknown.
     *
     * @param group Group to delete.
     */
    public void delete(final TGroup group) {
    }

    public void addParticipantToGroup(final ParticipantStatus participantStatus) throws EntityNotFoundException,
            OutdatedException, ParticipantNotInMeetingException {
        final TGroup group = entityManager.find(TGroup.class, participantStatus.getGroup().getId(),
                LockModeType.PESSIMISTIC_WRITE);
        if (group != null) {
            final ParticipantStatus currentStatus = entityManager.find(ParticipantStatus.class,
                    participantStatus.getId(), LockModeType.PESSIMISTIC_WRITE);
            if (currentStatus == null) {
                throw new ParticipantNotInMeetingException();
            } else if (participantStatus.getVersion() == null || !participantStatus.getVersion().equals(currentStatus.getVersion())) {
                throw new OutdatedException();
            } else {
                try {
                    entityManager.merge(participantStatus);
                } catch (OptimisticLockException e) {
                    throw new OutdatedException();
                }
            }
        } else {
            throw new EntityNotFoundException();
        }
    }

    /**
     * Returns the group with given id.
     *
     * @param id Id of the group in question.
     * @return Group with given id.
     */
    public TGroup getById(final String id) {
        return entityManager.find(TGroup.class, id);
    }

    /**
     * Returns the group with given tutorial.
     *
     * @param tutorial Tutorial of group in question.
     * @return Group with given tutorial.
     */
    public TGroup getGroupByTutorial(final Tutorial tutorial) {
        return null;
    }

    /**
     * Returns the groups with given submission.
     *
     * @param submission Submission of groups in question.
     * @return Groups with given submission.
     */
    public List<TGroup> getGroupsBySubmission(final Submission submission) {
        return null;
    }

    /**
     * Returns the groups with given tutorial.
     *
     * @param tutorial Tutorial of groups in question.
     * @return Groups with given tutorial.
     */
    public List<TGroup> getGroupsByTutorial(final Tutorial tutorial) {
        return entityManager.
                createQuery("SELECT g FROM TGroup g WHERE g.tutorial = :tutorial", TGroup.class).
                setParameter("tutorial", tutorial).getResultList();
    }

    public List<TGroup> getGroupsByMeeting(final Meeting meeting) {
        return entityManager.createQuery("SELECT g FROM TGroup g WHERE g.tutorial.meeting =:meeting ", TGroup.class).
                setParameter("meeting", meeting).getResultList();
    }

    /**
     * Returns the groups with given user and tutorial.
     *
     * @param user     User of Groups in question.
     * @param tutorial Tutorial of Groups in question.
     * @return Groups with given user and tutorial.
     */
    public List<TGroup> getGroupByUser(final User user, final Tutorial tutorial) {
        return null;
    }

    public void flush() {
        entityManager.flush();
    }

    /**
     * Returns the groups with given user and meeting.
     *
     * @param user User  in question.
     * @param meeting meeting  in question.
     * @return Groups with given user and meeting.
     */
    public List<TGroup> getGroupsByMeetingAndUser(final Meeting meeting, final User user) {
        return entityManager.createQuery("SELECT g FROM TGroup g JOIN g.tutorial.userMeetingRoles u WHERE g.tutorial" +
                ".meeting = :meeting AND u.user = :user", TGroup.class).setParameter("meeting", meeting).setParameter("user", user).getResultList();
    }
}
