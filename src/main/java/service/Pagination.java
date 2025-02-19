package service;

import model.MediaLink;
import model.MediaQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface Pagination {

    Page<MediaQuery> findPaginatedQueries(Pageable pageable, List<MediaQuery> mediaQueryList);

    Page<MediaLink> findPaginatedLinks(Pageable pageable, List<MediaLink> mediaLinks);
}
