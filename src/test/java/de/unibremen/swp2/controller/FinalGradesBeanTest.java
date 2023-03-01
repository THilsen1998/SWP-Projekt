package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.service.*;
import lombok.Getter;
import net.vidageek.mirror.dsl.Mirror;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.faces.annotation.RequestParameterMap;
import javax.inject.Inject;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class FinalGradesBeanTest
{

    private Role role;

    @Mock
    private Meeting meeting;
    @Mock
    private Exam exam;

    @Mock
    private List<Boolean> boolList;

    @Mock
    @RequestParameterMap
    private Map<String, String> parameterMap;

    @Mock
    private List<Participant> participants;

    @Mock
    private Principal principal;

    @Mock
    private MeetingService meetingService;

    @Mock
    private UserService userService;

    @Mock
    private ParticipantService participantService;

    @Mock
    private FinalGradeService finalGradeService;

    @Mock
    private EvaluationService evaluationService;

    @Mock
    private ExamService examService;

    @InjectMocks
    FinalGradesBean finalGradesBean;

    @Test
    void initTestWithParamerterMap () {

        String  value= "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);

        Meeting meeting2 = Mockito.mock(Meeting.class);
        when(meetingService.getById(any())).thenReturn(meeting2);

        Exam exam2 = Mockito.mock(Exam.class);
        when(examService.getExamByMeeting(meeting2)).thenReturn(exam2);

        User user1 =new User();
        user1.setRole(Role.A);
        when(userService.getUsersByEmail(principal.getName())).thenReturn(user1);
        new Mirror().on(finalGradesBean).invoke().method("init").withoutArgs();

        User userD =new User();
        userD.setRole(Role.D);
        when(userService.getUsersByEmail(principal.getName())).thenReturn(userD);
        UserMeetingRole userMeetingRole = new UserMeetingRole();
        when(userService.getUserMeetingRoleByUserAndMeeting(userD, meeting2)).thenReturn(userMeetingRole);

        new Mirror().on(finalGradesBean).invoke().method("init").withoutArgs();

        Meeting meeting3 = Mockito.mock(Meeting.class);
        meeting3.setVisible(true);
        when(meetingService.getById(any())).thenReturn(meeting3);
        when(userService.getUserMeetingRoleByUserAndMeeting(userD, meeting3)).thenReturn(userMeetingRole);

        new Mirror().on(finalGradesBean).invoke().method("init").withoutArgs();
    }
}