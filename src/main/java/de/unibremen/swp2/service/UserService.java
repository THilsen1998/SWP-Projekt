package de.unibremen.swp2.service;


import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.Exceptions.*;
import de.unibremen.swp2.persistence.Interceptors.LockUserDAO;
import de.unibremen.swp2.persistence.UserDAO;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author Navy, Khaled
 * Allows to edit,update,delete,creat and to perform other operations
 * on a User (stored in {@link #userDao}).
 */
@RequestScoped
@Transactional(rollbackOn = PersistenceException.class)
public class UserService implements UserServiceInterface {


    @Inject
    private UserDAO userDao;

    @Inject
    private SessionService sessionService;

    /**
     * sets a role for a specific user pass .
     *
     * @param user the User associated with the given role
     * @param role the role associated with the given user
     */
    public void setUsersRole(User user, Role role) {
        user.setRole(role);
    }

    /**
     * resets a pass word of a user  .
     *
     * @param user the User associated with the pass words
     */
    @Override
    @LockUserDAO
    public void resetPw(User user) throws UserNotFoundException, OutdatedException, DuplicateEmailException {
        if (user == null) {
            throw new UserNotFoundException();
        } else {
            userDao.update(user);
            sessionService.logout(user.getEmail());
        }
    }

    /**
     * changes the given Pass Word .
     *
     * @param newPw1 the new passWord
     * @param oldPw  the old passWord
     * @param user   the User associated with the pass words
     */
    @LockUserDAO
    @Override
    public void changePassWord(User user, String oldPw, String newPw1, String newPw2) throws UserNotFoundException,
            OutdatedException, DuplicateEmailException, TwoPaswordsArentIdentical, DoesntMatchOldPsWrdException {
        userDao.update(user);
        sessionService.logout(user.getEmail());
    }

    /**
     * creates the given user.
     *
     * @param user User to create.
     */
    @LockUserDAO
    @Override
    public void create(final User user) throws DuplicateEmailException, UserNotPermittedException {
        if (user.getRole().equals(Role.CEO)) {
            throw new UserNotPermittedException();
        }
        userDao.insert(user);
    }

    /**
     * Updates the given user.
     *
     * @param user User to update.
     * @throws NullPointerException If {@code user} is {@code null}.
     */
    @LockUserDAO
    public void update(final User user) throws NullPointerException, DuplicateEmailException, UserNotFoundException,
            OutdatedException, UserNotPermittedException {
        if (user.getRole().equals(Role.CEO)) {
            throw new UserNotPermittedException();
        }
        final String email = userDao.getById(user.getId()).getEmail();
        userDao.update(user);
        sessionService.logout(email);
    }

    /**
     * Deletes the given user. Does not fail if {@code user} is {@code null}
     * or unknown.
     *
     * @param user User to delete.
     */
    public void delete(final User user) {
        userDao.delete(user);
    }

    /**
     * Returns the user with given id.
     *
     * @param id Id of the user in question.
     * @return User with given id.
     */
    public User getById(final String id) {
        return userDao.getById(id);
    }

    /**
     * Returns the users with given meeting.
     *
     * @param meeting Meeting of the Lecturers in question.
     * @return Lecturers with given meeting.
     */
    public List<User> getLecturersByMeeting(final Meeting meeting) {
        return userDao.getLecturersByMeeting(meeting);
    }

    /**
     * Returns the Ceo with given meeting.
     *
     * @param meeting Meeting of the ceo in question.
     * @return Ceo with given meeting.
     */
    public User getCeoByMeeting(final Meeting meeting) {
        try {
            return userDao.getCeoByMeeting(meeting);
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Returns all users.
     *
     * @return All Users.
     */
    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    /**
     * Returns the users with given tutorial.
     *
     * @param tutorial Tutorial of the users in question.
     * @return Users with given tutorial.
     */
    public List<User> getUsersByTutorial(final Tutorial tutorial) {
        return userDao.getUsersByTutorial(tutorial);
    }

    /**
     * Returns the user with given email.
     *
     * @param email Email of the user in question.
     * @return User with given email.
     */
    public User getUsersByEmail(final String email) {
        return userDao.getUserByEmail(email);
    }

    /**
     * Returns the user with given email and password.
     *
     * @param email    Email of the user in question.
     * @param password Password of the user in question.
     * @return User with given email and password.
     */
    @Override
    public User getUserByEmailAndPassword(final String email, final String password) throws NoResultException {
        return userDao.getUserByEmailAndPassword(email, password);
    }

    /**
     * Returns the users not in meeting.
     *
     * @param meeting Meeting of the users in question.
     * @return Users not in meeting.
     */
    public List<User> getAllUsersNotInMeeting(final Meeting meeting) {
        return userDao.getAllUsersNotInMeeting(meeting);
    }

    /**
     * Returns the users not in tutorial.
     *
     * @param tutorial Tutorial of the users in question.
     * @return Users not in tutorial.
     */
    public List<User> getAllUsersNotInTutorial(final Tutorial tutorial) {
        return userDao.getAllUsersNotInTutorial(tutorial);
    }

    /**
     * gets all users with the role 'Admin'
     * @return list of users.
     */
    public List<User> getAllLecturers() {
        return userDao.getAllLecturers();
    }

    /**
     * gets the role of a user in a meeting.
     * @param user the user
     * @param meeting the meeting
     * @return
     */
    public UserMeetingRole getUserMeetingRoleByUserAndMeeting(final User user, final Meeting meeting) {
        return userDao.getUserMeetingRoleByUserAndMeeting(user, meeting);
    }

    /**
     * gets a list of users linked to their roles to display in a datatable.
     * @param meeting the meeting
     * @return the list of users linked to their role.
     */
    public List<UserWithRole> getUsersWithRoleByMeeting(final Meeting meeting) {
        final List<UserWithRole> usersWithRole = new ArrayList<>();
        final List<UserMeetingRole> users = userDao.getUserMeetingRolesByMeeting(meeting);
        for (final UserMeetingRole u : users) {
            usersWithRole.add(new UserWithRole(u.getUser(), u));
        }
        return usersWithRole;
    }

    /**
     * gets the Role of a user based on the tutorial,
     * @param tutorial the tutorial.
     * @param user the user to get the role for.
     * @return the role of the user.
     * @throws NoResultException
     */
    public UserMeetingRole getUserMeetingRoleByTutorialAndUser(final Tutorial tutorial, final User user) throws NoResultException {
        return userDao.getUserMeetingRoleByTutorialAndUser(tutorial, user);
    }
}