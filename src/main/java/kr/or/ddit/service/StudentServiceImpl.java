package kr.or.ddit.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import kr.or.ddit.command.Criteria;
import kr.or.ddit.command.PageMaker;
import kr.or.ddit.command.SearchCriteria;
import kr.or.ddit.dao.StudentDAO;
import kr.or.ddit.dto.MemberVO;
import kr.or.ddit.dto.NoticeVO;
import kr.or.ddit.dto.PacounseltVO;
import kr.or.ddit.dto.StudentVO;
import kr.or.ddit.dto.SuggestVO;

public class StudentServiceImpl implements StudentService {

	private StudentDAO studentDAO;

	public void setStudentDAO(StudentDAO studentDAO) {
		this.studentDAO = studentDAO;
	}

	/*
	 * @Override public List<StudentVO> getStudentList() throws Exception { return
	 * studentDAO.selectStudentList(); }
	 * 
	 * @Override public List<StudentVO> getStudentList(Criteria cri) throws
	 * Exception { // TODO Auto-generated method stub return null; }
	 * 
	 * @Override public Map<String, Object> getStudentListForPage(Criteria cri)
	 * throws Exception { SearchCriteria searchCri = (SearchCriteria) cri;
	 * Map<String, Object> dataMap = new HashMap<String, Object>();
	 * 
	 * PageMaker pageMaker = new PageMaker(); pageMaker.setCri(cri);
	 * pageMaker.setTotalCount(studentDAO.selectSearchStudentListCount(searchCri));
	 * 
	 * List<StudentVO> studentList = studentDAO.selectStudentList(searchCri);
	 * 
	 * dataMap.put("studentList", studentList); dataMap.put("pageMaker", pageMaker);
	 * 
	 * return dataMap; }
	 */
	@Override
	public Map<String, Object> getStudentList(Criteria cri, String id) throws SQLException {
		SearchCriteria searchCri = (SearchCriteria) cri;
		Map<String, Object> dataMap = new HashMap<String, Object>();

		PageMaker pageMaker = new PageMaker();
		pageMaker.setCri(cri);
		try {
			pageMaker.setTotalCount(studentDAO.selectStudentListCount(searchCri));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<StudentVO> studentList = studentDAO.selectSearchStudentList(searchCri);

		for (StudentVO student : studentList) {
			try {
				// String universityname=studentDAO.selectFirstHopeSchool(student.getId());

				// student.setUniversityName(universityname);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		dataMap.put("studentList", studentList);
		dataMap.put("pageMaker", pageMaker);

		return dataMap;
	}

	@Override
	public void regist(StudentVO student) throws Exception {
		String stid = studentDAO.selectStudentSequenceNextValue();

		if (student.getAuthority().equals("ROLE_STUDENT")) {
			student.setId("S" + stid);
		} else if (student.getAuthority().equals("ROLE_TEACHER")) {
			student.setId("T" + stid);
		}

		studentDAO.insertStudent(student);
		
		String hostNameUrl = "https://sens.apigw.ntruss.com";     		// ????????? URL
		String requestUrl= "/sms/v2/services/";                   		// ?????? URL
		String requestUrlType = "/messages";                      		// ?????? URL
		String accessKey = "NoBq1UwqZvVwUKmhBTHt";                     						// ????????? ???????????? ????????? ???????????? ???????????? ?????? ?????????
		String secretKey = "KfAuUX6Leinn8GMNVfSQH1Y1WXML53pVbPRzn8Oz";  										// 2??? ????????? ?????? ??????????????? ???????????? service secret
		String serviceId = "ncp:sms:kr:288275248801:sms";        									// ??????????????? ????????? SMS ????????? ID
		String method = "POST";											// ?????? method
		String timestamp = Long.toString(System.currentTimeMillis()); 	// current timestamp (epoch)
		requestUrl += serviceId + requestUrlType;
		String apiUrl = hostNameUrl + requestUrl;
		
		// JSON ??? ????????? body data ??????
		
		JSONObject bodyJson = new JSONObject();
		JSONObject toJson = new JSONObject();
	    JSONArray  toArr = new JSONArray();
	    
	    String refuse ="????????????"+student.getId()+"?????? ??????????????? 1234?????????.??????????????? ??????????????????";  //       <-----------????????? ????????? 
	    
	    toJson.put("subject","???LMS?");				// ????????? ?????? * LMS Type????????? ????????? ??? ????????????.
	    toJson.put("content",refuse);				// ????????? ?????? * Type?????? ?????? byte ????????? ????????????.* SMS: 80byte / LMS: 2000byte  <!-- ?????? ?????????!!!! -->
	    toJson.put("to","01096470026");					// ???????????? ??????  * ?????? 50????????? ????????? ????????? ??? ????????????.
	    //toJson.put("to","01032290177");					// ???????????? ??????  * ?????? 50????????? ????????? ????????? ??? ????????????.
	    toArr.add(toJson);
	    
	    bodyJson.put("type","SMS");				// ????????? Type (sms | lms)
	    bodyJson.put("contentType","COMM");			// ????????? ?????? Type (AD | COMM) * AD: ?????????, COMM: ????????? (default: COMM) * ????????? ????????? ?????? ??? ?????? ?????? ????????? ?????? ?????????????????? (??? 50???)??? ???????????????.
	    bodyJson.put("countryCode","82");		// ?????? ????????????
	    bodyJson.put("from","01051448830");				// ???????????? * ????????? ??????/????????? ????????? ????????? ??? ????????????.		
	    bodyJson.put("subject","AlphaGo");				// ????????? ?????? * LMS Type????????? ????????? ??? ????????????.
	    bodyJson.put("content","?????? ?????? ????????????");				// ????????? ?????? * Type?????? ?????? byte ????????? ????????????.* SMS: 80byte / LMS: 2000byte  <!-- ??? ???????????????????????? ????????? ?????? -->
	    bodyJson.put("messages", toArr);		
	    

	    String body = bodyJson.toJSONString();
	    
	    System.out.println(body);
	    
        try {

            URL url = new URL(apiUrl);

            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setUseCaches(false);
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestProperty("content-type", "application/json");
            con.setRequestProperty("x-ncp-apigw-timestamp", timestamp);
            con.setRequestProperty("x-ncp-iam-access-key", accessKey);
            con.setRequestProperty("x-ncp-apigw-signature-v2", makeSignature(requestUrl, timestamp, method, accessKey, secretKey));
            con.setRequestMethod(method);
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            
            wr.write(body.getBytes());
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            BufferedReader br;
            System.out.println("responseCode" +" " + responseCode);
            if(responseCode==202) { // ?????? ??????
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // ?????? ??????
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            
            System.out.println(response.toString());

        } catch (Exception e) {
            System.out.println(e);
        }
    }
	
	public static String makeSignature(String url, String timestamp, String method, String accessKey, String secretKey) throws NoSuchAlgorithmException, InvalidKeyException {
	    String space = " ";                    // one space
	    String newLine = "\n";                 // new line
	    

	    String message = new StringBuilder()
	        .append(method)
	        .append(space)
	        .append(url)
	        .append(newLine)
	        .append(timestamp)
	        .append(newLine)
	        .append(accessKey)
	        .toString();

	    SecretKeySpec signingKey;
	    String encodeBase64String;
		try {
			
			signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(signingKey);
			byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
		    encodeBase64String = Base64.getEncoder().encodeToString(rawHmac);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			encodeBase64String = e.toString();
		}
	    

	  return encodeBase64String;
	}

	@Override
	public StudentVO getStudent(String id) throws Exception {
		StudentVO student = studentDAO.selectStudentById(id);
		return student;
	}

	@Override
	public void remove(String id) throws Exception {
		studentDAO.deleteStudent(id);

	}

	@Override
	public Map<String, Object> selectHopeSchool(String id) throws SQLException {
		Map<String, Object> dat = new HashMap<String, Object>();

		List<StudentVO> hopeschool = null;
		try {
			hopeschool = studentDAO.selectHopeSchool(id);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		dat.put("hopeschool", hopeschool);

		return dat;
	}

	@Override
	public StudentVO selectFirstHopeSchool(String id) throws SQLException {
		StudentVO student = studentDAO.selectFirstHopeSchool(id);

		return student;
	}

}
