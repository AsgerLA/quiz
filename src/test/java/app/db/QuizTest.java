package app.db;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DBTest.class)
class QuizTest
{

    @Test
    void load()
        throws DBException
    {
        Quiz.load(DBTest.db, 1);
    }

    @Test
    void updatePlayCount()
        throws DBException
    {
        Quiz quiz;
        Quiz.updatePlayCount(DBTest.db, 1);
        quiz = Quiz.load(DBTest.db, 1);
        assertEquals(1, quiz.playCount);
    }
}
