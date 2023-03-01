package de.unibremen.swp2.service;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.Exceptions.*;
import de.unibremen.swp2.persistence.MeetingDAO;
import de.unibremen.swp2.persistence.ParticipantDAO;
import de.unibremen.swp2.persistence.UserDAO;
import net.vidageek.mirror.dsl.Mirror;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.inject.Inject;
import javax.persistence.NoResultException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import  org.powermock.api.mockito.PowerMockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest
{

    @Mock
    private MeetingDAO meetingDAO;

    @Mock
    private UserDAO userDAO;

    @Mock
    private ParticipantDAO participantDAO;

    @InjectMocks
    MeetingService meetingService =PowerMockito.spy(new MeetingService());;

    @Test
    void create() throws MeetingNotFoundException, ParticipantAlreadyInMeetingException, UserAlreadyInOtherRoleException, UserNotFoundException, UserAlreadyInMeetingException, UserNotPermittedException, ParticipantNotFoundException
    {

        Meeting meeting = Mockito.mock(Meeting.class);

        //to mock the for loop    for (Participant p : participants)
        Iterator<Participant> iterator = mock(Iterator.class);
        List<Participant> participants = mock(List.class);
        Participant participant  = mock(Participant.class);
        when(participants.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(participant);

        //to mock the for loop   for (User u : lecturers)
        Iterator<User> iterator1 = mock(Iterator.class);
        List<User> lecturers = mock(List.class);
        User user  = new User();
        user.setRole(Role.A);
        when(lecturers.iterator()).thenReturn(iterator1);
        when(iterator1.hasNext()).thenReturn(true, false);
        when(iterator1.next()).thenReturn(user);

        User ceo = new User();
        ceo.setRole(Role.CEO);


        doNothing().when(meetingDAO).insert(any());
        doNothing().when(meetingDAO).flush();
        ParticipantStatus status= Mockito.mock(ParticipantStatus.class);
        lenient().doNothing().when(meetingDAO).addParticipantToMeeting(status);

        meetingService.create(meeting,participants,lecturers,ceo);

        verify(meetingDAO, times(1)).insert(any());
        verify(meetingDAO, times(1)).flush();

    }

//    @Test
//    void update() throws Exception
//    {
//
//        Meeting meeting = Mockito.mock(Meeting.class);
//
//        //to mock the for loop    for (Participant p : participants)
//        Iterator<Participant> iterator = mock(Iterator.class);
//        List<Participant> participants = mock(List.class);
//        Participant participant  = mock(Participant.class);
//        when(participants.iterator()).thenReturn(iterator);
//        when(iterator.hasNext()).thenReturn(true, false);
//        when(iterator.next()).thenReturn(participant);
//
//        //to mock the for loop   for (User u : lecturers)
//        Iterator<User> iterator1 = mock(Iterator.class);
//        List<User> lecturers = mock(List.class);
//        User user  = new User();
//        user.setRole(Role.A);
//        when(lecturers.iterator()).thenReturn(iterator1);
//        when(iterator1.hasNext()).thenReturn(true, false);
//        when(iterator1.next()).thenReturn(user);
//
//        User ceo = new User();
//        ceo.setRole(Role.CEO);
//
//        doNothing().when(meetingDAO).update(meeting);
//        List<User> currentLecturers = Mockito.mock(List.class);
//        when(userDAO.getLecturersByMeeting(meeting)).thenReturn(currentLecturers);
//        List<Participant> currentParticipants = Mockito.mock(List.class);
//        when(participantDAO.getAllParticipantsByMeeting(meeting)).thenReturn(currentParticipants);
//
//        ParticipantStatus status= Mockito.mock(ParticipantStatus.class);
//        lenient().doNothing().when(meetingDAO).addParticipantToMeeting(status);
//        User currentCEO =new User();
//        currentCEO.setRole(Role.CEO);
//        when(userDAO.getCeoByMeeting(meeting)).thenReturn(currentCEO) ;
//
//        doNothing().when(meetingDAO).deleteUserFromMeeting(any(), any());
//        doNothing().when(meetingDAO).deleteUserFromMeeting(any(), any());
//        MeetingService meetingService1 = PowerMockito.spy(new MeetingService());
//        Method privateMethod = MeetingService.class.
//                getDeclaredMethod("addCEOToMeeting", User.class, Meeting.class, List.class);
//        privateMethod.setAccessible(true);
//
//        PowerMockito.doNothing().when( meetingService, "addCEOToMeeting");
//        meetingService.update(meeting,lecturers,participants,ceo);
//
//
////        verify(meetingDAO, times(1)).insert(any());
////        verify(meetingDAO, times(1)).flush();
//    }

    @Test
    void addCEOToMeeting() throws EntityNotFoundException, UserNotInMeetingException, UserAlreadyInMeetingException, UserNotPermittedException, OutdatedException, UserAlreadyInOtherRoleException, ParticipantAlreadyInMeetingException
    {

        Meeting meeting = Mockito.mock(Meeting.class);
        List<User> lecturers = mock(List.class);
        User ceo = new User();
        ceo.setRole(Role.CEO);

        UserMeetingRole currentRole =Mockito.mock(UserMeetingRole.class);
        when(userDAO.getUserMeetingRoleByUserAndMeeting(ceo, meeting)).thenReturn(currentRole);
        doNothing().when(meetingDAO).updateUserMeetingRole(any());

        new Mirror().on(meetingService).invoke().method("addCEOToMeeting").withArgs(ceo,meeting,lecturers);

        //        verify(meetingDAO, times(1)).insert(any());
        //        verify(meetingDAO, times(1)).flush();
    }

    @Test
    void addCEOToMeetingException() throws EntityNotFoundException, UserNotInMeetingException,
            UserAlreadyInMeetingException, UserNotPermittedException, OutdatedException, UserAlreadyInOtherRoleException, ParticipantAlreadyInMeetingException
    {
        User ceo = new User();
        ceo.setRole(Role.CEO);
        Meeting meeting = Mockito.mock(Meeting.class);
        List<User> lecturers = mock(List.class);
        lecturers.add(ceo);

        UserMeetingRole currentRole =Mockito.mock(UserMeetingRole.class);
        doThrow(new NoResultException()).when(userDAO).getUserMeetingRoleByUserAndMeeting(ceo, meeting);

        new Mirror().on(meetingService).invoke().method("addCEOToMeeting").withArgs(ceo,meeting,lecturers);



        //        verify(meetingDAO, times(1)).insert(any());
        //        verify(meetingDAO, times(1)).flush();
    }

    @Test
    void updateMeetingOnlyTest() throws EntityNotFoundException, OutdatedException
    {
       doNothing().when(meetingDAO) .update(any());
        meetingService.updateMeetingOnly(any());
        verify(meetingDAO,times(1)).update(any());
    }

    @Test
    void getById()
    {
        String s = "";
        Meeting m = Mockito.mock( Meeting.class);
        when(meetingDAO.getById(s)).thenReturn(m);
        meetingService.getById(s);
        verify(meetingDAO,times(1)).getById(s);
    }
    @Test
    void getMeetingByTutorial()
    {
        when(meetingDAO.getMeetingByTutorial(any())).thenReturn(new Meeting());
        meetingService.getMeetingByTutorial(any());
        verify(meetingDAO,times(1)).getMeetingByTutorial(any());
    }

    @Test
    void getMeetingsByUser()
    {
        User u = new User();
        u.setRole(Role.A);
        when(meetingDAO.getVisibleMeetingsByUser(u)).thenReturn(new ArrayList<>());
        meetingService.getMeetingsByUser(u);
        verify(meetingDAO,times(1)).getVisibleMeetingsByUser(u);

        u.setRole(Role.D);
        when(meetingDAO.getMeetingsByUser(u)).thenReturn(new ArrayList<>());
         meetingService.getMeetingsByUser(u);
        verify(meetingDAO,times(1)).getMeetingsByUser(u);
    }

    @Test
    void getAllMeetings()
    {
        Meeting m = Mockito.mock( Meeting.class);
        when(meetingDAO.getAllMeetings()).thenReturn(new ArrayList<>());
        meetingService.getAllMeetings();
        verify(meetingDAO,times(1)).getAllMeetings();
    }



    @Test
    void delete()
    {
    }

    @Test
    void getMeetingBySubmission()
    {
        when(meetingDAO.getMeetingBySubmission(any())).thenReturn(new Meeting());
        meetingService.getMeetingBySubmission(any());
        verify(meetingDAO,times(1)).getMeetingBySubmission(any());
    }

    @Test
    void getMeetingsByExam()
    {
        when(meetingDAO.getMeetingsByExam(any())).thenReturn(new Meeting());
        meetingService.getMeetingsByExam(any());
        verify(meetingDAO,times(1)).getMeetingsByExam(any());
    }
}