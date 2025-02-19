package app.controller;

import model.OperationResult;
import model.MediaLink;
import model.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import service.ErrorNotificationService;
import service.MediaLinksService;
import service.PropertiesService;
import util.MediaIdentity;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
public class LinksController {

    @Autowired
    private MediaLinksService mediaLinksService;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private ErrorNotificationService errorNotificationService;

    private int sessionPageSize = 25;

    @ModelAttribute("error")
    public String getCurrentResult() {
        return errorNotificationService.getCurrentResult();
    }

//    @ModelAttribute("media_ignored")
//    public List<MediaLink> getAllIgnoredMedia() {
//        return mediaLinksService.getMediaIgnoredList();
//    }

    @ModelAttribute("user_paths")
    public boolean checkForUserProvidedPaths() {
        return propertiesService.checkUserPaths();
    }

    @ModelAttribute("query_list_size")
    public int getAllMediaQueries() {
        return mediaLinksService.getMediaQueryListSize();
    }

    @ModelAttribute("link_list")
    public List<MediaLink> getAllMediaLinks() {
        return mediaLinksService.getMediaLinks();
    }
    /*
     * Create new link with query id and query result object.
     * */
    @PostMapping("/newlink")
    public String newLink(QueryResult queryResult,
                          BindingResult bindingResult,
                          Model model) {
        MediaIdentity mediaIdentity = (queryResult.getImdbId().isEmpty()) ? MediaIdentity.TMDB : MediaIdentity.IMDB;
        // TODO pass exceptions info to user
        // get grouped queries that are marked only as part of the same title
        // TODO make this function for collection of media queries
        List<OperationResult> operationResults = mediaLinksService.createFileLink(queryResult, mediaIdentity);
        // TODO implement list of results
        operationResults.forEach(lcr -> errorNotificationService.setLinkCreationResult(lcr));
        return "redirect:/query";
    }

    @GetMapping("/newlink")
    public String newLinkGet() {
        return "redirect:/links";
    }

    /*
     * Show all existing symlinks.
     * */
    @RequestMapping(value = "/links", method = GET)
    public String linksSorted(@RequestParam(value = "sort", required = false) String sort,
                              @RequestParam("page") Optional<Integer> page,
                              @RequestParam("size") Optional<Integer> size,
                              Model model) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(sessionPageSize);
        sessionPageSize = pageSize;
        int min = currentPage * pageSize - pageSize + 1;
        int max = currentPage * pageSize;
        /*
         * Optional request parameter is being evaluated and list is sorted
         * accordingly. If no argument is given sorting falls back to default.
         * */
        Comparator<MediaLink> comparator = (sort != null && sort.equals("link"))
                ? Comparator.comparing(MediaLink::getLinkPath)
                : Comparator.comparing(MediaLink::getOriginalPath);
        List<MediaLink> allMediaLinks = mediaLinksService.getMediaLinks().stream().sorted(comparator).collect(Collectors.toList());
        Page<MediaLink> paginatedLinks = mediaLinksService.findPaginatedLinks(PageRequest.of(currentPage - 1, pageSize), allMediaLinks);
        // TODO update all dead files on reload
        // TODO show only missing files
        model.addAttribute("page", paginatedLinks);
        model.addAttribute("page_min", min);
        model.addAttribute("page_max", max);
        return "links";
    }

    @GetMapping("/clear-links/")
    public String clearLinks(Model model) {
        mediaLinksService.clearInvalidIgnoreAndLinks();
        return "redirect:/links";
    }

    // TODO add search for words after whitespaces
    @PostMapping("/search-link/")
    public String searchLink(@RequestParam("search") String search, Model model) {
        int min = 1;
        int max = sessionPageSize;
        Page<MediaLink> paginatedLinks = mediaLinksService.findPaginatedLinks(PageRequest.of(0, sessionPageSize), mediaLinksService.searchMediaLinks(search));
        model.addAttribute("page", paginatedLinks);
        model.addAttribute("page_min", min);
        model.addAttribute("page_max", max);
        return "links";
    }

    @PostMapping("/remove-link/{id}")
    public String newLink(@PathVariable("id") long id, Model model) {
        mediaLinksService.moveBackToQueue(id);
        return "redirect:/scan";
    }

    @PostMapping("/delete-original/{id}")
    public String deleteOriginal(@PathVariable("id") long id, Model model) {
        mediaLinksService.deleteOriginalFile(id);
        return "redirect:/links";
    }

    @PostMapping("/restore-original/{id}")
    public String restoreOriginal(@PathVariable("id") long id, Model model) {
        mediaLinksService.restoreOriginalFile(id);
        return "redirect:/links";
    }
}
