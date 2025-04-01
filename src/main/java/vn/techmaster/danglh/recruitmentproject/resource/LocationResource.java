package vn.techmaster.danglh.recruitmentproject.resource;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.techmaster.danglh.recruitmentproject.entity.Location;
import vn.techmaster.danglh.recruitmentproject.service.LocationService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/locations")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocationResource {

    LocationService locationService;

    @GetMapping
    public List<Location> getAllLocations() {
        return locationService.getAllLocations();
    }

}
