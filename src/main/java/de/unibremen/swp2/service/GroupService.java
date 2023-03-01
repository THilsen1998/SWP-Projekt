package de.unibremen.swp2.service;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.*;
import de.unibremen.swp2.persistence.Exceptions.EntityNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.OutdatedException;
import de.unibremen.swp2.persistence.Exceptions.ParticipantNotInMeetingException;
import de.unibremen.swp2.persistence.Exceptions.PersistenceException;
import de.unibremen.swp2.persistence.Interceptors.*;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.util.List;

/**
 * @Author Tommy
 * Allows to edit,update,delete,creat and to perform other operations on
 * a Group (stored in {@link #groupDao}).
 */
@Transactional(rollbackOn = PersistenceException.class)
@RequestScoped
public class GroupService
{

    @Inject
    private GroupDAO groupDao;

    @Inject
    private ParticipantDAO participantDAO;

    /**
     * creates the given group.
     *
     * @param group
     *      Group to create.
     */
    @LockMeetingDAO
    @LockTutorialDAO
    @LockGroupDAO
    @LockParticipantDAO
    public void create(final TGroup group, List<Participant> participants) throws ParticipantNotInMeetingException, EntityNotFoundException, OutdatedException {
        groupDao.insert(group);
        groupDao.flush();
        for (Participant p : participants) {
            try {
                ParticipantStatus status = participantDAO.getParticipantStatusByTutorialAndParticipant(group.getTutorial(), p);
                status.setGroup(group);
                groupDao.addParticipantToGroup(status);
            } catch (NoResultException e) {
                throw new ParticipantNotInMeetingException();
            }
        }
    }

    /**
     * Updates the given group.
     *
     * @param group
     *      Group to update.
     * @throws NullPointerException
     *      If {@code group} is {@code null}.
     */
    @LockParticipantDAO
    @LockMeetingDAO
    @LockGroupDAO
    @LockTutorialDAO
    public void update(final TGroup group, final List<Participant> participants) throws NullPointerException, OutdatedException, EntityNotFoundException {
        groupDao.update(group);
        final List<Participant> currentParticipants = participantDAO.getParticipantsByGroup(group);
        final TGroup currentGroup = groupDao.getById(group.getId());
        if (currentGroup != null) {
            for (Participant p : currentParticipants) {
                if (!participants.contains(p)) {
                    try {
                        final ParticipantStatus status = participantDAO.getParticipantStatusByTutorialAndParticipant(group.getTutorial(), p);
                        status.setGroup(null);
                        participantDAO.updateParticipantStatus(status);
                    } catch (NoResultException e) {
                        throw new EntityNotFoundException();
                    }
                }
                else
                {
                    participants.remove(p);
                }
            }
            for(Participant p: participants)
            {
                try {
                    final ParticipantStatus status = participantDAO.getParticipantStatusByTutorialAndParticipant(group.getTutorial(), p);
                    status.setGroup(group);
                    participantDAO.updateParticipantStatus(status);
                } catch (NoResultException e) {
                    throw new EntityNotFoundException();
                }
            }
        } else throw new EntityNotFoundException();
    }

    /**
     * Deletes the given object. Does not fail if {@code group} is {@code null}
     * or unknown.
     *
     * @param group
     *      Group to delete.
     */
    public void delete(final TGroup group){
        groupDao.delete(group);
    }

    /**
     * Returns the group with given id.
     *
     * @param id
     *      Id of the group in question.
     * @return
     *      Group with given id.
     */
    public TGroup getById(final String id){
        return groupDao.getById(id);
    }

    /**
     * Returns a groups according to a given tutorial.
     *
     * @param tutorial
     *      Tutorial of groups in question.
     * @return
     *      Groups with given tutorial.
     */
    public List<TGroup> getGroupsByTutorial(final Tutorial tutorial){
        return groupDao.getGroupsByTutorial(tutorial);
    }

    /**
     * gets all groups of a meeting managed by a specific user.
     * @param meeting the meeting
     * @param user the user
     * @return the list of groups
     */
    public List<TGroup> getGroupsByMeetingAndUser(final Meeting meeting, final User user) {
        return groupDao.getGroupsByMeetingAndUser(meeting, user);
    }

    /**
     * gets all groups of a meeting.
     * @param meeting the meeting.
     * @return the list of groups.
     */
    public List<TGroup> getGroupsByMeeting(final Meeting meeting){
       return groupDao.getGroupsByMeeting(meeting);
    }
}

