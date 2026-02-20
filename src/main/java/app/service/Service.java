package app.service;

import app.persistence.HibernateConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManagerFactory;

public final class Service
    extends ServiceQuiz
    implements IService
{

    private Service(){
    }

    private static final Service instance = new Service();
    public static Service getService()
    {
        return instance;
    }

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
