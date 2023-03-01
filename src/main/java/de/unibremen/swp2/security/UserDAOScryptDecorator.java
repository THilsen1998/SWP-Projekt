package de.unibremen.swp2.security;

import com.google.common.hash.Hashing;
import de.unibremen.swp2.model.User;
import de.unibremen.swp2.persistence.Exceptions.*;
import de.unibremen.swp2.service.UserServiceInterface;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import java.nio.charset.StandardCharsets;

@Decorator
public abstract class UserDAOScryptDecorator implements UserServiceInterface {


    /**
     * Returns the user with given email and password. The main purpose of this
     * method is to validate user credentials, i.e., if the returned optional
     * is not empty, the supplied email and password are valid.
     *
     * @return
     *      User with given email and password.
     */

    @Inject
    @Delegate
    @Any
    UserServiceInterface userServiceInterface;

    // -> übernimmt die Methode von UserDao(findByEmailAndPassword) bei uns getUserByEmailAndPassword
    // Checkt mit SCryptUtil.check(password, u.getPassword())) ob bereits der user drinne, durch die eingegeben
    // email und pw

    /**
     * Hashed the password and inserts user.
     *
     * @param user
     *      User to insert.
     */
    // hat im miniprojekt das pw gehashed und dann in die datenbank inserted

    @Override
    public void create(User user) throws EntityAlreadyInsertedException, DuplicateEmailException, UserNotPermittedException {
        final String pwHash = Hashing.sha256().hashString(user.getFirstName() + "." + user.getLastName(), StandardCharsets.UTF_8).toString();
        user.setPassword(pwHash);
        user.setEmail(user.getEmail().toLowerCase());
        userServiceInterface.create(user);
    }

    @Override
    public User getUserByEmailAndPassword(String email, String password) throws NoResultException {
        final String pwHash = Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString();
        return userServiceInterface.getUserByEmailAndPassword(email, pwHash);
    }

    @Override
    public void changePassWord (User user,String oldPw,String newPw1,String newPw2 ) throws UserNotFoundException, OutdatedException, DuplicateEmailException, DoesntMatchOldPsWrdException, TwoPaswordsArentIdentical {
        if(!(newPw1.equals(newPw2))) throw new TwoPaswordsArentIdentical();
        else
          {
            final String pwHash = Hashing.sha256().hashString(oldPw, StandardCharsets.UTF_8).toString();
            if (pwHash.equals(user.getPassword()))

            {   final String NwpwHash = Hashing.sha256().hashString(newPw1, StandardCharsets.UTF_8).toString();
                user.setPassword(NwpwHash);
            }
            else throw new DoesntMatchOldPsWrdException();

                userServiceInterface.changePassWord( user, oldPw, newPw1, newPw2 );
         }
    }

    @Override
    public void resetPw(User user) throws UserNotFoundException, OutdatedException, DuplicateEmailException
    {
        final String pwHash = Hashing.sha256().hashString(user.getFirstName() + "." + user.getLastName(), StandardCharsets.UTF_8).toString();
        user.setPassword(pwHash);
        userServiceInterface.
                resetPw(user);

    }

}