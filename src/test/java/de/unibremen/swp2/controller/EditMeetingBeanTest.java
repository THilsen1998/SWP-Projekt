package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.Exceptions.*;
import de.unibremen.swp2.service.MeetingService;
import de.unibremen.swp2.service.ParticipantService;
import de.unibremen.swp2.service.UserService;
import lombok.Getter;
import lombok.Setter;
import net.vidageek.mirror.dsl.Mirror;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.faces.annotation.RequestParameterMap;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class EditMeetingBeanTest
{
    @Mock
    private ExternalContext externalContext;

    @Mock
    private Meeting meeting;

    @Mock
    private MeetingService meetingService;

    @Mock
    private UserService userService;

    @Mock
    private Principal principal;

    @Mock
    ParticipantService participantService;
    @Mock
    private List<Participant> participantsToAdd;

    @Mock
    private List<Participant> filteredParticipantsToAdd;

    @Mock
    private List<Participant> participantsNotInMeeting;

    @Mock
    private List<Participant> filteredParticipantsNotInMeeting;

    @Mock
    private List<User> usersToAdd;

    @Mock
    private List<User> filteredUsersToAdd;

    @Mock
    private List<User> lecturersNotInMeeting;

    @Mock
    private List<User> filteredLecturersNotInMeeting;

    @Mock
    private User ceoToAdd;

    @Mock
    private List<User> allUsersNotInMeeting;

    @Mock
    private List<User> filteredAllUsersNotInMeeting;

    @Mock
    FacesContext facesContext;

    @Mock
    @RequestParameterMap
    private Map<String, String> parameterMap;

    @InjectMocks
    EditMeetingBean editMeetingBean;

    @Test
    void initTestUserRoleA () {

        String  value= "819cb31c-3b38-4706-b3d5-b7235e59df99";
        parameterMap.put("meeting-Id",value);
        Meeting meeting2 = new Meeting();
        when(meetingService.getById(value)).thenReturn(meeting2);
        when(parameterMap.get(anyString())).thenReturn(value);
        User user = new User();
        user.setRole(Role.A);
        when( userService.getUsersByEmail(principal.getName())).thenReturn(user);
        new Mirror().on(editMeetingBean).invoke().method("init").withoutArgs();

        verify(meetingService, times(1)).getById(value);
        verify(parameterMap, times(1)).get(anyString());

    }
    @Test
    void initTestUserRole () {

        String  value= "819cb31c-3b38-4706-b3d5-b7235e59df99";
        parameterMap.put("meeting-Id",value);
        Meeting meeting2 = new Meeting();
        when(meetingService.getById(value)).thenReturn(meeting2);
        when(parameterMap.get(anyString())).thenReturn(value);
        User user1 = new User();
        when( userService.getUsersByEmail(principal.getName())).thenReturn(user1);
        UserMeetingRole userMeetingRole = new UserMeetingRole();
        userMeetingRole.setRole(Role.D);
        when( userService.getUserMeetingRoleByUserAndMeeting(any(), any())).thenReturn(userMeetingRole);

        when(participantService.getAllParticipantsByMeeting(any())).thenReturn(new ArrayList<Participant>());
        when(participantService.getParticipantsNotInMeeting(any())).thenReturn(new ArrayList<Participant>());

        new Mirror().on(editMeetingBean).invoke().method("init").withoutArgs();

        verify(meetingService, times(1)).getById(value);
        verify(parameterMap, times(1)).get(anyString());
        verify(participantService, times(1)).getAllParticipantsByMeeting(any());
        verify(participantService, times(1)).getParticipantsNotInMeeting(any());
        verify(parameterMap, times(1)).get(anyString());

        doThrow(new NoResultException()).when(userService).getUserMeetingRoleByUserAndMeeting(any(), any());
        new Mirror().on(editMeetingBean).invoke().method("init").withoutArgs();

    }


    @Test
    void updateTest() throws EntityNotFoundException, UserNotInMeetingException
            , UserAlreadyInMeetingException, UserNotPermittedException
            , OutdatedException, UserAlreadyInOtherRoleException
            , ParticipantAlreadyInMeetingException, IOException
    {
        doNothing().when(meetingService).update(any(),any(),any(),any());
        editMeetingBean.update();
        verify(externalContext, times(1)).redirect("meeting.xhtml?meeting-Id=" + meeting.getId());

    }

    @Test
    void updateAllExcentes() throws EntityNotFoundException, UserNotInMeetingException
                                    , UserAlreadyInMeetingException, UserNotPermittedException
                                    , OutdatedException, UserAlreadyInOtherRoleException
                                    , ParticipantAlreadyInMeetingException, IOException
    {
        doThrow(new OutdatedException()).when(meetingService).update(any(),any(),any(),any());
        editMeetingBean.update();

        doThrow(new EntityNotFoundException()).when(meetingService).update(any(),any(),any(),any());
        editMeetingBean.update();

        doThrow(new UserNotPermittedException()).when(meetingService).update(any(),any(),any(),any());
        editMeetingBean.update();

        doThrow(new UserNotInMeetingException()).when(meetingService).update(any(),any(),any(),any());
        editMeetingBean.update();

        doThrow(new UserAlreadyInOtherRoleException()).when(meetingService).update(any(),any(),any(),any());
        editMeetingBean.update();

        doThrow(new ParticipantAlreadyInMeetingException()).when(meetingService).update(any(),any(),any(),any());
        editMeetingBean.update();

        doThrow(new UserAlreadyInMeetingException()).when(meetingService).update(any(),any(),any(),any());
        editMeetingBean.update();


        verify(meetingService, times(7)).update(any(),any(),any(),any());
        verify(facesContext, times(7)).addMessage(isNull(), notNull());
        verifyNoMoreInteractions(facesContext);

    }

    @Test
    void addCEO()
    {
        when(allUsersNotInMeeting.add(ceoToAdd)).thenReturn(true);
        when( allUsersNotInMeeting.remove(any())).thenReturn(true);

        editMeetingBean.addCEO(any());
        verify(allUsersNotInMeeting, times(1)).add(ceoToAdd);
        verify(allUsersNotInMeeting, times(1)).remove(any());
    }

    @Test
    void deleteCEO()
    {
        when(allUsersNotInMeeting.add(ceoToAdd)).thenReturn(true);
        editMeetingBean.deleteCEO();
        verify(allUsersNotInMeeting, times(1)).add(ceoToAdd);
    }

    @Test
    void addParticipant()
    {
        when(participantsToAdd.add(any())).thenReturn(true);
        when( participantsNotInMeeting.remove(any())).thenReturn(true);
        when( filteredParticipantsNotInMeeting.remove(any())).thenReturn(true);
        when(filteredParticipantsToAdd.add(any())).thenReturn(true);

        editMeetingBean.addParticipant(any());

        verify(participantsToAdd, times(1)).add(any());
        verify(participantsNotInMeeting, times(1)).remove(any());
        verify(filteredParticipantsNotInMeeting, times(1)).remove(any());
        verify(filteredParticipantsToAdd, times(1)).add(any());
    }

    @Test
    void addUser()
    {
        when(usersToAdd.add(any())).thenReturn(true);
        when( lecturersNotInMeeting.remove(any())).thenReturn(true);
        when(filteredUsersToAdd.add(any())).thenReturn(true);
        when( filteredLecturersNotInMeeting.remove(any())).thenReturn(true);

        editMeetingBean.addUser(any());

        verify(usersToAdd, times(1)).add(any());
        verify(lecturersNotInMeeting, times(1)).remove(any());

        verify(filteredUsersToAdd, times(1)).add(any());
        verify(filteredLecturersNotInMeeting, times(1)).remove(any());
    }

    @Test
    void deleteUser()
    {
        when(usersToAdd.remove(any())).thenReturn(true);
        when( lecturersNotInMeeting.add(any())).thenReturn(true);
        when(filteredUsersToAdd.remove(any())).thenReturn(true);
        when( filteredLecturersNotInMeeting.add(any())).thenReturn(true);

        editMeetingBean.deleteUser(any());

        verify(usersToAdd, times(1)).remove(any());
        verify(lecturersNotInMeeting, times(1)).add(any());

        verify(filteredUsersToAdd, times(1)).remove(any());
        verify(filteredLecturersNotInMeeting, times(1)).add(any());
    }
}