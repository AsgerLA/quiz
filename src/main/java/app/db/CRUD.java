package app.db;

import jakarta.persistence.EntityManager;

class CRUD
{
    static void create(DBContext db, Object o)
            throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(o);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    static <T> T read(DBContext db, Class<T> clazz, Integer id)
            throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            return em.find(clazz, id);
        } catch (Exception e) {
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    static void update(DBContext db, Object o)
            throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(o);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }

    static void delete(DBContext db, Object o)
            throws DBException
    {
        EntityManager em = db.emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.remove(o);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw new DBException(e.getMessage());
        } finally {
            em.close();
        }
    }
}
