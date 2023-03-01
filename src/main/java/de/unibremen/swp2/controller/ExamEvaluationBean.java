package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.Exceptions.EntityNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.HasEvaluationsException;
import de.unibremen.swp2.persistence.Exceptions.InvalidGradeException;
import de.unibremen.swp2.persistence.Exceptions.OutdatedException;
import de.unibremen.swp2.security.GlobalSecure;
import de.unibremen.swp2.security.MeetingRole;
import de.unibremen.swp2.service.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.RowEditEvent;

import javax.annotation.PostConstruct;
import javax.faces.annotation.FacesConfig;
import javax.faces.annotation.RequestParameterMap;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
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
 * @Author Martin
 * Allows the user to evaluate an exam.
 */
@Named
@ViewScoped
@FacesConfig
@GlobalSecure(roles = {Role.T ,Role.D, Role.A})
public class ExamEvaluationBean implements Serializable {

    /**
     * Meeting of the Evaluation
     */
    @Getter
    private Meeting meeting;

    /**
     * Copied Evaluation
     */
    private Evaluation copiedEvaluation;

    /**
     * Copied User
     */
    private User copiedUser;

    /**
     * The evaluation for the chosen exam loaded from EvaluationDAO.
     */
    @Getter
    private List<EvaluationToExam> evaluation;

    /**
     * User currently logged in
     */
    @Inject
    private Principal principal;

    /**
     * Role of an User
     */
    @Getter
    private Role role;

    /**
     * User to edit
     */
    private User user;

    /**
     * The chosen exam.
     */
    @Getter
    private Exam exam;

    /**
     * Allows to  update, reset a password, create and other operations
     */
    @Inject
    private UserService userService;

    /**
     * Allows to create, update, delete and other operations of an Exam
     */
    @Inject
    private ExamService examService;

    /**
     * Allows to createParticipantEvaluation, createGroupEvaluation, createEvaluationHelperForGroup, update, delete and other operations of an Evaluation
     */
    @Inject
    private EvaluationService evaluationService;
    /**
     * Allows to create, update and other operations of an Participant
     */
    @Inject
    private ParticipantService participantService;

    /**
     * Allows to addCEOToMeeting, create, update, delete and other operations of an Meeting
     */
    @Inject
    private MeetingService meetingService;

    /**
     * Used to redirection
     */
    @Inject
    private ExternalContext externalContext;

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

    /**
     * Initializes this ExamEvaluationBean
     */
    @PostConstruct
    private void init() {
        final String examId = parameterMap.get("exam-Id");
        if (examId != null) {
            exam = examService.getById(examId);
            if (exam != null) {
                try {
                    meeting = meetingService.getMeetingsByExam(exam);
                    user = userService.getUsersByEmail(principal.getName());
                    if (user.getRole().equals(Role.A)) {
                        role = Role.A;
                    } else {
                        try {
                            final UserMeetingRole userMeetingRole =
                                    userService.getUserMeetingRoleByUserAndMeeting(user, meeting);
                            role = userMeetingRole.getRole();
                        } catch (NoResultException ignored) {
                        }
                    }
                } catch (NoResultException ignored) {
                }
            }
            if (role != null && (meeting.getVisible() || role.equals(Role.D) || role.equals(Role.A))) {
                List<ParticipantStatus> participantStatus = participantService.getParticipantStatusByMeeting(meeting);
                evaluation = evaluationService.getEvaluationsByExamAndParticipants(exam, participantStatus);
            }
        }
    }

    /**
     *Initializes the Edit
     */
    public void onRowEditInit(RowEditEvent<EvaluationToExam> event) {
        EvaluationToExam evaluationToExam = event.getObject();
        copiedEvaluation = new Evaluation(evaluationToExam.getEvaluation());
        if (evaluationToExam.getUser() != null) {
            copiedUser = new User(evaluationToExam.getUser());
        }
    }

    /**
     *Edit on Row
     */
    @MeetingRole(allowedRoles = {Role.A, Role.D})
    public void onRowEdit(RowEditEvent<EvaluationToExam> event) {
        EvaluationToExam evaluationToExam = event.getObject();
        evaluationToExam.setUser(user);
        evaluationToExam.getEvaluation().setUser(user);
        try {
            evaluationService.createExamEvaluation(evaluationToExam.getEvaluation());
            evaluationToExam.setEvaluation(evaluationService.getById(evaluationToExam.getEvaluation().getId()));
        } catch (OutdatedException e) {
            failedEdit(evaluationToExam);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Daten veraltet. Seite bitte neu laden.");
            facesContext.addMessage(null, msg);
        } catch (EntityNotFoundException e) {
            failedEdit(evaluationToExam);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Prüfung nicht gefunden.");
            facesContext.addMessage(null, msg);
        } catch (InvalidGradeException e) {
            failedEdit(evaluationToExam);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Eine Note muss größer 0 und kleiner 5 sein.");
            facesContext.addMessage(null, msg);
        }
    }

    /**
     * Failed to edit
     */
    private void failedEdit(EvaluationToExam evaluationToExam) {
        evaluationToExam.setEvaluation(copiedEvaluation);
        evaluationToExam.setUser(copiedUser);
    }

    /**
     * Delete a grade of an Examevaluation
     */
    @MeetingRole(allowedRoles = {Role.A, Role.D})
    public void deleteGrade(EvaluationToExam evaluationToExam) {
        try {
            evaluationService.delete(evaluationToExam.getEvaluation());
            evaluationToExam.setUser(null);
            evaluationToExam.setEvaluation(evaluationService.createNewEvaluationByExamAndParticipants(exam, evaluationToExam.getEvaluation().getParticipantStatus()));
        } catch (EntityNotFoundException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Es existiert noch keine Bewertung für diesen Teilnehmer.");
            facesContext.addMessage(null, msg);
        }
    }

    /**
     *Delete an Exam
     */
    @MeetingRole(allowedRoles = {Role.D, Role.A})
    public void deleteExam() throws IOException {
        try {
            examService.delete(exam);
            externalContext.redirect("meeting.xhtml?meeting-Id=" + meeting.getId());
        } catch (HasEvaluationsException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Es existieren bereits Bewertungen für diese Prüfung.");
            facesContext.addMessage(null, msg);
        } catch (EntityNotFoundException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Prüfung wurde bereits gelöscht.");
            facesContext.addMessage(null, msg);
        }
    }

}