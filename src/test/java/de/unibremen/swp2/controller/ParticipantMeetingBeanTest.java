package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.Exceptions.*;
import de.unibremen.swp2.service.*;
import lombok.Getter;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ParticipantMeetingBeanTest {

    @InjectMocks
    ParticipantMeetingBean participantMeetingBean;

    @Mock
    FinalGradeService finalGradeService;

    @Mock
    ExternalContext externalContext;

    @Mock
    FacesContext facesContext;

    @Mock
    MeetingService meetingService;

    @Mock
    ParticipantService participantService;

    @Mock
    Meeting meeting;

    @Mock
    Participant participant;

    @Mock
    ParticipantStatus status;

    @Mock
    UserService userService;

    @Mock
    Principal principal;

    @Mock
    FinalGrade finalGrade;

    @Mock
    SubmissionService submissionService;

    @Mock
    EvaluationService evaluationService;

    @Mock
    ExamService examService;

    @Mock
    List<Evaluation> evaluations;


    @Mock
    @RequestParameterMap
    private Map<String, String> parameterMap;

    @Test
    void initAsAdminTestTest() {

        User user = new User();
        user.setRole(Role.A);

        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(parameterMap.get(anyString())).thenReturn(value);
        when(participantService.getById(any())).thenReturn(participant);
        when(meetingService.getById(any())).thenReturn(meeting);
        when(participantService.getParticipantStatusByParticipantAndMeeting(any(), any())).thenReturn(status);
        lenient().when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        lenient().when(meetingService.getMeetingByTutorial(any())).thenReturn(meeting);
        when(evaluationService.getEvaluationsByParticipantAndMeeting(any(), any())).thenReturn(evaluations);

        new Mirror().on(participantMeetingBean).invoke().method("init").withoutArgs();
        verify(parameterMap, times(2)).get(anyString());
        verify(participantService, times(1)).getById(any());

        Assertions.assertNotNull(participantService.getById(parameterMap.get("participant-Id")));
        Assertions.assertNotNull(meetingService.getById(parameterMap.get("meeting-Id")));


    }

    @Test
    void initTestAsOtherTest() {
        User user = new User();
        user.setRole(Role.CEO);

        UserMeetingRole userMeetingRole = new UserMeetingRole();
        userMeetingRole.setRole(Role.CEO);

        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(parameterMap.get(anyString())).thenReturn(value);
        when(participantService.getById(any())).thenReturn(participant);
        when(meetingService.getById(any())).thenReturn(meeting);
        meeting.setVisible(true);


        lenient().when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        lenient().when(userService.getUserMeetingRoleByUserAndMeeting(any(), any())).thenReturn(userMeetingRole);
        lenient().when(meetingService.getMeetingByTutorial(any())).thenReturn(meeting);


        new Mirror().on(participantMeetingBean).invoke().method("init").withoutArgs();
        verify(parameterMap, times(2)).get(anyString());
        verify(participantService, times(1)).getById(any());



    }


    @Test
    void createFinalGradeTest() throws DuplicateEmailException, IOException, EntityNotFoundException, OutdatedException {

        doNothing().when(finalGradeService).create(any());
        doNothing().when(externalContext).redirect(any());

        participantMeetingBean.createFinalGrade();
        verify(finalGradeService, times(1)).create(any());
        verifyNoMoreInteractions(finalGradeService);
        verify(externalContext, times(1)).redirect("meeting.xhtml?meeting-Id=" + meeting.getId());
        verifyNoMoreInteractions(externalContext);
        verifyNoInteractions(facesContext);
    }


    @Test
    public void OutdatedExceptionTest() throws EntityNotFoundException, OutdatedException, IOException {
        doThrow(new OutdatedException()).when(finalGradeService).create(any());

        participantMeetingBean.createFinalGrade();

        verify(finalGradeService, times(1)).create(any());
        verifyNoMoreInteractions(finalGradeService);
        verify(facesContext, times(1)).addMessage(anyObject(), notNull());
        verifyNoMoreInteractions(facesContext);


    }

    @Test
    public void EntityNotFoundException() throws EntityNotFoundException, OutdatedException, IOException {
        doThrow(new EntityNotFoundException()).when(finalGradeService).create(any());

        participantMeetingBean.createFinalGrade();

        verify(finalGradeService, times(1)).create(any());
        verifyNoMoreInteractions(finalGradeService);
        verify(facesContext, times(1)).addMessage(anyObject(), notNull());
        verifyNoMoreInteractions(facesContext);


    }

    @Test
    void calculateFinalGradeNotPassedTest() throws InvalidWeightingException, NotGradedException {

        when(finalGrade.getOverallGrade()).thenReturn(new BigDecimal("5.0"));

        participantMeetingBean.calculateFinalGrade();

        verify(status, times(1)).setMeetingStatus(ParticipantMeetingStatus.Durchgefallen);
    }

    @Test
    void calculateFinalGradePassedTest() throws InvalidWeightingException, NotGradedException {

        when(finalGrade.getOverallGrade()).thenReturn(new BigDecimal("1.0"));

        participantMeetingBean.calculateFinalGrade();

        verify(status, times(1)).setMeetingStatus(ParticipantMeetingStatus.Bestanden);

    }

    @Test
    void NotGradedExceptionTest() throws InvalidWeightingException, NotGradedException {

        doThrow(new NotGradedException()).when(finalGradeService).calculateFinalGrade(any(), any(),
                any(), anyBoolean(),
                anyBoolean(), anyBoolean(), anyInt(),
                anyBoolean());

        participantMeetingBean.calculateFinalGrade();

        verify(finalGradeService, times(1)).calculateFinalGrade(any(), any(),
                any(), anyBoolean(),
                anyBoolean(), anyBoolean(), anyInt(),
                anyBoolean());
        verifyNoMoreInteractions(finalGradeService);
        verify(facesContext, times(1)).addMessage(any(), notNull());
        verifyNoMoreInteractions(facesContext);


    }

    @Test
    void InvalidWeightingExceptionTest() throws InvalidWeightingException, NotGradedException {

        doThrow(new InvalidWeightingException()).when(finalGradeService).calculateFinalGrade(any(), any(),
                any(), anyBoolean(),
                anyBoolean(), anyBoolean(), anyInt(),
                anyBoolean());

        participantMeetingBean.calculateFinalGrade();

        verify(finalGradeService, times(1)).calculateFinalGrade(any(), any(),
                any(), anyBoolean(),
                anyBoolean(), anyBoolean(), anyInt(),
                anyBoolean());
        verifyNoMoreInteractions(finalGradeService);
        verify(facesContext, times(1)).addMessage(any(), notNull());
        verifyNoMoreInteractions(facesContext);


    }












    /*@Test
    public void NotGradedException() throws EntityNotFoundException, OutdatedException, IOException {
        doThrow(new NotGradedException()).when(finalGradeService).create(any());

        participantMeetingBean.createFinalGrade();

        verify(finalGradeService, times(1)).create(any());
        verifyNoMoreInteractions(finalGradeService);
        verify(facesContext, times(1)).addMessage(anyObject(), notNull());
        verifyNoMoreInteractions(facesContext);


    }


    @Test
    public void InvalidWeightingException() throws EntityNotFoundException, OutdatedException, IOException {
        doThrow(new InvalidWeightingException()).when(finalGradeService).create(any());

        participantMeetingBean.createFinalGrade();

        verify(finalGradeService, times(1)).create(any());
        verifyNoMoreInteractions(finalGradeService);
        verify(facesContext, times(1)).addMessage(anyObject(), notNull());
        verifyNoMoreInteractions(facesContext);


    }*/


}