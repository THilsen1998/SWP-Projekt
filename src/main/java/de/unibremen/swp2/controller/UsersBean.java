package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.Role;
import de.unibremen.swp2.model.User;
import de.unibremen.swp2.model.UserMeetingRole;
import de.unibremen.swp2.security.GlobalSecure;
import de.unibremen.swp2.service.MeetingService;
import de.unibremen.swp2.service.UserService;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.PostConstruct;
import javax.faces.annotation.FacesConfig;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.websocket.server.ServerEndpoint;
import java.io.Serializable;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author Khaled,Martin,Navy,Theo,Tommy
 * Allows to load, delete, and update users.
 */
@ViewScoped
@Named
@FacesConfig
@GlobalSecure(roles = {Role.A})
public class UsersBean implements Serializable {

    /**
     * The users loaded from UserDAO.
     */
    @Inject
    private UserService userService;

    /**
     * The users in tutorial loaded from UserDAO.
     */
    @Getter
    private List<User> users;

    /**
     * The users in tutorial loaded from UserDAO filtered.
     */
    @Getter
    @Setter
    private List<User> filteredUsers;

    /**
     * Initializes UserBean
     */
     @PostConstruct
     private void init()
      {
          users = userService.getAllUsers();
      }

    /**
     * Resets the passwords.
     */
    public void resetPassword() {}

}
