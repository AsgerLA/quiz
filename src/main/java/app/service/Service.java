package app.service;

import app.persistence.HibernateConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManagerFactory;

public final class Service
{

    private Service(){
    }

    public static IService.Quiz quiz = new ServiceQuiz();
    public static IService.User user = new ServiceUser();

    // TODO: persistence interface
    static final EntityManagerFactory emf;
    public static EntityManagerFactory getEntityManagerFactory()
    {
        return emf;
    }

    static final ObjectMapper jsonMapper;
    public static <T> T jsonToObject(String json, Class<T> clazz)
            throws JsonProcessingException
    {
        return jsonMapper.readValue(json, clazz);
    }

    static {
        emf = HibernateConfig.createEntityManagerFactory();
        jsonMapper = new ObjectMapper();
    }
}
