package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.Exceptions.EntityNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.HasEvaluationsException;
import de.unibremen.swp2.persistence.Exceptions.OutdatedException;
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
import java.util.List;
import java.util.Map;

/**
 * @Author Tommy, Martin
 * Allows to load, delete, and update a meeting.
 */
@ViewScoped
@Named
@FacesConfig
@GlobalSecure(roles = {Role.T, Role.D, Role.A})
public class MeetingBean implements Serializable {

    /**
     * Role of an User
     */
    @Getter
    private Role role;

    /**
     * The chosen meeting loaded from MeetingDAO.
     */
    @Getter
    private Meeting meeting;

    /**
     * name of an meeting
     */
    @Getter
    private String meetingName;

    /**
     * The participants in meeting loaded from ParticpantDAO.
     */


    @Getter
    private List<ParticipantWithStatus> participants;

    /**
     * The participants in meeting loaded from ParticpantDAO filtered.
     */
    @Getter
    @Setter
    private List<ParticipantWithStatus> filteredParticipants;

    /**
     * The submissions in meeting loaded from SubmissionDAO.
     */
    @Getter
    private List<Submission> submissions;

    /**
     * The tutorials in meeting loaded from TutorialDAO.
     */
    @Getter
    private List<Tutorial> tutorials;

    /**
     * The tutorials in meeting loaded from TutorialDAO filtered.
     */
    @Getter
    @Setter
    private List<Tutorial> filteredTutorials;

    /**
     * The lecturers in meeting loaded from UserDAO.
     */
    @Getter
    private List<UserWithRole> users;

    /**
     * The user loaded from UserDAO filtered
     */
    @Getter
    @Setter
    private List<UserWithRole> filteredUsers;

    /**
     * The exams in meeting loaded from ExamDAO.
     */
    @Getter
    private List<Exam> exams;

    /**
     * User currently logged in
     */
    @Inject
    private Principal principal;

    /**
     * Allows to  update, reset a password, create and other operations
     */
    @Inject
    private UserService userService;

    /**
     * Allows to create, update and other operations
     */
    @Inject
    private MeetingService meetingService;

    /**
     * Allows to create, update and other operations
     */
    @Inject
    private SubmissionService submissionService;

    /**
     * Allows to create, update and other operations
     */
    @Inject
    private ParticipantService participantService;

    /**
     * Allows to create, update and other operations
     */
    @Inject
    private TutorialService tutorialService;

    /**
     * Used to redirection
     */
    @Inject
    private ExternalContext externalContext;

    /**
     * Allows to create, update and other operations
     */

    @Inject
    private ExamService examService;

    /**
     * Used to add faces messages (in case of errors) or to complete
     * authentification (in case of success)
     */
    @Inject
    private FacesContext facesContext;

    /**
     * Parameter-Map which provides the id
     */
    @Inject
    @RequestParameterMap
    private Map<String, String> parameterMap;

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
                if (role != null && (meeting.getVisible() || role.equals(Role.D) || role.equals(Role.A))) {
                    meetingName = meeting.getName();
                    participants = participantService.getAllParticipantsWithStatusByMeeting(meeting);
                    users = userService.getUsersWithRoleByMeeting(meeting);
                    submissions = submissionService.getSubmissionByMeeting(meeting);
                    if (!role.equals(Role.A) && !role.equals(Role.CEO) && !role.equals(Role.D)) {
                        tutorials = tutorialService.getTutorialByUserAndMeeting(meeting, user);
                    } else {
                        tutorials = tutorialService.getTutorialsByMeeting(meeting);
                        exams = examService.getExamsByMeeting(meeting);
                    }
                }
            }
        }
    }

    /**
     * set name of an meeting
     * @param name
     */
    @MeetingRole(allowedRoles = {Role.D, Role.A})
    public void setMeetingName(String name) {
        meeting.setName(name);
        try {
            meetingService.updateMeetingOnly(meeting);
            meeting = meetingService.getById(meeting.getId());
            meetingName = meeting.getName();
        } catch (EntityNotFoundException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Veranstaltung wurde " +
                    "nicht gefunden");
            facesContext.addMessage(null, msg);
        } catch (OutdatedException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                    "Die Daten sind veraltet" + "." + " Seite bitte neu laden.");
            facesContext.addMessage(null, msg);
        }
    }

    /**
     * delete a meeting
     * @throws IOException
     */
    @MeetingRole(allowedRoles = {Role.D, Role.A})
    public void deleteMeeting() throws IOException {
        try {
            meetingService.delete(meeting);
            externalContext.redirect("home.xhtml");
        } catch (HasEvaluationsException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "In dieser Veranstaltung gibt es schon Bewertungen.");
            facesContext.addMessage(null, msg);
        }
    }

    /**
     * Deletes an exam.
     *
     * @param exam exam to delete
     */
    @MeetingRole(allowedRoles = {Role.D, Role.A})
    public void deleteExam(Exam exam) {
        try {
            examService.delete(exam);
            exams.remove(exam);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Erfolg", "Die Prüfung wurde erfolgreich " +
                    "gelöscht.");
            facesContext.addMessage(null, msg);
        } catch (HasEvaluationsException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Es existieren bereits " +
                    "Bewertungen für diese Prüfung.");
            facesContext.addMessage(null, msg);
        } catch (EntityNotFoundException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Prüfung wurde bereits gelöscht" +
                    ".");
            facesContext.addMessage(null, msg);
        }
    }

    /**
     * Updates the name of the edited meeting.
     *
     * @param participant The event object of the ajax event.
     */
    public void onItemSelectedListener(final ParticipantWithStatus participant) {
        try {
            participantService.updateParticipantStatus(participant.getMeetingStatus());
            final ParticipantStatus updated = participantService.getParticipantStatusById(participant.getMeetingStatus().getId());
            if (updated != null) {
                participant.setMeetingStatus(updated);
            }
        } catch (EntityNotFoundException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Teilnehmer nicht " +
                    "gefunden!");
            facesContext.addMessage(null, msg);
        } catch (OutdatedException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Datensatz veraltet. " +
                    "Bitte Seite neu laden.");
            facesContext.addMessage(null, msg);
        }
    }


}
