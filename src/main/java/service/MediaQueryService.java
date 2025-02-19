package service;

import dao.MediaTrackerDao;
import model.MediaQuery;
import model.multipart.MultiPartElement;
import model.path.FilePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import scanner.MediaFilesScanner;
import util.MediaType;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class MediaQueryService extends PaginationImpl{

    private static final Logger LOG = LoggerFactory.getLogger(MediaQueryService.class);
    private List<MediaQuery> mediaQueriesList = new LinkedList<>();
    private List<MediaQuery> groupedQueriesToProcess;
    private MediaQuery referenceQuery;
    private Map<Path, List<UUID>> mediaQueriesByRootMap = new HashMap<>();
    private final MediaTrackerDao mediaTrackerDao;
    private final MediaFilesScanner moviesFileScanner;
    private final PropertiesService propertiesService;

    public MediaQueryService(@Qualifier("spring") MediaTrackerDao mediaTrackerDao, MediaFilesScanner moviesFileScanner, PropertiesService propertiesService) {
        this.mediaTrackerDao = mediaTrackerDao;
        this.moviesFileScanner = moviesFileScanner;
        this.propertiesService = propertiesService;
    }

    public MediaQuery getReferenceQuery() {
        return referenceQuery;
    }

    public void setReferenceQuery(UUID mediaQueryUuid) {
        referenceQuery = getQueryByUuid(mediaQueryUuid);
    }

    // scan given paths and gather all files matching criteria
    // except ones that are already ignored or already has links
    public void scanForNewMediaQueries() {
//        List<Path> candidates = moviesFileScanner.scanMediaFolders(paths, mediaTrackerDao.getAllMediaLinks());
//        mediaQueriesList = new LinkedList<>();
//        candidates.forEach(c -> addQueryToQueue(c.toString()));
//        candidates = null;
        scanForNewMovies();
        groupByParentPathBatch(mediaQueriesList);
    }

    /*
     * Scan for new movie files and add them to the queue
     * */
    void scanForNewMovies() {
        mediaQueriesList = moviesFileScanner.scanMediaFolders(
                        propertiesService.getTargetFolderListMovie(),
                        mediaTrackerDao.getAllMediaLinks()
                )
                .stream()
                .map(this::createMovieQuery)
                .collect(Collectors.toList());
//                    .forEach(c -> addQueryToQueue(c.toString(), MediaType.MOVIE));
    }

    /*
     * Scan for tv episodes and add new files to the queue
     * */
    void scanForNewSeries() {
        mediaQueriesList = moviesFileScanner.scanMediaFolders(
                        propertiesService.getTargetFolderListTv(),
                        mediaTrackerDao.getAllMediaLinks()
                )
                .stream()
                .map(this::createTvQuery)
                .collect(Collectors.toList());
//                    .forEach(c -> addQueryToQueue(c.toString(), MediaType.TV));
        // TODO map addQuery function
    }

    MediaQuery createTvQuery(Path filePath) {
        return createQuery(filePath.toString(), MediaType.TV);
    }

    MediaQuery createMovieQuery(Path filePath) {
        return createQuery(filePath.toString(), MediaType.MOVIE);
    }

    public MediaQuery createQuery(String filepath, MediaType mediaType) {
        MediaQuery mq = new MediaQuery(filepath, mediaType);
        mq.setMediaType(mediaType);
        return mq;
    }

    /*
     * Removes given element from the query list
     * */
    public void removeQueryFromQueue(MediaQuery mediaQuery) {
        mediaQueriesList = getCurrentMediaQueries()
                .stream()
                .filter(mq -> !mq.getQueryUuid().equals(mediaQuery.getQueryUuid()))
                .collect(Collectors.toList());
        groupByParentPathBatch(mediaQueriesList);
    }

    /*
     * Removes list element with given path
     * */
    public void removeQueryByFilePath(String path) {
        mediaQueriesList = getCurrentMediaQueries()
                .stream()
                .filter(mq -> !mq.getFilePath().equals(path))
                .collect(Collectors.toList());
        groupByParentPathBatch(mediaQueriesList);
    }

    public MediaQuery getQueryByUuid(UUID uuid) {
        Optional<MediaQuery> first = mediaQueriesList
                .stream()
                .filter(x -> x.getQueryUuid().equals(uuid))
                .findFirst();
        return first.orElse(null);
    }

    void groupByParentPathBatch(List<MediaQuery> mediaQueryList) {
        mediaQueriesByRootMap = new HashMap<>();
        mediaQueryList.forEach(mq -> groupByParentPath(mq, propertiesService.getTargetFolderListMovie()));
    }

    /*
     * Group media query element ids by parent folder
     * */
    void groupByParentPath(MediaQuery mediaQuery, List<FilePath> targetFolderList) {
        Path parent = Path.of(mediaQuery.getFilePath()).getParent();
        if (targetFolderList.stream().noneMatch(target -> target.getPath().equals(parent))) {
            List<UUID> uuids = (mediaQueriesByRootMap.get(parent) == null)
                    ? new LinkedList<>()
                    : mediaQueriesByRootMap.get(parent);
            uuids.add(mediaQuery.getQueryUuid());
            mediaQueriesByRootMap.put(parent, uuids);
        }
    }

    /*
     * Returns list of media queries of elements sharing the same folder at the same file tree level.
     * */
    public List<MediaQuery> getGroupedQueries(UUID mediaQueryUuid) {
        Path parent = Path.of(getQueryByUuid(mediaQueryUuid).getFilePath()).getParent();
        List<UUID> uuids = mediaQueriesByRootMap.get(parent);
        if (mediaQueriesByRootMap.isEmpty() || uuids == null) return List.of();
        return uuids.stream()
                .map(this::getQueryByUuid)
                // after creating link other files within the same folder are ignored, so they won't appear here
                .filter(query -> query.getMultipart() == -1)
                .collect(Collectors.toList());
    }

    public List<MediaQuery> searchQuery(String search) {
        String[] words = search.toLowerCase().split(" ");
        return mediaQueriesList.stream()
                .filter(mq -> containsAllWords(words, mq.getFilePath()))
//                .filter(mq -> mq.getFilePath().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());
    }

    public boolean containsAllWords(String[] words, String filePath) {
        return Arrays.stream(words).allMatch(filePath.toLowerCase()::contains);
    }

    /*
     * Returns current media query list
     * */
    public List<MediaQuery> getCurrentMediaQueries() {
        return mediaQueriesList;
    }

    public List<MediaQuery> getProcessList() {
        return List.copyOf(groupedQueriesToProcess);
    }

    /*
     * Adds single query to process list, it's called when new link is created
     * */
    public List<MediaQuery> addQueryToProcess(MediaQuery mediaQuery) {
        // MediaType is never null
//        if (mediaQuery.getMediaType() == null) mediaQuery.setMediaType(MediaType.MOVIE);
        groupedQueriesToProcess = List.of(mediaQuery);
        return List.copyOf(groupedQueriesToProcess);
    }

    public List<MediaQuery> addQueriesToProcess(List<MultiPartElement> multiPartElementsList) {
        groupedQueriesToProcess = new LinkedList<>();
        for (MediaQuery mq : mediaQueriesList) {
            for (MultiPartElement mpe : multiPartElementsList) {
                if (mpe.getFilePath().equals(mq.getFilePath()) && mpe.getMultipartSwitch()) {
                    mq.setMultipart(mpe.getPartNumber());
                    mq.setMediaType(mpe.getMediaType());
                    groupedQueriesToProcess.add(mq);
                }
            }
        }
        LOG.info("[ query_service ] Grouped {} elements", groupedQueriesToProcess.size());
        if (groupedQueriesToProcess.size() == 0) {
            LOG.info("[ query_service ] No queries marked, adding reference to the queue");
            addQueryToProcess(referenceQuery);
        }
        return List.copyOf(groupedQueriesToProcess);
    }

    /*
     * Returns query with given id or null if no such query is found
     * */
    public MediaQuery getQueryById(Long id) {
        return mediaQueriesList
                .stream()
                .filter(x -> x.getQueryId() == id)
                .findFirst()
                .orElse(null);
//        return first.orElse(null);
    }

    /*
     * Returns query with given file path
     * */
    public MediaQuery findQueryByFilePath(String filepath) {
        return mediaQueriesList
                .stream()
                .filter(x -> x.getFilePath().equals(filepath))
                .findFirst()
                .orElse(null);
    }

}
