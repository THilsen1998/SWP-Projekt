package de.unibremen.swp2.controller;


import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.Exceptions.*;
import de.unibremen.swp2.service.MeetingService;
import de.unibremen.swp2.service.ParticipantService;
import de.unibremen.swp2.service.TutorialService;
import de.unibremen.swp2.service.UserService;
import net.vidageek.mirror.dsl.Mirror;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.faces.annotation.RequestParameterMap;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.NoResultException;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;

import static junit.framework.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddParticipantBeanTest {

    @Mock
    ExternalContext externalContext;

    @Mock
    FacesContext facesContext;

    @Mock
    ParticipantService participantService;

    @Mock
    Participant participant;

    @Mock
    TutorialService tutorialService;

    @Mock
    MeetingService meetingService;

    @Mock
    @RequestParameterMap
    private Map<String, String> parameterMap;

    @InjectMocks
    AddParticipantBean addParticipantBean;

    @Mock
    Tutorial tutorial;

    @Mock
    UserService userService;

    @Mock
    Meeting meeting;

    @Mock
    Principal principal;







    @Test
    void initTest() {
        User user = new User();
        user.setRole(Role.A);
        Participant participant = new Participant();

        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(tutorialService.getById(any())).thenReturn(tutorial);


        lenient().when(meetingService.getMeetingByTutorial(tutorial)).thenReturn(meeting);
        lenient().when(userService.getUsersByEmail(principal.getName())).thenReturn(user);

        new Mirror().on(addParticipantBean).invoke().method("init").withoutArgs();

        //assertNotNull(participantService.getById(parameterMap.get("participant-Id")));

        //Participant participant1 = Mockito.mock(Participant.class);
        //String  value= "819cb31c-3b38-4706-b3d5-b7235e59df99";
        // when(parameterMap.get(anyString())).thenReturn(value);
        //  when(participantService.getById(any())).thenReturn(participant1);

        //  assertNotNull(addParticipantBean.getParticipant());
        //verify(addParticipantBean, times(1)).init();
    }

    @Test
    void initAsOtherThanAdminSecondNonresultExceptionTest() {

        User user = new User();

        user.setRole(Role.CEO);

        UserMeetingRole userMeetingRole = new UserMeetingRole();
        userMeetingRole.setRole(Role.CEO);

        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(tutorialService.getById(any())).thenReturn(tutorial);

        when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        when(meetingService.getMeetingByTutorial(any())).thenReturn(meeting);
        when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        when(userService.getUserMeetingRoleByTutorialAndUser(any(), any())).thenReturn(userMeetingRole);

        new Mirror().on(addParticipantBean).invoke().method("init").withoutArgs();

    }

    @Test
    void NoResultExceptionTest()  {
        User user = new User();

        user.setRole(Role.CEO);

        UserMeetingRole userMeetingRole = new UserMeetingRole();
        userMeetingRole.setRole(Role.CEO);

        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(tutorialService.getById(any())).thenReturn(tutorial);

        when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        when(meetingService.getMeetingByTutorial(any())).thenReturn(meeting);
        when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        when(userService.getUserMeetingRoleByTutorialAndUser(tutorial, user)).thenThrow(new NoResultException());
        meeting.setVisible(true);

        UserMeetingRole userMeetingRole1 = new UserMeetingRole();
        when( userService.getUserMeetingRoleByUserAndMeeting(user, meeting)).thenReturn(userMeetingRole1);
        new Mirror().on(addParticipantBean).invoke().method("init").withoutArgs();
    }



    @Test
    void createParticipantTest() throws DuplicateEmailException, IOException, ParticipantNotFoundException, ParticipantAlreadyInMeetingException, MeetingNotFoundException {
        User user = new User();
        ParticipantStatus participantStatus = new ParticipantStatus();
        user.setRole(Role.A);
        meeting.setVisible(true);


        doNothing().when(participantService).create(any());
        doNothing().when(externalContext).redirect(any());
        //when(role.equals(Role.A)).thenReturn(true);


        // when(meetingService.addParticipantToMeeting(participantStatus)).thenReturn(true);

        ;         verifyNoMoreInteractions(externalContext);

        addParticipantBean.createParticipant();

        verify(participantService, times(1)).create(any());
        verifyNoMoreInteractions(participantService);
        //verify(externalContext, times(1)).redirect("single-tutorial.xhtml?tutorial-Id=" + tutorial.getId());
        verify(externalContext, times(1)).redirect("participants.xhtml");
        verifyNoMoreInteractions(externalContext);
        verifyNoInteractions(facesContext);

    }

    @Test
    void createParticipantTest2() throws DuplicateEmailException, IOException, ParticipantNotFoundException, ParticipantAlreadyInMeetingException, MeetingNotFoundException {
        User user = new User();
        ParticipantStatus participantStatus = new ParticipantStatus();
        user.setRole(Role.A);
        meeting.setVisible(true);
        addParticipantBean.setRole(Role.A);

        doNothing().when(participantService).create(any());
        doNothing().when(externalContext).redirect(any());
        //when(role.equals(Role.A)).thenReturn(true);


        // when(meetingService.addParticipantToMeeting(participantStatus)).thenReturn(true);

        ;         verifyNoMoreInteractions(externalContext);

        addParticipantBean.createParticipant();

        verify(participantService, times(1)).create(any());
        verifyNoMoreInteractions(participantService);
        //verify(externalContext, times(1)).redirect("single-tutorial.xhtml?tutorial-Id=" + tutorial.getId());
        verify(externalContext, times(1)).redirect("single-tutorial.xhtml?tutorial-Id=" + tutorial.getId());
        verifyNoMoreInteractions(externalContext);
        verifyNoInteractions(facesContext);

    }




    @Test
    public void duplicateEmailExceptionTest() throws UserNotPermittedException, DuplicateEmailException, EntityAlreadyInsertedException, IOException {
        doThrow(new DuplicateEmailException()).when(participantService).create(any());

        addParticipantBean.createParticipant();

        verify(participantService, times(1)).create(any());
        verifyNoMoreInteractions(participantService);
        verify(facesContext, times(1)).addMessage(anyObject(), notNull());
        verifyNoMoreInteractions(facesContext);


    }
/*
    @Test
    public void MeetingNotFoundException() throws UserNotPermittedException, DuplicateEmailException, EntityAlreadyInsertedException, IOException, MeetingNotFoundException, ParticipantAlreadyInMeetingException, UserAlreadyInOtherRoleException, UserNotFoundException, UserAlreadyInMeetingException, ParticipantNotFoundException {
        doThrow(new MeetingNotFoundException()).when(meetingService).create(any(), any(), any(), any());

        addParticipantBean.createParticipant();

        verify(meetingService, times(1)).create(any(), any(), any(), any());
        verifyNoMoreInteractions(meetingService);
        verify(facesContext, times(1)).addMessage(anyObject(), notNull());
        verifyNoMoreInteractions(facesContext);


    }
*/


    @Test
    public void ParticipantAlreadyInMeetingExceptionTest() throws UserNotPermittedException, DuplicateEmailException, EntityAlreadyInsertedException, IOException, MeetingNotFoundException, ParticipantAlreadyInMeetingException, UserAlreadyInOtherRoleException, UserNotFoundException, UserAlreadyInMeetingException, ParticipantNotFoundException {
        User user = new User();
        ParticipantStatus participantStatus = new ParticipantStatus();
        user.setRole(Role.A);
        meeting.setVisible(true);
        addParticipantBean.setRole(Role.A);

        doNothing().when(participantService).create(any());
        doThrow(new ParticipantAlreadyInMeetingException()).when(meetingService).addParticipantToMeeting(any());
        addParticipantBean.createParticipant();
        verify(participantService, times(1)).create(any());
        verifyNoMoreInteractions(meetingService);
        verify(facesContext, times(1)).addMessage(anyObject(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    public void MeetingNotFoundException() throws UserNotPermittedException, DuplicateEmailException, EntityAlreadyInsertedException, IOException, MeetingNotFoundException, ParticipantAlreadyInMeetingException, UserAlreadyInOtherRoleException, UserNotFoundException, UserAlreadyInMeetingException, ParticipantNotFoundException {
        User user = new User();
        ParticipantStatus participantStatus = new ParticipantStatus();
        user.setRole(Role.A);
        meeting.setVisible(true);
        addParticipantBean.setRole(Role.A);

        doNothing().when(participantService).create(any());
        doThrow(new MeetingNotFoundException()).when(meetingService).addParticipantToMeeting(any());
        addParticipantBean.createParticipant();
        verify(meetingService, times(1)).addParticipantToMeeting(any());
        verifyNoMoreInteractions(meetingService);
        verify(facesContext, times(1)).addMessage(anyObject(), notNull());
        verifyNoMoreInteractions(facesContext);


    }
    @Test
    public void ParticipantNotFoundException() throws UserNotPermittedException, DuplicateEmailException, EntityAlreadyInsertedException, IOException, MeetingNotFoundException, ParticipantAlreadyInMeetingException, UserAlreadyInOtherRoleException, UserNotFoundException, UserAlreadyInMeetingException, ParticipantNotFoundException {
        User user = new User();
        ParticipantStatus participantStatus = new ParticipantStatus();
        user.setRole(Role.A);
        meeting.setVisible(true);
        addParticipantBean.setRole(Role.A);

        doNothing().when(participantService).create(any());
        doThrow(new ParticipantNotFoundException()).when(meetingService).addParticipantToMeeting(any());
        addParticipantBean.createParticipant();
        verify(meetingService, times(1)).addParticipantToMeeting(any());
        verifyNoMoreInteractions(meetingService);
        verify(facesContext, times(1)).addMessage(anyObject(), notNull());
        verifyNoMoreInteractions(facesContext);


    }






}
