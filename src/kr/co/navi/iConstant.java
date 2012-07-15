package kr.co.navi;

/**
 *	상수 정의 인터페이스
 */
public interface iConstant {

	/**
	 * 앱 아이디
	 */
	public final static String APP_ID = "8100CFD2";

	/**
	 * 앱 테스트 키
	 */
	public final static String APP_KEY = "T77EDC52D13BB61";
	
	/**
	 * 경로를 찾을 url
	 */
	public final static String SEARCH_URL = "http://openapi.kt.com/maps/etc/RouteSearch?params=";	
	
	/**
	 * 주소 변환 url
	 */
	public final static String GEOCODE_URL ="http://maps.google.com/maps/api/geocode/json?sensor=false&language=ko&";
	
	/**
	 *	주소변환 요청 정상 처리값
	 */
	public final static String STATUS_OK ="OK";	
	
	/**
	 *	디폴트 위도
	 */
	public final static int DEFAULT_LAT = (int)(37.566538 * 1E6);	

	/**
	 *	디폴트 경도
	 */
	public final static int DEFAULT_LNG =(int)(126.977953 * 1E6);		

}
