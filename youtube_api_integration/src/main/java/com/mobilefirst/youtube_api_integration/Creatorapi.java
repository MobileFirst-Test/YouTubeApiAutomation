package com.mobilefirst.youtube_api_integration;

import static io.restassured.RestAssured.given;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class Creatorapi {

	public static final String AUTHTOKEN = "7f4458f778ec33b6aad12ab98b89b678";

	public static final String USERNAME = "devanand21";
	public static final String APPLICATION_NAME = "studioone";

	public static final String F_VIDEOTITLE = "Video_Title";
	public static final String F_VIDEOTYPE = "Video_Type";
	public static final String F_VIDEOURL = "Video_URL";
	public static final String F_UPLOADEDTIME = "Uploaded_Time";
	public static final String F_VIEWS = "Views";
	public static final String F_STUDIONAME = "Studio_Name";
	public static final String F_GPLACEID = "GPlaceID";
	public static final String F_UPDATED = "Updated";

	
	//This method will add records in creator form
	public static void addRecords(String videoTitle, String videoURL, String videoType, String uploadedTime,
		String views, String studioName, String gPlaceId) {
		String formName = "Youtube_Videos_Classes_Near_me_Screen";

		Response response = given().queryParam("authtoken", AUTHTOKEN).queryParam("scope", "creatorapi")
				.queryParam(F_VIDEOTITLE, videoTitle).queryParam(F_VIDEOURL, videoURL)
				.queryParam(F_VIDEOTYPE, videoType).queryParam(F_UPLOADEDTIME, uploadedTime)
				.queryParam(F_VIEWS, views).queryParam(F_STUDIONAME, studioName).queryParam(F_GPLACEID, gPlaceId)
				.when().post("https://creator.zoho.com/api/" + USERNAME + "/json/" + APPLICATION_NAME + "/form/"
						+ formName + "/record/add")
				.then().extract().response();
		String responseString = response.getBody().asString();

		Assert.assertTrue(responseString.contains("\"status\":\"Success\""));

	}

	
	//This method will delete records in creator form
	public static boolean deleteRecords(String criteria) {
		boolean success = true;
		String formName = "Youtube_Videos_Classes_Near_me_Screen";
		Response response = given().queryParam("authtoken", AUTHTOKEN).queryParam("scope", "creatorapi")
				.queryParam("criteria", F_VIDEOTYPE + "=" + criteria).when().post("https://creator.zoho.com/api/"
						+ USERNAME + "/json/" + APPLICATION_NAME + "/form/" + formName + "/record/delete")
				.then().assertThat().statusCode(200).extract().response();

		String responseString = response.getBody().asString();
		try {
			Assert.assertTrue(responseString.contains("\"status\":\"Success\""));
			System.out.println("Delete Records api from " + formName + " was successfull");
			success = true;

		} catch (AssertionError e) {
			if (responseString.contains("\"status\":\"Failure")) {
				System.out.println("No existing records found");
				success = true;

			} else {
				System.out.println("Delete Records api failed");
				success = false;
			}
		}
		return success;

	}

	
	// This method will return records from Studio Details form
	public static ArrayList<List<String>> getrecords_studiodetails() {

		String reportName = "All_Studio_Details";

		Response response = given().when()
				.get("https://creator.zoho.com/api/json/" + APPLICATION_NAME + "/view/" + reportName + "?authtoken="
						+ AUTHTOKEN + "&scope=creatorapi&zc_ownername=" + USERNAME)
				.then().assertThat().statusCode(200).extract().response();

		String responseString = response.getBody().asString();
		responseString = responseString.substring(responseString.indexOf("=") + 1);
		responseString = responseString.substring(0, responseString.indexOf(";"));
		JsonPath js = new JsonPath(responseString);
		int studioCount = js.getInt("Studio_Details.size()");
		ArrayList<List<String>> studioDetails_MainList = new ArrayList<List<String>>();
		for (int i = 0; i < studioCount; i++) {
			List<String> studioDetails_InnerList = new ArrayList<String>();
			studioDetails_InnerList.add(js.getString("Studio_Details[" + i + "].Studio_Name"));
			studioDetails_InnerList.add(js.getString("Studio_Details[" + i + "].GPlace_ID"));
			studioDetails_InnerList.add(js.getString("Studio_Details[" + i + "].YouTube_Channel_Id"));
			studioDetails_MainList.add(studioDetails_InnerList);

		}

		return studioDetails_MainList;

	}

	
}
