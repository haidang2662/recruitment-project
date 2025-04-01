package vn.techmaster.danglh.recruitmentproject.resource;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import vn.techmaster.danglh.recruitmentproject.exception.ObjectNotFoundException;
import vn.techmaster.danglh.recruitmentproject.model.request.FavoriteJobRequest;
import vn.techmaster.danglh.recruitmentproject.model.response.FavoriteJobResponse;
import vn.techmaster.danglh.recruitmentproject.service.FavoriteJobService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/favourite-jobs")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FavouriteJobResource {

    FavoriteJobService favoriteJobService;

    @PostMapping
    public FavoriteJobResponse favoriteJob(@RequestBody @Valid FavoriteJobRequest request) throws ObjectNotFoundException {
        return favoriteJobService.favoriteJob(request);
    }

    @DeleteMapping
    public void unfavoriteJob(@RequestBody @Valid FavoriteJobRequest request) throws ObjectNotFoundException {
        favoriteJobService.unfavoriteJob(request);
    }

}
