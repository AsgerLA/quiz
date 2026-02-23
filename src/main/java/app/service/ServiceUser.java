package app.service;

import app.persistence.DAO;
import app.persistence.User;

public class ServiceUser
        implements IService.User
{
    ServiceUser() {}
    public boolean signup(String username, String password)
    {
        return DAO.signup(Service.emf, username, password);
    }

    public UserDTO signin(String username, String password)
    {
        User user = DAO.signin(Service.emf, username, password);
        if (user == null)
            return null;
        return new UserDTO(user);
    }
}
