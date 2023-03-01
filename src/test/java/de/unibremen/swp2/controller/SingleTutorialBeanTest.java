package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.Exceptions.EntityNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.HasEvaluationsException;
import de.unibremen.swp2.persistence.Exceptions.OutdatedException;
import de.unibremen.swp2.service.*;
import net.vidageek.mirror.dsl.Mirror;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import javax.faces.annotation.RequestParameterMap;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.persistence.NoResultException;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class SingleTutorialBeanTest {

    @InjectMocks
    SingleTutorialBean singleTutorialBean;

    @Mock
    @RequestParameterMap
    private Map<String, String> parameterMap;

    @Mock
    ExternalContext externalContext;
    @Mock
    FacesContext facesContext;
    @Mock
    TutorialService tutorialService;

    @Mock
    UserService userService;

    @Mock
    MeetingService meetingService;

    @Mock
    GroupService groupService;

    @Mock
    Tutorial tutorial;

    @Mock
    Principal principal;

    @Mock
    Meeting meeting;

    @Mock
    List<Participant> participants;

    @Mock
    List<User> tutors;

    @Mock
    List<TGroup> groups;

    @Mock
    ParticipantService participantService;

    @Mock
    SubmissionService submissionService;

    @Mock
    List<Submission> submissions;

    @Test
    void initAsAdminTest() {

        User user = new User();
        user.setRole(Role.A);

        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(tutorialService.getById(any())).thenReturn(tutorial);
        Meeting meeting1 = Mockito.mock(Meeting.class);
        meeting1.setVisible(true);
        lenient().when(meetingService.getMeetingByTutorial(tutorial)).thenReturn(meeting1);
        lenient().when(userService.getUsersByEmail(principal.getName())).thenReturn(user);

        lenient().when(participantService.getAllParticipantsByTutorial(any())).thenReturn(participants);
        lenient().when(userService.getUsersByTutorial(any())).thenReturn(tutors);
        lenient().when(groupService.getGroupsByTutorial(any())).thenReturn(groups);
        lenient().when(submissionService.getSubmissionByMeeting(any())).thenReturn(submissions);

        new Mirror().on(singleTutorialBean).invoke().method("init").withoutArgs();

        verify(userService, times(1)).getUsersByEmail(any());
        verify(participantService, times(1)).getAllParticipantsByTutorial(any());
        verify(userService, times(1)).getUsersByTutorial(any());
        verify(groupService, times(1)).getGroupsByTutorial(any());
        verify(groupService, times(1)).getGroupsByTutorial(any());
    }


    @Test
    void initTestAsOther() {
        User user = new User();
        user.setRole(Role.T);

        UserMeetingRole userMeetingRole = new UserMeetingRole();
        userMeetingRole.setRole(Role.T);


        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(tutorialService.getById(any())).thenReturn(tutorial);
        Meeting meeting1 = Mockito.mock(Meeting.class);
        meeting1.setVisible(true);
        lenient().when(meetingService.getMeetingByTutorial(tutorial)).thenReturn(meeting1);
        lenient().when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        lenient().when(userService.getUserMeetingRoleByTutorialAndUser(any(), any())).thenReturn(userMeetingRole);


        new Mirror().on(singleTutorialBean).invoke().method("init").withoutArgs();

        verify(userService, times(1)).getUsersByEmail(any());
        verify(userService, times(1)).getUserMeetingRoleByTutorialAndUser(any(), any());
    }


    @Test
    void initAsOtherThanAdmin() {

        User user = new User();
        user.setRole(Role.CEO);

        UserMeetingRole userMeetingRole = new UserMeetingRole();
        userMeetingRole.setRole(Role.CEO);

        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(tutorialService.getById(any())).thenReturn(tutorial);

        when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        when(userService.getUserMeetingRoleByTutorialAndUser(any(), any())).thenThrow(new NoResultException());
        when(userService.getUserMeetingRoleByUserAndMeeting(any(), any())).thenReturn(userMeetingRole);
        when(meetingService.getMeetingByTutorial(any())).thenReturn(meeting);


        new Mirror().on(singleTutorialBean).invoke().method("init").withoutArgs();

        verify(userService, times(1)).getUsersByEmail(any());
        verify(userService, times(1)).getUserMeetingRoleByTutorialAndUser(any(), any());
        verify(userService, times(1)).getUserMeetingRoleByUserAndMeeting(any(), any());
        verify(meetingService, times(1)).getMeetingByTutorial(any());
    }


    @Test
    void initAsOtherThanAdminSecondNonresultException() {

        User user = new User();
        user.setRole(Role.CEO);

        UserMeetingRole userMeetingRole = new UserMeetingRole();
        userMeetingRole.setRole(Role.CEO);

        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(tutorialService.getById(any())).thenReturn(tutorial);

        when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        when(userService.getUserMeetingRoleByTutorialAndUser(any(), any())).thenThrow(new NoResultException());
        when(userService.getUserMeetingRoleByUserAndMeeting(any(), any())).thenThrow(new NoResultException());
        when(meetingService.getMeetingByTutorial(any())).thenReturn(meeting);

        new Mirror().on(singleTutorialBean).invoke().method("init").withoutArgs();

        verify(userService, times(1)).getUsersByEmail(any());
        verify(userService, times(1)).getUserMeetingRoleByTutorialAndUser(any(), any());
        verify(userService, times(1)).getUserMeetingRoleByUserAndMeeting(any(), any());
        verify(meetingService, times(1)).getMeetingByTutorial(any());
    }


    @Test
    void initAsNon() {

        User user = new User();
        user.setRole(Role.T);

        UserMeetingRole userMeetingRole = new UserMeetingRole();
        userMeetingRole.setRole(Role.T);

        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(tutorialService.getById(any())).thenReturn(tutorial);

        when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        when(userService.getUserMeetingRoleByTutorialAndUser(any(), any())).thenThrow(new NoResultException());
        when(userService.getUserMeetingRoleByUserAndMeeting(any(), any())).thenReturn(userMeetingRole);
        when(meetingService.getMeetingByTutorial(any())).thenReturn(meeting);


        new Mirror().on(singleTutorialBean).invoke().method("init").withoutArgs();

        verify(userService, times(1)).getUsersByEmail(any());
        verify(userService, times(1)).getUserMeetingRoleByTutorialAndUser(any(), any());
        verify(userService, times(1)).getUserMeetingRoleByUserAndMeeting(any(), any());
        verify(meetingService, times(1)).getMeetingByTutorial(any());

    }
    @Test
    void deleteMeetingTest() throws HasEvaluationsException, IOException, EntityNotFoundException, OutdatedException
    {
        doNothing().when(tutorialService).delete(any());
        doNothing().when(externalContext).redirect(any());
        singleTutorialBean.deleteTutorial();

        doThrow(new EntityNotFoundException()).when(tutorialService).delete(any());
        singleTutorialBean.deleteTutorial();

        doThrow(new OutdatedException()).when(tutorialService).delete(any());
        singleTutorialBean.deleteTutorial();


        verify(tutorialService, times(3)).delete(any());
        verify(facesContext, times(2)).addMessage(isNull(), notNull());

    }


}