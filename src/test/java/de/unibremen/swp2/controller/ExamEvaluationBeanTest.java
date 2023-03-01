package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.Exceptions.*;
import de.unibremen.swp2.service.*;
import lombok.Getter;
import lombok.Setter;
import net.vidageek.mirror.dsl.Mirror;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.primefaces.event.RowEditEvent;


import javax.faces.annotation.RequestParameterMap;
import javax.faces.component.UIComponent;import javax.faces.component.behavior.Behavior;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
class ExamEvaluationBeanTest {



    @Mock
    private Participant participant;

    @Mock
    private UIComponent growl;

    @Mock
    private Meeting meeting;

    @Mock
    private Evaluation copiedEvaluation;
    @Mock
    private User copiedUser;

    @Mock
    private List<EvaluationToExam> evaluation;

    @Mock
    private Principal principal;

    @Getter
    private Role role;

    @Mock
    private List<Participant> participants;
    @Mock
    private ParticipantStatus participantStatus;
    @Mock
    private User user;

    @Mock
    private Exam exam;

    @Mock
    private UserService userService;

    @Mock
    private ExamService examService;

    @Mock
    private EvaluationService evaluationService;

    @Mock
    private ParticipantService participantService;

    @Mock
    private MeetingService meetingService;
    @Mock
    private ExternalContext externalContext;

    @Mock
    private FacesContext facesContext;

    @Mock
    @RequestParameterMap
    private Map<String, String> parameterMap;

    @InjectMocks
    ExamEvaluationBean examEvaluationBean;


    @Test
    void initTest1()
    {
        Exam exam1 = Mockito.mock(Exam.class);
        String  value= "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(examService.getById(any())).thenReturn(exam1);

        Meeting meeting1 = new Meeting();
        User user1 =new User();
        user1.setRole(Role.A);

//        User user2 =new User();
//        user2.setRole(Role.D);
//
        when(meetingService.getMeetingsByExam(any())).thenReturn(meeting1);
        when(userService.getUsersByEmail(principal.getName())).thenReturn(user1);
        new Mirror().on(examEvaluationBean).invoke().method("init").withoutArgs();

        verify(parameterMap,times(1)).get(anyString());
        verify(examService,times(1)).getById(any());
        verify(meetingService,times(1)).getMeetingsByExam(any());
        verify(userService,times(1)).getUsersByEmail(principal.getName());

    }

    @Test
    void initTest12()
    {
        Exam exam1 = Mockito.mock(Exam.class);
        String  value= "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(examService.getById(any())).thenReturn(exam1);

        Meeting meeting1 = Mockito.mock(Meeting.class);
        User user2 =new User();
        user2.setRole(Role.D);

        when(meetingService.getMeetingsByExam(any())).thenReturn(meeting1);
        when(userService.getUsersByEmail(principal.getName())).thenReturn(user2);

         UserMeetingRole userMeetingRole = new UserMeetingRole();
         userMeetingRole.setRole(Role.T);
        when(userService.getUserMeetingRoleByUserAndMeeting(any(), any())).thenReturn(userMeetingRole);

        new Mirror().on(examEvaluationBean).invoke().method("init").withoutArgs();
        verify(parameterMap,times(1)).get(anyString());
        verify(examService,times(1)).getById(any());
        verify(meetingService,times(1)).getMeetingsByExam(any());
        verify(userService,times(1)).getUsersByEmail(principal.getName());

    }


    @Test
    void onRowEdit() {
    }

    @Test
    void deleteGrade() {

    }

    @Test
    void deleteExam() throws IOException, EntityNotFoundException, HasEvaluationsException {

        doNothing().when(examService).delete(any());
        doNothing().when(externalContext).redirect(any());
        examEvaluationBean.deleteExam();
        verify(examService, times(1)).delete(any());
        verifyNoMoreInteractions(examService);
        verify(externalContext, times(1)).redirect("meeting.xhtml?meeting-Id=" + meeting.getId());
        verifyNoMoreInteractions(externalContext);
        verifyNoInteractions(facesContext);

    }

    @Test
    public void HasEvaluationsException() throws EntityNotFoundException, OutdatedException, UserNotPermittedException, DuplicateEmailException, IOException, HasEvaluationsException {

        doThrow(new HasEvaluationsException()).when(examService).delete(any());
        examEvaluationBean.deleteExam();
        verify(examService, times(1)).delete(any());
        verifyNoMoreInteractions(examService);
        verify(facesContext, times(1)).addMessage(anyObject(), notNull());
        verifyNoMoreInteractions(facesContext);


    }
    @Test
    public void EntityNotFoundException() throws EntityNotFoundException, OutdatedException, UserNotPermittedException, DuplicateEmailException, IOException, HasEvaluationsException {

        doThrow(new EntityNotFoundException()).when(examService).delete(any());
        examEvaluationBean.deleteExam();
        verify(examService, times(1)).delete(any());
        verifyNoMoreInteractions(examService);
        verify(facesContext, times(1)).addMessage(anyObject(), notNull());
        verifyNoMoreInteractions(facesContext);


    }
}