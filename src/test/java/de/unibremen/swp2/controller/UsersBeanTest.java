package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.User;
import de.unibremen.swp2.service.UserService;
import lombok.Getter;
import net.vidageek.mirror.dsl.Mirror;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.inject.Inject;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UsersBeanTest
{

    @Mock
     UserService userService;
    @Mock
     List<User> users;

    @InjectMocks
    UsersBean userBean;

    @Test
    void initTest() {

        new Mirror().on(userBean).invoke().method("init").withoutArgs();
        assertNotNull(userBean.getUsers());

    }

}