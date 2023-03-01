package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.Exceptions.EntityNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.InvalidWeightingException;
import de.unibremen.swp2.persistence.Exceptions.NotGradedException;
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
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * @Author Dennis, Khaled
 * Allows to load, update and rate a participant to a meeting.
 */
@Named
@ViewScoped
@FacesConfig
@GlobalSecure(roles = {Role.T, Role.D, Role.A})
public class ParticipantMeetingBean implements Serializable {

    /**
     * Role of an User
     */
    @Getter
    private Role role;

    /**
     * count submission
     */
    @Getter
    private Long submissionCount;

    /**
     * List of Evaluations of submissions.
     */
    @Getter
    private List<Evaluation> evaluations;

    /**
     * Status of an participant
     */
    @Getter
    private ParticipantStatus status;

    /**
     * Chosen participant
     */
    @Getter
    private Participant participant;

    /**
     * Chosen meeting
     */
    @Getter
    private Meeting meeting;

    /**
     * Chosen finalgrade
     */
    @Getter
    private FinalGrade finalGrade;

    /**
     * Chosen examgrade
     */
    @Getter
    private Evaluation examGrade;

    /**
     * Chosen exam
     */
    @Getter
    private Exam exam;

    /**
     * boolean of grades or points
     */
    @Getter
    @Setter
    private boolean gradesOrPoints = false;

    /**
     * Boolean percent
     */
    @Getter
    @Setter
    private boolean anyOrPercent = true;

    /**
     * n-1
     */
    @Getter
    @Setter
    private boolean nMinus1 = false;

    @Getter
    @Setter
    private int nPercentage = 50;

    @Getter
    @Setter
    private boolean newCalc = true;
    /**
     * Boolean final or sub grade
     */
    @Getter
    private boolean finalOrSubGrade = true;

    /**
     * set final or sub
     */
    public void setFinalOrSubGradeTrue() {
        this.finalOrSubGrade = true;
    }

    /**
     * set final or sub Grade
     */
    public void setFinalOrSubGradeFalse() {
        this.finalOrSubGrade = false;
    }

    @Inject
    @RequestParameterMap
    private Map<String, String> parameterMap;

    /**
     * Allows to create, update and other operations
     */
    @Inject
    private ParticipantService participantService;

    /**
     * Allows to create, update and other operations
     */
    @Inject
    private MeetingService meetingService;

    /**
     * Allows to create, update and other operations
     */
    @Inject
    private UserService userService;

    /**
     * Allows to create, update and other operations
     */
    @Inject
    private EvaluationService evaluationService;

    /**
     * Allows to create, update and other operations
     */
    @Inject
    private SubmissionService submissionService;

    /**
     * Allows to create, update and other operations
     */
    @Inject
    private FinalGradeService finalGradeService;

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
     * Used to redirection
     */
    @Inject
    private ExternalContext externalContext;

    /**
     * User currently logged in
     */
    @Inject
    private Principal principal;

    /**
     * Initializes ParticipantMeetingBean
     */
    @PostConstruct
    private void init() {
        final String participantId = parameterMap.get("participant-Id");
        final String meetingId = parameterMap.get("meeting-Id");
        if (participantId != null && meetingId != null) {
            participant = participantService.getById(participantId);
            if (participant != null) {
                meeting = meetingService.getById(meetingId);
                if (meeting != null) {
                    try {
                        status = participantService.getParticipantStatusByParticipantAndMeeting(participant, meeting);
                        final User user = userService.getUsersByEmail(principal.getName());
                        if (user.getRole().equals(Role.A)) {
                            role = Role.A;
                        } else {
                            final UserMeetingRole userMeetingRole = userService.getUserMeetingRoleByUserAndMeeting(user, meeting);
                            role = userMeetingRole.getRole();
                        }
                    } catch (NoResultException ignored) {}
                    if (role != null && (meeting.getVisible() || role.equals(Role.D) || role.equals(Role.A))) {
                        submissionCount = submissionService.countSubmissionsByMeeting(meeting);
                        evaluations = evaluationService.getEvaluationsByParticipantAndMeeting(participant, meeting);
                        finalGrade = finalGradeService.getFinalGradeByParticipantAndMeeting(status, meeting);
                        try {
                            exam = examService.getExamByMeeting(meeting);
                            examGrade = evaluationService.getEvaluationByExamAndParticipant(exam, status);
                        } catch (NoResultException ignore) {}
                    }
                }
            }
        }
    }

    /**
     * Calculated Finalgrade
     */
    @MeetingRole(allowedRoles = {Role.D, Role.A})
    public void calculateFinalGrade() {
        try {
            if (finalOrSubGrade) {
                finalGrade.setOverallGrade(finalGradeService.calculateFinalGrade(status, participant, meeting, gradesOrPoints, anyOrPercent, nMinus1, nPercentage, newCalc));
                if (finalGrade.getOverallGrade().compareTo(new BigDecimal("5.0")) == 0) {
                    status.setMeetingStatus(ParticipantMeetingStatus.Durchgefallen);
                } else {
                    status.setMeetingStatus(ParticipantMeetingStatus.Bestanden);
                }
            } else {
                finalGrade.setSubmissionGrade(finalGradeService.calculateSubmissionGradeHelper(participant, meeting, gradesOrPoints, anyOrPercent, nMinus1, nPercentage, newCalc));
            }
        } catch (NotGradedException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Die Prüfung oder eine der Abgaben wurden noch nicht bewertet.");
            facesContext.addMessage(null, msg);
        } catch (InvalidWeightingException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Die Gewichtung ergib nicht 100%");
            facesContext.addMessage(null, msg);
        }
    }

    /**
     * Sets the status.
     */
    public void setStatus(ParticipantMeetingStatus newStatus) {
    }

    /**
     * Sets the finalGrade.
     */
    @MeetingRole(allowedRoles = {Role.D, Role.A})
    public void createFinalGrade() throws IOException {
        try {
            finalGradeService.create(finalGrade);
            externalContext.redirect("meeting.xhtml?meeting-Id=" + meeting.getId());
        } catch (OutdatedException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Daten veraltet. Seite bitte neu laden");
            facesContext.addMessage(null, msg);
        } catch (EntityNotFoundException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Daten nicht gefunden. Seite bitte neu laden");
            facesContext.addMessage(null, msg);
        }
    }

}
