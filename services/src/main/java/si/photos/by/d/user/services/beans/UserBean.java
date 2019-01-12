package si.photos.by.d.user.services.beans;

import com.kumuluz.ee.discovery.annotations.DiscoverService;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.metrics.annotation.Timed;
import si.photos.by.d.user.models.dtos.Album;
import si.photos.by.d.user.models.dtos.Photo;
import si.photos.by.d.user.models.entities.User;
import si.photos.by.d.user.services.configuration.AppProperties;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Array;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@RequestScoped
public class UserBean {
    private Logger log = Logger.getLogger(UserBean.class.getName());

    @Inject
    private EntityManager em;

    @Inject
    private AppProperties appProperties;

    @Inject
    private UserBean userBean;

    private Client httpClient;

    @Inject
    @DiscoverService("photo-management-service")
    private Optional<String> photoUrl;

    @Inject
    @DiscoverService("album-management-service")
    private Optional<String> albumUrl;

    @PostConstruct
    private void init() {
        httpClient = ClientBuilder.newClient();
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

        if (user == null) {
            throw new NotFoundException();
        }
        List<Photo> photos = userBean.getPhotosForUser(userId);
        if(photos != null) {
            user.setPhotos(photos);
        }

        List<Album> albums = userBean.getAlbumsForUser(userId);
        user.setAlbums(albums);

        return user;
    }

    public User createUser(User user) {
        try {
            beginTx();
            em.persist(user);
            commitTx();
        } catch (Exception e) {
            log.warning("There was a problem with saving new user with email " + user.getEmail());
            rollbackTx();
        }
        log.info("Successfully saved new user with email " + user.getEmail());
        return user;
    }

    public User updateUser(Integer userId, User user) {
        User u = em.find(User.class, userId);

        if (u == null) return null;

        try {
            beginTx();
            user.setId(userId);
            em.merge(user);
            commitTx();
        } catch (Exception e) {
            log.warning("There was a problem with updating user with email " + user.getEmail());
            rollbackTx();
        }
        log.info("Successfully updated user with email " + user.getEmail());
        return user;
    }

    public boolean deleteUser(Integer userId) {
        User user = em.find(User.class, userId);

        if (user != null) {
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

    @Timed
    @CircuitBreaker(requestVolumeThreshold = 1)
    @Timeout(value = 2, unit = ChronoUnit.SECONDS)
    @Fallback(fallbackMethod = "getPhotosFallback")
    public List<Photo> getPhotosForUser(Integer userId) {
        if (appProperties.isExternalServicesEnabled() && photoUrl.isPresent()) {
            try {
                return httpClient
                        .target(photoUrl.get() + "/v1/photos?where=userId:EQ:" + userId)
                        .request().get(new GenericType<List<Photo>>() {
                        });
            } catch (WebApplicationException | ProcessingException e) {
                log.severe(e.getMessage());
                throw new InternalServerErrorException(e);
            }
        }
        return null;
    }

    public List<Photo> getPhotosFallback(Integer userId) {
        Photo photo = new Photo();
        photo.setTitle("This is default photo");
        photo.setPhotoURL("Test");
        return Collections.singletonList(photo);
    }
    public List<Album> getAlbumsFallback(Integer userId) {
        return Collections.emptyList();
    }

    @Timed
    @CircuitBreaker(requestVolumeThreshold = 1)
    @Timeout(value = 2, unit = ChronoUnit.SECONDS)
    @Fallback(fallbackMethod = "getAlbumsFallback")
    public List<Album> getAlbumsForUser(Integer userId) {
        if (albumUrl.isPresent()) {
            try {
                return httpClient
                        .target(albumUrl.get() + "/v1/albums/user/" + userId)
                        .request().get(new GenericType<List<Album>>() {
                        });
            } catch (WebApplicationException | ProcessingException e) {
                log.severe(e.getMessage());
                throw new InternalServerErrorException(e);
            }
        }
        return null;
    }

}
