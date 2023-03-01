package de.unibremen.swp2.controller;
import static org.mockito.Mockito.*;

import de.unibremen.swp2.model.Role;
import de.unibremen.swp2.model.User;
import de.unibremen.swp2.persistence.Exceptions.*;
import de.unibremen.swp2.service.UserService;
import lombok.Getter;
import lombok.Setter;
import net.vidageek.mirror.dsl.Mirror;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import java.io.IOException;
import java.security.Principal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EditPersonalBeanTest
{

    @Mock
     User user;
    @Mock
    Principal principal;
    @Mock
     FacesContext facesContext;

    @Mock
     UserService userService;

    @Mock
     ExternalContext externalContext;



    @InjectMocks
    EditPersonalBean editPersonalBean;

    @Test
    void initTest(){
        User u = new User();
        u.setRole(Role.A);
        lenient().when(userService.getUsersByEmail(principal.getName())).thenReturn(u);
        new Mirror().on(editPersonalBean).invoke().method("init").withoutArgs();
//        Assertions.assertNotNull(editPersonalBean.getUser());


    }
    @Test
    void update() throws IOException, UserNotFoundException, OutdatedException, UserNotPermittedException, DuplicateEmailException
    {
        doNothing().when(userService).update(any());
        editPersonalBean.update();
        verify(userService, times(1)).update(any());
        verifyNoMoreInteractions(userService);
        verify(facesContext, times(1)).addMessage(anyObject(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    public void UserNotFoundExceptionTest() throws UserNotFoundException, OutdatedException, UserNotPermittedException, DuplicateEmailException, IOException
    {
        doThrow(new UserNotFoundException()).when(userService).update(any());
        editPersonalBean.update();
        verify(userService, times(1)).update(any());
        verifyNoMoreInteractions(userService);
        verify(facesContext, times(1)).addMessage(anyObject(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    public void OutdatedExceptionTest() throws UserNotFoundException, OutdatedException, UserNotPermittedException, DuplicateEmailException, IOException {

        doThrow(new OutdatedException()).when(userService).update(any());
        editPersonalBean.update();
        verify(userService, times(1)).update(any());
        verifyNoMoreInteractions(userService);
        verify(facesContext, times(1)).addMessage(anyObject(), notNull());
        verifyNoMoreInteractions(facesContext);


    }
    @Test
    public void DuplicateEmailExceptionTest() throws UserNotFoundException, OutdatedException, UserNotPermittedException, DuplicateEmailException, IOException {

        doThrow(new DuplicateEmailException()).when(userService).update(any());
        editPersonalBean.update();
        verify(userService, times(1)).update(any());
        verifyNoMoreInteractions(userService);



    }
    @Test
    public void UserNotPermittedExceptionTest () throws UserNotFoundException, OutdatedException, UserNotPermittedException, DuplicateEmailException, IOException {

        doThrow(new UserNotPermittedException()).when(userService).update(any());
        editPersonalBean.update();
        verify(userService, times(1)).update(any());
        verifyNoMoreInteractions(userService);

    }
    @Test
    void resetPassword() throws UserNotFoundException, OutdatedException, DuplicateEmailException, IOException
    {
        User u = new User();
        doNothing().when(userService).resetPw(any());
        editPersonalBean.resetPassword(any());
        verify(externalContext, times(1)).redirect("login.xhtml");
        verifyNoMoreInteractions(externalContext);

        doThrow(new UserNotFoundException()).when(userService).resetPw(any());
        editPersonalBean.resetPassword(any());

        doThrow(new OutdatedException()).when(userService).resetPw(any());
        editPersonalBean.resetPassword(any());

        doThrow(new DuplicateEmailException()).when(userService).resetPw(any());
        editPersonalBean.resetPassword(any());

    }

    @Test
    void restPw() throws IOException
    {
        User u = new User();
        u.setRole(Role.A);
        lenient().when(userService.getUsersByEmail(principal.getName())).thenReturn(u);
        editPersonalBean.restPw();
    }

    @Test
    void setNewPassword() throws UserNotFoundException, OutdatedException, TwoPaswordsArentIdentical, DoesntMatchOldPsWrdException, DuplicateEmailException, IOException
    {
        doNothing().when(userService).changePassWord(any(), any(), any(), any());
        editPersonalBean.setNewPassword();
        verify(externalContext, times(1)).redirect("login.xhtml");
        verifyNoMoreInteractions(externalContext);

        doThrow(new DoesntMatchOldPsWrdException()).when(userService).changePassWord(any(), any(), any(), any());
        editPersonalBean.setNewPassword();
        verify(facesContext, times(1)).addMessage(anyObject(), notNull());
        verifyNoMoreInteractions(facesContext);

        doThrow(new TwoPaswordsArentIdentical()).when(userService).changePassWord(any(), any(), any(), any());
        editPersonalBean.setNewPassword();


        doThrow(new UserNotFoundException()).when(userService).changePassWord(any(), any(), any(), any());
        editPersonalBean.setNewPassword();

        doThrow(new DuplicateEmailException()).when(userService).changePassWord(any(), any(), any(), any());
        editPersonalBean.setNewPassword();

        doThrow(new OutdatedException()).when(userService).changePassWord(any(), any(), any(), any());
        editPersonalBean.setNewPassword();

    }
}