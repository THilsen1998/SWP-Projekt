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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.faces.annotation.RequestParameterMap;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
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

@ExtendWith(MockitoExtension.class)
class AddGroupBeanTest {

    @Mock
    ExternalContext externalContext;

    @Mock
    FacesContext facesContext;

    @Mock
    GroupService groupService;

    @Mock
    ParticipantService participantService;

    @Mock
    UserService userService;

    @Mock
    Principal principal;

    @Mock
    TutorialService tutorialService;

    @Mock
    MeetingService meetingService;

    @Mock
    Meeting meeting;

    @Mock
    Tutorial tutorial;

    @Mock
    TGroup group;

    @Mock
    @RequestParameterMap
    private Map<String, String> parameterMap;

    @InjectMocks
    AddGroupBean addGroupBean;

    @Mock
    List<Participant> participantsToAdd;

    @Mock
    List<Participant> filteredParticipantsToAdd;

    @Mock
    List<Participant> filteredParticipantsInTutorial;

    @Mock
    List<Participant> participantsInTutorial;

    @Test
    void initAsAdminTest() {
        User user = new User();
        user.setRole(Role.A);

        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(tutorialService.getById(any())).thenReturn(tutorial);
        lenient().when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        lenient().when(meetingService.getMeetingByTutorial(any())).thenReturn(meeting);

        new Mirror().on(addGroupBean).invoke().method("init").withoutArgs();

        Assertions.assertNotNull(tutorialService.getById(parameterMap.get("tutorial-Id")));
        verify(parameterMap, times(2)).get(anyString());
        verify(tutorialService, times(2)).getById(any());
        verify(meetingService, times(1)).getMeetingByTutorial(any());

    }

    @Test
    void initTestAsOtherTest() {
        User user = new User();
        user.setRole(Role.T);

        UserMeetingRole userMeetingRole = new UserMeetingRole();
        userMeetingRole.setRole(Role.T);

        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(tutorialService.getById(any())).thenReturn(tutorial);

        lenient().when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        lenient().when(userService.getUserMeetingRoleByTutorialAndUser(any(), any())).thenReturn(userMeetingRole);
        lenient().when(meetingService.getMeetingByTutorial(any())).thenReturn(meeting);

        new Mirror().on(addGroupBean).invoke().method("init").withoutArgs();

        verify(parameterMap, times(1)).get(anyString());
        verify(tutorialService, times(1)).getById(any());
        verify(meetingService, times(1)).getMeetingByTutorial(any());

    }

    @Test
    void initAsOtherThanAdminTest() {

        User user = new User();
        user.setRole(Role.CEO);

        UserMeetingRole userMeetingRole = new UserMeetingRole();
        userMeetingRole.setRole(Role.CEO);

        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(tutorialService.getById(any())).thenReturn(tutorial);

        when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        when(userService.getUserMeetingRoleByTutorialAndUser(any(), any())).thenThrow(new NoResultException());
        lenient().when(userService.getUserMeetingRoleByUserAndMeeting(any(), any())).thenReturn(userMeetingRole);
        when(meetingService.getMeetingByTutorial(any())).thenReturn(meeting);

        new Mirror().on(addGroupBean).invoke().method("init").withoutArgs();

    }

    @Test
    void initAsOtherThanAdminSecondNonResultExceptionTest() {

        User user = new User();
        user.setRole(Role.CEO);

        UserMeetingRole userMeetingRole = new UserMeetingRole();
        userMeetingRole.setRole(Role.CEO);

        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(tutorialService.getById(any())).thenReturn(tutorial);

        when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        lenient().when(userService.getUserMeetingRoleByTutorialAndUser(any(), any())).thenReturn(userMeetingRole);
        lenient().when(userService.getUserMeetingRoleByUserAndMeeting(any(), any())).thenThrow(new NoResultException());
        meeting.setVisible(true);
        when(meetingService.getMeetingByTutorial(any())).thenReturn(meeting);

        new Mirror().on(addGroupBean).invoke().method("init").withoutArgs();
    }


    @Test
    void initAsNonTest() {

        User user = new User();
        user.setRole(Role.T);

        UserMeetingRole userMeetingRole = new UserMeetingRole();
        userMeetingRole.setRole(Role.T);

        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(tutorialService.getById(any())).thenReturn(tutorial);

        when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        when(userService.getUserMeetingRoleByTutorialAndUser(any(), any())).thenThrow(new NoResultException());
        lenient().when(userService.getUserMeetingRoleByUserAndMeeting(any(), any())).thenReturn(userMeetingRole);
        meeting.setVisible(true);
        when(meetingService.getMeetingByTutorial(any())).thenReturn(meeting);

        new Mirror().on(addGroupBean).invoke().method("init").withoutArgs();

        verify(parameterMap, times(1)).get(anyString());
        verify(tutorialService, times(1)).getById(any());
        verify(meetingService, times(1)).getMeetingByTutorial(any());
    }

    @Test
    void createGroupTest() throws IOException, ParticipantNotInMeetingException, EntityNotFoundException, OutdatedException {


        doNothing().when(groupService).create(any(), any());
        doNothing().when(externalContext).redirect(any());

        addGroupBean.createGroup();

        verify(groupService, times(1)).create(any(), any());
        verifyNoMoreInteractions(groupService);
        verify(externalContext, times(1)).redirect("single-tutorial.xhtml?tutorial-Id=" + tutorial.getId());
        verifyNoMoreInteractions(externalContext);
        verifyNoInteractions(facesContext);

    }

    @Test
    public void participantNotInMeetingExceptionTest() throws IOException, EntityNotFoundException, OutdatedException, ParticipantNotInMeetingException {
        doThrow(new ParticipantNotInMeetingException()).when(groupService).create(any(), any());

        addGroupBean.createGroup();

        verify(groupService, times(1)).create(any(), any());
        verifyNoMoreInteractions(groupService);
        verify(facesContext, times(1)).addMessage(any(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    public void entityNotFoundExceptionTest() throws IOException, EntityNotFoundException, OutdatedException, ParticipantNotInMeetingException {
        doThrow(new EntityNotFoundException()).when(groupService).create(any(), any());

        addGroupBean.createGroup();

        verify(groupService, times(1)).create(any(), any());
        verifyNoMoreInteractions(groupService);
        verify(facesContext, times(1)).addMessage(any(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    public void outdatedExceptionTest() throws IOException, EntityNotFoundException, OutdatedException, ParticipantNotInMeetingException {
        doThrow(new OutdatedException()).when(groupService).create(any(), any());

        addGroupBean.createGroup();

        verify(groupService, times(1)).create(any(), any());
        verifyNoMoreInteractions(groupService);
        verify(facesContext, times(1)).addMessage(any(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void addParticipantTest()       // 1 if Zweig
    {

        Participant participant = new Participant();
        when(!participantsToAdd.contains(participant)).thenReturn(false);
        lenient().when(participantsToAdd.add(participant)).thenReturn(true);
        lenient().when(participantsInTutorial.remove(participant)).thenReturn(true);

        addGroupBean.addParticipant(participant);

        verify(participantsToAdd, times(1)).add(any());
        verify(participantsInTutorial, times(1)).remove(any());
    }

    @Test
    void addParticipant2Test()  // else Teil
    {

        Participant participant = new Participant();
        when(!participantsToAdd.contains(participant)).thenReturn(true);

        addGroupBean.addParticipant(participant);

        verify(facesContext, times(1)).addMessage(any(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void addParticipant3Test()  // 2 if Zweig und else Zweig
    {
        Participant participant = new Participant();
        when(!participantsToAdd.contains(participant)).thenReturn(false);
        lenient().when(participantsToAdd.add(participant)).thenReturn(false);
        lenient().when(participantsInTutorial.remove(participant)).thenReturn(false);
        addGroupBean.setFilteredParticipantsInTutorial(filteredParticipantsInTutorial);
        lenient().when(filteredParticipantsInTutorial.remove(participant)).thenReturn(true);

        addGroupBean.addParticipant(participant);

        verify(participantsToAdd, times(1)).add(any());
        verify(participantsInTutorial, times(1)).remove(any());
        verify(filteredParticipantsInTutorial, times(1)).remove(any());
    }


    @Test
    void addParticipant4Test()  // 3 if Zweig
    {
        Participant participant = new Participant();
        when(!participantsToAdd.contains(participant)).thenReturn(false);
        lenient().when(participantsToAdd.add(participant)).thenReturn(false);
        lenient().when(participantsInTutorial.remove(participant)).thenReturn(false);
        addGroupBean.setFilteredParticipantsToAdd(filteredParticipantsToAdd);
        lenient().when(filteredParticipantsToAdd.add(participant)).thenReturn(true);

        addGroupBean.addParticipant(participant);

        verify(participantsToAdd, times(1)).add(any());
        verify(participantsInTutorial, times(1)).remove(any());
        verify(filteredParticipantsToAdd, times(1)).add(any());
    }


    @Test
    void deleteParticipant() {
        Participant participant = new Participant();
        when(participantsToAdd.contains(participant)).thenReturn(true);
        lenient().when(participantsToAdd.remove(participant)).thenReturn(true);
        lenient().when(participantsInTutorial.add(participant)).thenReturn(true);

        addGroupBean.deleteParticipant(participant);

        verify(participantsToAdd, times(1)).remove(any());
        verify(participantsInTutorial, times(1)).add(any());
    }

    @Test
    void deleteParticipan2t() {
        Participant participant = new Participant();
        when(participantsToAdd.contains(participant)).thenReturn(false);

        addGroupBean.deleteParticipant(participant);

        verify(facesContext, times(1)).addMessage(any(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void deleteParticipant3() {
        Participant participant = new Participant();
        when(participantsToAdd.contains(participant)).thenReturn(true);
        lenient().when(participantsToAdd.remove(participant)).thenReturn(true);
        lenient().when(participantsInTutorial.add(participant)).thenReturn(true);
        addGroupBean.setFilteredParticipantsInTutorial(filteredParticipantsInTutorial);
        lenient().when(filteredParticipantsInTutorial.add(participant)).thenReturn(false);

        addGroupBean.deleteParticipant(participant);

        verify(participantsToAdd, times(1)).remove(any());
        verify(participantsInTutorial, times(1)).add(any());
        verify(filteredParticipantsInTutorial, times(1)).add(any());
    }

    @Test
    void deleteParticipant4() {
        Participant participant = new Participant();
        when(participantsToAdd.contains(participant)).thenReturn(true);
        lenient().when(participantsToAdd.remove(participant)).thenReturn(true);
        lenient().when(participantsInTutorial.add(participant)).thenReturn(true);
        addGroupBean.setFilteredParticipantsToAdd(filteredParticipantsToAdd);
        lenient().when(filteredParticipantsToAdd.remove(participant)).thenReturn(false);

        addGroupBean.deleteParticipant(participant);

        verify(participantsToAdd, times(1)).remove(any());
        verify(participantsInTutorial, times(1)).add(any());
        verify(filteredParticipantsToAdd, times(1)).remove(any());
    }
}