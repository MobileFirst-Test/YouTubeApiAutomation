package com.mobilefirst.youtube_api_integration;

import static io.restassured.RestAssured.given;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;
import java.util.TreeMap;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class PopularVideos {

	HashMap<String, String> Channel_Id = new HashMap<String, String>();

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
	String videoType = "Popular";

	ArrayList<List<String>> studioDetailsMainList = new ArrayList<List<String>>();
	List<String> studioDetailsInnerList = new ArrayList<String>();
	String youtubeChannelID = null;

	// This will get records from creator such as studio name,gPlace id
	@BeforeClass
	public void getStudioDetails() {
		studioDetailsMainList = Creatorapi.getrecords_studiodetails();
	}

	@Test
	public void popularVideos() throws ParseException, InterruptedException {

		int statusCode = 0;
		boolean workingKey = true;
		int index = 0;

		do {

			String youtubeKey = youtubeKeys[index];
			statusCode = getPopularYoutubeVideos(youtubeKey);

			if (statusCode == 400) {
				System.out.println("Youtube Response for popular videos was successfull !!!");
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
			}
		} while (workingKey == false);
	}

	// This method will get recent youtube videos from our channel ids
	int getPopularYoutubeVideos(String youtubeKey) throws ParseException {
		int responseCode1 = 0;
		int responseCode2 = 0;
		int responseCode3 = 0;
		int responseCode4 = 0;

		TreeMap<Integer, ArrayList<String>> tm = new TreeMap<Integer, ArrayList<String>>(Collections.reverseOrder());

		for (int a = 0; a < studioDetailsMainList.size(); a++) {
			studioDetailsInnerList = studioDetailsMainList.get(a);

			youtubeChannelID = studioDetailsInnerList.get(2);
			String videosid_joined1 = null;
			String videosid_joined2 = null;
			ArrayList<String> videosid_list1 = new ArrayList<String>();
			ArrayList<String> videosid_list2 = new ArrayList<String>();

			Response response1 = given().param("key", youtubeKey).param("part", "id")
					.param("channelId", youtubeChannelID).param("type", "video").param("maxResults", "50")
					.param("order", "viewCount").when().get("https://www.googleapis.com/youtube/v3/search").then()
					.extract().response();
			responseCode1 = response1.statusCode();
			String nextPageToken1 = null;
			JsonPath js1 = null;
			int resultsCount1 = 0;
			if (responseCode1 == 200) {
				String responseString1 = response1.asString();

				js1 = new JsonPath(responseString1);

				int itemsCount1 = js1.get("items.size()");

				for (int i = 0; i < itemsCount1; i++) {
					String videoID = js1.get("items[" + i + "].id.videoId").toString();
					videosid_list1.add(videoID);
				}

				StringJoiner joiner = new StringJoiner(",");
				for (int i = 0; i < videosid_list1.size(); i++) {
					joiner.add(videosid_list1.get(i));
				}

				videosid_joined1 = joiner.toString();

			}

			if (resultsCount1 > 50) {
				nextPageToken1 = js1.get("nextPageToken");

				Response response2 = given().param("key", youtubeKey).param("part", "id")
						.param("channelId", youtubeChannelID).param("type", "video").param("maxResults", "50")
						.param("order", "viewCount").param("pageToken", nextPageToken1).when()
						.get("https://www.googleapis.com/youtube/v3/search").then().extract().response();
				responseCode2 = response2.statusCode();
				if (responseCode2 == 200) {

					String responseString2 = response2.asString();
					JsonPath js2 = new JsonPath(responseString2);

					int itemsCount2 = js2.get("items.size()");

					for (int i = 0; i < itemsCount2; i++) {
						String videoID = js2.get("items[" + i + "].id.videoId").toString();
						videosid_list2.add(videoID);
					}

					StringJoiner joiner = new StringJoiner(",");
					for (int i = 0; i < videosid_list2.size(); i++) {
						joiner.add(videosid_list2.get(i));
					}

					videosid_joined2 = joiner.toString();

				}
			}

			Response response3 = given().param("key", youtubeKey).param("part", "snippet,statistics")
					.param("id", videosid_joined1).when().get("https://www.googleapis.com/youtube/v3/videos").then()
					.extract().response();
			responseCode3 = response3.statusCode();

			if (responseCode3 == 200) {
				String responseString3 = response3.asString();
				JsonPath js3 = new JsonPath(responseString3);
				int itemsCount3 = js3.get("items.size()");

				for (int i = 0; i < itemsCount3; i++) {
					String videoID = js3.get("items[" + i + "].id").toString();
					String videoURL = "https://www.youtube.com/watch?v=" + videoID;
					String videoTitle = js3.get("items[" + i + "].snippet.title").toString();
					String videoPublishedAt = js3.get("items[" + i + "].snippet.publishedAt").toString();
					String uploadedTime = formatDateTime(videoPublishedAt);
					int views = Integer.parseInt(js3.get("items[" + i + "].statistics.viewCount").toString());
					String viewCount = String.valueOf(views);
					String studioName = studioDetailsInnerList.get(0);
					String gPlaceId = studioDetailsInnerList.get(1);

					String[] videodetails_array = new String[] { videoTitle, videoURL, uploadedTime, viewCount,
							studioName, gPlaceId };
					ArrayList<String> videodetails_list = new ArrayList<String>();

					for (int j = 0; j < videodetails_array.length; j++) {
						videodetails_list.add(videodetails_array[j]);
					}

					tm.put(views, videodetails_list);

				}

			}

			Response res4 = given().param("key", youtubeKey).param("part", "snippet,statistics")
					.param("id", videosid_joined2).when().get("https://www.googleapis.com/youtube/v3/videos").then()
					.extract().response();
			responseCode4 = res4.statusCode();

			if (responseCode4 == 200) {
				String resString4 = res4.asString();
				JsonPath js4 = new JsonPath(resString4);
				int ItemsCount4 = js4.get("items.size()");

				for (int i = 0; i < ItemsCount4; i++) {
					String videoID = js4.get("items[" + i + "].id").toString();
					String videoURL = "https://www.youtube.com/watch?v=" + videoID;
					String videoTitle = js4.get("items[" + i + "].snippet.title").toString();
					String videoPublishedAt = js4.get("items[" + i + "].snippet.publishedAt").toString();
					String uploadedTime = formatDateTime(videoPublishedAt);
					int views = Integer.parseInt(js4.get("items[" + i + "].statistics.viewCount").toString());
					String viewCount = String.valueOf(views);
					String studioName = studioDetailsInnerList.get(0);
					String gPlaceId = studioDetailsInnerList.get(1);

					String[] videodetails_array = new String[] { videoTitle, videoURL, uploadedTime, viewCount,
							studioName, gPlaceId };
					ArrayList<String> videodetails_list = new ArrayList<String>();

					for (int j = 0; j < videodetails_array.length; j++) {
						videodetails_list.add(videodetails_array[j]);
					}

					tm.put(views, videodetails_list);

				}

			}

		}

		Iterator itr = tm.keySet().iterator();

		for (int i = 0; itr.hasNext() && i < 100; i++) {
			int key = Integer.parseInt(itr.next().toString());

			ArrayList<String> values = new ArrayList<String>();
			values = tm.get(key);
			videoTitle.add(values.get(0));
			videoURL.add(values.get(1));
			uploadedTime.add(values.get(2));
			views.add(values.get(3));
			studioName.add(values.get(4));
			gPlaceId.add(values.get(5));

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

		if (responseCode1 == 200) {
			if (responseCode2 == 0 || responseCode2 == 200) {
				responseCode1 = 200;
			} else {
				responseCode1 = 400;
			}
		}

		if (responseCode3 == 200) {
			if (responseCode4 == 0 || responseCode4 == 200) {
				responseCode3 = 200;
			} else {
				responseCode3 = 400;
			}
		}

		return responseCode1 + responseCode3;

	}

	// This method will format data and time
	public String formatDateTime(String Date_Time) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		SimpleDateFormat output = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
		Date d = sdf.parse(Date_Time);
		String formattedTime = output.format(d);
		return formattedTime;

	}

}
