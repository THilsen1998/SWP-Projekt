package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.Exceptions.*;
import de.unibremen.swp2.service.*;
import net.vidageek.mirror.dsl.Mirror;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.faces.annotation.RequestParameterMap;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.NoResultException;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SubmissionBeanTest {

    @Mock
    SubmissionService submissionService;


    @Mock
    Meeting meeting;


    @Mock
    ExternalContext externalContext;

    @Mock
    FacesContext facesContext;

    @InjectMocks
    SubmissionBean submissionBean;

    @Mock
    @RequestParameterMap
    private Map<String, String> parameterMap;

    @Mock
    Participant participant;

    @Mock
    UserService userService;

    @Mock
    Principal principal;

    @Mock
    MeetingService meetingService;

    @Mock
    Submission submission;

    @Mock
    ParticipantService participantService;

    @Mock
    List<Participant> participantList;

    @Mock
    TGroup group;

    @Mock
    GroupService groupService;


    @Test
    void initAsAdminTest() {
        User user = new User();
        user.setRole(Role.A);

        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(submissionService.getById(any())).thenReturn(submission);
        lenient().when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        lenient().when(meetingService.getMeetingByTutorial(any())).thenReturn(meeting);
        when(participantService.getAllParticipantsByMeeting(any())).thenReturn(participantList);


        new Mirror().on(submissionBean).invoke().method("init").withoutArgs();
        assertNotNull(submissionService.getById(parameterMap.get("submission-Id")));
        verify(parameterMap, times(2)).get(anyString());
    }

    @Test
    void initAsOtherTest() {
        User user = new User();
        user.setRole(Role.T);

        UserMeetingRole userMeetingRole = new UserMeetingRole();
        userMeetingRole.setRole(Role.T);

        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(submissionService.getById(any())).thenReturn(submission);
        when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        lenient().when(userService.getUserMeetingRoleByUserAndMeeting(any(), any())).thenReturn(userMeetingRole);


        new Mirror().on(submissionBean).invoke().method("init").withoutArgs();

    }

    @Test
    void initAsTutorGroupWorkTrueTest() {
        User user = new User();
        user.setRole(Role.T);

        UserMeetingRole userMeetingRole = new UserMeetingRole();
        userMeetingRole.setRole(Role.T);

        Submission submission1 = new Submission();
        submission1.setGroupWork(true);

        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(submissionService.getById(any())).thenReturn(submission1);
        when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        lenient().when(userService.getUserMeetingRoleByUserAndMeeting(any(), any())).thenReturn(userMeetingRole);


        new Mirror().on(submissionBean).invoke().method("init").withoutArgs();

    }

    @Test
    void initAsOtherGroupWorkFalseTest() {
        User user = new User();
        user.setRole(Role.T);

        UserMeetingRole userMeetingRole = new UserMeetingRole();
        userMeetingRole.setRole(Role.T);

        Submission submission1 = new Submission();
        submission1.setGroupWork(false);

        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(submissionService.getById(any())).thenReturn(submission1);
        when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        lenient().when(userService.getUserMeetingRoleByUserAndMeeting(any(), any())).thenReturn(userMeetingRole);


        new Mirror().on(submissionBean).invoke().method("init").withoutArgs();

    }

    @Test
    void initAsOtherThanAdminNonResultExceptionTest() {

        User user = new User();
        user.setRole(Role.CEO);

        UserMeetingRole userMeetingRole = new UserMeetingRole();
        userMeetingRole.setRole(Role.CEO);

        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(submissionService.getById(any())).thenReturn(submission);
        when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        lenient().when(userService.getUserMeetingRoleByTutorialAndUser(any(), any())).thenReturn(userMeetingRole);
        lenient().when(userService.getUserMeetingRoleByUserAndMeeting(any(), any())).thenThrow(new NoResultException());

        new Mirror().on(submissionBean).invoke().method("init").withoutArgs();
    }

    @Test
    void initAsOtherGroupWorkTrueTest() {
        User user = new User();
        user.setRole(Role.CEO);

        UserMeetingRole userMeetingRole = new UserMeetingRole();
        userMeetingRole.setRole(Role.CEO);

        Submission submission1 = new Submission();
        submission1.setGroupWork(true);

        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(submissionService.getById(any())).thenReturn(submission1);
        when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        lenient().when(userService.getUserMeetingRoleByUserAndMeeting(any(), any())).thenReturn(userMeetingRole);


        new Mirror().on(submissionBean).invoke().method("init").withoutArgs();

    }


    @Test
    void deleteSubmissionTest() throws IOException, EntityNotFoundException, HasEvaluationsException {

        doNothing().when(submissionService).delete(any());
        doNothing().when(externalContext).redirect(any());
        submissionBean.deleteSubmission();
        verify(submissionService, times(1)).delete(any());
        verifyNoMoreInteractions(submissionService);
        verify(externalContext, times(1)).redirect("meeting.xhtml?meeting-Id=" + meeting.getId());
        verifyNoMoreInteractions(externalContext);
        verifyNoInteractions(facesContext);

    }

    @Test
    public void EntityNotFoundException() throws EntityNotFoundException, OutdatedException, UserNotPermittedException, DuplicateEmailException, IOException, HasEvaluationsException {

        doThrow(new EntityNotFoundException()).when(submissionService).delete(any());
        submissionBean.deleteSubmission();
        verify(submissionService, times(1)).delete(any());
        verifyNoMoreInteractions(submissionService);
        verify(facesContext, times(1)).addMessage(anyObject(), notNull());
        verifyNoMoreInteractions(facesContext);


    }

    @Test
    public void HasEvaluationsException() throws EntityNotFoundException, OutdatedException, UserNotPermittedException, DuplicateEmailException, IOException, HasEvaluationsException {

        doThrow(new HasEvaluationsException()).when(submissionService).delete(any());
        submissionBean.deleteSubmission();
        verify(submissionService, times(1)).delete(any());
        verifyNoMoreInteractions(submissionService);
        verify(facesContext, times(1)).addMessage(anyObject(), notNull());
        verifyNoMoreInteractions(facesContext);


    }

}