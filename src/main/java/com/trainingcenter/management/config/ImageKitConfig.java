package com.trainingcenter.management.config;

import io.imagekit.client.ImageKitClient;
import io.imagekit.client.okhttp.ImageKitOkHttpClient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ImageKitConfig.ImageKitProperties.class)
public class ImageKitConfig {

	@Bean
	public ImageKitClient imageKitClient(ImageKitProperties properties) {
		return ImageKitOkHttpClient.builder()
				.privateKey(properties.getPrivateKey())
				.build();
	}

	@Getter
	@Setter
	@ConfigurationProperties(prefix = "imagekit")
	public static class ImageKitProperties {
		private String publicKey;
		private String privateKey;
		private String urlEndpoint;
	}
}
