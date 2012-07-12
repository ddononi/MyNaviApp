package kr.co.navi;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Base64;
import android.util.Log;

public class GeoRouteSearch implements iConstant {
	private final Params params;
	public GeoRouteSearch(final Params params){
		this.params = params;
	}

	protected String execute() {
		try {
			// AppID:Key 로 구성한 값을 Base64 Encoding 한다.
			String appid =  APP_ID + ":" + APP_KEY;
			// base64 로 인코딩
			byte[] encodeBytes = Base64.encode(appid.getBytes(), Base64.DEFAULT);
			String encodedAppId = new String(encodeBytes);
			// 인증 value는 "Basic ODE*************" 로 입력 한다.
			// ODE*************는 AppID:Key를 Base64 인코딩 된 값이다.
			// 마지막에 공백이 들어 문제가 발생 할 수 있어 trim() 처리 한다.
			String authValue = "Basic " + encodedAppId.trim();

			URL url = new URL(
					"http://openapi.kt.com/maps/geocode/GetGeocodeByAddr?params="
							+ getParams());
			// URL 연결
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 헤더에 인증 값 셋팅
			conn.addRequestProperty("authorization", authValue);
			InputStream is;
			BufferedReader br;
			String data = null;
			// 요청 결과값 받기 위한 인풋 스트림 얻기
			is = conn.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			int i = 0;
			// 데이터 가져오기.
			while ((data = br.readLine()) != null) {
				// data는 Result 데이터 이다.

				String[] datas = data.split(",");
				String[] datas2 = datas[4].split(":");
				// payload는 검색 결과 데이터이이고
				// UTF-8 Encoding 되어있어 decode해준다.
				String payload = java.net.URLDecoder.decode(datas2[1],
						"utf-8");
				Log.i("naviApp", "Result Data=" + payload);
				return payload;

			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.i("naviApp", "error");
		}

		return null;
	}

	public String getParams() throws UnsupportedEncodingException, JSONException {
		// 주소 파라미터
		String addr = "서울특별시 강남구 삼성동 168-1";
		// 주소를 UTF-8로 URLEncoding 해 준다.
		String utf8Addr = java.net.URLEncoder.encode(addr, "utf-8");
		String timestamp = "20120413092033765";
		String addrcdtype = "0";
		// 입력 파라미터들을 Json 형태로 만들어 준다.
		// JSON 라이브러리는 인터넷이서 구할 수 있다.
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("timestamp", timestamp);
		jsonObj.put("addrcdtype", addrcdtype);
		jsonObj.put("addr", utf8Addr);
		String params = jsonObj.toString();
		System.out.println(params);
		// 입력 파라미터가 모두 들어간 파라미터를 UTF-8 URLEncoding 을 해준다.
		String paramsURLEncoding = java.net.URLEncoder.encode(
				jsonObj.toString(), "utf-8");

		return paramsURLEncoding;
	}

	/**
	 *	요청 파라미터 클래스
	 */
	public static class Params{

		/**
		 * 출발지 X좌표
		 */
		String SX;

		/**
		 * 출발지 Y좌표
		 */
		String SY;

		/**
		 * 도착지 X좌표
		 */
		String EX;
		/**
		 * 도착지 Y좌표
		 */
		String EY;

		/**
		 * 경로 탐색 종류( Option, default 값 RPTYPE=0)
			1. 자동차 길찾기 (RPTYPE=0)
			2. 대중교통 길찾기 (RPTYPE=1)
		 */
		String RPTYPE;


		/**
		 * 		좌표계 타입( Option , default 값 COORDTYPE = 4 )
				1. Geographic (COORDTYPE = 0)
				2. TM WEST (COORDTYPE = 1)
				3. TM MID (COORDTYPE = 2)
				4. TM EAST (COORDTYPE = 3)
				5. KATEC (COORDTYPE = 4)
				8 | 페 이 지
				6. UTM52 (COORDTYPE = 5)
				7. UTM51 (COORDTYPE = 6)
				8. UTMK (COORDTYPE = 7)
		 *
		 */
		String COORDTYPE;

		/**
		 * 경유지 첫번째 X 좌표 (Option, 자동차 길찾기만 적용)
		 */
		String VX1;

		/**
		 * 경유지 첫번째 Y 좌표 (Option, 자동차 길찾기만 적용
		 */
		String VY1;

		/**
		 * 경유지 두번째 X 좌표 (Option, 자동차 길찾기만 적용
		 */
		String VX2;

		/**
		 * 경유지 두번째 Y 좌표 (Option, 자동차 길찾기만 적용
		 */
		String VY2;

		/**
		 * 경유지 세번째 X 좌표 (Option, 자동차 길찾기만 적용
		 */
		String VX3;

		/**
		 * 경유지 세번째 Y 좌표 (Option, 자동차 길찾기만 적용
		 */
		String VY3;

		/**
		 * 		PRIORITY [자동차 길찾기(RPTYPE=0) 인 경우]
				자동차 경로탐색 우선 순위 (Option,  기본값
				PRIORITY=0)
				1. 최단 거리 우선 (PRIORITY = 0)
				2. 고속도로 우선 (PRIORITY = 1)
				3. 무료 도로 우선 (PRIORITY = 2)
				4. 최적 경로 (PRIORITY = 3)
				5. 실시간 도로 우선 (PRIORITY = 5)
				[대중교통 길찾기(RPTYPE=1) 인 경우]
				대중교통 경로탐색 우선 순위 (Option,  기본값
				PRIORITY=0)
				1. 추천 (PRIORITY = 0)
				2. 버스 (PRIORITY = 1) : 사용하지 않음
				3. 지하철 (PRIORITY = 2) : 사용하지 않음
				4. 버스+지하철 (PRIORITY = 3) : 사용하지 않음
		 *
		 */
		String PRIORITY;

		/**
		 * “yyyyMMddHHmmssSSS” 포맷의 요청 시간
		 */
		String timestamp;
	}
}
