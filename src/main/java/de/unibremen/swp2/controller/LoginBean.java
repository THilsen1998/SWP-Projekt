package de.unibremen.swp2.controller;

import de.unibremen.swp2.service.SessionService;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.SecurityContext;
import javax.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.IOException;

/**
 * @Author Martin, Khaled
 * Used to login a user. The user credentials are stored in {@link #email}
 * (login email) and {@link #password} (login password).
 */
@Named
@RequestScoped
public class LoginBean {

    /**
     * Login name.
     */
    @NotBlank
    @Getter
    @Setter
    @NonNull
    @Email
    private String email = "";

    /**
     * Login password.
     */
    @NotBlank
    @Getter
    @Setter
    @NonNull
    private String password = "";

    /**
     * Secure
     */
    @Inject
    private SecurityContext securityContext;

    /**
     * Used to redirection
     */
    @Inject
    private ExternalContext externalContext;

    /**
     * Used to add faces messages (in case of errors) or to complete
     * authentification (in case of success)
     */
    @Inject
    private FacesContext facesContext;

    @Inject
    private SessionService sessionService;

    /**
     * Authenticates a user with its login name and password.
     * If the login page was called explicitly (i.e., no
     * login to continue) and the user could be authenticated, a redirection to
     * 'home.xhtml' is registered.
     *
     */
    public void login() throws IOException {
        final AuthenticationParameters credentials =
                AuthenticationParameters.withParams().credential(new UsernamePasswordCredential(email, password));
        final AuthenticationStatus status = securityContext.authenticate(
                (HttpServletRequest) externalContext.getRequest(),
                (HttpServletResponse) externalContext.getResponse(),
                credentials);
        if (status.equals(AuthenticationStatus.SEND_CONTINUE)) {
            facesContext.responseComplete();
            sessionService.loggedIn(email, (HttpSession) externalContext.getSession(false));
        } else if (status.equals(AuthenticationStatus.SUCCESS)) {
            externalContext.redirect("home.xhtml");
            sessionService.loggedIn(email, (HttpSession) externalContext.getSession(false));
        } else {
            final FacesMessage msg = new FacesMessage("Ungültiger Benutzername oder Passwort");
            facesContext.addMessage(null, msg);
        }
    }

    /**
     * Allows to reset the passwordof a particular user
     */
    //public void resetPassword() {}



}
