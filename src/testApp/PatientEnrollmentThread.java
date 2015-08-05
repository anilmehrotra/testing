package testApp;

import gov.in.mcd.apps.bd.health.HospitalAdministrationBD;
import gov.in.mcd.apps.bd.health.IHospitalAdministrationBD;
import gov.in.mcd.apps.library.constants.HealthConstants;
import gov.in.mcd.apps.library.exception.PresentationException;
import gov.in.mcd.apps.library.exception.ServiceException;
import gov.in.mcd.apps.library.logger.Logger;
import gov.in.mcd.apps.library.util.ApplicationEnvProperties;
import gov.in.mcd.apps.library.util.ApplicationUtil;
import gov.in.mcd.apps.web.health.hospitaladmin.form.PatientEnrollmentForm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class PatientEnrollmentThread implements Runnable {

	private String userID;
	private PatientEnrollmentForm enrollmentForm;
	private String requestId;
	private java.sql.Timestamp currentTimestamp;
	
	public PatientEnrollmentThread(String userID, PatientEnrollmentForm enrollmentForm,
			String requestId, java.sql.Timestamp currentTimestamp) {
		this.userID = userID;
		this.enrollmentForm = enrollmentForm;
		this.requestId = requestId;
		this.currentTimestamp = currentTimestamp;
	}

	public void run() {
		try {
			StringBuffer insertValues = new StringBuffer();
			String dateOfBirth = enrollmentForm.getPatientDetailForm().getBirthDateForm();
			if(!ApplicationUtil.isNull(enrollmentForm.getPatientDetailForm()) && !ApplicationUtil.isNull(dateOfBirth)){
				dateOfBirth = ""+ApplicationUtil.convertStringDateToSQLDate(dateOfBirth);
			}
			insertValues.append(enrollmentForm.getPatientTypeId()+" ,'"+enrollmentForm.getHospitalId()+"' ,'"+enrollmentForm.getPatientDetailForm().getFirstName()+"', ");
			insertValues.append("'"+enrollmentForm.getPatientDetailForm().getMiddleName()+"','"+enrollmentForm.getPatientDetailForm().getLastName()+"', ");
			insertValues.append("'"+enrollmentForm.getPatientDetailForm().getFathersName()+"','"+enrollmentForm.getPatientDetailForm().getMothersName()+"', ");
			insertValues.append("'"+enrollmentForm.getPatientDetailForm().getGender()+"','"+enrollmentForm.getPatientDetailForm().getMaritalStatus()+"', ");
			insertValues.append("'"+enrollmentForm.getPatientDetailForm().getHusbandsName()+"', ");
			if(!ApplicationUtil.isNull(dateOfBirth)){
				insertValues.append(" '"+dateOfBirth+"', ");
			}else{
				insertValues.append(" null, ");
			}
			
			insertValues.append(" "+enrollmentForm.getPatientDetailForm().getAgeDay()+" ,"+enrollmentForm.getPatientDetailForm().getAgeMonth()+" , ");
			insertValues.append(" "+enrollmentForm.getPatientDetailForm().getAgeYear()+" ,"+enrollmentForm.getPatientDetailForm().getOccupationId()+" , ");
			insertValues.append(" "+enrollmentForm.getPatientDetailForm().getEducationId()+" ,"+enrollmentForm.getPatientDetailForm().getReligion()+" , ");
			insertValues.append("'"+enrollmentForm.getPatientDetailForm().getReligionOther()+"',"+enrollmentForm.getPatientDetailForm().getCategory()+", ");
			insertValues.append("'"+enrollmentForm.getPatientDetailForm().getCategoryOther()+"', ");
			if(!ApplicationUtil.isNull(enrollmentForm.getPatientDetailForm().getCmnPresentAddress())){
				insertValues.append("'"+enrollmentForm.getPatientDetailForm().getCmnPresentAddress().getAddress1()+"','"+enrollmentForm.getPatientDetailForm().getCmnPresentAddress().getAddress2()+"', ");
				insertValues.append(" "+enrollmentForm.getPatientDetailForm().getCmnPresentAddress().getState()+" , ");
				insertValues.append("'"+enrollmentForm.getPatientDetailForm().getCmnPresentAddress().getCity()+"','"+enrollmentForm.getPatientDetailForm().getCmnPresentAddress().getZoneId()+"', ");
				insertValues.append("'"+enrollmentForm.getPatientDetailForm().getCmnPresentAddress().getWardId()+"','"+enrollmentForm.getPatientDetailForm().getCmnPresentAddress().getColonyId()+"', ");
				insertValues.append("'"+enrollmentForm.getPatientDetailForm().getCmnPresentAddress().getDistrictId()+"','"+enrollmentForm.getPatientDetailForm().getCmnPresentAddress().getPinCode()+"', ");
			}else{
				insertValues.append(" null,null,0,null,null,null,null,null,null, ");
			}
			insertValues.append("'"+enrollmentForm.getPatientDetailForm().getIsSameAsPresentAddress()+"', ");
			if(!ApplicationUtil.isNull(enrollmentForm.getPatientDetailForm().getIsSameAsPresentAddress()) && !enrollmentForm.getPatientDetailForm().getIsSameAsPresentAddress().equals("Y")){
				if(!ApplicationUtil.isNull(enrollmentForm.getPatientDetailForm().getCmnPermanentAddress())){
					insertValues.append("'"+enrollmentForm.getPatientDetailForm().getCmnPermanentAddress().getAddress1()+"','"+enrollmentForm.getPatientDetailForm().getCmnPermanentAddress().getAddress2()+"', ");
					insertValues.append(" "+enrollmentForm.getPatientDetailForm().getCmnPermanentAddress().getState()+" , ");
					insertValues.append("'"+enrollmentForm.getPatientDetailForm().getCmnPermanentAddress().getCity()+"','"+enrollmentForm.getPatientDetailForm().getCmnPermanentAddress().getZoneId()+"', ");
					insertValues.append("'"+enrollmentForm.getPatientDetailForm().getCmnPermanentAddress().getWardId()+"','"+enrollmentForm.getPatientDetailForm().getCmnPermanentAddress().getColonyId()+"', ");
					insertValues.append("'"+enrollmentForm.getPatientDetailForm().getCmnPermanentAddress().getDistrictId()+"','"+enrollmentForm.getPatientDetailForm().getCmnPermanentAddress().getPinCode()+"', ");
				}else{
					insertValues.append(" null,null,0,null,null,null,null,null,null, ");
				}
			}else{
				insertValues.append(" null,null,0,null,null,null,null,null,null, ");
			}
			insertValues.append("'"+enrollmentForm.getPatientDetailForm().getStdCode()+"', ");
			insertValues.append("'"+enrollmentForm.getPatientDetailForm().getLandLine()+"', ");
			insertValues.append("'"+enrollmentForm.getPatientDetailForm().getMobile()+"', ");
			insertValues.append("'"+enrollmentForm.getIsPatientReferred()+"', ");
			if(!ApplicationUtil.isNull(enrollmentForm.getIsPatientReferred()) && enrollmentForm.getIsPatientReferred().equals("Y")){
				insertValues.append("'"+enrollmentForm.getReferredFrom()+"', ");
				insertValues.append("'"+enrollmentForm.getReferredFromOther()+"', ");
				insertValues.append("'"+enrollmentForm.getReferredRemark()+"', ");
				if(!ApplicationUtil.isNull(enrollmentForm.getCmnReferralAddress())){
					insertValues.append("'"+enrollmentForm.getCmnReferralAddress().getAddress1()+"','"+enrollmentForm.getCmnReferralAddress().getAddress2()+"', ");
					insertValues.append(" "+enrollmentForm.getCmnReferralAddress().getState()+" , ");
					insertValues.append("'"+enrollmentForm.getCmnReferralAddress().getCity()+"','"+enrollmentForm.getCmnReferralAddress().getPinCode()+"', ");
				}else{
					insertValues.append(" null,null,0,null,null, ");
				}
			}else{
				insertValues.append(" null,null,null,null,null,0,null,null, ");
			}
			
			insertValues.append("'"+requestId+"', ");
			insertValues.append("'"+enrollmentForm.getRegistrationType()+"', ");
			
			insertValues.append(" '"+currentTimestamp+"', ");
			
			insertValues.append("'"+userID+"', ");
			if(ApplicationUtil.isNull(enrollmentForm.getRegFor())){
				insertValues.append(" "+0+", ");
			}else{
				insertValues.append(" "+enrollmentForm.getRegFor()+", ");
			}
			if(ApplicationUtil.isNull(enrollmentForm.getRelationship())){
				insertValues.append(" "+0+", ");
			}else{
				insertValues.append(" "+enrollmentForm.getRelationship()+", ");
			}
			
			insertValues.append(" "+enrollmentForm.getEmployeeName());
			
			String regStr = requestId;
			if(requestId == null){
				regStr = "";
			}
			String[] temp;
			 
			String delimiter = "/";
			temp = regStr.split(delimiter);
			System.out.println(temp.length);
			int year = 2000;
			int regNo = 0;
			if(temp.length > 3){
				year += Integer.parseInt(temp[2]);
				regNo = Integer.parseInt(temp[3].replaceAll("^0*", ""));
			}
			 
			insertValues.append(" , "+year + ", "+regNo +" ");
			boolean  isUpdated = false;
			isUpdated = insertIntoMcwis(userID,insertValues.toString());
			String mcwisInsert = "N";
			if(isUpdated){
				mcwisInsert = "Y";
			}
			IHospitalAdministrationBD hospitalAdministrationBD = new HospitalAdministrationBD();
			hospitalAdministrationBD.updateMcwisPatientEnrollment(userID,requestId,mcwisInsert);
		} catch (ServiceException serviceException) {
			PresentationException presentationException = new PresentationException(
					serviceException,
					HealthConstants.HLT_NEWENROLLMENT_SAVE_ERROR,
					this.getClass(), "PatientEnrollmentThread.runTask");
			Logger.logError(userID, presentationException);
		}
	}
	
	private boolean insertIntoMcwis(String userid, String insertQryForMCWIS) {
		//String connectionURL = "jdbc:mysql://172.16.1.36:3306/MCWIS1";
		String methodName = "insertIntoMcwis";
		Connection con = null;
		String dbUser = "", dbPwd = "", dbURL = "", schema = "";
		boolean insertFlag = false;
		try {
			dbURL = ApplicationEnvProperties.getProperty(userid, "MCWIS_DB_URL");
			dbUser = ApplicationEnvProperties.getProperty(userid, "MCWIS_DB_USER");
			dbPwd = ApplicationEnvProperties.getProperty(userid, "MCWIS_DB_PWD");
			
			StringBuffer query = new StringBuffer();
			schema = ApplicationEnvProperties.getProperty(userid, "MCWIS_SCHEMA");
			if(ApplicationUtil.isNull(schema)){
				schema = "MCWIS1";
			}
			
			insertQryForMCWIS = insertQryForMCWIS.replaceAll("'null'", "null");
			query.append(" insert into ");
			query.append(schema+".patMsatVot_t");
			      
			query.append(" ( PATIENT_TYPE_ID, HOSPITAL_ID,   FIRST_NAME, MIDDLE_NAME, LAST_NAME, FATHERS_NAME, MOTHERS_NAME, ");
			query.append(" GENDER, MARITAL_STATUS,   HUSBANDS_NAME, BIRTH_DATE, AGE_DAY, AGE_MONTH, AGE_YEAR, OCCUPATION_ID, EDUCATION_ID, ");
			query.append(" RELIGION, RELIGION_OTHER, CATEGORY, CATEGORY_OTHER, ADDRESS1, ADDRESS2, STATE1, CITY1, ZONE_ID1, WARD_ID1, ");
			query.append(" COLONY_ID1, DISTRICT_ID1, PIN_CODE1, IS_SAME_AS_PRESENT_ADDRESS, ADDRESS3, ADDRESS4, STATE2, CITY2, ZONE_ID2, ");
			query.append(" WARD_ID2, COLONY_ID2, DISTRICT_ID2, PIN_CODE2, STD_CODE, LAND_LINE, MOBILE, IS_PATIENT_REFERRED, REFERRED_FROM, ");
			query.append(" REFERRED_FROM_OTHER, REFERRED_REMARK, ADDRESS5, ADDRESS6, STATE3, CITY3, PIN_CODE3, PATIENT_REGISTRATION_ID, REGISTRATION_TYPE, ");
			query.append(" REGISTRATION_DTM, REGISTERED_BY, REG_FOR, RELATIONSHIP, EMPLOYEE_NAME, PATIENT_REGISTRATION_YR, PATIENT_REGISTRATION_NO ) ");
			query.append(" VALUES ( ");
			query.append(insertQryForMCWIS);
			query.append(" )");
			String str=query.toString();
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			//con = DriverManager.getConnection(connectionURL, "msat",	"msat");
			con = DriverManager.getConnection(dbURL, dbUser,dbPwd);
			
			Statement stmt = con.createStatement();
			
			stmt.executeUpdate(query.toString());
			stmt.close();
			insertFlag = true;
		} catch (SQLException exception) {
			PresentationException presentationException = new PresentationException(
					exception,
					HealthConstants.HLT_NEWENROLLMENT_SAVE_ERROR,
					this.getClass(), methodName);
			Logger.logError(userID, presentationException);
		} catch (Exception exception) {
			PresentationException presentationException = new PresentationException(
					exception,
					HealthConstants.HLT_NEWENROLLMENT_SAVE_ERROR,
					this.getClass(), methodName);
			Logger.logError(userID, presentationException);
		} finally {
			// Close the connection
			try {
				con.close();
			} catch (Exception exception) {
				PresentationException presentationException = new PresentationException(
						exception,
						HealthConstants.HLT_NEWENROLLMENT_SAVE_ERROR,
						this.getClass(), methodName);
				Logger.logError(userID, presentationException);
			}
		}
		return insertFlag;
	}

}
