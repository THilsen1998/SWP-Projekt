package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.*;

import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.unibremen.swp2.persistence.Exceptions.EntityNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.OutdatedException;
import de.unibremen.swp2.persistence.Exceptions.ParticipantNotInMeetingException;
import de.unibremen.swp2.security.GlobalSecure;
import de.unibremen.swp2.security.MeetingRole;
import de.unibremen.swp2.service.*;
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

/**
 * @Author Theo, Martin
 * Allows to add a new group.
 */

@Named
@ViewScoped
@FacesConfig
@GlobalSecure(roles = {Role.T, Role.D, Role.A})
public class AddGroupBean implements Serializable {

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
     * Allows to edit,update,delete,creat and to perform other operations
     */
    @Inject
    private TutorialService tutorialService;

    /**
     * Allows to edit,update,delete,creat and to perform other operations
     * on a Participant
     */
    @Inject
    private ParticipantService participantService;

    /**
     * Allows to edit,update,delete,creat and to perform other operations
     * on a Group
     */
    @Inject
    private GroupService groupService;

    /**
     * Allows to edit,update,delete,creat and to perform other operations
     * on a User
     */
    @Inject
    private UserService userService;

    /**
     * Allows to edit,update,delete,creat and to perform other operations
     * on a Meeting
     */
    @Inject
    private MeetingService meetingService;

    /**
     * Group to add.
     */
    @Getter
    private TGroup group;

    /**
     * Tutorial to add.
     */
    @Getter
    private Tutorial tutorial;


    /**
     * The participants in tutorial loaded from ParticipantDAO.
     */
    @Getter
    private List<Participant> participantsInTutorial;


    /**
     * The participants in tutorial loaded from ParticipantDAO filterd.
     */
    @Setter
    @Getter
    private List<Participant> filteredParticipantsInTutorial;

    /**
      Participants that are added to the Group
    */
    @Getter
    private List<Participant> participantsToAdd;

    /**
     Participants that are added to the Group filtered
    */
    @Getter
    @Setter
    private List<Participant> filteredParticipantsToAdd;

    /**
     * Parameter-Map which provides the id
     */
    @Inject
    @RequestParameterMap
    private Map<String, String> parameterMap;

    /**
     * User currently locked in
     */
    @Inject
    private Principal principal;

    /**
     * Global roles of a user
     */
    @Getter
    private Role role;

    /**
     * Meeting where the Group is in
     */
    @Getter
    private Meeting meeting;


    /**
     * Initializes this bean.
     */
    @PostConstruct
    private void init() {
        final String id = parameterMap.get("tutorial-Id");
        if (id != null) {
            tutorial = tutorialService.getById(id);
            if (tutorial != null) {
                meeting = meetingService.getMeetingByTutorial(tutorial);
                final User user = userService.getUsersByEmail(principal.getName());
                if (user.getRole().equals(Role.A)) {
                    role = Role.A;
                } else {
                    UserMeetingRole userMeetingRole;
                    try {
                        userMeetingRole = userService.getUserMeetingRoleByTutorialAndUser(tutorial, user);
                        role = userMeetingRole.getRole();
                    } catch (NoResultException ignored) {
                        userMeetingRole = userService.getUserMeetingRoleByUserAndMeeting(user, meeting);
                        role = userMeetingRole.getRole();
                    }
                }
                if (role != null && (role.equals(Role.A) || role.equals(Role.D) || meeting.getVisible())) {
                    group = new TGroup();
                    group.setTutorial(tutorial);
                    participantsToAdd = new ArrayList<>();
                    participantsInTutorial = participantService.getParticipantsNotInAnyGroup(tutorial);
                }
            }
        }
    }

    /**
     * Creates a group.
     */
    @MeetingRole(allowedRoles = {Role.D, Role.CEO, Role.T, Role.A})
    public void createGroup() throws IOException {
        try{
            groupService.create(group, participantsToAdd);
            externalContext.redirect("single-tutorial.xhtml?tutorial-Id="+ tutorial.getId());
        }catch(ParticipantNotInMeetingException e){
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Der Teilnehmer muss in der Veranstaltung sein");
            facesContext.addMessage(null, msg);
        } catch (EntityNotFoundException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Die Gruppe wurde nicht gefunden");
            facesContext.addMessage(null, msg);
        } catch (OutdatedException e) {
            FacesMessage msg = new FacesMessage("Die Gruppe ist veraltet. Bitte Seite neu laden.");
            facesContext.addMessage(null, msg);
        }

    }

    /**
     * Adds a participant.
     *
     * @param participant participant to add
     */
    public void addParticipant(Participant participant) {
        if (!participantsToAdd.contains(participant)) {
            participantsToAdd.add(participant);
            participantsInTutorial.remove(participant);
            if (filteredParticipantsInTutorial != null) {
                filteredParticipantsInTutorial.remove(participant);
            }
            if (filteredParticipantsToAdd != null) {
                filteredParticipantsToAdd.add(participant);
            }
        } else {

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
            participantsInTutorial.add(participant);
            if (filteredParticipantsInTutorial != null) {
                filteredParticipantsInTutorial.add(participant);
            }
            if (filteredParticipantsToAdd != null) {
                filteredParticipantsToAdd.remove(participant);
            }
        } else {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Der Teilnehmer wurde bereits aus der Veranstaltung entfernt");
            facesContext.addMessage(null, msg);
        }
    }

}
