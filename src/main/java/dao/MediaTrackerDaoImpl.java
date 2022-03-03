package dao;

import model.MediaLink;
import model.MediaQuery;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.nio.file.Path;
import java.util.List;

public class MediaTrackerDaoImpl implements MediaTrackerDao {

//    private static EntityManager entityManager;
//
//    public MediaTrackerDaoImpl() {
//        entityManager = MediaEntityManager.getEntityManagerFactory().createEntityManager();
//    }

    @Override
    public void addQueryToQueue(MediaQuery query) {
        EntityManager entityManager = MediaEntityManager.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.persist(query);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void removeQueryFromQueue(MediaQuery mediaQuery) {
        EntityManager entityManager = MediaEntityManager.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = entityManager.getTransaction();
            transaction.begin();
            MediaQuery find = entityManager.find(MediaQuery.class, mediaQuery.getQueryId());
            entityManager.remove(find);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            entityManager.close();
        }

    }

    @Override
    public MediaQuery findQueryByFilePath(String filePath) {
        // typed query throws exception if nothing is found
        EntityManager entityManager = MediaEntityManager.getEntityManagerFactory().createEntityManager();
        MediaQuery singleResult = null;
        try {
            TypedQuery<MediaQuery> typedQuery = entityManager.createQuery("SELECT q FROM MediaQuery q WHERE q.filePath=:filepath", MediaQuery.class);
            typedQuery.setParameter("filepath", filePath);
            singleResult = typedQuery.getSingleResult();
        } catch (NoResultException e) {
            System.out.println(e.getMessage());
        } finally {
            entityManager.close();
        }

       return singleResult;
    }


    @Override
    public List<MediaQuery> getAllMediaQueries() {
        EntityManager entityManager = MediaEntityManager.getEntityManagerFactory().createEntityManager();
//        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//        CriteriaQuery<MediaQuery> cq = cb.createQuery(MediaQuery.class);
//        Root<MediaQuery> rootEntry = cq.from(MediaQuery.class);
//        CriteriaQuery<MediaQuery> all = cq.select(rootEntry);
        List<MediaQuery> resultList = List.of();
        try {
            String all = "SELECT q FROM MediaQuery q";
            TypedQuery<MediaQuery> allQuery = entityManager.createQuery(all, MediaQuery.class);
            resultList = allQuery.getResultList();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            entityManager.close();
        }
        return resultList;
    }

    @Override
    public List<MediaQuery> findQueryByParentPath(String filePath) {
        // typed query throws exception if nothing is found
        EntityManager entityManager = MediaEntityManager.getEntityManagerFactory().createEntityManager();
        List<MediaQuery> listResult = null;
        try {

            TypedQuery<MediaQuery> typedQuery = entityManager.createQuery("SELECT q FROM MediaQuery q WHERE q.parentPath=:filepath" , MediaQuery.class);
            typedQuery.setParameter("filepath", filePath);
            listResult = typedQuery.getResultList();
        } catch (NoResultException e) {
            System.out.println(e.getMessage());
        } finally {
            entityManager.close();
        }
        return listResult;
    }

    @Override
    public void addNewLink(MediaLink mediaLInk) {
        EntityManager entityManager = MediaEntityManager.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.persist(mediaLInk);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void removeLink(MediaLink mediaLInk) {
        EntityManager entityManager = MediaEntityManager.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = entityManager.getTransaction();
            transaction.begin();
            MediaLink find = entityManager.find(MediaLink.class, mediaLInk.getMediaId());
            entityManager.remove(find);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public List<MediaLink> getAllMediaLinks() {
        EntityManager entityManager = MediaEntityManager.getEntityManagerFactory().createEntityManager();
        List<MediaLink> resultList = List.of();
        try {
            String all = "SELECT q FROM MediaLink q";
            TypedQuery<MediaLink> allQuery = entityManager.createQuery(all, MediaLink.class);
            resultList = allQuery.getResultList();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            entityManager.close();
        }
        return resultList;
    }

    @Override
    public List<MediaLink> findMediaLinkByParentPath(String filePath) {
        EntityManager entityManager = MediaEntityManager.getEntityManagerFactory().createEntityManager();
        List<MediaLink> listResult = null;
        try {
            TypedQuery<MediaLink> typedQuery = entityManager.createQuery("SELECT q FROM MediaLink q WHERE q.parentPath=:filepath" , MediaLink.class);
            typedQuery.setParameter("filepath", filePath);
            listResult = typedQuery.getResultList();
        } catch (NoResultException e) {
            System.out.println(e.getMessage());
        } finally {
            entityManager.close();
        }
        return listResult;
    }

    @Override
    public MediaLink findMediaLinkByFilePath(String filePath) {
        EntityManager entityManager = MediaEntityManager.getEntityManagerFactory().createEntityManager();
        MediaLink singleResult = null;
        try {
            TypedQuery<MediaLink> typedQuery = entityManager.createQuery("SELECT q FROM MediaLink q WHERE q.targetPath=:filepath", MediaLink.class);
            typedQuery.setParameter("filepath", filePath);
            singleResult = typedQuery.getSingleResult();
        } catch (NoResultException e) {
            System.out.println(e.getMessage());
        } finally {
            entityManager.close();
        }
        return singleResult;
    }

    public static void main(String[] args) {
        String file = ".\\test-folder\\movies-target\\Arang\\Arang_korean_hun.avi";
        Path testPath = Path.of(file).getParent();

        System.out.println("parent " + testPath + " of " + file);

        MediaTrackerDao dao = new MediaTrackerDaoImpl();

        MediaQuery mq = new MediaQuery();
        mq.setFilePath(file);
        mq.setParentPath(testPath.toString());
        dao.addQueryToQueue(mq);

        List<MediaQuery> allMediaQueries = dao.getAllMediaQueries();
        for (MediaQuery mq1 : allMediaQueries) {
            System.out.println(mq1);
        }


        List<MediaQuery> queryByFilePath = dao.findQueryByParentPath(".\\test-folder\\movies-target\\Arang");

        System.out.println("found " + queryByFilePath);

//        MediaQuery findQuery = dao.findQueryByFilePath(file);
//        System.out.println(findQuery);
//
//        dao.removeQueryFromQueue(findQuery);
//        allMediaQueries = dao.getAllMediaQueries();
//        for (MediaQuery mq : allMediaQueries) {
//            System.out.println(mq);
//        }

//        List<MediaLink> allMediaLinks = dao.getAllMediaLinks();
//        for (MediaLink ml : allMediaLinks) {
//            System.out.println(ml);
//        }

    }

}
