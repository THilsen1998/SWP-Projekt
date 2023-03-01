package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.*;
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
import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author Theo, mArtin
 * Allows to edit a group group.
 */
@Named
@ViewScoped
@FacesConfig
@GlobalSecure(roles = {Role.D, Role.A, Role.T})
public class GroupBean implements Serializable {

    /**
     * Role of an User
     */
    @Getter
    private Role role;

    /**
     * Used to add faces messages (in case of errors) or to complete
     * authentification (in case of success)
     */
    @Inject
    private FacesContext facesContext;

    /**
     * Used to redirection
     */
    @Inject
    private ExternalContext externalContext;

    /**
     * Allows to create, update and other operations of an tutorial
     */
    @Inject
    private TutorialService tutorialService;

    /**
     * Allows to create, update and other operations of an participant
     */
    @Inject
    private ParticipantService participantService;

    /**
     * Allows to create, update and other operation of an group
     */
    @Inject
    private GroupService groupService;

    /**
     * Allows to  update, reset a password, create and other operations
     */
    @Inject
    private UserService userService;

    /**
     * Allows to addCEOToMeeting, create, update, delete and other operations of an Meeting
     */
    @Inject
    private MeetingService meetingService;

    /**
     * User currently logged in
     */
    @Inject
    private Principal principal;

    /**
     * Chosen group.
     */
    @Getter
    private TGroup group;

    /**
     * New Group for splitting.
     */
    @Getter
    private TGroup newGroup;

    /**
     * Tutorial to add.
     */
    @Getter
    private Tutorial tutorial;

    /**
     * Tutorial to add.
     */
    @Getter
    private Meeting meeting;

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
     * Participants that are freshly added to the Group
     */
    @Getter
    private List<Participant> participantsToAdd;

    /**
     *Participants that are freshly added to the Group filtered
     */
    @Getter
    @Setter
    private List<Participant> filteredParticipantsToAdd;

    /**
     *notifys the Component to show up ( Split Group button was clicked)
     */
    @Getter
    private boolean split;

    /**
     * Splittet group
     */
    @Getter
    private boolean groupSplit;

    /**
     *Participants that are freshly added to the new Group
     */
    @Getter
    private List<Participant> newGroupParticipantsToAdd;

    /**
     *Participants that are freshly added to the new Group filtered
     */
    @Getter
    @Setter
    private List<Participant> filteredNewGroupParticipantsToAdd;

    /**
     *Participants that are in Tutorial and avalibile to add to new Group.
     */
    @Getter
    private List<Participant> participantsInTutorialForNewGroup;


    /**
     *Participants that are freshly added to the new Group filtered
     */
    @Getter
    @Setter
    private List<Participant> filteredParticipantsInTutorialForNewGroup;

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
        final String id = parameterMap.get("group-Id");
        if (id != null) {
            group = groupService.getById(id);
            if (group != null) {
                tutorial = tutorialService.getTutorialByGroup(group);

                final User user = userService.getUsersByEmail(principal.getName());
                if (user.getRole().equals(Role.A)) {
                    role = Role.A;
                } else {
                    try {
                        final UserMeetingRole userMeetingRole = userService.getUserMeetingRoleByTutorialAndUser(tutorial, user);
                        role = userMeetingRole.getRole();
                    } catch (NoResultException e) {
                        try {
                            final Meeting meeting = meetingService.getMeetingByTutorial(tutorial);
                            final UserMeetingRole userMeetingRole = userService.getUserMeetingRoleByUserAndMeeting(user, meeting);
                            role = userMeetingRole.getRole();
                            if (!role.equals(Role.T)) {

                                role = userMeetingRole.getRole();
                            }
                        } catch (NoResultException ignored) {
                        }
                    }
                }
                if (role != null) {

                    meeting = meetingService.getMeetingByTutorial(tutorial);

                    participantsToAdd = participantService.getParticipantsByGroup(group);


                    participantsInTutorial = participantService.getParticipantsNotInAnyGroup(tutorial);

                    newGroup = new TGroup();
                    newGroup.setTutorial(tutorial);
                    newGroupParticipantsToAdd = new ArrayList<>();
                    participantsInTutorialForNewGroup = participantService.getAllParticipantsByTutorial(tutorial);

                    groupSplit = meeting.getOnlyGroupSplit();
                }
            }
        }
    }

    /**
     * Updates {@link #group}. On success, a redirect to 'tutorial.xhtml' is
     * registered.
     */
    public void update() throws IOException {
        try {
            groupService.update(group, participantsToAdd);
            externalContext.redirect("single-tutorial.xhtml?tutorial-Id=" + tutorial.getId());
        } catch (EntityNotFoundException e) {
            FacesMessage msg = new FacesMessage("Die Gruppe ist nicht mehr im System.");
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

        //for normal use
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

        //for normal use
        if (participantsToAdd.contains(participant) && !groupSplit) {
            participantsToAdd.remove(participant);
            participantsInTutorial.add(participant);
            if (filteredParticipantsInTutorial != null && !groupSplit) {
                filteredParticipantsInTutorial.add(participant);
            }
            if (filteredParticipantsToAdd != null && !groupSplit) {
                filteredParticipantsToAdd.remove(participant);
            }
        } else {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Der Teilnehmer wurde bereits aus der Veranstaltung entfernt");
            facesContext.addMessage(null, msg);
        }

        //only group switching allowed
        if (participantsToAdd.contains(participant) && groupSplit) {
            participantsToAdd.remove(participant);
            newGroupParticipantsToAdd.add(participant);
            if (filteredNewGroupParticipantsToAdd != null && groupSplit) {
                filteredNewGroupParticipantsToAdd.add(participant);
            }
            if (filteredParticipantsToAdd != null && groupSplit) {
                filteredParticipantsToAdd.remove(participant);
            }
        } else {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Der Teilnehmer wurde bereits aus der Veranstaltung entfernt");
            facesContext.addMessage(null, msg);
        }
    }

    /**
     *If split button was pushed turn split to ture;
     */
    @MeetingRole(allowedRoles = {Role.A, Role.D, Role.CEO})
    public void showTable() {


        split = true;

        final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Info:", "Eine neue Gruppe mit neuen Teilnehmern kann nun hinzugefügt werden");
        facesContext.addMessage(null, msg);

    }


    /**
     * Creates one new Group and updates old one
     */
    public void createGroups() throws IOException {

        try {
            groupService.create(newGroup, newGroupParticipantsToAdd);

            groupService.update(group, participantsToAdd);

            externalContext.redirect("single-tutorial.xhtml?tutorial-Id=" + tutorial.getId());

        } catch (ParticipantNotInMeetingException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Der Teilnehmer muss in der Veranstaltung sein");
            facesContext.addMessage(null, msg);
        } catch (EntityNotFoundException e) {
            FacesMessage msg = new FacesMessage("Die Gruppe ist nicht mehr im System.");
            facesContext.addMessage(null, msg);
        } catch (OutdatedException e) {
            FacesMessage msg = new FacesMessage("Die Gruppen sind veraltet. Bitte Seite neu laden.");
            facesContext.addMessage(null, msg);
        }


    }


    /**
     * Adds a participant to new Group.
     *
     * @param participant participant to delete
     */
    public void addParticipantFromNewGroup(Participant participant) {
        if (!newGroupParticipantsToAdd.contains(participant) && !groupSplit) {
            newGroupParticipantsToAdd.add(participant);
            participantsInTutorialForNewGroup.remove(participant);
            if (filteredParticipantsInTutorialForNewGroup != null && !groupSplit) {
                filteredParticipantsInTutorialForNewGroup.remove(participant);
            }
            if (filteredNewGroupParticipantsToAdd != null && !groupSplit) {
                filteredNewGroupParticipantsToAdd.add(participant);
            }
        } else {

            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Der Teilnehmer ist bereits in der Veranstaltung");
            facesContext.addMessage(null, msg);
        }

    }

    /**
     * Deletes a participant from new Group.
     *
     * @param participant participant to delete
     */
    public void deleteParticipantsFromNewGroup(Participant participant) {
        //for normal use
        if (newGroupParticipantsToAdd.contains(participant) && !groupSplit) {
            newGroupParticipantsToAdd.remove(participant);
            participantsInTutorialForNewGroup.add(participant);
            if (filteredParticipantsInTutorialForNewGroup != null && !groupSplit) {
                filteredParticipantsInTutorialForNewGroup.add(participant);
            }
            if (filteredNewGroupParticipantsToAdd != null && !groupSplit) {
                filteredNewGroupParticipantsToAdd.remove(participant);
            }
        } else {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Der Teilnehmer wurde bereits aus der Veranstaltung entfernt");
            facesContext.addMessage(null, msg);
        }


        //----------------------------
        //only group switching allowed
        if (newGroupParticipantsToAdd.contains(participant) && groupSplit) {
            newGroupParticipantsToAdd.remove(participant);
            participantsToAdd.add(participant);
            if (filteredNewGroupParticipantsToAdd != null && groupSplit) {
                filteredNewGroupParticipantsToAdd.add(participant);
            }
            if (filteredParticipantsToAdd != null && groupSplit) {
                filteredParticipantsToAdd.remove(participant);
            }
        } else {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Der Teilnehmer wurde bereits aus der Veranstaltung entfernt");
            facesContext.addMessage(null, msg);
        }
    }


}


