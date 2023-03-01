package de.unibremen.swp2.service;

import de.unibremen.swp2.model.Meeting;
import de.unibremen.swp2.model.Submission;
import de.unibremen.swp2.model.Task;
import de.unibremen.swp2.persistence.Exceptions.*;
import de.unibremen.swp2.persistence.Interceptors.LockEvaluationDAO;
import de.unibremen.swp2.persistence.Interceptors.LockMeetingDAO;
import de.unibremen.swp2.persistence.Interceptors.LockSubmissionDAO;
import de.unibremen.swp2.persistence.Interceptors.LockTaskDAO;
import de.unibremen.swp2.persistence.SubmissionDAO;

import javax.ejb.Lock;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * @Author Martin
 *Allows to edit,update,delete,creat and to perform other operations
 *  on a Submission (stored in {@link #submissionDAO}).
 */
@RequestScoped
@Transactional(rollbackOn = PersistenceException.class)
public class SubmissionService
{

    @Inject
    private SubmissionDAO submissionDAO;

    /**
     * creates the given submission.
     *
     * @param submission
     *      Submission to create.
     */
    @LockMeetingDAO
    public void create(final Submission submission) throws MeetingNotFoundException {
            submissionDAO.insert(submission);
    }

    /**
     * Updates the given submission.
     *
     * @param submission
     *      Submission to update.
     * @throws NullPointerException
     *      If {@code submission} is {@code null}.
     */
    @LockMeetingDAO
    @LockSubmissionDAO
    public void update(final Submission submission) throws OutdatedException, EntityNotFoundException {
        submissionDAO.update(submission);
    }

    /**
     * Deletes the given submission. Does not fail if {@code submission} is {@code null}
     * or unknown.
     *
     * @param submission
     *      Submission to delete.
     */
    @LockMeetingDAO
    @LockSubmissionDAO
    @LockEvaluationDAO
    public void delete(final Submission submission) throws HasEvaluationsException, EntityNotFoundException {
        submissionDAO.delete(submission);
    }

    /**
     * Returns the submission with given id.
     *
     * @param id
     *      Id of the submission in question.
     * @return
     *      Submission with given id.
     */
    public Submission getByIdWithTasks(final String id){
        Submission submission = submissionDAO.getById(id);
        if (submission != null) {
            submission.getTasks().size();
        }
        return submission;
    }

    /**
     * gets the submission by Id.
     * @param id
     * @return
     */
    public Submission getById(final String id) {
        return submissionDAO.getById(id);
    }

    /**
     * Returns the submissions with given meeting.
     *
     * @param meeting
     *      Meeting of the submissions in question.
     * @return
     *      Submissions with given meeting.
     */
    public List<Submission> getSubmissionByMeeting(final Meeting meeting){return submissionDAO.getSubmissionByMeeting(meeting);}

    /**
     * counts the number of submissions in a meeting.
     * @param meeting
     * @return
     */
    public Long countSubmissionsByMeeting(final Meeting meeting) {
        return submissionDAO.countSubmissionsByMeeting(meeting);
    }
}

