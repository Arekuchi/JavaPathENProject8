package tourGuide;

import static org.junit.Assert.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import org.junit.Ignore;
import org.junit.Test;


import org.mockito.Mockito;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.proxy.gpsUtil.GpsUtilProxy;
import tourGuide.proxy.gpsUtil.dto.Attraction;
import tourGuide.proxy.gpsUtil.dto.VisitedLocation;
import tourGuide.service.RewardCentralService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserReward;

public class TestRewardsService {

	GpsUtilProxy gpsUtilService = Mockito.mock(GpsUtilProxy.class);

	@Test
	public void userGetRewards() {

		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentralService());



		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		Attraction attraction = gpsUtilService.getAttractions().get(0);
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
		tourGuideService.trackUserLocation(user).join();
		List<UserReward> userRewards = user.getUserRewards();
		tourGuideService.tracker.stopTracking();
		assertTrue(userRewards.size() == 1);
	}
	
	@Test
	public void isWithinAttractionProximity() {
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentralService());
		Attraction attraction = gpsUtilService.getAttractions().get(0);
		assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
	}
	
	// Needs fixed - can throw ConcurrentModificationException
	@Test
	public void nearAllAttractions() {
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentralService());
		rewardsService.setProximityBuffer(Integer.MAX_VALUE);

		InternalTestHelper.setInternalUserNumber(1);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);
		
		rewardsService.calculateRewards(tourGuideService.getAllUsers().get(0)).join();
		List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0));
		tourGuideService.tracker.stopTracking();

		assertEquals(gpsUtilService.getAttractions().size(), userRewards.size());
	}
	
}
