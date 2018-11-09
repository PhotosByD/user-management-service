package si.photos.by.d.user.models.dtos;

import javax.persistence.Column;
import javax.persistence.Transient;
import java.util.List;

public class Album {
    private Integer id;
    private String name;

    @Transient
    private List<Photo> photos;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }
}
