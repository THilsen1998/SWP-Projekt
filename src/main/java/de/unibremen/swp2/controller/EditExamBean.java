package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.Exceptions.EntityNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.HasEvaluationsException;
import de.unibremen.swp2.persistence.Exceptions.OutdatedException;
import de.unibremen.swp2.security.GlobalSecure;
import de.unibremen.swp2.security.MeetingRole;
import de.unibremen.swp2.service.ExamService;
import de.unibremen.swp2.service.MeetingService;
import de.unibremen.swp2.service.UserService;
import lombok.Getter;

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
import java.util.Map;

/**
 * @Author Tommy, Theo
 * Allows to edit a exam .
 */
@Named
@ViewScoped
@FacesConfig
@GlobalSecure(roles = {Role.D, Role.A})
public class EditExamBean implements Serializable {

    /**
     * Exam to edit.
     */
    @Getter
    private Exam exam;

    /**
     * Global roles of a user
     */
    @Getter
    private Role role;

    /**
     * Meeting of the exam
     */
    private Meeting meeting;

    /**
     * User currently locked in
     */
    @Inject
    private Principal principal;

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
     * Parameter-Map which provides the id
     */
    @Inject
    @RequestParameterMap
    private Map<String,String> parameterMap;

    /**
     * Allows to edit,update,delete,creat and to perform other operations
     * on a Exam
     */
    @Inject
    private ExamService examService;

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
     * Initializes this bean.
     */
    @PostConstruct
    private void init() {
        final String id = parameterMap.get("exam-Id");
        if (id != null) {
            exam = examService.getById(id);
            if (exam != null) {
                final User user = userService.getUsersByEmail(principal.getName());
                if (user.getRole().equals(Role.A)) {
                    role = Role.A;
                } else {
                    try {
                        meeting = meetingService.getMeetingsByExam(exam);
                        final UserMeetingRole userMeetingRole = userService.getUserMeetingRoleByUserAndMeeting(user, meeting);
                        role = userMeetingRole.getRole();
                    } catch (NoResultException ignored) {}
                }
            }
        }
    }

    /**
     * Updates {@link #exam}. On success, a redirect to 'meeting.xhtml' is
     * registered.
     */
    @MeetingRole(allowedRoles = {Role.D, Role.A})
    public void update() throws IOException {
        try {
            examService.update(exam);
            externalContext.redirect("exam-evaluation.xhtml?exam-Id=" + exam.getId());
        } catch (OutdatedException e) {
            FacesMessage msg = new FacesMessage("Die Prüfung ist veraltet. Bitte Seite neu laden.");
            facesContext.addMessage(null, msg);
        } catch (EntityNotFoundException e) {
            FacesMessage msg = new FacesMessage("Error:", "Die Prüfung konnte nicht gefunden werden");
            facesContext.addMessage(null, msg);
        }
    }

    /**
     * Deletes {@link #exam}. On success, a redirect to 'meeting.xhtml' is
     * registered.
     */
    @MeetingRole(allowedRoles = {Role.D, Role.A})
    public void deleteExam() throws IOException {
        try {
            examService.delete(exam);
            externalContext.redirect("meeting.xhtml?meeting-Id=" + meeting.getId());
        } catch (HasEvaluationsException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Es bestehen bereits Bewertungen zu der Prüfung");
            facesContext.addMessage(null, msg);
        } catch (EntityNotFoundException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Prüfung oder Veranstaltung nicht gefunden.");
            facesContext.addMessage(null, msg);
        }
    }

}
