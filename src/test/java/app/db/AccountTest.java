package app.db;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AccountTest
{
    @Test
    void verifyUsername()
    {
        assertTrue(Account.verifyUsername("Test_user2"));
        assertTrue(Account.verifyUsername("nameØ123"));
        assertFalse(Account.verifyUsername("123name"));
        assertFalse(Account.verifyUsername("123name"+(char)0xd7));
    }
}
