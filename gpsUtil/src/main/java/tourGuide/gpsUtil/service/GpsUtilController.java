package tourGuide.gpsUtil.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class GpsUtilController {

    private GpsUtil gpsUtil;

    public GpsUtilController() {
        gpsUtil = new GpsUtil();
    }

    @RequestMapping("/getAttractions")
    public List<Attraction> getAttractions() {
        return gpsUtil.getAttractions();
    }

    @RequestMapping("/getUserLocation")
    public VisitedLocation getUserLocation(@RequestParam String userId) {
        return gpsUtil.getUserLocation(UUID.fromString(userId));
    }
}