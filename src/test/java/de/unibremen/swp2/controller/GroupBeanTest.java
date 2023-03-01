package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.Exceptions.*;
import de.unibremen.swp2.persistence.MeetingDAO;
import de.unibremen.swp2.service.*;
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
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class GroupBeanTest {

    @InjectMocks
    GroupBean groupBean;

    @Mock
    Tutorial tutorial;

    @Mock
    TGroup group;

    @Mock
    Meeting meeting;

    @Mock
    GroupService groupService;

    @Mock
    MeetingService meetingService;

    @Mock
    ParticipantService participantService;

    @Mock
    TutorialService tutorialService;

    @Mock
    UserService userService;

    @Mock
    ExternalContext externalContext;

    @Mock
    FacesContext facesContext;

    @Mock
    Principal principal;

    @Mock
    @RequestParameterMap
    private Map<String, String> parameterMap;

    @Mock
    List<Participant> participantsToAdd;

    @Mock
    List<Participant> filteredParticipantsToAdd;

    @Mock
    List<Participant> filteredParticipantsInTutorial;

    @Mock
    List<Participant> participantsInTutorial;

    @Mock
    List<Participant> newGroupParticipantsToAdd;

    @Mock
    List<Participant> filteredNewGroupParticipantsToAdd;

    @Mock
    List<Participant> participantsInTutorialForNewGroup;

    @Mock
    List<Participant> filteredParticipantsInTutorialForNewGroup;

    @Test
    void initAsAdminTestTest() {

        User user = new User();
        user.setRole(Role.A);

        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(groupService.getById(any())).thenReturn(group);
        lenient().when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        lenient().when(meetingService.getMeetingByTutorial(any())).thenReturn(meeting);

        new Mirror().on(groupBean).invoke().method("init").withoutArgs();

        //meeting.getOnlySplitGroup == false !!!!!
        assertFalse(meeting.getOnlyGroupSplit());
        assertNotNull(groupService.getById(parameterMap.get("group-Id")));

    }

    @Test
    void initTestAsOtherTest() {

        User user = new User();
        user.setRole(Role.T);

        UserMeetingRole userMeetingRole = new UserMeetingRole();
        userMeetingRole.setRole(Role.T);

        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(groupService.getById(any())).thenReturn(group);

        lenient().when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        lenient().when(userService.getUserMeetingRoleByTutorialAndUser(any(), any())).thenReturn(userMeetingRole);
        lenient().when(meetingService.getMeetingByTutorial(any())).thenReturn(meeting);


        new Mirror().on(groupBean).invoke().method("init").withoutArgs();

    }

    @Test
    void initAsOtherThanAdminTest() {

        User user = new User();
        user.setRole(Role.CEO);

        UserMeetingRole userMeetingRole = new UserMeetingRole();
        userMeetingRole.setRole(Role.CEO);

        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(groupService.getById(any())).thenReturn(group);

        when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        when(userService.getUserMeetingRoleByTutorialAndUser(any(), any())).thenThrow(new NoResultException());
        when(userService.getUserMeetingRoleByUserAndMeeting(any(), any())).thenReturn(userMeetingRole);
        when(meetingService.getMeetingByTutorial(any())).thenReturn(meeting);


        new Mirror().on(groupBean).invoke().method("init").withoutArgs();

    }

    @Test
    void initAsOtherThanAdminSecondNonResultExceptionTest() {

        User user = new User();
        user.setRole(Role.CEO);

        UserMeetingRole userMeetingRole = new UserMeetingRole();
        userMeetingRole.setRole(Role.CEO);

        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(groupService.getById(any())).thenReturn(group);

        when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        when(userService.getUserMeetingRoleByTutorialAndUser(any(), any())).thenThrow(new NoResultException());
        when(userService.getUserMeetingRoleByUserAndMeeting(any(), any())).thenThrow(new NoResultException());
        when(meetingService.getMeetingByTutorial(any())).thenReturn(meeting);

        new Mirror().on(groupBean).invoke().method("init").withoutArgs();

    }


    @Test
    void initAsNonTest() {

        User user = new User();
        user.setRole(Role.T);

        UserMeetingRole userMeetingRole = new UserMeetingRole();
        userMeetingRole.setRole(Role.T);

        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(groupService.getById(any())).thenReturn(group);

        when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        when(userService.getUserMeetingRoleByTutorialAndUser(any(), any())).thenThrow(new NoResultException());
        when(userService.getUserMeetingRoleByUserAndMeeting(any(), any())).thenReturn(userMeetingRole);
        when(meetingService.getMeetingByTutorial(any())).thenReturn(meeting);


        new Mirror().on(groupBean).invoke().method("init").withoutArgs();

    }

    @Test
    void updateTest() throws EntityNotFoundException, OutdatedException, IOException {
        doNothing().when(groupService).update(any(), any());
        doNothing().when(externalContext).redirect(any());

        groupBean.update();

        verify(groupService, times(1)).update(any(), any());
        verifyNoMoreInteractions(groupService);
        verify(externalContext, times(1)).redirect("single-tutorial.xhtml?tutorial-Id=" + tutorial.getId());
        verifyNoMoreInteractions(externalContext);
        verifyNoInteractions(facesContext);
    }

    @Test
    public void EntityNotFoundExceptionTest() throws EntityNotFoundException, OutdatedException, UserNotPermittedException, DuplicateEmailException, IOException {

        doThrow(new EntityNotFoundException()).when(groupService).update(any(), any());

        groupBean.update();

        verify(groupService, times(1)).update(any(), any());
        verifyNoMoreInteractions(groupService);
        verify(facesContext, times(1)).addMessage(any(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    public void OutdatedExceptionTest() throws EntityNotFoundException, OutdatedException, UserNotPermittedException, DuplicateEmailException, IOException {

        doThrow(new OutdatedException()).when(groupService).update(any(), any());

        groupBean.update();

        verify(groupService, times(1)).update(any(), any());
        verifyNoMoreInteractions(groupService);
        verify(facesContext, times(1)).addMessage(any(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void showTableTest() {

        assertFalse(groupBean.isSplit());
        groupBean.showTable();
        assertTrue(groupBean.isSplit());
        verify(facesContext, times(1)).addMessage(any(), notNull());

    }

    @Test
    void addParticipantTest() {

        Participant participant = Mockito.mock(Participant.class);
        when(!participantsToAdd.contains(participant)).thenReturn(false);
        lenient().when(participantsToAdd.add(participant)).thenReturn(true);
        lenient().when(participantsInTutorial.remove(participant)).thenReturn(true);

        groupBean.addParticipant(participant);

        verify(participantsToAdd, times(1)).add(any());
        verify(participantsInTutorial, times(1)).remove(any());

    }

    @Test
    void addParticipant2TestFacesMessage() {

        Participant participant = Mockito.mock(Participant.class);
        when(!participantsToAdd.contains(participant)).thenReturn(true);

        groupBean.addParticipant(participant);

        verify(facesContext, times(1)).addMessage(any(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void addParticipant3Test()  // 2 if Zweig und else Zweig
    {
        Participant participant = Mockito.mock(Participant.class);
        when(!participantsToAdd.contains(participant)).thenReturn(false);
        lenient().when(participantsToAdd.add(participant)).thenReturn(false);
        lenient().when(participantsInTutorial.remove(participant)).thenReturn(false);
        groupBean.setFilteredParticipantsInTutorial(filteredParticipantsInTutorial);
        lenient().when(filteredParticipantsInTutorial.remove(participant)).thenReturn(true);

        groupBean.addParticipant(participant);

        verify(participantsToAdd, times(1)).add(any());
        verify(participantsInTutorial, times(1)).remove(any());
        verify(filteredParticipantsInTutorial, times(1)).remove(any());
    }


    @Test
    void addParticipant4Test()  // 3 if Zweig
    {
        Participant participant = Mockito.mock(Participant.class);
        when(!participantsToAdd.contains(participant)).thenReturn(false);
        lenient().when(participantsToAdd.add(participant)).thenReturn(false);
        lenient().when(participantsInTutorial.remove(participant)).thenReturn(false);
        groupBean.setFilteredParticipantsToAdd(filteredParticipantsToAdd);
        lenient().when(filteredParticipantsToAdd.add(participant)).thenReturn(true);

        groupBean.addParticipant(participant);

        verify(participantsToAdd, times(1)).add(any());
        verify(participantsInTutorial, times(1)).remove(any());
        verify(filteredParticipantsToAdd, times(1)).add(any());
    }

    @Test
    void deleteParticipantTest() {
        Participant participant = Mockito.mock(Participant.class);
        when(participantsToAdd.contains(participant)).thenReturn(true);
        lenient().when(participantsToAdd.remove(participant)).thenReturn(true);
        lenient().when(participantsInTutorial.add(participant)).thenReturn(true);

        groupBean.deleteParticipant(participant);

        verify(participantsToAdd, times(1)).remove(any());
        verify(participantsInTutorial, times(1)).add(any());
    }

    @Test
    void deleteParticipant2Test() {
        Participant participant = Mockito.mock(Participant.class);
        when(participantsToAdd.contains(participant)).thenReturn(false);

        groupBean.deleteParticipant(participant);

        verify(facesContext, times(2)).addMessage(any(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void deleteParticipant3Test() {
        Participant participant = Mockito.mock(Participant.class);
        when(participantsToAdd.contains(participant)).thenReturn(true);
        meeting.setOnlyGroupSplit(true);
        lenient().when(participantsToAdd.remove(participant)).thenReturn(true);
        lenient().when(participantsInTutorial.add(participant)).thenReturn(true);
        groupBean.setFilteredParticipantsInTutorial(filteredParticipantsInTutorial);
        lenient().when(filteredParticipantsInTutorial.add(participant)).thenReturn(false);

        groupBean.deleteParticipant(participant);

        verify(participantsToAdd, times(1)).remove(any());
        verify(participantsInTutorial, times(1)).add(any());
        verify(filteredParticipantsInTutorial, times(1)).add(any());
    }

    @Test
    void deleteParticipant4Test() {

        User user = new User();
        user.setRole(Role.A);

        meeting.setOnlyGroupSplit(true);
        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(groupService.getById(any())).thenReturn(group);
        lenient().when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        lenient().when(meetingService.getMeetingByTutorial(any())).thenReturn(meeting);
        Meeting meeting1 = Mockito.mock(Meeting.class);
        meeting1.setOnlyGroupSplit(true);

        new Mirror().on(groupBean).invoke().method("init").withoutArgs();

        Participant participant = Mockito.mock(Participant.class);
        lenient().when(participantsToAdd.contains(participant)).thenReturn(true);
//        lenient().when(participantsToAdd.remove(participant)).thenReturn(true);
//        lenient().when(participantsInTutorial.add(participant)).thenReturn(true);
//        groupBean.setFilteredParticipantsToAdd(filteredParticipantsToAdd);
//        lenient().when(filteredParticipantsToAdd.remove(participant)).thenReturn(false);

        groupBean.deleteParticipant(participant);

//        verify(participantsToAdd, times(1)).remove(any());
//        verify(participantsInTutorial, times(1)).add(any());
//        verify(filteredParticipantsToAdd, times(1)).remove(any());
    }

/*    @Test
    void deleteParticipantGroupSwitchTrue() {
        assertTrue(groupBean.isGroupSplit());
        Participant participant = Mockito.mock(Participant.class);
        when(participantsToAdd.contains(participant)).thenReturn(true);
        groupBean.deleteParticipant(participant);
        assertTrue(groupBean.isGroupSplit());
        verify(participantsToAdd, times(1)).remove(any());
        verify(newGroupParticipantsToAdd, times(1)).remove(any());
    }*/


    @Test
    void createGroupsTest() throws EntityNotFoundException, OutdatedException, IOException, ParticipantNotInMeetingException {

        doNothing().when(groupService).update(any(), any());
        doNothing().when(groupService).create(any(), any());
        doNothing().when(externalContext).redirect(any());


        groupBean.createGroups();

        verify(groupService, times(1)).update(any(), any());
        verify(groupService, times(1)).create(any(), any());

        verifyNoMoreInteractions(groupService);
        verify(externalContext, times(1)).redirect("single-tutorial.xhtml?tutorial-Id=" + tutorial.getId());
        verifyNoMoreInteractions(externalContext);
        verifyNoInteractions(facesContext);
    }

    @Test
    public void ParticipantNotInMeetingExceptionTest() throws EntityNotFoundException, OutdatedException, IOException, ParticipantNotInMeetingException {

        doThrow(new ParticipantNotInMeetingException()).when(groupService).create(any(), any());

        groupBean.createGroups();

        verify(groupService, times(1)).create(any(), any());
        verifyNoMoreInteractions(groupService);
        verify(facesContext, times(1)).addMessage(any(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    public void EntityNotFoundExceptionForCreateGroupsTest() throws EntityNotFoundException, OutdatedException, IOException {

        doThrow(new EntityNotFoundException()).when(groupService).update(any(), any());

        groupBean.createGroups();

        verify(groupService, times(1)).update(any(), any());
        verify(facesContext, times(1)).addMessage(any(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    public void OutdatedExceptionForCreateGroupsTest() throws EntityNotFoundException, OutdatedException, IOException, ParticipantNotInMeetingException {

        doThrow(new OutdatedException()).when(groupService).create(any(), any());

        groupBean.createGroups();

        verify(groupService, times(1)).create(any(), any());
        verify(facesContext, times(1)).addMessage(any(), notNull());
        verifyNoMoreInteractions(facesContext);
    }


    @Test
    void addParticipantFromNewGroupTest() {

        Participant participant = Mockito.mock(Participant.class);
        when(!newGroupParticipantsToAdd.contains(participant)).thenReturn(false);
        lenient().when(newGroupParticipantsToAdd.add(participant)).thenReturn(true);
        lenient().when(participantsInTutorialForNewGroup.remove(participant)).thenReturn(true);

        groupBean.addParticipantFromNewGroup(participant);

        verify(newGroupParticipantsToAdd, times(1)).add(any());
        verify(participantsInTutorialForNewGroup, times(1)).remove(any());
    }

    @Test
    void addParticipantFromNewGroupFacesMessageTest() {

        Participant participant = Mockito.mock(Participant.class);
        when(!newGroupParticipantsToAdd.contains(participant)).thenReturn(true);

        groupBean.addParticipantFromNewGroup(participant);

        verify(facesContext, times(1)).addMessage(any(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void deleteParticipantsFromNewGroupTest() {

        Participant participant = Mockito.mock(Participant.class);
        when(newGroupParticipantsToAdd.contains(participant)).thenReturn(true);
        lenient().when(newGroupParticipantsToAdd.remove(participant)).thenReturn(true);
        lenient().when(participantsInTutorialForNewGroup.add(participant)).thenReturn(true);

        groupBean.deleteParticipantsFromNewGroup(participant);

        verify(newGroupParticipantsToAdd, times(1)).remove(any());
        verify(participantsInTutorialForNewGroup, times(1)).add(any());
    }

    @Test
    void deleteParticipantFromNewGroupFacesMessageTest() {

        Participant participant = Mockito.mock(Participant.class);
        when(newGroupParticipantsToAdd.contains(participant)).thenReturn(false);

        groupBean.deleteParticipantsFromNewGroup(participant);

        verify(facesContext, times(2)).addMessage(any(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void isSplit() {
    }

    @Test
    void isGroupSplit() {
    }
}