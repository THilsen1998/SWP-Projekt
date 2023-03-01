package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.Meeting;
import de.unibremen.swp2.model.Participant;
import de.unibremen.swp2.model.Role;
import de.unibremen.swp2.model.User;
import de.unibremen.swp2.persistence.Exceptions.*;
import de.unibremen.swp2.security.GlobalSecure;
import de.unibremen.swp2.service.MeetingService;
import de.unibremen.swp2.service.ParticipantService;
import de.unibremen.swp2.persistence.Exceptions.UserNotPermittedException;
import de.unibremen.swp2.service.UserService;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.PostConstruct;
import javax.faces.annotation.FacesConfig;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author Theo, Martin
 * Allows to add a new meeting.
 */
@Named
@ViewScoped
@FacesConfig
@GlobalSecure(roles = {Role.D, Role.A})
public class AddMeetingBean implements Serializable {

    /**
     * Used to add faces messages (in case of errors) or to complete
     * authentication (in case of success).
     */
    @Inject
    private FacesContext facesContext;

    /**
     * Allows to edit,update,delete,creat and to perform other operations
     * on a Meeting
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
     * Used for redirection.
     */
    @Inject
    private ExternalContext externalContext;

    /**
     * User currently locked in
     */
    @Inject
    private Principal principal;

    /**
     * Meeting to add.
     */
    @Getter
    private Meeting meeting;

    /**
     * The participants loaded from ParticipantDAO in the System.
     */
    @Getter
    private List<Participant> participants;

    /**
     * The participants loaded from ParticipantDAO in the System fitered.
     */
    @Setter
    @Getter
    private List<Participant> filteredParticipants;

    /**
     Participants that are added to the Meeting
     */
    @Getter
    private List<Participant> participantsToAdd;

    /**
     Participants that are added to the Meeting filtered
     */
    @Getter
    @Setter
    private List<Participant> filteredParticipantsToAdd;


    /**
     * The users loaded from userDAO in the System.
     */
    @Getter
    private List<User> users;

    /**
     Participants that are added to the Meeting
     */
    @Setter
    @Getter
    private List<User> filteredUsers;

    /**
     Users that are added to the Meeting
     */
    @Getter
    private List<User> usersToAdd;

    @Getter
    @Setter
    private List<User> filteredUsersToAdd;

    /**
     User that is CEO of Meeting
     */
    @Getter
    private User ceoToAdd;

    /**
     * All Users loaded from userDAO in the System.
     */
    @Getter
    private List<User> allUsers;

    /**
     * All Users loaded from userDAO in the System filtered.
     */
    @Getter
    @Setter
    private List<User> filteredAllUsers;

    /**
     * Initializes this bean.
     */
    @PostConstruct
    private void init() {
        meeting = new Meeting();
        usersToAdd = new ArrayList<>();
        participantsToAdd = new ArrayList<>();
        allUsers = userService.getAllUsers();
        users = allUsers.stream().filter(u -> u.getRole() == Role.D || u.getRole() == Role.A).collect(Collectors.toList());
        participants = participantService.getAllParticipants();
        final User user = userService.getUsersByEmail(principal.getName());
        usersToAdd.add(user);
        users.remove(user);
    }

    /**
     * Creates a meeting.
     */
    public void createMeeting() throws IOException {

        try {
            meetingService.create(meeting, participantsToAdd, usersToAdd, ceoToAdd);
            externalContext.redirect("home.xhtml");
        }
        catch (final ParticipantNotFoundException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Der Teilnehmer konnte nicht gefunden werden");
            facesContext.addMessage(null, msg);
        } catch (ParticipantAlreadyInMeetingException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Der Teilnehmer ist bereits in der Veranstaltung");
            facesContext.addMessage(null, msg);
        } catch (UserNotPermittedException e){
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Der Nutzer ist nicht erlaubt");
            facesContext.addMessage(null, msg);
        }
        catch (UserNotFoundException e){
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Der Nutzer konnte nicht gefunden werden");
            facesContext.addMessage(null, msg);
        }
        catch (UserAlreadyInMeetingException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Der Nutzer ist bereits in der Veranstaltung");
            facesContext.addMessage(null, msg);
        } catch (MeetingNotFoundException e) {
            final FacesMessage  msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Da war wohl jemand so schnell und hat das Meeting während der Erstellung gelöscht :P.");
            facesContext.addMessage(null, msg);
        } catch (UserAlreadyInOtherRoleException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Nutzer kann nicht gleichzeitig Dozent und CEO sein.");
            facesContext.addMessage(null, msg);
        }
    }

    /**
     * Adds a ceo.
     *
     * @param ceo ceo to add
     */
    public void addCEO(User ceo) {
        if (ceoToAdd != null) {
            allUsers.add(ceoToAdd);
        }
        allUsers.remove(ceo);
        ceoToAdd = ceo;
    }

    /**
     * Deletes a ceo.
     */
    public void deleteCEO() {
        if (ceoToAdd != null) {
            allUsers.add(ceoToAdd);
            ceoToAdd = null;
        }
    }


    /**
     * Adds a participant.
     *
     * @param participant participant to add
     */
    public void addParticipant(Participant participant) {

        //fals der teilnehemr noch nicht in der liste ist dann füge ihn hinzu
        if (!participantsToAdd.contains(participant)) {
            participantsToAdd.add(participant);
            participants.remove(participant);
            if (filteredParticipants != null) {
                filteredParticipants.remove(participant);
            }
            if (filteredParticipantsToAdd != null) {
                filteredParticipantsToAdd.add(participant);
            }
        } else {
            //else teilnehemr wurde bereits hinzugefügt
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Der Teilnehmer ist bereits in der Veranstaltung");
            facesContext.addMessage(null, msg);
        }
    }

    /**
     * Deletes a participant.
     *
     * @param participant participant to delete
     */
    public void deleteParticipant(Participant participant) {

        if (participantsToAdd.contains(participant)) {
            participantsToAdd.remove(participant);
            participants.add(participant);
            if (filteredParticipants != null) {
                filteredParticipants.add(participant);
            }
            if (filteredParticipantsToAdd != null) {
                filteredParticipantsToAdd.remove(participant);
            }
        } else {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Der Teilnehmer wurde bereits aus der Veranstaltung entfernt");
            facesContext.addMessage(null, msg);
        }

    }

    /**
     * Adds a User.
     *
     * @param user user to add
     */
    public void addUser(User user) {
        if (!usersToAdd.contains(user)) {
            usersToAdd.add(user);
            users.remove(user);
            if (filteredUsers != null) {
                filteredUsers.remove(user);
            }
            if (filteredUsersToAdd != null) {
                filteredUsersToAdd.add(user);
            }
        } else {
            //else teilnehemr wurde bereits hinzugefügt
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Der Nutzer ist bereits in der Veranstaltung");
            facesContext.addMessage(null, msg);
        }
    }

    /**
     * Deletes a user.
     *
     * @param user user to delete
     */
    public void deleteUser(User user) {
        if (usersToAdd.contains(user)) {
            usersToAdd.remove(user);
            users.add(user);
            if (filteredUsers != null) {
                filteredUsers.add(user);
            }
            if (filteredUsersToAdd != null) {
                filteredUsersToAdd.remove(user);
            }
        } else {
            //else teilnehemr wurde bereits hinzugefügt
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Der Nutzer wurde bereits aus der Veranstaltung entfernt");
            facesContext.addMessage(null, msg);
        }
    }
}
