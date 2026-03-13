package app.db;

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
}
