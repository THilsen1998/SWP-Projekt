package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.User;
import de.unibremen.swp2.persistence.Exceptions.DuplicateEmailException;
import de.unibremen.swp2.persistence.Exceptions.UserNotPermittedException;
import de.unibremen.swp2.service.UserService;
import net.vidageek.mirror.dsl.Mirror;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import java.io.IOException;

import static junit.framework.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AddUserBeanTest {

    @Mock
    ExternalContext externalContext;

    @Mock
    FacesContext facesContext;

    @InjectMocks
    AddUserBean addUserBean;

    @Mock
    UserService userService;

    @Mock
    User user;


    @Test
    void initTest(){
        Assertions.assertNotNull(addUserBean.getUser());
        new Mirror().on(addUserBean).invoke().method("init").withoutArgs();
        Assertions.assertNotNull(addUserBean.getUser());


    }


    @Test
    void createUserTest() throws IOException, UserNotPermittedException, DuplicateEmailException {

        doNothing().when(userService).create(any());
        doNothing().when(externalContext).redirect(any());

        addUserBean.createUser();

        verify(userService, times(1)).create(any());
        verifyNoMoreInteractions(userService);
        verify(externalContext, times(1)).redirect("users.xhtml");
        verifyNoMoreInteractions(externalContext);
        verifyNoInteractions(facesContext);

    }




    @Test
    public void userNotPermittedExceptionTest() throws UserNotPermittedException, DuplicateEmailException, IOException {


        doThrow(new UserNotPermittedException()).when(userService).create(any());

        addUserBean.createUser();

        verify(userService, times(1)).create(any());
        verifyNoMoreInteractions(userService);
        verify(facesContext, times(1)).addMessage(anyObject(), notNull());
        verifyNoMoreInteractions(facesContext);
    }


    @Test
    public void DuplicateEmailExceptionTest() throws UserNotPermittedException, DuplicateEmailException, IOException {


        doThrow(new DuplicateEmailException()).when(userService).create(any());

        addUserBean.createUser();

        verify(userService, times(1)).create(any());
        verifyNoMoreInteractions(userService);
        verify(facesContext, times(1)).addMessage(anyObject(), notNull());
        verifyNoMoreInteractions(facesContext);
    }
}