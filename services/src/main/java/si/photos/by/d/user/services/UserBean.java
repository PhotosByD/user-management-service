package si.photos.by.d.user.services;

import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import si.photos.by.d.user.models.entities.User;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.logging.Logger;

@RequestScoped
public class UserBean {
    private Logger log = Logger.getLogger(UserBean.class.getName());

    @Inject
    private EntityManager em;

    @Inject
    private UserBean userBean;

    private Client httpClient;

    private String baseUrl;

    @PostConstruct
    private void init() {
        httpClient = ClientBuilder.newClient();
        baseUrl = "http://localhost:8081"; // only for demonstration
    }

    public List<User> getUsers() {
        TypedQuery<User> query = em.createNamedQuery("User.getAll", User.class);
        return query.getResultList();
    }

    public List<User> getUsersFilter(UriInfo uriInfo) {

        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery()).defaultOffset(0)
                .build();

        return JPAUtils.queryEntities(em, User.class, queryParameters);
    }

    public User getUser(Integer userId) {
        User user = em.find(User.class, userId);

        if(user == null) {
            throw new NotFoundException();
        }

        return user;
    }

    public User createUser(User user) {
        try{
            beginTx();
            em.persist(user);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return user;
    }

    public User updateUser(Integer userId, User user) {
        User u = em.find(User.class, userId);

        if(u == null) return null;

        try {
            beginTx();
            user.setId(userId);
            em.merge(user);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return user;
    }

    public boolean deleteUser(Integer userId) {
        User user = em.find(User.class, userId);

        if(user != null) {
            try {
                beginTx();
                em.remove(user);
                commitTx();
            } catch (Exception e) {
                rollbackTx();
            }
        } else {
            return false;
        }

        return true;
    }

    private void beginTx() {
        if (!em.getTransaction().isActive())
            em.getTransaction().begin();
    }

    private void commitTx() {
        if (em.getTransaction().isActive())
            em.getTransaction().commit();
    }

    private void rollbackTx() {
        if (em.getTransaction().isActive())
            em.getTransaction().rollback();
    }

}
