package vn.techmaster.danglh.recruitmentproject.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vn.techmaster.danglh.recruitmentproject.entity.Location;
import vn.techmaster.danglh.recruitmentproject.repository.LocationRepository;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class LocationService {

    LocationRepository locationRepository;

    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }
}
