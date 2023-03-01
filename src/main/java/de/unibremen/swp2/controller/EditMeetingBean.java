package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.Exceptions.*;
import de.unibremen.swp2.security.GlobalSecure;
import de.unibremen.swp2.security.MeetingRole;
import de.unibremen.swp2.service.MeetingService;
import de.unibremen.swp2.service.ParticipantService;
import de.unibremen.swp2.service.UserService;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.PostConstruct;
import javax.faces.annotation.FacesConfig;
import javax.faces.annotation.RequestParameterMap;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author Theo, Dennis
 * Allows to edit a meeting.
 */
@Named
@ViewScoped
@FacesConfig
@GlobalSecure(roles = {Role.D, Role.T, Role.A})
public class EditMeetingBean implements Serializable {

    /**
     * Used to add faces messages (in case of errors) or to complete
     * authentication (in case of success).
     */
    @Inject
    private FacesContext facesContext;

    /**
     * Used for redirection.
     */
    @Inject
    private ExternalContext externalContext;

    /**
     * Global roles of a user
     */
    @Getter
    private Role role;

    /**
     * Meeting to edit.
     */
    @Getter
    private Meeting meeting;

    /**
     * Allows to edit,update,delete,creat and to perform other operations
     * on a Exam
     */
    @Inject
    private MeetingService meetingService;

    /**
     * Allows to edit,update,delete,creat and to perform other operations
     * on a Participant
     */
    @Inject
    private ParticipantService participantService;

    /**
     * Allows to edit,update,delete,creat and to perform other operations
     * on a User
     */
    @Inject
    private UserService userService;

    /**
     * User currently locked in
     */
    @Inject
    private Principal principal;

    /**
     * The Participants that are freshly addet to the Meeting .
     */
    @Getter
    private List<Participant> participantsToAdd;

    /**
    The Participants that are freshly added to the Meeting Filtered
     */
    @Getter
    @Setter
    private List<Participant> filteredParticipantsToAdd;

    /**
    All Participants Not in Meeting
     */
    @Getter
    private List<Participant> participantsNotInMeeting;

    /**
    All Participants Not in Meeting Filtered
     */
    @Getter
    @Setter
    private List<Participant> filteredParticipantsNotInMeeting;


    /**
     * The Users that are freshly added to the Meeting .
     */
    @Getter
    private List<User> usersToAdd;

    /**
     * The Users that are freshly added to the Meeting Filtered .
     */
    @Setter
    @Getter
    private List<User> filteredUsersToAdd;


    /**
    All Lecturers Not in Meeting
     */
    @Getter
    private List<User> lecturersNotInMeeting;

    /**
    All Lecturers Not in Meeting Filtered
    */
    @Setter
    @Getter
    private List<User> filteredLecturersNotInMeeting;

    /**
     User that is CEO of Meeting
    */
    @Getter
    private User ceoToAdd;


    /**
     Users that are capably to be CEO of Meeting and are not in the Meeting
   */
    @Getter
    private List<User> allUsersNotInMeeting;

    /**
    User that are capably to be CEO of Meeting Filtered
   */
    @Getter
    @Setter
    private List<User> filteredAllUsersNotInMeeting;

    /**
     * Parameter-Map which provides the id
     */
    @Inject
    @RequestParameterMap
    private Map<String, String> parameterMap;

    /**
     * Initializes this bean.
     */
    @PostConstruct
    private void init() {
        final String id = parameterMap.get("meeting-Id");
        if (id != null) {
            meeting = meetingService.getById(id);
            if (meeting != null) {
                final User user = userService.getUsersByEmail(principal.getName());
                if (user.getRole().equals(Role.A)) {
                    role = Role.A;
                } else {
                    try {
                        final UserMeetingRole userMeetingRole = userService.getUserMeetingRoleByUserAndMeeting(user,
                                meeting);
                        role = userMeetingRole.getRole();
                    } catch (NoResultException ignored) {
                    }
                }
                if (role != null && !role.equals(Role.T) && ((meeting.getVisible() || role.equals(Role.D) || role.equals(Role.A)) )) {

                    participantsToAdd = participantService.getAllParticipantsByMeeting(meeting);
                    participantsNotInMeeting = participantService.getParticipantsNotInMeeting(meeting);
                    usersToAdd = userService.getLecturersByMeeting(meeting);

                    allUsersNotInMeeting = userService.getAllUsersNotInMeeting(meeting);

                    lecturersNotInMeeting =
                            allUsersNotInMeeting.stream().filter(u -> u.getRole() == Role.D || u.getRole() == Role.A).collect(Collectors.toList());
                    ceoToAdd = userService.getCeoByMeeting(meeting);
                }
            }
        }
    }

    /**
     * Updates {@link #meeting}. On success, a redirect to 'meeting.xhtml' is
     * registered.
     * wird aus liste von meeting entfernt und wieder hinzugefügt
     */
    @MeetingRole(allowedRoles = {Role.CEO, Role.D, Role.A})
    public void update() throws IOException {

        try {
            meetingService.update(meeting, usersToAdd, participantsToAdd, ceoToAdd);
            externalContext.redirect("meeting.xhtml?meeting-Id=" + meeting.getId());
        } catch (OutdatedException e) {
            final FacesMessage msg = new FacesMessage("Die Veranstaltung ist veraltet. Bitte Seite neu laden.");
            facesContext.addMessage(null, msg);
        } catch (EntityNotFoundException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Veranstaltung nicht gefunden oder Teilenhmer oder Nutzer nicht gefunden.");
            facesContext.addMessage(null, msg);
        } catch (UserNotPermittedException | UserNotInMeetingException  e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Keine Berechtigung diese Aktion auszuführen.");
            facesContext.addMessage(null, msg);
        }  catch (UserAlreadyInOtherRoleException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Ein Nutzer kann nicht gleichzeitig CEO und Dozent sein.");
            facesContext.addMessage(null, msg);
        } catch (UserAlreadyInMeetingException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Einer der Nutzer ist bereits in der Veranstaltung.");
            facesContext.addMessage(null, msg);
        } catch (ParticipantAlreadyInMeetingException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Einer der Teilnehmer ist bereits in der Veranstalung");
            facesContext.addMessage(null, msg);
        }

    }

    /**
     * Adds a ceo.
     *
     * @param ceo ceo to add
     */
    @MeetingRole(allowedRoles = {Role.D, Role.A})
    public void addCEO(User ceo) {
        if (ceoToAdd != null) {
            allUsersNotInMeeting.add(ceoToAdd);
        }
        allUsersNotInMeeting.remove(ceo);
        ceoToAdd = ceo;
    }

    /**
     * Deletes a ceo.
     */
    @MeetingRole(allowedRoles = {Role.D, Role.A})
    public void deleteCEO() {
        if (ceoToAdd != null) {
            allUsersNotInMeeting.add(ceoToAdd);
            ceoToAdd = null;
        }
    }


    /**
     * Adds a participant.
     *
     * @param participant participant to add
     */
    public void addParticipant(Participant participant) {

        participantsToAdd.add(participant);
        participantsNotInMeeting.remove(participant);
        if (filteredParticipantsNotInMeeting != null) {
            filteredParticipantsNotInMeeting.remove(participant);
        }
        if (filteredParticipantsToAdd != null) {
            filteredParticipantsToAdd.add(participant);
        }
    }

    /**
     * Adds a User.
     *
     * @param user user to add
     */
    @MeetingRole(allowedRoles = {Role.D, Role.A})
    public void addUser(User user) {
        usersToAdd.add(user);
        lecturersNotInMeeting.remove(user);
        if (filteredLecturersNotInMeeting != null) {
            filteredLecturersNotInMeeting.remove(user);
        }
        if (filteredUsersToAdd != null) {
            filteredUsersToAdd.add(user);
        }
    }

    /**
     * Deletes a user.
     *
     * @param user user to delete
     */
    @MeetingRole(allowedRoles = {Role.D, Role.A})
    public void deleteUser(User user) {
        usersToAdd.remove(user);
        lecturersNotInMeeting.add(user);
        if (filteredLecturersNotInMeeting != null) {
            filteredLecturersNotInMeeting.add(user);
        }
        if (filteredUsersToAdd != null) {
            filteredUsersToAdd.remove(user);
        }

    }


}
