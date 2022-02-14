package model;

import javax.persistence.*;

@Entity
@Table(name = "media_link")
public class MediaLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long mediaId;

    @Column(name = "source_path")
    private String sourcePath;

    @Column(name = "destination_path")
    private String destPath;

    private String theMovieDbId;

    private String sourceParentPath;

    public long getMediaId() {
        return mediaId;
    }

    public void setMediaId(long mediaId) {
        this.mediaId = mediaId;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getDestPath() {
        return destPath;
    }

    public void setDestPath(String destPath) {
        this.destPath = destPath;
    }

    public String getTheMovieDbId() {
        return theMovieDbId;
    }

    public void setTheMovieDbId(String theMovieDbId) {
        this.theMovieDbId = theMovieDbId;
    }

    public String getSourceParentPath() {
        return sourceParentPath;
    }

    public void setSourceParentPath(String sourceParentPath) {
        this.sourceParentPath = sourceParentPath;
    }
}
