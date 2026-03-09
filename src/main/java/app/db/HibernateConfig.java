package app.db;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import jakarta.persistence.EntityManagerFactory;


import java.util.Properties;

class HibernateConfig
{
        private HibernateConfig() {
        }

        private static void registerEntities(Configuration configuration) {
            configuration.addAnnotatedClass(Quiz.class);
            configuration.addAnnotatedClass(Question.class);
            configuration.addAnnotatedClass(Answer.class);
            configuration.addAnnotatedClass(Category.class);
            configuration.addAnnotatedClass(Tag.class);
            // TODO: Add more entities here...
        }

        static EntityManagerFactory createEntityManagerFactory(Properties props)
        {
            EntityManagerFactory emf;
            try {
                Configuration configuration = new Configuration();
                configuration.setProperties(props);

                // Register entities to make Hibernate aware
                registerEntities(configuration);

                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties())
                        .build();

                // Make JPA compliant version of Hibernates sf
                SessionFactory sf = configuration.buildSessionFactory(serviceRegistry);
                emf = sf.unwrap(EntityManagerFactory.class);
            } catch (Throwable ex) {
                System.err.println("Initial SessionFactory creation failed: " + ex);
                throw new ExceptionInInitializerError(ex);
            }

            return emf;
        }

    }
