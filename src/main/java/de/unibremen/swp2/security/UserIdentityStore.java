package de.unibremen.swp2.security;

import de.unibremen.swp2.model.User;
import de.unibremen.swp2.persistence.UserDAO;
import de.unibremen.swp2.service.UserServiceInterface;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;
import java.util.Set;

/**
 * This identity store uses the data storage {@link User} to validate user
 * credentials based on email address and password (see
 * {@link UserDAO#getUserByEmailAndPassword(String, String)}).
 */
@RequestScoped
public class UserIdentityStore implements IdentityStore {

    @Inject
    private UserServiceInterface userServiceInterface;


    /**
     * Validates the credentials of an user
     */
    @Override
    public CredentialValidationResult validate(final Credential credential) {
        if (!(credential instanceof UsernamePasswordCredential)) {
            return CredentialValidationResult.NOT_VALIDATED_RESULT;
        }
        final UsernamePasswordCredential userAndPass = (UsernamePasswordCredential) credential;
        try {
            final User user = userServiceInterface.getUserByEmailAndPassword(userAndPass.getCaller(), userAndPass.getPasswordAsString());
            if (user.getStatus().equals(false)){
                return CredentialValidationResult.INVALID_RESULT;
            }
            final String callerName = user.getEmail();
            final Set<String> groups = Set.of(user.getRole().name());
            return new CredentialValidationResult(callerName, groups);
        } catch (NoResultException e) {
            return CredentialValidationResult.INVALID_RESULT;
        }
    }
}
