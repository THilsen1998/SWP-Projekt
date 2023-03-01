package de.unibremen.swp2.service;


import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.*;
import de.unibremen.swp2.persistence.Exceptions.*;
import de.unibremen.swp2.persistence.Interceptors.LockEvaluationDAO;
import de.unibremen.swp2.persistence.Interceptors.LockExamDAO;
import de.unibremen.swp2.persistence.Interceptors.LockMeetingDAO;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.List;

/**
 * @Author Martin, Dennis
 * Allows to edit,update,delete,creat and other operations on an Exam
 * (stored in {@link #examDAO}).
 */

@Transactional(rollbackOn = PersistenceException.class)
@RequestScoped
public class ExamService implements Serializable
{

    @Inject
    private ExamDAO examDAO;


    /**
     * creates the given exam.
     *
     * @param exam Exam to create.
     */
    @LockMeetingDAO
    @LockExamDAO
    public void create(final Exam exam) throws MeetingNotFoundException {
        examDAO.insert(exam);
    }

    /**
     * Updates the given exam.
     *
     * @param exam Exam to update.
     * @throws NullPointerException If {@code exam} is {@code null}.
     */
    @LockMeetingDAO
    @LockExamDAO
    public void update(final Exam exam) throws NullPointerException, OutdatedException, EntityNotFoundException
    {
        examDAO.update(exam);
    }

    /**
     * Deletes the given exam. Does not fail if {@code exam} is {@code null}
     * or unknown.
     *
     * @param exam Exam to delete.
     */
    @LockMeetingDAO
    @LockExamDAO
    @LockEvaluationDAO
    public void delete(final Exam exam) throws HasEvaluationsException, EntityNotFoundException {
        examDAO.delete(exam);
    }

    /**
     * Returns the Exam with given id.
     *
     * @param id Id of the exam in question.
     * @return Exam with given id.
     */
    public Exam getById(final String id) {return examDAO.getById(id); }

    /**
     * Returns the exams with given meeting.
     *
     * @param meeting Meeting of evaluation in question.
     * @return Exams with given meeting.
     */
    public List<Exam> getExamsByMeeting(final Meeting meeting) {return examDAO.getExamsByMeeting(meeting);}

    public Exam getExamByMeeting(final Meeting meeting) throws NoResultException {
        return examDAO.getExamByMeeting(meeting);
    }
}






