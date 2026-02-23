package app;

import app.service.ServiceAPI;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(ServiceTest.class)
class ServiceAPITest
{
    @Test
    @Disabled
    void randomQuestion()
    {
        assertNotNull(ServiceAPI.randomQuestions(1));
    }
}
