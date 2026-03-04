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
            // TODO: Add more entities here...
        }

        static EntityManagerFactory createEntityManagerFactory(String username, String password, String url)
        {
            EntityManagerFactory emf;
            try {
                Properties props = new Properties();
                props.put("hibernate.connection.driver_class", "org.postgresql.Driver");
                props.put("hibernate.current_session_context_class", "thread");
                props.put("hibernate.show_sql", "true");
                props.put("hibernate.format_sql", "false");
                props.put("hibernate.use_sql_comments", "false");
                props.put("hibernate.hikari.maximumPoolSize", "10");
                props.put("hibernate.hikari.minimumIdle", "2");
                props.put("hibernate.hikari.connectionTimeout", "20000");
                props.put("hibernate.generate_statistics", "true");
                props.put("hibernate.hbm2ddl.auto", "create");

                props.setProperty("hibernate.connection.username", username);
                props.setProperty("hibernate.connection.password", password);
                props.setProperty("hibernate.connection.url", url);

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
