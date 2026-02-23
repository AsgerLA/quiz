package app;

import app.service.Service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ServiceTest.class)
class ServiceUserTest
{

    @Test
    void signup_signin()
    {
        String username = "testUser";
        String password = "password1234";

        assertTrue(Service.user.signup(username, password));
        assertNotNull(Service.user.signin(username, password));
    }
}
