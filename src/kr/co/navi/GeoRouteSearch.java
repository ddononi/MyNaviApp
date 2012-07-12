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
			// AppID:Key �� ������ ���� Base64 Encoding �Ѵ�.
			String appid =  APP_ID + ":" + APP_KEY;
			// base64 �� ���ڵ�
			byte[] encodeBytes = Base64.encode(appid.getBytes(), Base64.DEFAULT);
			String encodedAppId = new String(encodeBytes);
			// ���� value�� "Basic ODE*************" �� �Է� �Ѵ�.
			// ODE*************�� AppID:Key�� Base64 ���ڵ� �� ���̴�.
			// �������� ������ ��� ������ �߻� �� �� �־� trim() ó�� �Ѵ�.
			String authValue = "Basic " + encodedAppId.trim();

			URL url = new URL(
					"http://openapi.kt.com/maps/geocode/GetGeocodeByAddr?params="
							+ getParams());
			// URL ����
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// ����� ���� �� ����
			conn.addRequestProperty("authorization", authValue);
			InputStream is;
			BufferedReader br;
			String data = null;
			// ��û ����� �ޱ� ���� ��ǲ ��Ʈ�� ���
			is = conn.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			int i = 0;
			// ������ ��������.
			while ((data = br.readLine()) != null) {
				// data�� Result ������ �̴�.

				String[] datas = data.split(",");
				String[] datas2 = datas[4].split(":");
				// payload�� �˻� ��� ���������̰�
				// UTF-8 Encoding �Ǿ��־� decode���ش�.
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
		// �ּ� �Ķ����
		String addr = "����Ư���� ������ �Ｚ�� 168-1";
		// �ּҸ� UTF-8�� URLEncoding �� �ش�.
		String utf8Addr = java.net.URLEncoder.encode(addr, "utf-8");
		String timestamp = "20120413092033765";
		String addrcdtype = "0";
		// �Է� �Ķ���͵��� Json ���·� ����� �ش�.
		// JSON ���̺귯���� ���ͳ��̼� ���� �� �ִ�.
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("timestamp", timestamp);
		jsonObj.put("addrcdtype", addrcdtype);
		jsonObj.put("addr", utf8Addr);
		String params = jsonObj.toString();
		System.out.println(params);
		// �Է� �Ķ���Ͱ� ��� �� �Ķ���͸� UTF-8 URLEncoding �� ���ش�.
		String paramsURLEncoding = java.net.URLEncoder.encode(
				jsonObj.toString(), "utf-8");

		return paramsURLEncoding;
	}

	/**
	 *	��û �Ķ���� Ŭ����
	 */
	public static class Params{

		/**
		 * ����� X��ǥ
		 */
		String SX;

		/**
		 * ����� Y��ǥ
		 */
		String SY;

		/**
		 * ������ X��ǥ
		 */
		String EX;
		/**
		 * ������ Y��ǥ
		 */
		String EY;

		/**
		 * ��� Ž�� ����( Option, default �� RPTYPE=0)
			1. �ڵ��� ��ã�� (RPTYPE=0)
			2. ���߱��� ��ã�� (RPTYPE=1)
		 */
		String RPTYPE;


		/**
		 * 		��ǥ�� Ÿ��( Option , default �� COORDTYPE = 4 )
				1. Geographic (COORDTYPE = 0)
				2. TM WEST (COORDTYPE = 1)
				3. TM MID (COORDTYPE = 2)
				4. TM EAST (COORDTYPE = 3)
				5. KATEC (COORDTYPE = 4)
				8 | �� �� ��
				6. UTM52 (COORDTYPE = 5)
				7. UTM51 (COORDTYPE = 6)
				8. UTMK (COORDTYPE = 7)
		 *
		 */
		String COORDTYPE;

		/**
		 * ������ ù��° X ��ǥ (Option, �ڵ��� ��ã�⸸ ����)
		 */
		String VX1;

		/**
		 * ������ ù��° Y ��ǥ (Option, �ڵ��� ��ã�⸸ ����
		 */
		String VY1;

		/**
		 * ������ �ι�° X ��ǥ (Option, �ڵ��� ��ã�⸸ ����
		 */
		String VX2;

		/**
		 * ������ �ι�° Y ��ǥ (Option, �ڵ��� ��ã�⸸ ����
		 */
		String VY2;

		/**
		 * ������ ����° X ��ǥ (Option, �ڵ��� ��ã�⸸ ����
		 */
		String VX3;

		/**
		 * ������ ����° Y ��ǥ (Option, �ڵ��� ��ã�⸸ ����
		 */
		String VY3;

		/**
		 * 		PRIORITY [�ڵ��� ��ã��(RPTYPE=0) �� ���]
				�ڵ��� ���Ž�� �켱 ���� (Option,  �⺻��
				PRIORITY=0)
				1. �ִ� �Ÿ� �켱 (PRIORITY = 0)
				2. ��ӵ��� �켱 (PRIORITY = 1)
				3. ���� ���� �켱 (PRIORITY = 2)
				4. ���� ��� (PRIORITY = 3)
				5. �ǽð� ���� �켱 (PRIORITY = 5)
				[���߱��� ��ã��(RPTYPE=1) �� ���]
				���߱��� ���Ž�� �켱 ���� (Option,  �⺻��
				PRIORITY=0)
				1. ��õ (PRIORITY = 0)
				2. ���� (PRIORITY = 1) : ������� ����
				3. ����ö (PRIORITY = 2) : ������� ����
				4. ����+����ö (PRIORITY = 3) : ������� ����
		 *
		 */
		String PRIORITY;

		/**
		 * ��yyyyMMddHHmmssSSS�� ������ ��û �ð�
		 */
		String timestamp;
	}
}
