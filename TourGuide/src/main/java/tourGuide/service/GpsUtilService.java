package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import tourGuide.user.User;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GpsUtilService {

    private GpsUtil gpsUtil;

    public GpsUtilService() {
        this.gpsUtil = new GpsUtil();
    }

    // Service qui permets de gérer un pool de Tread pour des traitements simultané (ici 10000)
    private ExecutorService executor = Executors.newFixedThreadPool(10000);

    public List<Attraction> getAttractions() {

        return gpsUtil.getAttractions();
    }

    // On récupère la localisation de l'utilisateur de manière asynchrone
    public CompletableFuture<VisitedLocation> getUserLocation(User user) {

        return CompletableFuture.supplyAsync(() -> gpsUtil.getUserLocation(user.getUserId()), executor);
    }



}
