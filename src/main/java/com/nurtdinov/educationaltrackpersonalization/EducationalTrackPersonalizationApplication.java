package com.nurtdinov.educationaltrackpersonalization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// docker run --name postgres -p 5432:5432 -e POSTGRES_PASSWORD=password -d postgres
//
@SpringBootApplication
public class EducationalTrackPersonalizationApplication {

	public static void main(String[] args) {
		SpringApplication.run(EducationalTrackPersonalizationApplication.class, args);
	}

}
