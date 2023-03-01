package de.unibremen.swp2.persistence;

import de.unibremen.swp2.model.Exam;
import de.unibremen.swp2.model.Meeting;
import de.unibremen.swp2.persistence.Exceptions.EntityNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.*;
import de.unibremen.swp2.persistence.Interceptors.LockExamDAOInterceptor;
import lombok.NonNull;

import javax.enterprise.context.RequestScoped;
import javax.interceptor.Interceptors;
import javax.persistence.*;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
/**@Author Martin
 */
@RequestScoped
public class ExamDAO implements DAO<Exam> {


    @PersistenceContext(name = "IGradeBook")
    private EntityManager entityManager;

    private final static ReentrantLock reentrantLock = new ReentrantLock();

    public void lock() {
        reentrantLock.lock();
    }

    public void unlock() {
        reentrantLock.unlock();
    }

    /**
     * Inserts the given exam.
     *
     * @param exam
     *      Exam to insert.
     * @throws NullPointerException
     *      If {@code exam} is {@code null}.
     */
    public void insert(final @NonNull Exam exam) throws NullPointerException, MeetingNotFoundException {
        final Meeting meeting = entityManager.find(Meeting.class, exam.getMeeting().getId(), LockModeType.PESSIMISTIC_WRITE);
        Query q = entityManager.createQuery("SELECT COUNT(e) FROM Exam e WHERE e.meeting = :meeting").setParameter("meeting", meeting);
        final Long i = (Long) q.getResultList().get(0);
        if (meeting == null || i > 0) {
            throw new MeetingNotFoundException();
        } else {
            entityManager.persist(exam);
        }
    }

    /**
     * Updates the given exam.
     *
     * @param exam
     *      Exam to update.
     * @throws NullPointerException
     *      If {@code exam} is {@code null}.
     */
    public void update(final Exam exam) throws  NullPointerException, EntityNotFoundException, OutdatedException {
        final Exam current = entityManager.find(Exam.class, exam.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (current == null) {
            throw new EntityNotFoundException();
        } else if (current.getVersion() == null || !exam.getVersion().equals(current.getVersion())) {
            throw new OutdatedException();
        }
        try {
            entityManager.merge(exam);
        } catch (OptimisticLockException e) {
            throw new OutdatedException();
        }
    }

    /**
     * Deletes the given exam. Does not fail if {@code exam} is {@code null}
     * or unknown.
     *
     * @param exam
     *      Exam to delete.
     */
    public void delete(final Exam exam) throws EntityNotFoundException, HasEvaluationsException {
        Query q = entityManager.createQuery("SELECT COUNT(e) FROM Evaluation e WHERE e.exam = :exam").setParameter("exam", exam);
        final Long i = (Long) q.getResultList().get(0);
        if (i == 0) {
            final Exam currentExam = entityManager.find(Exam.class, exam.getId(), LockModeType.PESSIMISTIC_WRITE);
            if (currentExam != null) {
                entityManager.remove(currentExam);
            } else {
                throw new EntityNotFoundException();
            }
        } else {
            throw new HasEvaluationsException();
        }
    }

    /**
     * Returns the Exam with given id.
     *
     * @param id
     *      Id of the exam in question.
     * @return
     *      Exam with given id.
     */
    public Exam getById(final String id){
        return entityManager.find(Exam.class,id);
    }

    /**
     * Returns the exams with given meeting.
     *
     * @param meeting
     *      Meeting of evaluation in question.
     * @return
     *      Exams with given meeting.
     */
    /*
    Noch ändern.
     */
    public List<Exam> getExamsByMeeting(final Meeting meeting){
           return entityManager.createQuery("SELECT m FROM Exam m WHERE m.meeting =:meeting ",Exam.class).
                   setParameter("meeting",meeting).getResultList();
    }

    /**
     * Returns an exam with given meeting.
     *
     * @param meeting
     *      Meeting of evaluation in question.
     * @return
     *      Exam with given meeting.
     */
    public Exam getExamByMeeting(final Meeting meeting) throws NoResultException {
        return entityManager.createQuery("SELECT e FROM Exam e WHERE e.meeting = :meeting", Exam.class).setParameter("meeting", meeting).getSingleResult();
    }
}
