package nextstep.subway.acceptance;

import static nextstep.subway.acceptance.LineSteps.*;
import static nextstep.subway.acceptance.StationSteps.*;
import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

@DisplayName("지하철 경로 검색")
class PathAcceptanceTest extends AcceptanceTest {
	private Long 교대역;
	private Long 강남역;
	private Long 양재역;
	private Long 남부터미널역;
	private Long 이호선;
	private Long 신분당선;
	private Long 삼호선;

	/**
	 * 교대역    --- *2호선* ---   강남역
	 * |                        |
	 * *3호선*                   *신분당선*
	 * |                        |
	 * 남부터미널역  --- *3호선* ---   양재
	 */
	@BeforeEach
	public void setUp() {
		super.setUp();

		교대역 = 지하철역_생성_요청(관리자, "교대역").jsonPath().getLong("id");
		강남역 = 지하철역_생성_요청(관리자, "강남역").jsonPath().getLong("id");
		양재역 = 지하철역_생성_요청(관리자, "양재역").jsonPath().getLong("id");
		남부터미널역 = 지하철역_생성_요청(관리자, "남부터미널역").jsonPath().getLong("id");

		이호선 = 지하철_노선_생성_요청("2호선", "green", 교대역, 강남역, 10, 1);
		신분당선 = 지하철_노선_생성_요청("신분당선", "red", 강남역, 양재역, 10, 3);
		삼호선 = 지하철_노선_생성_요청("3호선", "orange", 교대역, 남부터미널역, 2, 3);

		지하철_노선에_지하철_구간_생성_요청(관리자, 삼호선, createSectionCreateParams(남부터미널역, 양재역, 3, 6));
	}

	@DisplayName("두 역의 최단 거리 경로를 조회한다.")
	@Test
	void findPathByDistance() {
		// when
		ExtractableResponse<Response> response = 두_역의_최단_거리_경로_조회를_요청(교대역, 양재역, "DISTANCE");

		// then
		assertThat(response.jsonPath().getList("stations.id", Long.class)).containsExactly(교대역, 남부터미널역, 양재역);
		assertThat(response.jsonPath().getLong("distance")).isEqualTo(5);
		assertThat(response.jsonPath().getLong("duration")).isEqualTo(9);
	}

	@DisplayName("두 역의 최소시간 경로를 조회한다.")
	@Test
	void findPathByDuration() {
		// when
		ExtractableResponse<Response> response = 두_역의_최단_거리_경로_조회를_요청(교대역, 양재역, "DURATION");

		// then
		assertThat(response.jsonPath().getList("stations.id", Long.class)).containsExactly(교대역, 강남역, 양재역);
		assertThat(response.jsonPath().getLong("distance")).isEqualTo(20);
		assertThat(response.jsonPath().getLong("duration")).isEqualTo(4);

	}

	private ExtractableResponse<Response> 두_역의_최단_거리_경로_조회를_요청(Long source, Long target, String pathCode) {
		return RestAssured
			.given().log().all()
			.accept(MediaType.APPLICATION_JSON_VALUE)
			.when()
			.get("/paths?source={sourceId}&target={targetId}&pathBaseCode={pathBaseCode}", source, target,
				pathCode)
			.then().log().all().extract();
	}

	private ExtractableResponse<Response> 두_역의_최소_시간_경로_조회를_요청(Long source, Long target, String pathCode) {
		return RestAssured
			.given()
			.log()
			.all()
			.accept(MediaType.APPLICATION_JSON_VALUE)
			.when()
			.get("/paths?source={sourceId}&target={targetId}&pathBaseCode={pathBaseCode}", source, target,
				pathCode)
			.then()
			.log()
			.all()
			.extract();
	}

	private Long 지하철_노선_생성_요청(String name, String color, Long upStation, Long downStation, int distance, int duration) {
		Map<String, String> lineCreateParams;
		lineCreateParams = new HashMap<>();
		lineCreateParams.put("name", name);
		lineCreateParams.put("color", color);
		lineCreateParams.put("upStationId", upStation + "");
		lineCreateParams.put("downStationId", downStation + "");
		lineCreateParams.put("distance", distance + "");
		lineCreateParams.put("duration", duration + "");

		return LineSteps.지하철_노선_생성_요청(관리자, lineCreateParams).jsonPath().getLong("id");
	}

	private Map<String, String> createSectionCreateParams(Long upStationId, Long downStationId, int distance,
		int duration) {
		Map<String, String> params = new HashMap<>();
		params.put("upStationId", upStationId + "");
		params.put("downStationId", downStationId + "");
		params.put("distance", distance + "");
		params.put("duration", duration + "");
		return params;
	}
}
