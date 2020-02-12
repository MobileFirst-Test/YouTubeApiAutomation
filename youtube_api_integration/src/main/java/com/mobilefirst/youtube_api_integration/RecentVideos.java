package com.mobilefirst.youtube_api_integration;

import static io.restassured.RestAssured.given;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class RecentVideos {

	// Storing YouTube keys in an array
	String[] youtubeKeys = new String[] { "AIzaSyB7-VS4jEFA2SFizpYgrhMnIogHMgRWJTY",
			"AIzaSyA9Ali7DapS_jQAlPAQbeSamzzL1WqDIqk", "AIzaSyCsSdSDa2nnH3l-O_xIDQIOBQXNZhOfd2w",
			"AIzaSyDj5gucr34n2sBhV0oMvNjz3rs4e1vyYek"

	};

	ArrayList<String> videoTitle = new ArrayList<String>();
	ArrayList<String> videoURL = new ArrayList<String>();
	ArrayList<String> uploadedTime = new ArrayList<String>();
	ArrayList<String> views = new ArrayList<String>();
	ArrayList<String> studioName = new ArrayList<String>();
	ArrayList<String> gPlaceId = new ArrayList<String>();
	String videoType = "Recent";

	ArrayList<List<String>> studioDetailsMainList = new ArrayList<List<String>>();
	List<String> studioDetailsInnerList = new ArrayList<String>();
	String youtubeChannelID = null;

	
	// This will get records from creator such as studio name,gPlace id
	@BeforeClass
	public void getStudioDetails() {
		studioDetailsMainList = Creatorapi.getrecords_studiodetails();
	}

	@Test
	public void recentVideos() throws ParseException, InterruptedException {
		int recentStatusCode = 0;
		boolean workingKey = true;
		int index = 0;

		do {

			String youtubeKey = youtubeKeys[index];
			recentStatusCode = getRecentYoutubeVideos(youtubeKey);

			if (recentStatusCode == 200) {
				System.out.println("Youtube Response for recent videos was successfull !!!");
				workingKey = true;

				Creatorapi.deleteRecords(videoType);
				Thread.sleep(4000);
				for (int i = 0; i < videoTitle.size(); i++) {
					Creatorapi.addRecords(videoTitle.get(i), videoURL.get(i), videoType, uploadedTime.get(i),
							views.get(i), studioName.get(i), gPlaceId.get(i));

				}
				System.out.println("Added records successfully in creator");

			} else {

				System.out.println("Youtube Response for recent videos was failed. Need to change api key");
				index++;
				workingKey = false;
				System.out.println("Api Key Changed");
			}
		} while (workingKey == false);
	}

	// This method will get recent youtube videos from our channel ids
	public int getRecentYoutubeVideos(String youtubeKey) throws ParseException {
		

		int responseCode1 = 0;
		int responseCode2 = 0;
		int responseCode = 0;
		String date = generateDateBeforeSixMonth();

		for (int a = 0; a < studioDetailsMainList.size(); a++) {

			studioDetailsInnerList = studioDetailsMainList.get(a);

			youtubeChannelID = studioDetailsInnerList.get(2);

			Response response1 = given().param("key", youtubeKey).param("part", "snippet")
					.param("channelId", youtubeChannelID).param("publishedAfter", date).param("type", "video")
					.param("order", "date").when().get("https://www.googleapis.com/youtube/v3/search").then().extract()
					.response();
			responseCode1 = response1.statusCode();
			if (responseCode1 == 200) {
				String responseString1 = response1.asString();
				JsonPath js1 = new JsonPath(responseString1);
				int itemsCount = js1.get("items.size()");

				for (int i = 0; i < itemsCount; i++) {
					String videoID = js1.get("items[" + i + "].id.videoId").toString();
					videoURL.add("https://www.youtube.com/watch?v=" + videoID);
					videoTitle.add(js1.get("items[" + i + "].snippet.title").toString());
					String videoPublishedAt = js1.get("items[" + i + "].snippet.publishedAt").toString();
					uploadedTime.add(formatDateTime(videoPublishedAt));

					Response response2 = given().param("key", youtubeKey).param("part", "snippet,statistics")
							.param("id", videoID).when().get("https://www.googleapis.com/youtube/v3/videos").then()
							.extract().response();
					responseCode2 = response2.statusCode();
					studioName.add(studioDetailsInnerList.get(0));
					gPlaceId.add(studioDetailsInnerList.get(1));

					if (responseCode2 == 200) {
						String responseString2 = response2.asString();
						JsonPath js2 = new JsonPath(responseString2);
						views.add(js2.get("items[0].statistics.viewCount").toString());

					}

				}

			}

		}

		if (responseCode1 == 200 && responseCode2 == 200) {
			responseCode = 200;
		}

		for (int i = 0; i < videoTitle.size(); i++) {
			System.out.println(i + 1 + " Video Type is: " + videoType);
			System.out.println("Video Title is: " + videoTitle.get(i));
			System.out.println("Video URL is: " + videoURL.get(i));
			System.out.println("Uploaded Time is: " + uploadedTime.get(i));
			System.out.println("View Count is: " + views.get(i));
			System.out.println("Comapany Name is: " + studioName.get(i));

			System.out.println(
					"-----------------------------------------------------------------------------------------------------");

		}
		return responseCode;
	}

	// This method will format data and time
	public String formatDateTime(String Date_Time) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		SimpleDateFormat output = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
		Date d = sdf.parse(Date_Time);
		String formattedTime = output.format(d);
		return formattedTime;

	}

	// This method will generate date before six month from current date
	public String generateDateBeforeSixMonth() {
		String date = null;
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, -6);
		Date d = c.getTime();
		SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00'Z'");
		date = output.format(d);
		return date;

	}

}
