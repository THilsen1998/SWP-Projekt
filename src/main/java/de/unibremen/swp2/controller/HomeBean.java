package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.Meeting;
import de.unibremen.swp2.model.Role;
import de.unibremen.swp2.model.User;
import de.unibremen.swp2.persistence.Exceptions.EntityNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.OutdatedException;
import de.unibremen.swp2.persistence.Interceptors.LockMeetingDAOInterceptor;
import de.unibremen.swp2.security.GlobalSecure;
import de.unibremen.swp2.service.MeetingService;
import de.unibremen.swp2.service.UserService;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.PostConstruct;
import javax.faces.annotation.FacesConfig;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.interceptor.Interceptors;
import java.io.Serializable;
import java.security.Principal;
import java.util.List;

/**
 * @Author Tommy
 * Allows to load, delete, and update meetings.
 */

@Named
@FacesConfig
@ViewScoped
@GlobalSecure(roles = {Role.T, Role.D, Role.A})
public class HomeBean implements Serializable {

    /**
     * Allows to  update, reset a password, create and other operations
     */
    @Inject
    private UserService userService;

    /**
     * Allows to  update, reset a password, create and other operations
     */

    @Inject
    private MeetingService meetingService;

    /**
     * User currently logged in
     */
    @Inject
    private Principal principal;

    /**
     * Used to add faces messages (in case of errors) or to complete
     * authentification (in case of success)
     */
    @Inject
    private FacesContext facesContext;

    /**
     * The meetings loaded from MeetingDAO.
     */
    @Getter
    private List<Meeting> meetings;

    /**
     * Filtered meetings
     */
    @Getter
    @Setter
    private List<Meeting> filteredMeetings;

    /**
     * Initializes HomeBean
     */
    @PostConstruct
    private void init() {
        final User user = userService.getUsersByEmail(principal.getName());
        if (user.getRole().equals(Role.A)) {
            meetings = meetingService.getAllMeetings();
        } else {
            meetings = meetingService.getMeetingsByUser(user);
        }
    }

    /**
     * Deletes a meeting.
     *
     * @param meeting
     *      meeting to delete
     */
    @GlobalSecure(roles = {Role.D, Role.A})
    public void deleteMeeting(Meeting meeting) {
    }

    /**
     * Hides a meeting.
     *
     * @param meeting
     *      meeting to hide
     */
    @GlobalSecure(roles = {Role.D, Role.A})
    @Interceptors({LockMeetingDAOInterceptor.class})
    public void hideMeeting(Meeting meeting) {
        try {
            meetingService.updateMeetingOnly(meeting);
            final Meeting updated = meetingService.getById(meeting.getId());
            if (updated != null) {
                final int i = meetings.indexOf(meeting);
                meetings.set(i, updated);
            }
        } catch (EntityNotFoundException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Veranstaltung nicht gefunden.");
            facesContext.addMessage(null, msg);
        } catch (OutdatedException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Datensatz veraltet. Bitte Seite neu laden");
            facesContext.addMessage(null, msg);
        }
    }


}
