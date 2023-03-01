package de.unibremen.swp2.service;


import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.*;
import de.unibremen.swp2.persistence.Exceptions.*;
import de.unibremen.swp2.persistence.Interceptors.LockMeetingDAO;
import de.unibremen.swp2.persistence.Interceptors.LockParticipantDAO;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author Theo, Dennnis
 *Allows to edit,update,delete,create and to perform other operations
 *  on a Participant (stored in {@link #participantDAO}).
 */
@RequestScoped
@Transactional(rollbackOn = PersistenceException.class)
public class ParticipantService {
    @Inject
    private ParticipantDAO participantDAO;

    /**
     * creates the given evaluation.
     *
     * @param participant Participant to create.
     */
    @LockParticipantDAO
    public void create(final Participant participant) throws NullPointerException, DuplicateEmailException {
        participantDAO.insert(participant);
    }

    /**
     * Updates the given participant.
     *
     * @param participant Participant to update.
     * @throws NullPointerException If {@code participant} is {@code null}.
     */
    @LockParticipantDAO
    public void update(final Participant participant) throws NullPointerException, OutdatedException, ParticipantNotFoundException, DuplicateEmailException {
        participantDAO.update(participant);
    }

    /**
     * Updates the status of a participant in a meeting.
     * @param participantStatus
     * @throws OutdatedException
     * @throws EntityNotFoundException
     */
    @LockParticipantDAO
    @LockMeetingDAO
    public void updateParticipantStatus(final ParticipantStatus participantStatus) throws OutdatedException, EntityNotFoundException {
        participantDAO.updateParticipantStatus(participantStatus);
    }

    /**
     * Returns the participant with given id.
     *
     * @param id Id of the participant in question.
     * @return Participant with given id.
     */
    public Participant getById(final String id) throws NullPointerException {
        return participantDAO.getById(id);
    }

    /**
     * Returns all participants.
     *
     * @return All Participants.
     */
    public List<Participant> getAllParticipants() {
        return participantDAO.getAllParticipants();
    }

    /**
     * Returns the participants with given tutorial.
     *
     * @param tutorial Tutorial of Participants in question.
     * @return Participants with given and tutorial.
     */
    public List<Participant> getAllParticipantsByTutorial(final Tutorial tutorial) {
        return participantDAO.getAllParticipantsByTutorial(tutorial);
    }

    /**
     * Returns the participants with given Meeting.
     *
     * @param meeting Meeting of Participants in question.
     * @return Participants with given meeting.
     */
    public List<ParticipantWithStatus> getAllParticipantsWithStatusByMeeting(final Meeting meeting) {
        final List<ParticipantWithStatus> list = new ArrayList<>();
        List<ParticipantStatus> statuses = participantDAO.getParticipantStatusByMeeting(meeting);
        for (ParticipantStatus status : statuses) {
            final Participant participant = status.getParticipant();
            if (participant != null) {
                list.add(new ParticipantWithStatus(participant, status));
            }
        }
        return list;
    }

    /**
     * Gets the status of a participant in a meeting by Id.
     * @param id the id.
     * @return the status of the participant.
     */
    public ParticipantStatus getParticipantStatusById(final String id) {
        return participantDAO.getParticipantStatusById(id);
    }

    /**
     * Returns the participants with given group.
     *
     * @param tGroup Group of Participants in question.
     * @return Participants with given group.
     */
    public List<Participant> getParticipantsByGroup(final TGroup tGroup) {
        return participantDAO.getParticipantsByGroup(tGroup);
    }

    /**
     * gets all participants in a meeting
     * @param meeting the meeting
     * @return the list of participants
     */
    public List<Participant> getAllParticipantsByMeeting(final Meeting meeting) {
        return participantDAO.getAllParticipantsByMeeting(meeting);
    }

    /**
     * Gets all participants which aren't in the meeting
     * @param meeting the meeting
     * @return the list of participants.
     */
    public List<Participant> getParticipantsNotInMeeting(final Meeting meeting) {
        return participantDAO.getParticipantsNotInMeeting(meeting);

    }

    /**
     * gets the status of a single participant in a meeting
     * @param participant the participant
     * @param meeting the meeting
     * @return the status
     * @throws NoResultException
     */
    public ParticipantStatus getParticipantStatusByParticipantAndMeeting(final Participant participant, final Meeting meeting) throws NoResultException {
        return participantDAO.getParticipantStatusByParticipantAndMeeting(participant, meeting);
    }

    /**
     * gets the status of a participant in specific group.
     * @param participant the participant
     * @param group the group.
     * @return the status
     */
    public ParticipantStatus getParticipantStatusByParticipantAndGroup(final Participant participant, final TGroup group) {
        return participantDAO.getParticipantStatusByParticipantAndGroup(participant, group);

    }

    /**
     * gets the statuses of all participants in the given meeting
     * @param meeting the meeting
     * @return the list of statuses
     */
    public List<ParticipantStatus> getParticipantStatusByMeeting(Meeting meeting) {
        return participantDAO.getParticipantStatusByMeeting(meeting);
    }

    /**
     * gets all participants which aren't in the tutorial, but in the meeting.
     * @param meeting the meeting
     * @return the list of participants
     */
    public List<Participant> getAllParticipantsNotInTutorial(final Meeting meeting) {
        return participantDAO.getAllParticipantsNotInTutorial(meeting);
    }

    /**
     * checks if the participant is in the tutorial of a specific user
     * @param user the user
     * @param participant the participant
     * @param meeting the meeting.
     * @return yes or no
     */
    public Boolean checkIfParticipantInUserTutorial(final User user, final Participant participant, final Meeting meeting) {
        return participantDAO.getCountOfParticipantInUserTutorial(user, participant, meeting) > 0;
    }

    /**
     * gets all participants which are managed by a specific user in the meeting.
     * @param meeting the meeting
     * @param user the user
     * @return the list of participants.
     */
    public List<Participant> getParticipantsByMeetingAndUser(final Meeting meeting, final User user) {
        return participantDAO.getParticipantsByMeetingAndUser(meeting, user);
    }

    /**
     * Gets all participants which aren't in any group of the tutorial.
     * @param tutorial the tutorial
     * @return the list of participants.
     */
    public List<Participant> getParticipantsNotInAnyGroup(final Tutorial tutorial) {
        return participantDAO.getParticipantsNotInAnyGroup(tutorial);
    }
}