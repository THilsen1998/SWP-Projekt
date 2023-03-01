package de.unibremen.swp2.service;

import de.unibremen.swp2.model.User;
import de.unibremen.swp2.persistence.Exceptions.*;

import javax.persistence.NoResultException;

public interface UserServiceInterface {

    void create(User user) throws EntityAlreadyInsertedException, DuplicateEmailException, UserNotPermittedException;

    User getUserByEmailAndPassword(String email, String password) throws NoResultException;

    void resetPw(User user) throws UserNotFoundException, OutdatedException, DuplicateEmailException;

    void changePassWord (User user,String oldPw,String newPw1,String newPw2 ) throws UserNotFoundException, OutdatedException, DuplicateEmailException, DoesntMatchOldPsWrdException, TwoPaswordsArentIdentical;


}
