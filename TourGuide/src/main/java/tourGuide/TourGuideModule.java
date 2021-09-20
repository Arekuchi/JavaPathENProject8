package tourGuide;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import rewardCentral.RewardCentral;


import tourGuide.proxy.gpsUtil.GpsUtilProxy;
import tourGuide.service.RewardCentralService;
import tourGuide.service.RewardsService;


@Configuration
public class TourGuideModule {

	
	@Bean
	public RewardsService getRewardsService(GpsUtilProxy getGpsUtilService) {
		return new RewardsService(getGpsUtilService, getRewardCentralService());
	}
	
	@Bean
	public RewardCentralService getRewardCentralService() {
		return new RewardCentralService();
	}
	
}
