package tourGuide.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.user.User;
import tourGuide.user.UserReward;

@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
    private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;
	private final GpsUtilService gpsUtilService;
	private final RewardCentralService rewardsCentralService;
	
	public RewardsService(GpsUtilService gpsUtilService, RewardCentralService rewardsCentralService) {
		this.gpsUtilService = gpsUtilService;
		this.rewardsCentralService = rewardsCentralService;
	}
	
	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}
	
	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}

	/*
	* On calcule les rewards de l'utilisateur qui dépendent des attractions visitées.
	*/
	public CompletableFuture<Void> calculateRewards(User user) {
		List<VisitedLocation> userLocations = new ArrayList<>(user.getVisitedLocations());
		List<Attraction> attractions = gpsUtilService.getAttractions();

		// Pour construire une reward pour l'utilisateur, on a besoin de rewardPoints et la Localisation associé
		// On collecte les résultats des appels asynchrone pour chaque attraction
		Map<Attraction, Integer> rewardPoints = new HashMap<>();

		// On collecte la localisation de l'utilisateur pour chaque attraction
		Map<Attraction, VisitedLocation> rewardVisitedLocations = new HashMap<>();

		// On collecte les appels asynchrone pour chaque attraction
		Map<Attraction, CompletableFuture<Void>> rewardFutures = new HashMap<>();

		// Parcours les lieux visités par l'utilisateur (position gps brute)
		for(VisitedLocation visitedLocation : userLocations) {
			// parcours les positions gps des attractions
			for(Attraction attraction : attractions) {
				// on regarde si le USER a déjà des rewards sur cette attraction
				if(user.getUserRewards().stream().noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName))) {
					// on vérifie que les positions du USER et de l'attraction sont proche.
					if(nearAttraction(visitedLocation, attraction)) {
						// le reward est attribué
						rewardFutures.putIfAbsent(attraction, rewardsCentralService.getAttractionRewardPoints(attraction, user).thenAccept(rewardPoint -> rewardPoints.put(attraction, rewardPoint)));
						rewardVisitedLocations.putIfAbsent(attraction, visitedLocation);
					}
				}
			}
		}
		return CompletableFuture.allOf(rewardFutures.values().toArray(new CompletableFuture[0]))
				.thenAccept(v -> rewardPoints.forEach((attraction, rewardPoint) -> user.addUserReward(
						new UserReward(
								rewardVisitedLocations.get(attraction),
								attraction,
								rewardPoint
						))
				));

	}
	
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}
	
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}

	
	public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                               + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
	}

}
