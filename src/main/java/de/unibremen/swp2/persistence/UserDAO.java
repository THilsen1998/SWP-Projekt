package de.unibremen.swp2.persistence;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.Exceptions.DuplicateEmailException;
import de.unibremen.swp2.persistence.Exceptions.EntityNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.OutdatedException;
import de.unibremen.swp2.persistence.Exceptions.UserNotFoundException;
import de.unibremen.swp2.persistence.Interceptors.LockUserDAOInterceptor;

import javax.enterprise.context.RequestScoped;
import javax.interceptor.Interceptors;
import javax.persistence.*;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
/*
@Author Martin, Tommy
 * Process date from and into the data base
 */
@RequestScoped
public class UserDAO implements DAO<User> {

    @PersistenceContext(name = "IGradeBook")
    private EntityManager entityManager;

    private final static ReentrantLock lock = new ReentrantLock();

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    /**
     * Inserts the given user.
     *
     * @param user User to insert.
     * @throws NullPointerException If {@code user} is {@code null}.
     */
    public void insert(final User user) throws NullPointerException, DuplicateEmailException {
        try {
            getUserByEmail(user.getEmail());
            throw new DuplicateEmailException();
        } catch (NoResultException ignored) {
        }
        entityManager.persist(user);
    }

    /**
     * Updates the given user.
     *
     * @param user User to update.
     * @throws NullPointerException If {@code user} is {@code null}.
     */
    public void update(final User user) throws NullPointerException, DuplicateEmailException, UserNotFoundException, OutdatedException {
        final User current = entityManager.find(User.class, user.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (current == null) {
            throw new UserNotFoundException();
        } else if (user.getVersion() == null || !user.getVersion().equals(current.getVersion())) {
            throw new OutdatedException();
        } else {
            try {
                final User other = getUserByEmail(user.getEmail());
                if (!other.equals(user)) {
                    throw new DuplicateEmailException();
                }
            } catch (NoResultException ignored) {
            }
            try {
                entityManager.merge(user);
            } catch (OptimisticLockException e) {
                throw new OutdatedException();
            }
        }
    }

    /**
     * Deletes the given user. Does not fail if {@code user} is {@code null}
     * or unknown.
     *
     * @param user User to delete.
     */
    public void delete(final User user) {
    }

    /**
     * Returns the user with given id.
     *
     * @param id Id of the user in question.
     * @return User with given id.
     */
    public User getById(final String id) {
        return entityManager.find(User.class, id);
    }

    /**
     * Returns the users with given meeting.
     *
     * @param meeting Meeting of the Tutors in question.
     * @return Tutors with given meeting.
     */
    public List<User> getTutorsByMeeting(final Meeting meeting) {
        return null;
    }

    /**
     * Returns the users with given meeting.
     *
     * @param meeting Meeting of the Lecturers in question.
     * @return Lecturers with given meeting.
     */
    public List<User> getLecturersByMeeting(final Meeting meeting) {
        return entityManager.
                createQuery("SELECT u FROM User u JOIN u.userMeetingRoles r WHERE r.meeting = :meeting AND r.role = :role", User.class).
                setParameter("meeting", meeting).
                setParameter("role", Role.D).getResultList();
    }

    /**
     * Returns the Ceo with given meeting.
     *
     * @param meeting Meeting of the ceo in question.
     * @return Ceo with given meeting.
     */
    public User getCeoByMeeting(final Meeting meeting) throws NoResultException {
        return entityManager.
                createQuery("SELECT u FROM User u JOIN u.userMeetingRoles r WHERE r.meeting = :meeting AND r.role = :role", User.class).
                setParameter("meeting", meeting).
                setParameter("role", Role.CEO).
                getSingleResult();
    }

    /**
     * Returns all users.
     *
     * @return All Users.
     */
    public List<User> getAllUsers() {
        return entityManager.createQuery("SELECT u FROM User u", User.class).getResultList();

    }

    /**
     * Returns the users with given tutorial.
     *
     * @param tutorial Tutorial of the users in question.
     * @return Users with given tutorial.
     */
    public List<User> getUsersByTutorial(final Tutorial tutorial) {
        return entityManager.createQuery("SELECT u FROM User u JOIN u.userMeetingRoles r WHERE :tutorial MEMBER OF r.tutorials", User.class)
                .setParameter("tutorial", tutorial).getResultList();
    }

    /**
     * Returns the user with given email.
     *
     * @param email Email of the user in question.
     * @return User with given email.
     */
    public User getUserByEmail(final String email) throws NoResultException {
        return entityManager.createQuery("SELECT s FROM User s WHERE s.email = :email", User.class).
                setParameter("email", email).getSingleResult();
    }

    /**
     * Returns the user with given email and password.
     *
     * @param email    Email of the user in question.
     * @param password Password of the user in question.
     * @return User with given email and password.
     */
    public User getUserByEmailAndPassword(final String email, String password) throws NoResultException {
        return entityManager.createQuery("SELECT u FROM User u WHERE u.email = :email AND u.password=:password", User.class).
                setParameter("email", email).
                setParameter("password", password).
                getSingleResult();
    }

    /**
     * Returns the users not in meeting.
     *
     * @param meeting Meeting of the users in question.
     * @return Users not in meeting.
     */
    public List<User> getAllUsersNotInMeeting(final Meeting meeting) {
        return entityManager.createQuery("SELECT u FROM User u WHERE NOT EXISTS (SELECT 1 FROM UserMeetingRole r where r.user = u AND r.meeting = :meeting)", User.class).
                setParameter("meeting", meeting).
                getResultList();
    }

    public List<User> getAllUsersByMeeting(final Meeting meeting) {
        return entityManager.
                createQuery("SELECT u FROM User u JOIN u.userMeetingRoles r WHERE r.meeting = :meeting", User.class).
                setParameter("meeting", meeting).
                getResultList();
    }

    /**
     * Returns the users not in tutorial.
     *
     * @param tutorial Tutorial of the users in question.
     * @return Users not in tutorial.
     */
    public List<User> getAllUsersNotInTutorial(final Tutorial tutorial) {
        return entityManager.createQuery("SELECT u FROM User u WHERE NOT EXISTS (SELECT 1 FROM Tutorial t JOIN t.userMeetingRoles r WHERE t = :tutorial AND r.user = u)", User.class)
                .setParameter("tutorial", tutorial).getResultList();

    }
    /**
     * Returns all users .
     *
     * @return Users .
     */

    public List<User> getAllLecturers() {
        return entityManager.createQuery("SELECT u FROM User u WHERE u.role = :role OR u.role = :role2", User.class).setParameter("role2", Role.A).
                setParameter("role", Role.D).getResultList();
    }
    /**
     * Returns UserMeetingRole.
     *
     * @param user in a specific meeting.
     * @param meeting
     * @return UserMeetingRole.
     */
    public UserMeetingRole getUserMeetingRoleByUserAndMeeting(final User user, final Meeting meeting) throws NoResultException {
        return entityManager.
                createQuery("SELECT u FROM UserMeetingRole u WHERE u.user = :user AND u.meeting = :meeting", UserMeetingRole.class).
                setParameter("user", user).setParameter("meeting", meeting).
                getSingleResult();
    }
    /**
     * Returns a list of UserMeetingRole by a specific meeting.
     *
     * @param meeting .
     * @return List<UserMeetingRole>.
     */
    public List<UserMeetingRole> getUserMeetingRolesByMeeting(final Meeting meeting) {
        return entityManager.
                createQuery("SELECT u FROM UserMeetingRole u WHERE u.meeting = :meeting", UserMeetingRole.class).
                setParameter("meeting", meeting).getResultList();
    }
    /**
     * Returns UserMeetingRole by a user and tutorial.
     *
     * @param user .
     * @param tutorial
     * @return UserMeetingRole.
     */
    public UserMeetingRole getUserMeetingRoleByTutorialAndUser(final Tutorial tutorial, final User user) throws NoResultException {
        return entityManager.
                createQuery("SELECT u FROM UserMeetingRole u WHERE u.user = :user AND :tutorial MEMBER OF u.tutorials", UserMeetingRole.class).
                setParameter("user", user).setParameter("tutorial", tutorial).getSingleResult();
    }
    /**
     * Returns a list of UserMeetingRole by a specific tutorial.
     *
     * @param tutorial .
     * @return List<UserMeetingRole>.
     */
    public List<UserMeetingRole> getUserMeetingRolesByTutorial(final Tutorial tutorial) {
        return entityManager.createQuery("SELECT r FROM UserMeetingRole r JOIN r.tutorials t WHERE t = :tutorial",
                UserMeetingRole.class).setParameter("tutorial", tutorial).getResultList();
    }
    /**
     * Returns a list of UserMeetingRole by a specific tutorial.
     *
     * @param role .
     */
    public void updateUserMeetingRole(final UserMeetingRole role) throws EntityNotFoundException, OutdatedException {
        final UserMeetingRole current = entityManager.find(UserMeetingRole.class, role.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (current == null) {
            throw new EntityNotFoundException();
        }
        if (current.getVersion() == null || !current.getVersion().equals(role.getVersion())) {
            throw new OutdatedException();
        }
        try {
            entityManager.merge(role);
        } catch (OptimisticLockException e) {
            throw new OutdatedException();
        }
    }
}
