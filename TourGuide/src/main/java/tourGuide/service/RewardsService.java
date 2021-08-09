package tourGuide.service;

import java.util.ArrayList;
import java.util.List;
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
		List<CompletableFuture<Void>> rewardPoints = new ArrayList<>();

		// Parcours les lieux visités par l'utilisateur (position gps brute)
		for(VisitedLocation visitedLocation : userLocations) {
			// parcours les positions gps des attractions
			for(Attraction attraction : attractions) {
				// on regarde si le USER a déjà des rewards sur cette attraction
				if(user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
					// on vérifie que les positions du USER et de l'attraction sont proche.
					if(nearAttraction(visitedLocation, attraction)) {
						// le reward est attribué
						rewardPoints.add(rewardsCentralService.getAttractionRewardPoints(attraction, user).thenAccept(reward -> user.addUserReward(new UserReward(visitedLocation, attraction, reward))));
					}
				}
			}
		}
		return CompletableFuture.allOf(rewardPoints.toArray(new CompletableFuture[0]));
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
