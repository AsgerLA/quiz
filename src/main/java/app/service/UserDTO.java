package app.service;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.persistence.User;

public class UserDTO
{
    @JsonProperty
    public Long id;
    @JsonProperty
    public String name;

    public UserDTO() {}
    public UserDTO(User user)
    {
        this.id = user.id;
        this.name = user.name;
    }
}
