package de.unibremen.swp2.service;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.*;
import de.unibremen.swp2.persistence.Exceptions.*;
import de.unibremen.swp2.persistence.Interceptors.LockFinalGradeDAO;
import de.unibremen.swp2.persistence.Interceptors.LockMeetingDAO;
import de.unibremen.swp2.persistence.Interceptors.LockParticipantDAO;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author Martin
 * Allows to edit,update,delete,creat and to perform other operations
 * on a FinalGrade (stored in {@link #finalGradeDAO}).
 */
@Transactional(rollbackOn = PersistenceException.class)
@RequestScoped
public class FinalGradeService {

    @Inject
    private FinalGradeDAO finalGradeDAO;

    @Inject
    private SubmissionDAO submissionDAO;

    @Inject
    private EvaluationDAO evaluationDAO;

    @Inject
    private ExamDAO examDAO;

    /**
     * creates the given finalGrade.
     *
     * @param finalGrade FinalGrade to create.
     */
    @LockMeetingDAO
    @LockFinalGradeDAO
    @LockParticipantDAO
    public void create(final FinalGrade finalGrade) throws OutdatedException, EntityNotFoundException {
        if (finalGrade.getVersion() == null) {
            finalGradeDAO.insert(finalGrade);
        } else {
            finalGradeDAO.update(finalGrade);
        }
    }

    /**
     * Returns the finalGrade with given participant and meeting.
     *
     * @param participant Participant of finalGrade in question.
     * @param meeting     Meeting of finalGrade in question.
     * @return FinalGrade with given participant and meeting.
     */
    public FinalGrade getFinalGradeByParticipantAndMeeting(final ParticipantStatus participant, final Meeting meeting) {
        try {
            return finalGradeDAO.getFinalGradeByParticipantAndMeeting(participant, meeting);
        } catch (NoResultException e) {
            FinalGrade finalGrade = new FinalGrade();
            finalGrade.setMeeting(meeting);
            finalGrade.setParticipantStatus(participant);
            return finalGrade;
        }
    }

    /**
     * gets the final grade of a participant.
     * @param participant
     * @param meeting
     * @return
     */
    public FinalGrade getFinalGrade(final Participant participant, final Meeting meeting) {
        try {
            return finalGradeDAO.getFinalGrade(participant, meeting);
        } catch (NoResultException e) {
            return new FinalGrade();
        }
    }

    /**
     * calculates the grade of a submission.
     * @param points the achieved points.
     * @param maxPoints the maximum points
     * @return the grade.
     * @throws InvalidGradeException
     */
    public BigDecimal calculateSingleSubmissionGrade(final BigDecimal points, final BigDecimal maxPoints) throws InvalidGradeException {
        if (maxPoints.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidGradeException();
        } else {
            final int percentage = points.divide(maxPoints, 2, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).intValue();
            return returnGradeByPercentage(percentage);
        }
    }

    /**
     * @param percentage a percentage in int.
     * @return the Grade to a percenteage.
     */
    public BigDecimal returnGradeByPercentage(final int percentage) {
        if (percentage >= 95) {
            return new BigDecimal("1.0");
        } else if (percentage >= 90) {
            return new BigDecimal("1.3");
        } else if (percentage >= 85) {
            return new BigDecimal("1.7");
        } else if (percentage >= 80) {
            return new BigDecimal("2.0");
        } else if (percentage >= 75) {
            return new BigDecimal("2.3");
        } else if (percentage >= 70) {
            return new BigDecimal("2.7");
        } else if (percentage >= 65) {
            return new BigDecimal("3.0");
        } else if (percentage >= 60) {
            return new BigDecimal("3.3");
        } else if (percentage >= 55) {
            return new BigDecimal("3.7");
        } else if (percentage >= 50) {
            return new BigDecimal("4.0");
        } else {
            return new BigDecimal("5.0");
        }
    }

    /**
     * rounds the grade.
     * @param finalGrade the final grade to be rounded
     * @return the actual final grade.
     */
    private BigDecimal roundGrade(final BigDecimal finalGrade) {
        if (finalGrade.compareTo(new BigDecimal("1.15")) <= 0) {
            return new BigDecimal("1.0");
        } else if (finalGrade.compareTo(new BigDecimal("1.50")) <= 0) {
            return new BigDecimal("1.3");
        } else if (finalGrade.compareTo(new BigDecimal("1.85")) <= 0) {
            return new BigDecimal("1.7");
        } else if (finalGrade.compareTo(new BigDecimal("2.15")) <= 0) {
            return new BigDecimal("2.0");
        } else if (finalGrade.compareTo(new BigDecimal("2.50")) <= 0) {
            return new BigDecimal("2.3");
        } else if (finalGrade.compareTo(new BigDecimal("2.85")) <= 0) {
            return new BigDecimal("2.7");
        } else if (finalGrade.compareTo(new BigDecimal("3.15")) <= 0) {
            return new BigDecimal("3.0");
        } else if (finalGrade.compareTo(new BigDecimal("3.50")) <= 0) {
            return new BigDecimal("3.3");
        } else if (finalGrade.compareTo(new BigDecimal("3.85")) <= 0) {
            return new BigDecimal("3.7");
        } else if (finalGrade.compareTo(new BigDecimal("4.0")) <= 0) {
            return new BigDecimal("4.0");
        } else {
            return new BigDecimal("5.0");
        }
    }

    /**
     * claculates the final grade.
     * @param status status of the participant in a meeting.
     * @param participant the participant.
     * @param meeting the meeting.
     * @param gradesOrPoints defines if the grade is calculated by grades or points.
     * @param anyOrPercent defines if the points per submission don't matter or do matter.
     * @param nMinus1 defines if if calculated with n-1 or not.
     * @param nPercentage the >=% number
     * @param newCalc defines if the grades of a submission are newly calculated or if it takes them from the evaluation.
     * @return returns the final grade.
     * @throws InvalidWeightingException
     * @throws NotGradedException
     */
    public BigDecimal calculateFinalGrade(final ParticipantStatus status, final Participant participant,
                                          final Meeting meeting, final boolean gradesOrPoints,
                                          final boolean anyOrPercent, final boolean nMinus1, final int nPercentage,
                                          final boolean newCalc) throws InvalidWeightingException, NotGradedException {
        Exam exam = null;
        Evaluation examEval = null;
        if (meeting.getSubmissionWeighting().compareTo(new BigDecimal("100")) < 0) {
            try {
                exam = examDAO.getExamByMeeting(meeting);
                examEval = evaluationDAO.getEvaluationByExamAndParticipant(exam, status);
                if (exam.getWeighting().add(meeting.getSubmissionWeighting()).compareTo(new BigDecimal("100")) != 0) {
                    throw new InvalidWeightingException();
                }
                if (exam.getWeighting().compareTo(new BigDecimal("100")) == 0) {
                    return examEval.getGrade();
                }
                if (examEval.getGrade().compareTo(new BigDecimal("4.0")) > 0) {
                    return new BigDecimal("5.0");
                }
                BigDecimal submissionGrade = calculateSubmissionGrade(participant, meeting, gradesOrPoints,
                        anyOrPercent, nMinus1, nPercentage, newCalc);
                if (submissionGrade.compareTo(new BigDecimal("4.0")) > 0) {
                    return new BigDecimal("5.0");
                }
                BigDecimal finalGrade =
                        examEval.getGrade().multiply(exam.getWeighting()).add(submissionGrade.multiply(meeting.getSubmissionWeighting())).divide(new BigDecimal("100"));
                return roundGrade(finalGrade);
            } catch (NoResultException e) {
                throw new NotGradedException();
            }
        } else {
            return calculateSubmissionGrade(participant, meeting, gradesOrPoints, anyOrPercent, nMinus1, nPercentage,
                    newCalc);
        }
    }

    public BigDecimal calculateSubmissionGradeHelper(final Participant participant, final Meeting meeting,
                                                     final boolean gradesOrPoints, final boolean anyOrNPercent,
                                                     final boolean nMinus1, final int nPercentage, final boolean newCalc) throws NotGradedException {
        BigDecimal finalGrade = calculateSubmissionGrade(participant, meeting, gradesOrPoints, anyOrNPercent, nMinus1, nPercentage, newCalc);
        if (finalGrade.compareTo(new BigDecimal("4.0")) > 0) {
            return new BigDecimal("5.0");
        } else {
            return finalGrade;
        }
    }

    /**
     * calculates the grade of all submissions combined.
     * @param participant same as above.
     * @param meeting same as above.
     * @param gradesOrPoints same as above.
     * @param anyOrNPercent same as above.
     * @param nMinus1 same as above.
     * @param nPercentage same as above.
     * @param newCalc same as above.
     * @return
     * @throws NotGradedException
     */
    private BigDecimal calculateSubmissionGrade(final Participant participant, final Meeting meeting,
                                               final boolean gradesOrPoints, final boolean anyOrNPercent,
                                               final boolean nMinus1, final int nPercentage, final boolean newCalc) throws NotGradedException {
        final List<Submission> submissions = submissionDAO.getSubmissionByMeeting(meeting);
        final List<Evaluation> evaluations = evaluationDAO.getEvaluationsByParticipantAndMeeting(participant, meeting);
        if (submissions.size() != evaluations.size() && submissions.size() != 0) {
            throw new NotGradedException();
        }
        if (gradesOrPoints) {
            if (anyOrNPercent) {
                if (newCalc) {
                    return calculateByGrades(submissions, evaluations);
                } else {
                    return calculateBySetGrades(submissions, evaluations);
                }
            } else {
                if (nMinus1) {
                    int i = 0;
                    List<Evaluation> copy = new ArrayList<>(evaluations);
                    for (Evaluation e : evaluations) {
                        if (e.getPoints().divide(e.getSubmission().getMaxGrade(), 2, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).compareTo(BigDecimal.valueOf(nPercentage)) < 0) {
                            i++;
                            copy.remove(e);
                            submissions.remove(e.getSubmission());
                        }
                    }
                    if (i <= 1) {
                        if (newCalc) {
                            return calculateByGrades(submissions, copy);
                        } else {
                            return calculateBySetGrades(submissions, copy);
                        }
                    } else {
                        return new BigDecimal("5.0");
                    }
                } else {
                    for (Evaluation e : evaluations) {
                        if (e.getPoints().divide(e.getSubmission().getMaxGrade(), 2, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).compareTo(BigDecimal.valueOf(nPercentage)) < 0) {
                            return new BigDecimal("5.0");
                        }
                    }
                    if (newCalc) {
                        return calculateByGrades(submissions, evaluations);
                    } else {
                        return calculateBySetGrades(submissions, evaluations);
                    }
                }
            }
        } else {
            if (anyOrNPercent) {
                return calculateByPoints(submissions, evaluations);
            } else {
                if (nMinus1) {
                    int i = 0;
                    List<Evaluation> copy = new ArrayList<>(evaluations);
                    for (Evaluation e : evaluations) {
                        if (e.getPoints().divide(e.getSubmission().getMaxGrade(), 2, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).compareTo(BigDecimal.valueOf(nPercentage)) < 0) {
                            i++;
                            copy.remove(e);
                            submissions.remove(e.getSubmission());
                        }
                    }
                    if (i <= 1) {
                        return calculateByPoints(submissions, copy);
                    } else {
                        return new BigDecimal("5.0");
                    }
                } else {
                    for (Evaluation e : evaluations) {
                        if (e.getPoints().divide(e.getSubmission().getMaxGrade(), 2, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).compareTo(BigDecimal.valueOf(nPercentage)) < 0) {
                            return new BigDecimal("5.0");
                        }
                    }
                    return calculateByPoints(submissions, evaluations);
                }
            }
        }
    }

    /**
     * calculates the grade of all submissions by points.
     * @param submissions the submissions.
     * @param evaluations the evaluations to the submissions.
     * @return the grade.
     * @throws NotGradedException
     */
    private BigDecimal calculateByPoints(final List<Submission> submissions, final List<Evaluation> evaluations) throws NotGradedException {
        checkSubSize(submissions);
        BigDecimal maxPoints = new BigDecimal("0");
        for (Submission s : submissions) {
            maxPoints = maxPoints.add(s.getMaxGrade().multiply(s.getWeighting()));
        }
        BigDecimal points = new BigDecimal("0");
        for (Evaluation e : evaluations) {
            points = points.add(e.getPoints().multiply(e.getSubmission().getWeighting()));
        }
        final int percentage =
                points.divide(maxPoints, 2, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).intValue();
        return returnGradeByPercentage(percentage);
    }

    /**
     * calculates the grade of all submissions by newly calculated grades.
     * @param submissions the submissions.
     * @param evaluations the evaluations to the submissions.
     * @return the grade.
     * @throws NotGradedException
     */
    private BigDecimal calculateByGrades(final List<Submission> submissions, final List<Evaluation> evaluations) throws NotGradedException {
        checkSubSize(submissions);
        BigDecimal divider = new BigDecimal("0");
        for (Submission s : submissions) {
            divider = divider.add(s.getWeighting());
        }
        BigDecimal grade = new BigDecimal("0");
        for (Evaluation e : evaluations) {
            final int percentage =
                    e.getPoints().divide(e.getSubmission().getMaxGrade(), 2, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).intValue();
            grade = grade.add(returnGradeByPercentage(percentage).multiply(e.getSubmission().getWeighting()));
        }
        return grade.divide(divider, 2, RoundingMode.HALF_UP);
    }

    /**
     * calculates the grade of all submissions by the grades set by a user.
     * @param submissions the submissions.
     * @param evaluations the evaluations to the submissions.
     * @return
     * @throws NotGradedException
     */
    private BigDecimal calculateBySetGrades(final List<Submission> submissions, final List<Evaluation> evaluations) throws NotGradedException {
        checkSubSize(submissions);
        BigDecimal divider = new BigDecimal("0");
        for (Submission s : submissions) {
            divider = divider.add(s.getWeighting());
        }
        BigDecimal grade = new BigDecimal("0");
        for (Evaluation e : evaluations) {
            grade = grade.add(e.getGrade().multiply(e.getSubmission().getWeighting()));
        }
        return grade.divide(divider, 2, RoundingMode.HALF_UP);
    }

    /**
     * checks if there are submissions(dividing by zero)
     * @param submissions the submissions
     * @throws NotGradedException
     */
    private void checkSubSize(final List<Submission> submissions) throws NotGradedException {
        if (submissions.size() == 0) {
            throw new NotGradedException();
        }
    }
}