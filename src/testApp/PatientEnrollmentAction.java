package testApp;

import gov.in.mcd.apps.bd.common.CommonBD;
import gov.in.mcd.apps.bd.health.HospitalAdministrationBD;
import gov.in.mcd.apps.bd.health.IHospitalAdministrationBD;
import gov.in.mcd.apps.bd.health.IPublicHealthBD;
import gov.in.mcd.apps.bd.health.PublicHealthBD;
import gov.in.mcd.apps.library.constants.CommonConstants;
import gov.in.mcd.apps.library.constants.HealthConstants;
import gov.in.mcd.apps.library.exception.PresentationException;
import gov.in.mcd.apps.library.exception.ServiceException;
import gov.in.mcd.apps.library.logger.Logger;
import gov.in.mcd.apps.library.util.ApplicationUtil;
import gov.in.mcd.apps.pojo.MCDApplicationUser;
import gov.in.mcd.apps.pojo.health.HltCmnAddress;
import gov.in.mcd.apps.pojo.health.HltCodeValue;
import gov.in.mcd.apps.pojo.health.HltColony;
import gov.in.mcd.apps.pojo.health.HltDistrict;
import gov.in.mcd.apps.pojo.health.HltPatientDetail;
import gov.in.mcd.apps.pojo.health.HltPatientEnrollment;
import gov.in.mcd.apps.pojo.health.HltPatientVisitRecord;
import gov.in.mcd.apps.pojo.health.HltSchStudentRegistration;
import gov.in.mcd.apps.pojo.health.HltState;
import gov.in.mcd.apps.pojo.integration.MCDDependantsDetail;
import gov.in.mcd.apps.pojo.integration.MCDEmployeeDetail;
import gov.in.mcd.apps.pojo.integration.MCDOffice;
import gov.in.mcd.apps.pojo.integration.MCDWard;
import gov.in.mcd.apps.pojo.integration.MCDZone;
import gov.in.mcd.apps.web.common.dms.util.DMSThreadPool;
import gov.in.mcd.apps.web.common.dms.util.PatientEnrollmentThread;
import gov.in.mcd.apps.web.health.CacheUtil;
import gov.in.mcd.apps.web.health.PaginationHelper;
import gov.in.mcd.apps.web.health.hospitaladmin.form.CmnAddressForm;
import gov.in.mcd.apps.web.health.hospitaladmin.form.PatientCommonViewForm;
import gov.in.mcd.apps.web.health.hospitaladmin.form.PatientDetailForm;
import gov.in.mcd.apps.web.health.hospitaladmin.form.PatientEnrollmentForm;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.struts2.interceptor.validation.SkipValidation;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.validator.annotations.RegexFieldValidator;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;

public class PatientEnrollmentAction extends ActionSupport implements
		ModelDriven<PatientEnrollmentForm> {
	private static final long serialVersionUID = 1L;
	private PatientEnrollmentForm patientEnrollmentForm = new PatientEnrollmentForm();
	List<HltState> stateMap;
	List<MCDZone> zonesMap = new ArrayList<MCDZone>();
	List<MCDWard> wardMap = new ArrayList<MCDWard>();
	List<HltColony> colonyMap = new ArrayList<HltColony>();

	List<MCDWard> wardPresentMap = new ArrayList<MCDWard>();
	List<HltColony> colonyPresentMap = new ArrayList<HltColony>();

	List<HltDistrict> districtMap = new ArrayList<HltDistrict>();
	Map<Short, String> educationMap = new HashMap<Short, String>();
	Map<Short, String> occupationMap = new HashMap<Short, String>();
	List<MCDOffice> hospitalList = new ArrayList<MCDOffice>();
	List<MCDOffice> publicHospitalList = new ArrayList<MCDOffice>();
	List<MCDOffice> chestClinicsList = new ArrayList<MCDOffice>();
	List<HltPatientEnrollment> hltPatientEnrollmentList = new CopyOnWriteArrayList<HltPatientEnrollment>();
	HltPatientEnrollment hltPatientEnrollment = new HltPatientEnrollment();
	List<HltCodeValue> religionTypesList = new ArrayList<HltCodeValue>();
	List<HltCodeValue> categoryTypesList = new ArrayList<HltCodeValue>();
	private MCDApplicationUser mcdApplicationUser;
	private String validMCDRefrence = "N";
	private String presentState = null;
	private String permanantState = null;
	private String patientRegistrationNumberSearch;
	private String authCode = null;
	private String node = null;
	private String lid = null;
	private String resultSuccessPage;
	private String actionSuccessMsg;
	List<String> yearList = new ArrayList<String>();
	List<String> relationshipList;
	// private String dbHost;
	// private String dbPort;
	// private String dbInstance;
	// private String dbUser;
	// private String dbPwd;

	boolean patientOpdCardExpired = false;

	public boolean isPatientOpdCardExpired() {
		return patientOpdCardExpired;
	}

	public void setPatientOpdCardExpired(boolean patientOpdCardExpired) {
		this.patientOpdCardExpired = patientOpdCardExpired;
	}

	public String getAuthCode() {
		return authCode;
	}

	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}

	private String nextAction = "addPatientRegistration";
	private boolean noOpdCardFoud;

	public boolean isNoOpdCardFoud() {
		return noOpdCardFoud;
	}

	public void setNoOpdCardFoud(boolean noOpdCardFoud) {
		this.noOpdCardFoud = noOpdCardFoud;
	}

	private String patientFullName;
	private String sex;
	// private String hospitalName;
	private String patientAddress;
	// private String stateName;
	private String issuedByName;

	private String isList = "";

	public MCDApplicationUser getMcdApplicationUser() {
		return mcdApplicationUser;
	}

	public void setMcdApplicationUser(MCDApplicationUser mcdApplicationUser) {
		this.mcdApplicationUser = mcdApplicationUser;
	}

	public String getIsList() {
		return isList;
	}

	public void setIsList(String isList) {
		this.isList = isList;
	}

	public List<HltPatientEnrollment> getHltPatientEnrollmentList() {
		return hltPatientEnrollmentList;
	}

	public void setHltPatientEnrollmentList(
			List<HltPatientEnrollment> hltPatientEnrollmentList) {
		this.hltPatientEnrollmentList = hltPatientEnrollmentList;
	}

	public String getNextAction() {
		return nextAction;
	}

	public void setNextAction(String nextAction) {
		this.nextAction = nextAction;
	}

	public String getPatientFullName() {
		return patientFullName;
	}

	public void setPatientFullName(String patientFullName) {
		this.patientFullName = patientFullName;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getPatientAddress() {
		return patientAddress;
	}

	public void setPatientAddress(String patientAddress) {
		this.patientAddress = patientAddress;
	}

	public String getIssuedByName() {
		return issuedByName;
	}

	public void setIssuedByName(String issuedByName) {
		this.issuedByName = issuedByName;
	}

	public String getPatientRegistrationNumberSearch() {
		return patientRegistrationNumberSearch;
	}

	public void setPatientRegistrationNumberSearch(
			String patientRegistrationNumberSearch) {
		this.patientRegistrationNumberSearch = patientRegistrationNumberSearch;
	}

	public String getPresentState() {
		return presentState;
	}

	public void setPresentState(String presentState) {
		this.presentState = presentState;
	}

	public String getPermanantState() {
		return permanantState;
	}

	public void setPermanantState(String permanantState) {
		this.permanantState = permanantState;
	}

	public String getValidMCDRefrence() {
		return validMCDRefrence;
	}

	public void setValidMCDRefrence(String validMCDRefrence) {
		this.validMCDRefrence = validMCDRefrence;
	}

	String userID = "";
	private IHospitalAdministrationBD hospitalAdministrationBD = new HospitalAdministrationBD();
	CommonBD commonBD = new CommonBD();

	public PatientEnrollmentForm getPatientEnrollmentForm() {
		return patientEnrollmentForm;
	}

	public void setPatientEnrollmentForm(
			PatientEnrollmentForm patientEnrollmentForm) {
		this.patientEnrollmentForm = patientEnrollmentForm;
	}

	public List<HltState> getStateMap() {
		return stateMap;
	}

	public void setStateMap(List<HltState> stateMap) {
		this.stateMap = stateMap;
	}

	public List<MCDZone> getZonesMap() {
		return zonesMap;
	}

	public void setZonesMap(List<MCDZone> zonesMap) {
		this.zonesMap = zonesMap;
	}

	public List<MCDWard> getWardMap() {
		return wardMap;
	}

	public void setWardMap(List<MCDWard> wardMap) {
		this.wardMap = wardMap;
	}

	public List<HltColony> getColonyMap() {
		return colonyMap;
	}

	public void setColonyMap(List<HltColony> colonyMap) {
		this.colonyMap = colonyMap;
	}

	public List<HltDistrict> getDistrictMap() {
		return districtMap;
	}

	public void setDistrictMap(List<HltDistrict> districtMap) {
		this.districtMap = districtMap;
	}

	public Map<Short, String> getEducationMap() {
		return educationMap;
	}

	public void setEducationMap(Map<Short, String> educationMap) {
		this.educationMap = educationMap;
	}

	public Map<Short, String> getOccupationMap() {
		return occupationMap;
	}

	public void setOccupationMap(Map<Short, String> occupationMap) {
		this.occupationMap = occupationMap;
	}

	public List<MCDOffice> getHospitalList() {
		return hospitalList;
	}

	public void setHospitalList(List<MCDOffice> hospitalList) {
		this.hospitalList = hospitalList;
	}

	public List<HltCodeValue> getReligionTypesList() {
		return religionTypesList;
	}

	public void setReligionTypesList(List<HltCodeValue> religionTypesList) {
		this.religionTypesList = religionTypesList;
	}

	public List<HltCodeValue> getCategoryTypesList() {
		return categoryTypesList;
	}

	public void setCategoryTypesList(List<HltCodeValue> categoryTypesList) {
		this.categoryTypesList = categoryTypesList;
	}

	@Override
	public PatientEnrollmentForm getModel() {
		return patientEnrollmentForm;
	}

	/**
	 * Start of Medical Care Action Methods
	 * 
	 **/
	/** Start of Patient Records ***/
	@SkipValidation
	public String patientDetailsMc() {
		loadDefaultSearchInfo(getUserID());
		return "patientDetailsSearchMc";
	}

	@SkipValidation
	public String patientRegistrationSearchMc() {
		String returnPage = patientRegistrationSearch();
		getModel().setFromGetRelationshipDetials(false);
		if (returnPage.equals("patientDetailsView")) {
			returnPage = "patientDetailsViewMc";
		} else {
			returnPage = "patientDetailsSearchMc";
		}
		return returnPage;
	}

	@SkipValidation
	public String updatePatientRegistrationDetailsMc() {
		String returnPage = updatePatientRegistrationDetails();
		if (returnPage.equals("patientDetailsEdit")) {
			getModel().setModule("MC");
			returnPage = "patientDetailsEditMc";
		} else {
			returnPage = "patientDetailsSearchMc";
		}
		return returnPage;
	}

	public String updatePatientRegistrationMc() {
		String returnPage = updatePatientRegistration();
		if (returnPage.equals("updatesuccess")) {
			returnPage = "updatesuccessMc";
		} else {
			returnPage = "patientDetailsEditMc";
		}
		return returnPage;
	}

	/** End of Patient Records ***/

	/** Start of View Patient History ***/
	@SkipValidation
	public String viewPatientDetailsMc() {
		loadDefaultSearchInfo(getUserID());
		return "viewpatientDetailsSearchMc";
	}

	@SkipValidation
	public String viewPatientSearchMc() {
		String returnPage = viewPatientSearch();

		if (returnPage.equals("historypatientDetailsView")) {
			returnPage = "historypatientDetailsViewMc";
		} else {
			returnPage = "viewpatientDetailsSearchMc";
		}
		return returnPage;
	}

	/** End of View Patient History ***/

	/**
	 * added by Madhu for Common Functionality to view Patient Details for
	 * Homeopathy
	 **/
	@SkipValidation
	public String addPatientSpeciality() throws Exception {
		String returnPage = patientRegistrationSearchView();
		if (returnPage.equals("patientDetailsView")) {
			returnPage = "addPatientSpeciality";
		} else {
			returnPage = "patientDetailsSearchMc";
		}
		return returnPage;

	}

	@SkipValidation
	public String patientRegistrationSearchHM() {
		String returnPage = patientRegistrationSearch();
		if (returnPage.equals("patientDetailsView")) {
			returnPage = "patientDetailsViewHM";
		} else {
			returnPage = "patientDetailsSearchMc";
		}
		return returnPage;
	}

	@SkipValidation
	public String updatePatientRegistrationDetailsHM() {
		String returnPage = updatePatientRegistrationDetails();
		if (returnPage.equals("patientDetailsEdit")) {
			getModel().setModule("MC");
			returnPage = "patientDetailsEditHM";
		} else {
			returnPage = "patientDetailsSearchMc";
		}
		return returnPage;
	}

	@SkipValidation
	public String updatePatientRegistrationHM() {
		String returnPage = updatePatientRegistration();
		if (returnPage.equals("updatesuccess")) {
			returnPage = "updatesuccessHM";
		} else {
			returnPage = "patientDetailsEditHM";
		}
		return returnPage;
	}

	/**
	 * added by Madhu for Common Functionality to View Patient History for
	 * Homeopathy
	 **/
	@SkipValidation
	public String viewPatientSpeciality() throws Exception {

		String returnPage = "viewPatientSpeciality";

		setPatientCommonView();

		return returnPage;

	}

	private void setPatientCommonView() throws PresentationException {
		try {

			if (!ApplicationUtil.isNull(getPatientRegistrationNumberSearch())) {
				getPatientRegistrationDetails(userID,
						getPatientRegistrationNumberSearch(), null);
			}

			PatientCommonViewForm patientCommonViewForm = new PatientCommonViewForm();
			if (!ApplicationUtil.isNull(hltPatientEnrollmentList)
					&& !hltPatientEnrollmentList.isEmpty()) {
				if (hltPatientEnrollmentList.size() == 1) {
					boolean isPatientExpired = isPatientExpired(userID,
							hltPatientEnrollmentList.get(0)
									.getPatientEnrollmentGenId());
					if (isPatientExpired) {
						patientCommonViewForm.setIsExpire("Y");
					}
				}
			}
			hltPatientEnrollment = hltPatientEnrollmentList.get(0);

			if (!ApplicationUtil.isNull(hltPatientEnrollment
					.getRegistrationDtm())) {
				patientCommonViewForm.setRegistrationDtm(hltPatientEnrollment
						.getRegistrationDtm());
			}
			if (!ApplicationUtil.isNull(hltPatientEnrollment.getHospitalId())) {
				patientCommonViewForm.setHospitalId(hltPatientEnrollment
						.getHospitalId());
			}
			if (!ApplicationUtil.isNull(hltPatientEnrollment
					.getPatientEnrollmentGenId())) {
				patientCommonViewForm
						.setPatientEnrollmentGenId(hltPatientEnrollment
								.getPatientEnrollmentGenId());
			}
			if (!ApplicationUtil.isNull(hltPatientEnrollment
					.getPatientRegistrationId())) {
				patientCommonViewForm
						.setPatientRegistrationId(hltPatientEnrollment
								.getPatientRegistrationId());
			}
			if (!ApplicationUtil.isNull(hltPatientEnrollment.getRegisteredBy())) {
				CommonBD commonBd = new CommonBD();
				mcdApplicationUser = commonBd.getEmployeeDetailsById(
						hltPatientEnrollment.getRegisteredBy(),
						hltPatientEnrollment.getRegisteredBy());
				if (!ApplicationUtil.isNull(mcdApplicationUser))
					patientCommonViewForm.setRegisteredBy(mcdApplicationUser
							.getFirstName()
							+ " " + mcdApplicationUser.getLastName());
			}
			if (!ApplicationUtil.isNull(hltPatientEnrollment.getHospitalId())) {
				String hospitalName = hospitalAdministrationBD.getHospitalName(
						hltPatientEnrollment.getHospitalId(), 12,
						hltPatientEnrollment.getHospitalId());
				if (!ApplicationUtil.isNull(hospitalName))
					patientCommonViewForm.setHospitalName(hospitalName);
			}

			if (!ApplicationUtil.isNull(hltPatientEnrollment
					.getMcdReferenceId())) {
				patientCommonViewForm.setMcdReferenceId(hltPatientEnrollment
						.getMcdReferenceId());
			} else {
				patientCommonViewForm.setMcdReferenceId("");
			}

			if (!ApplicationUtil.isNull(hltPatientEnrollment.getEmployeeName())) {
				patientCommonViewForm.setEmployeeName(hltPatientEnrollment
						.getEmployeeName());
			} else {
				patientCommonViewForm.setEmployeeName("");
			}

			if (!ApplicationUtil
					.isNull(hltPatientEnrollment.getPatientTypeId())) {
				patientCommonViewForm.setPatientTypeId(hltPatientEnrollment
						.getPatientTypeId());
			} else {
				patientCommonViewForm.setPatientTypeId((short) 0);
			}
			if (!ApplicationUtil.isNull(hltPatientEnrollment.getRegFor())) {
				patientCommonViewForm.setRegFor(""
						+ hltPatientEnrollment.getRegFor());
			} else {
				patientCommonViewForm.setRegFor("");
			}
			if (!ApplicationUtil.isNull(hltPatientEnrollment.getRelationship())) {
				patientCommonViewForm.setRelationship(""
						+ hltPatientEnrollment.getRelationship());
			} else {
				patientCommonViewForm.setRelationship("");
			}

			HltPatientDetail hltPatientDetails = hltPatientEnrollment
					.getHltPatientDetail();
			if (!ApplicationUtil.isNull(hltPatientDetails)) {
				if (!ApplicationUtil.isNull(hltPatientDetails.getBirthDate())) {
					patientCommonViewForm.setBirthDate(hltPatientDetails
							.getBirthDate());
				}
				if (!ApplicationUtil.isNull(hltPatientDetails.getMobile())) {
					patientCommonViewForm.setMobile(hltPatientDetails
							.getMobile());
				}
				if (!ApplicationUtil.isNull(hltPatientDetails.getAgeYear())) {
					patientCommonViewForm.setAgeYears(hltPatientDetails
							.getAgeYear());
				}
				if (!ApplicationUtil.isNull(hltPatientDetails.getFathersName())) {
					patientCommonViewForm.setFathersName(hltPatientDetails
							.getFathersName());
				}
				String patientNm = "";
				if (!ApplicationUtil.isNull(hltPatientDetails.getFirstName())) {
					patientNm = hltPatientDetails.getFirstName();
				}
				if (!ApplicationUtil.isNull(hltPatientDetails.getMiddleName())) {
					patientNm = patientNm + " "
							+ hltPatientDetails.getMiddleName();
				}
				if (!ApplicationUtil.isNull(hltPatientDetails.getLastName())) {
					patientNm = patientNm + " "
							+ hltPatientDetails.getLastName();
				}
				patientCommonViewForm.setPatientFullName(patientNm);

				String genderCd = hltPatientDetails.getGender();
				if (!ApplicationUtil.isNull(genderCd)) {
					if (genderCd.equalsIgnoreCase("M")) {
						patientCommonViewForm.setSex("Male");
					} else if (genderCd.equalsIgnoreCase("F")) {
						patientCommonViewForm.setSex("Female");
					} else {
						patientCommonViewForm.setSex("Other");
					}
				}
			}
			getModel().setPatientCommonViewForm(patientCommonViewForm);
		} catch (Exception exception) {
			exception.printStackTrace();

			// throw presentationException;
		}
	}

	/**
	 * End of Medical Care Action Methods
	 * 
	 **/

	@SkipValidation
	public String patientDetails() {
		loadDefaultSearchInfo(getUserID());
		return "patientDetailsSearch";
	}

	@SkipValidation
	public String patientDetailsReset() {
		loadDefaultSearchInfo(getUserID());
		clearActionErrors();
		clearFieldErrors();
		clearErrorsAndMessages();
		getModel().setPatientTypeId((short) -1);
		getModel().setMcdReferenceId("");
		getModel().setPatientRegistrationId("");
		getModel().getPatientDetailForm().setFirstName("");
		getModel().getPatientDetailForm().setLastName("");
		getModel().getPatientDetailForm().setFathersName("");
		getModel().getPatientDetailForm().setGender(null);
		getModel().getPatientDetailForm().setBirthDateForm("");
		getModel().setRegistrationDtmFrm("");
		getModel().setYearDtmFrm("");
		getModel().setHospitalId("");
		hltPatientEnrollmentList = null;
		return "patientDetailsSearch";
	}

	private void getPatientRegistrationDetails(String userID,
			String patientRegistrationNumberSearch, Short patientTypeId)
			throws ServiceException {
		String methodName = "getPatientRegistrationDetails";
		Logger.logDebug(userID, CommonConstants.LOG_DEBUG, this.getClass(),
				methodName);
		String regisId,year;
		char[] ch = patientRegistrationNumberSearch.toCharArray();
		int asc;
		boolean isNum = true;
		for (char c : ch) {
			asc = (int) c;
			if (asc >= 48 && asc <= 57)
				isNum = true;
			else {
				isNum = false;
				break;
			}
		}
		if (isNum == true) {

			int reNumber = Integer.parseInt(patientRegistrationNumberSearch);
			BigDecimal no = new BigDecimal(reNumber);
			DecimalFormat df = new DecimalFormat("#00000000");
			
			
			//String regNu = df.format(no.doubleValue()).toString();
			// System.out.println(df.format(no.doubleValue()));
			hltPatientEnrollmentList = hospitalAdministrationBD
					.getPatientEnrollmentDetails(userID, df.format(no.doubleValue()).toString(), patientTypeId,null,null);
			setPatientRegistrationNumberSearch(null);
		} else
			hltPatientEnrollmentList = hospitalAdministrationBD
					.getPatientEnrollmentDetails(userID,
							patientRegistrationNumberSearch, patientTypeId,null,null);
	}

	private void setPatientDetails() {
		getModel().setCmnReferralAddress(new CmnAddressForm());
		getModel().setPatientDetailForm(new PatientDetailForm());
		getModel().getPatientDetailForm().setCmnPermanentAddress(
				new CmnAddressForm());
		getModel().getPatientDetailForm().setCmnPresentAddress(
				new CmnAddressForm());
		hltPatientEnrollment = hltPatientEnrollmentList.get(0);
		bindToForm(userID, getModel(), hltPatientEnrollment);
		if (ApplicationUtil.isNull(getModel().getOpdCardNumber())) {
			Set<HltPatientVisitRecord> hltPatientVisitRecordSet = hltPatientEnrollment
					.getHltPatientVisitRecord();
			if (hltPatientVisitRecordSet != null
					&& hltPatientVisitRecordSet.size() > 0) {
				String loginOfficeId = null;
				Timestamp dateOfVisit = null;
				if (!ApplicationUtil.isNull(getLoginUser())) {
					loginOfficeId = getLoginUser().getPrimaryOfficeId();
				}
				for (HltPatientVisitRecord hltPatientVisitRecord : hltPatientVisitRecordSet) {
					if (hltPatientVisitRecord.getOfficeId().equals(
							loginOfficeId)
							&& (dateOfVisit == null || dateOfVisit
									.before(hltPatientVisitRecord
											.getDateOfVisit()))) {
						getModel().setOpdCardNumber(
								hltPatientVisitRecord.getOpdCardNumber());
						dateOfVisit = hltPatientVisitRecord.getDateOfVisit();
					}
				}
			}
		}
		if (!ApplicationUtil.isNull(hltPatientEnrollment.getRegistrationDtm())) {
			getModel().setRegistrationDtmFrm(
					ApplicationUtil.getDateString(hltPatientEnrollment
							.getRegistrationDtm(), HealthConstants.DD_MM_YYYY));
		}

		if (!ApplicationUtil.isNull(hltPatientEnrollment.getHltCmnAddress())) {
			bindToForm(userID, getModel().getCmnReferralAddress(),
					hltPatientEnrollment.getHltCmnAddress());
		}
		HltPatientDetail hltPatientDetails = hltPatientEnrollment
				.getHltPatientDetail();
		if (!ApplicationUtil.isNull(hltPatientDetails)) {
			bindToForm(userID, getModel().getPatientDetailForm(),
					hltPatientDetails);
			if (!ApplicationUtil.isNull(hltPatientDetails.getHltCmnAddress1())) {
				bindToForm(userID, getModel().getPatientDetailForm()
						.getCmnPermanentAddress(), hltPatientDetails
						.getHltCmnAddress1());
			}
			if (!ApplicationUtil.isNull(hltPatientDetails.getHltCmnAddress2())) {
				bindToForm(userID, getModel().getPatientDetailForm()
						.getCmnPresentAddress(), hltPatientDetails
						.getHltCmnAddress2());
			}
			if (!ApplicationUtil.isNull(hltPatientDetails.getBirthDate())) {
				getModel().getPatientDetailForm().setBirthDateForm(
						ApplicationUtil.getDateString(hltPatientDetails
								.getBirthDate(), HealthConstants.DD_MM_YYYY));
			}
		}
	}

	@SkipValidation
	public String printPatientCard() {
		String methodName = "printPatientCard";
		String userID = getUserID();
		String returnPage = "printPatientCard";
		try {
			if (!ApplicationUtil.isNull(getModel().getPatientRegistrationId())) {
				getPatientRegistrationDetails(userID, getModel()
						.getPatientRegistrationId(), null);
				if (!ApplicationUtil.isNull(hltPatientEnrollmentList)
						&& !hltPatientEnrollmentList.isEmpty()) {
					if (hltPatientEnrollmentList.size() == 1) {
						setPatientDetails();
					}
				}
			}

			/*
			 * ; private String hospitalName; private String stateName;
			 */

			if (!ApplicationUtil.isNull(getModel())) {
				if (!ApplicationUtil.isNull(getModel().getPatientDetailForm())) {

					String patientNm = getModel().getPatientDetailForm()
							.getFirstName();
					if (!ApplicationUtil.isNull(getModel()
							.getPatientDetailForm().getMiddleName())) {
						patientNm = patientNm
								+ " "
								+ getModel().getPatientDetailForm()
										.getMiddleName();
					}
					if (!ApplicationUtil.isNull(getModel()
							.getPatientDetailForm().getLastName())) {
						patientNm = patientNm
								+ " "
								+ getModel().getPatientDetailForm()
										.getLastName();
					}
					setPatientFullName(patientNm);

					String genderCd = getModel().getPatientDetailForm()
							.getGender();
					if (!ApplicationUtil.isNull(genderCd)) {
						if (genderCd.equalsIgnoreCase("M")) {
							setSex("Male");
						} else if (genderCd.equalsIgnoreCase("F")) {
							setSex("Female");
						} else {
							setSex("Other");
						}
					}

					String issuedByName = getLoginUser().getFirstName();
					if (!ApplicationUtil.isNull(getLoginUser().getLastName())) {
						issuedByName = issuedByName + " "
								+ getLoginUser().getLastName();
					}
					setIssuedByName(issuedByName);

					String patientAddress = "";
					if (!ApplicationUtil.isNull(getModel()
							.getPatientDetailForm().getCmnPresentAddress())) {
						if (!ApplicationUtil.isNull(getModel()
								.getPatientDetailForm().getCmnPresentAddress()
								.getAddress1())) {
							patientAddress = getModel().getPatientDetailForm()
									.getCmnPresentAddress().getAddress1();
						}
						if (!ApplicationUtil.isNull(getModel()
								.getPatientDetailForm().getCmnPresentAddress()
								.getAddress2())) {
							patientAddress = patientAddress
									+ " "
									+ getModel().getPatientDetailForm()
											.getCmnPresentAddress()
											.getAddress2();
						}
					}
					setPatientAddress(patientAddress);

				}
			}
		} catch (ServiceException serviceException) {
			addActionError(""
					+ HealthConstants.HLT_YELLOW_FEVER_REGISTRATION_READ_ERROR
					+ ":: "
					+ getText(HealthConstants.HLT_PROBLEM_OCCURED_WHILE_READING));
			PresentationException presentationException = new PresentationException(
					serviceException,
					HealthConstants.HLT_YELLOW_FEVER_REGISTRATION_READ_ERROR,
					this.getClass(), methodName);
			Logger.logError(userID, presentationException);
			returnPage = "printPatientCard";
			// throw presentationException;
		} catch (Exception exception) {
			addActionError(""
					+ HealthConstants.HLT_YELLOW_FEVER_REGISTRATION_READ_ERROR
					+ ":: "
					+ getText(HealthConstants.HLT_PROBLEM_OCCURED_WHILE_READING));
			PresentationException presentationException = new PresentationException(
					exception,
					HealthConstants.HLT_YELLOW_FEVER_REGISTRATION_READ_ERROR,
					this.getClass(), methodName);
			Logger.logError(userID, presentationException);

			returnPage = "printPatientCard";
			// throw presentationException;
		} finally {
			loadDefaultInfo(userID);
		}
		return "printPatientCard";
	}

	@SkipValidation
	public String patientRegistrationSearch() {
		String methodName = "patientRegistrationSearch";
		String userID = getUserID();
		String loginOfficeId = null;
		boolean isNumSearchPresent=false;
		if (!ApplicationUtil.isNull(getLoginUser())) {
			loginOfficeId = getLoginUser().getPrimaryOfficeId();
		}
		String returnPage = "";
		Logger.logDebug(userID, CommonConstants.LOG_DEBUG, this.getClass(),
				methodName);

		Map<String, String> criteria = new HashMap<String, String>();

		try {

			if (ApplicationUtil.isNull(getPatientRegistrationNumberSearch())
					&& !ApplicationUtil.isNull(getModel().getPatientTypeId())
					&& getModel().getPatientTypeId() < 4) {
				if (ApplicationUtil.isNull(getModel().getMcdReferenceId())) {
					addActionError("Please enter MCD Reference ID and click on Search button");
					isList = "Y";
					return "patientDetailsSearch";
				} else {
					criteria.put("1", getModel().getMcdReferenceId().trim()
							.toUpperCase());
					getPatientRegistrationList(userID, criteria, getModel()
							.getPatientTypeId(), loginOfficeId,getModel().getRegFor());
				}
			} else {

				if (!ApplicationUtil.isNull(getModel()
						.getPatientRegistrationId())
						&& ApplicationUtil
								.isNull(getPatientRegistrationNumberSearch())) {
					setPatientRegistrationNumberSearch(getModel()
							.getPatientRegistrationId());
				}
				if (!ApplicationUtil
						.isNull(getPatientRegistrationNumberSearch())) {
					getPatientRegistrationDetails(userID,
							getPatientRegistrationNumberSearch(), getModel()
									.getPatientTypeId());
					if(!ApplicationUtil.isNull(getModel().getPatientDetailForm())){
					if(!ApplicationUtil.isNull(getModel().getPatientDetailForm().getGender()))
					{int i=0;
					for (HltPatientEnrollment hltPatientEnrollment : hltPatientEnrollmentList) {
						isNumSearchPresent=true;
						if(!hltPatientEnrollment.getHltPatientDetail().getGender().equals(getModel().getPatientDetailForm().getGender()))
						{
							hltPatientEnrollmentList.remove(i);
						}else{
						i++;}
					}
				}}
					if(!ApplicationUtil.isNull(getModel().getYearDtmFrm()))
					{
						int i=0;
						String iYear=""; 
					for (HltPatientEnrollment hltPatientEnrollment : hltPatientEnrollmentList) {
						isNumSearchPresent=true;
						iYear = hltPatientEnrollment.getRegistrationDtm().toString().substring(0,4); 
						if(!getModel().getYearDtmFrm().equals(iYear))
						{
							hltPatientEnrollmentList.remove(i);
						}else{
						i++;}
					}}
				}else if (!ApplicationUtil.isNull(getModel())
						&& !ApplicationUtil.isNull(getModel()
								.getPatientDetailForm())) {
					String firstNm = getModel().getPatientDetailForm()
							.getFirstName();
					String lastNm = getModel().getPatientDetailForm()
							.getLastName();
					String fathersNm = getModel().getPatientDetailForm()
							.getFathersName();
					String genderSr = getModel().getPatientDetailForm()
							.getGender();
					String birthDtmStr = getModel().getPatientDetailForm()
							.getBirthDateForm();
					String regDtmStr = getModel().getRegistrationDtmFrm();
					String yrDtmStr = getModel().getYearDtmFrm();
					String regAt = getModel().getHospitalId();
					String treatementType = getModel().getTreatmentType();
					if (!ApplicationUtil.isNull(firstNm) && !firstNm.isEmpty()) {
						criteria.put("2", firstNm.trim().toUpperCase());
					}
					if (!ApplicationUtil.isNull(lastNm) && !lastNm.isEmpty()) {
						criteria.put("3", lastNm.trim().toUpperCase());
					}
					if (!ApplicationUtil.isNull(fathersNm)
							&& !fathersNm.isEmpty()) {
						criteria.put("4", fathersNm.trim().toUpperCase());
					}
					if (!ApplicationUtil.isNull(genderSr)
							&& !genderSr.isEmpty()) {
						criteria.put("5", genderSr);
					}
					if (!ApplicationUtil.isNull(birthDtmStr)
							&& !birthDtmStr.isEmpty()) {
						criteria.put("6", birthDtmStr);
					}
					if (!ApplicationUtil.isNull(regDtmStr)
							&& !regDtmStr.isEmpty()) {
						criteria.put("7", regDtmStr);
					}
					if (!ApplicationUtil.isNull(yrDtmStr)
							&& !yrDtmStr.isEmpty()) {
						criteria.put("8", yrDtmStr);
					}
					if (!ApplicationUtil.isNull(regAt) && !regAt.isEmpty()) {
						criteria.put("9", regAt);
					}
					if (!ApplicationUtil.isNull(treatementType)
							&& !treatementType.isEmpty()) {
						criteria.put("10", treatementType);
					}
					if (!ApplicationUtil.isNull(getModel().getOpdCardNumber())
							&& !getModel().getOpdCardNumber().isEmpty()) {
						criteria.put("11", getModel().getOpdCardNumber());
					}
					if (!ApplicationUtil.isNull(getModel().getLastVisitDate())
							&& !getModel().getLastVisitDate().isEmpty()) {
						criteria.put("12", getModel().getLastVisitDate());
					}
					if (!criteria.isEmpty()) {
						getPatientRegistrationList(userID, criteria, getModel()
								.getPatientTypeId(), loginOfficeId,null);
					}
				}
			}

			if (!ApplicationUtil.isNull(hltPatientEnrollmentList)
					&& !hltPatientEnrollmentList.isEmpty()) {
				hltPatientEnrollment = hltPatientEnrollmentList.get(0);
				/*
				 * if
				 * (!hltPatientEnrollment.getHospitalId().equals(mcdApplicationUser
				 * .getPrimaryOfficeId())) { hltPatientEnrollmentList.clear();
				 * addActionError("Not authorised to view this patient");
				 * hltPatientEnrollment = new HltPatientEnrollment(); }
				 */
				short relationshipId=hltPatientEnrollment.getRelationshipId();
				if(!ApplicationUtil.isNull(relationshipId) && relationshipId!=0)
				{
					
					if(relationshipId==2)
					{
						hltPatientEnrollment.setRelationshipName("DAUGHTER");
					}
					else if(relationshipId==3)
					{
						hltPatientEnrollment.setRelationshipName("FATHER");
						
					}
					else if(relationshipId==4)
					{
						hltPatientEnrollment.setRelationshipName("MOTHER");
						
					}
					else if(relationshipId==1)
					{
						hltPatientEnrollment.setRelationshipName("BROTHER");
						
					}
					else if(relationshipId==5)
					{
						hltPatientEnrollment.setRelationshipName("SISTER");
						
					}
					else if(relationshipId==6)
					{
						hltPatientEnrollment.setRelationshipName("SON");
						
					}
					else if(relationshipId==7)
					{
						hltPatientEnrollment.setRelationshipName("WIFE");
						
					}
					else if(relationshipId==10)
					{
						hltPatientEnrollment.setRelationshipName("HUSBAND");
						
					}
					else if(relationshipId==9)
					{
						hltPatientEnrollment.setRelationshipName("FATHER-IN-LAW");
						
					}
					else if(relationshipId==8)
					{
						hltPatientEnrollment.setRelationshipName("MOTHER-IN-LAW");
						
					}
								
			getModel().setRealtionship(hltPatientEnrollment.getRelationshipName());
				}
				Set<HltPatientVisitRecord> hltPatientVisitRecordSet = hltPatientEnrollment
						.getHltPatientVisitRecord();
				if (hltPatientEnrollmentList.size() == 1) {
					boolean isPatientExpired = isPatientExpired(userID,
							hltPatientEnrollmentList.get(0)
									.getPatientEnrollmentGenId());
					boolean isOpdCardGeneratedForCurrentHospital = isOpdCardGeneratedForCurrentHospital(
							mcdApplicationUser.getPrimaryOfficeId(),
							hltPatientEnrollmentList.get(0)
									.getPatientEnrollmentGenId(),
							hltPatientVisitRecordSet);
					if (isPatientExpired) {
						addActionError("Patient has expired - To view details, Please go to View Patient History");
						hltPatientEnrollmentList.clear();
						setPatientRegistrationNumberSearch(null);
						returnPage = "patientDetailsSearch";
					}
					if (isOpdCardGeneratedForCurrentHospital == false) {
						addActionError("Opd Card Not generated for current hospital");
						hltPatientEnrollmentList.clear();
						setPatientRegistrationNumberSearch(null);
						returnPage = "patientDetailsSearch";
					} else if (hltPatientVisitRecordSet != null
							&& hltPatientVisitRecordSet.size() > 0) {
						setPatientDetails();
						setPatientRegistrationNumberSearch(hltPatientEnrollmentList
								.get(0).getPatientRegistrationId());
						setPatientCommonView();
						returnPage = "patientDetailsView";
					} else {
						// addActionMessage("No OPD card found !");
						returnPage = "patientDetailsSearch";
					}
				} else {

					hltPatientEnrollmentList = new PaginationHelper<HltPatientEnrollment>()
							.trimList(userID, hltPatientEnrollmentList,
									getModel(),
									HealthConstants.NO_OF_RECORDS_DISPLAY);
					returnPage = "patientDetailsSearch";
				}

			} else {
				if (criteria.isEmpty()&& !isNumSearchPresent
						&& ApplicationUtil
								.isNull(getPatientRegistrationNumberSearch())) {
					addActionError("Please enter any of the following options and click on Search button");
					isList = "Y";
				} else {
					// addActionError(getText("Please enter valid Patient Details"));
					isList = "N";
				}
				setPatientRegistrationNumberSearch(null);
				returnPage = "patientDetailsSearch";
			}

		} catch (ServiceException serviceException) {
			addActionError(""
					+ HealthConstants.HLT_YELLOW_FEVER_REGISTRATION_READ_ERROR
					+ ":: "
					+ getText(HealthConstants.HLT_PROBLEM_OCCURED_WHILE_READING));
			PresentationException presentationException = new PresentationException(
					serviceException,
					HealthConstants.HLT_YELLOW_FEVER_REGISTRATION_READ_ERROR,
					this.getClass(), methodName);
			Logger.logError(userID, presentationException);
			setPatientRegistrationNumberSearch(null);
			returnPage = "patientDetailsSearch";
			// throw presentationException;
		} catch (Exception exception) {
			addActionError(""
					+ HealthConstants.HLT_YELLOW_FEVER_REGISTRATION_READ_ERROR
					+ ":: "
					+ getText(HealthConstants.HLT_PROBLEM_OCCURED_WHILE_READING));
			PresentationException presentationException = new PresentationException(
					exception,
					HealthConstants.HLT_YELLOW_FEVER_REGISTRATION_READ_ERROR,
					this.getClass(), methodName);
			Logger.logError(userID, presentationException);
			setPatientRegistrationNumberSearch(null);
			returnPage = "patientDetailsSearch";
			// throw presentationException;
		} finally {
			loadDefaultInfo(userID);
			loadDefaultSearchInfo(userID);
		}
		Logger.logDebug(userID, CommonConstants.LOG_END, this.getClass(),
				methodName);
		return returnPage;
	}

	private boolean isOpdCardGeneratedForCurrentHospital(String userID,
			long patientEnrollmentGenId,
			Set<HltPatientVisitRecord> hltPatientVisitRecordSet) {
		List<String> officeIds = new ArrayList<String>();
		for (HltPatientVisitRecord hltPatientVisitRecord : hltPatientVisitRecordSet) {
			officeIds.add(hltPatientVisitRecord.getOfficeId());
		}
		if (officeIds.contains(userID)) {
			return true;
		} else {
			return false;
		}
	}

	@SkipValidation
	public String patientRegistrationSearchView() {
		String methodName = "patientRegistrationSearch";
		String returnPage = patientRegistrationSearch();
		String userID = getUserID();

		Map<String, String> criteria = new HashMap<String, String>();
		if (hltPatientEnrollmentList != null
				&& hltPatientEnrollmentList.size() > 0) {
			try {
				setPatientOpdCardExpired(isPatientOpdCardExpired(userID,
						hltPatientEnrollmentList.get(0)
								.getPatientEnrollmentGenId()));
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (isNoOpdCardFoud() == true
				|| (ApplicationUtil.isNull(getModel().getOpdCardNumber()) || getModel()
						.getOpdCardNumber().equals(""))) {
			addActionError("No OPD card found");
			return "patientDetailsSearch";
		}
		return "patientDetailsView";
	}

	private boolean isPatientOpdCardExpired(String userID,
			long patientEnrollmentGenId) throws ServiceException {
		String methodName = "isPatientOpdCardExpired";
		boolean isPatientOpdCardExpired = false;
		Logger.logDebug(userID, CommonConstants.LOG_DEBUG, this.getClass(),
				methodName);
		List<HltPatientVisitRecord> patientVisitRecordList = hospitalAdministrationBD
				.getPatientVisitRecordList(userID, patientEnrollmentGenId);
		if (patientVisitRecordList != null && patientVisitRecordList.size() > 0) {
			setNoOpdCardFoud(false);
			final Timestamp currentTimeStamp = new Timestamp(System
					.currentTimeMillis());
			// for (HltPatientVisitRecord hltPatientVisitRecord :
			// patientVisitRecordList) {
			HltPatientVisitRecord hltPatientVisitRecord = new HltPatientVisitRecord();
			hltPatientVisitRecord = patientVisitRecordList.get(0);
			isPatientOpdCardExpired = hltPatientVisitRecord.getCardExpiryDate()
					.before(currentTimeStamp);
			// }
		} else {
			setNoOpdCardFoud(true);
		}
		Logger.logDebug(userID, CommonConstants.LOG_END, this.getClass(),
				methodName);
		return isPatientOpdCardExpired;
	}

	public boolean isPatientExpired(String userID, long patientEnrollmentGenId)
			throws ServiceException {
		String methodName = "isPatientExpired";
		boolean isPatientExpired = false;
		Logger.logDebug(userID, CommonConstants.LOG_DEBUG, this.getClass(),
				methodName);
		isPatientExpired = hospitalAdministrationBD.isPatientExpired(userID,
				patientEnrollmentGenId);
		Logger.logDebug(userID, CommonConstants.LOG_END, this.getClass(),
				methodName);
		return isPatientExpired;
	}

	@SkipValidation
	public String viewPatientDetails() {
		loadDefaultSearchInfo(getUserID());
		return "viewpatientDetailsSearch";
	}

	private void getPatientRegistrationList(String userID,
			Map<String, String> patientRegistrationCriteria,
			Short patientTypeId, String loginOfficeId, String regFor) throws ServiceException {
		String methodName = "getPatientRegistrationList";
		Logger.logDebug(userID, CommonConstants.LOG_START, this.getClass(),
				methodName);
		hltPatientEnrollmentList = hospitalAdministrationBD
				.getPatientEnrollmentDetailsList(userID,
						patientRegistrationCriteria, patientTypeId,
						loginOfficeId, regFor,getModel().isFromGetRelationshipDetials());
		Logger.logDebug(userID, CommonConstants.LOG_END, this.getClass(),
				methodName);
	}

	@SkipValidation
	public String viewPatientSearch() {
		String methodName = "viewPatientSearch";
		String userID = getUserID();
		String returnPage = "";
		String loginOfficeId = null;
		if (!ApplicationUtil.isNull(getLoginUser())) {
			loginOfficeId = getLoginUser().getPrimaryOfficeId();
		}
		Logger.logDebug(userID, CommonConstants.LOG_START, this.getClass(),
				methodName);
		Map<String, String> criteria = new HashMap<String, String>();
		try {
			if (ApplicationUtil.isNull(getPatientRegistrationNumberSearch())
					&& !ApplicationUtil.isNull(getModel().getPatientTypeId())
					&& getModel().getPatientTypeId() > 0
					&& getModel().getPatientTypeId() < 4) {
				if (ApplicationUtil.isNull(getModel().getMcdReferenceId())) {
					addActionError("Please enter MCD Reference ID and click on Search button");
					isList = "Y";
					return "patientDetailsSearch";
				} else {
					criteria.put("1", getModel().getMcdReferenceId().trim()
							.toUpperCase());
					getPatientRegistrationList(userID, criteria, null,
							loginOfficeId,null);
				}
			} else {
				if (!ApplicationUtil.isNull(getModel()
						.getPatientRegistrationId())
						&& ApplicationUtil
								.isNull(getPatientRegistrationNumberSearch())) {
					setPatientRegistrationNumberSearch(getModel()
							.getPatientRegistrationId());
				}
				if (!ApplicationUtil
						.isNull(getPatientRegistrationNumberSearch())) {
					getPatientRegistrationDetails(userID,
							getPatientRegistrationNumberSearch(), getModel()
									.getPatientTypeId());

				} else if (!ApplicationUtil.isNull(getModel())
						&& !ApplicationUtil.isNull(getModel()
								.getPatientDetailForm())) {
					String firstNm = getModel().getPatientDetailForm()
							.getFirstName();
					String lastNm = getModel().getPatientDetailForm()
							.getLastName();
					String fathersNm = getModel().getPatientDetailForm()
							.getFathersName();
					String genderSr = getModel().getPatientDetailForm()
							.getGender();
					String birthDtmStr = getModel().getPatientDetailForm()
							.getBirthDateForm();
					String regDtmStr = getModel().getRegistrationDtmFrm();
					String yrDtmStr = getModel().getYearDtmFrm();
					String regAt = getModel().getHospitalId();

					if (!ApplicationUtil.isNull(firstNm) && !firstNm.isEmpty()) {
						criteria.put("2", firstNm.trim().toUpperCase());
					}
					if (!ApplicationUtil.isNull(lastNm) && !lastNm.isEmpty()) {
						criteria.put("3", lastNm.trim().toUpperCase());
					}
					if (!ApplicationUtil.isNull(fathersNm)
							&& !fathersNm.isEmpty()) {
						criteria.put("4", fathersNm.trim().toUpperCase());
					}
					if (!ApplicationUtil.isNull(genderSr)
							&& !genderSr.isEmpty()) {
						criteria.put("5", genderSr);
					}
					if (!ApplicationUtil.isNull(birthDtmStr)
							&& !birthDtmStr.isEmpty()) {
						criteria.put("6", birthDtmStr);
					}
					if (!ApplicationUtil.isNull(regDtmStr)
							&& !regDtmStr.isEmpty()) {
						criteria.put("7", regDtmStr);
					}
					if (!ApplicationUtil.isNull(yrDtmStr)
							&& !yrDtmStr.isEmpty()) {
						criteria.put("8", yrDtmStr);
					}
					if (!ApplicationUtil.isNull(regAt) && !regAt.isEmpty()) {
						criteria.put("9", regAt);
					}
					if (!criteria.isEmpty()) {
						getPatientRegistrationList(userID, criteria, null,
								loginOfficeId,null);
					}
				}
			}

			if (!ApplicationUtil.isNull(hltPatientEnrollmentList)
					&& !hltPatientEnrollmentList.isEmpty()) {
				if (hltPatientEnrollmentList.size() == 1) {
					setPatientRegistrationNumberSearch(hltPatientEnrollmentList
							.get(0).getPatientRegistrationId());
					getModel().setPatientTypeId(
							hltPatientEnrollmentList.get(0).getPatientTypeId());
					returnPage = "historypatientDetailsView";
				} else {
					hltPatientEnrollmentList = new PaginationHelper<HltPatientEnrollment>()
							.trimList(userID, hltPatientEnrollmentList,
									getModel(),
									HealthConstants.NO_OF_RECORDS_DISPLAY);
					returnPage = "viewpatientDetailsSearch";
				}

			} else {
				if (criteria.isEmpty()
						&& ApplicationUtil
								.isNull(getPatientRegistrationNumberSearch())) {
					addActionError("Please enter any of the following options and click on Search button");
					isList = "Y";
				} else {
					addActionError(getText("Please enter valid Patient Details"));
					isList = "N";
				}
				setPatientRegistrationNumberSearch(null);
				returnPage = "viewpatientDetailsSearch";
			}

		} catch (ServiceException serviceException) {
			addActionError(""
					+ HealthConstants.HLT_YELLOW_FEVER_REGISTRATION_READ_ERROR
					+ ":: "
					+ getText(HealthConstants.HLT_PROBLEM_OCCURED_WHILE_READING));
			PresentationException presentationException = new PresentationException(
					serviceException,
					HealthConstants.HLT_YELLOW_FEVER_REGISTRATION_READ_ERROR,
					this.getClass(), methodName);
			Logger.logError(userID, presentationException);
			returnPage = "viewpatientDetailsSearch";
			// throw presentationException;
		} catch (Exception exception) {
			addActionError(""
					+ HealthConstants.HLT_YELLOW_FEVER_REGISTRATION_READ_ERROR
					+ ":: "
					+ getText(HealthConstants.HLT_PROBLEM_OCCURED_WHILE_READING));
			PresentationException presentationException = new PresentationException(
					exception,
					HealthConstants.HLT_YELLOW_FEVER_REGISTRATION_READ_ERROR,
					this.getClass(), methodName);
			Logger.logError(userID, presentationException);

			returnPage = "viewpatientDetailsSearch";
			// throw presentationException;
		} finally {
			loadDefaultInfo(userID);
			loadDefaultSearchInfo(userID);
		}
		return returnPage;
	}

	@SkipValidation
	public String updatePatientRegistrationDetails() {
		String methodName = "updatePatientRegistrationDetails";
		String userID = getUserID();
		String returnPage = "";
		Logger.logDebug(userID, CommonConstants.LOG_START, this.getClass(),
				methodName);
		try {
			if (!ApplicationUtil.isNull(getPatientRegistrationNumberSearch())) {
				getPatientRegistrationDetails(userID,
						getPatientRegistrationNumberSearch(), null);
				if (hltPatientEnrollmentList.size() == 1) {
					setPatientDetails();
				}
			}
			if (!ApplicationUtil.isNull(hltPatientEnrollmentList)
					&& !hltPatientEnrollmentList.isEmpty()) {
				getModel().setModule("TB");
				returnPage = "patientDetailsEdit";
			}
		} catch (ServiceException serviceException) {
			addActionError(""
					+ HealthConstants.HLT_YELLOW_FEVER_REGISTRATION_READ_ERROR
					+ ":: "
					+ getText(HealthConstants.HLT_PROBLEM_OCCURED_WHILE_READING));
			PresentationException presentationException = new PresentationException(
					serviceException,
					HealthConstants.HLT_YELLOW_FEVER_REGISTRATION_READ_ERROR,
					this.getClass(), methodName);
			Logger.logError(userID, presentationException);
			returnPage = "patientDetailsSearch";
			// throw presentationException;
		} catch (Exception exception) {
			addActionError(""
					+ HealthConstants.HLT_YELLOW_FEVER_REGISTRATION_READ_ERROR
					+ ":: "
					+ getText(HealthConstants.HLT_PROBLEM_OCCURED_WHILE_READING));
			PresentationException presentationException = new PresentationException(
					exception,
					HealthConstants.HLT_YELLOW_FEVER_REGISTRATION_READ_ERROR,
					this.getClass(), methodName);
			Logger.logError(userID, presentationException);

			returnPage = "patientDetailsSearch";
			// throw presentationException;
		} finally {
			loadDefaultInfo(userID);
		}
		return returnPage;
	}

	@SkipValidation
	public String addTBPatientRegistration() {
		String userID = getUserID();
		String methodName = "addPatientRegistration";
		String returnPage = "addPatientRegistration";
		mcdApplicationUser = getLoginUser();
		if (!ApplicationUtil.isNull(mcdApplicationUser)) {
			getModel().setHospitalId(mcdApplicationUser.getPrimaryOfficeId());
		}
		loadDefaultInfo(userID);
		getModel().setRegistrationType("TB");
		if (ApplicationUtil.isNull(getModel().getIsPatientRegistered())) {
			returnPage = "patientRegistered";
			return returnPage;
		} else {
			return returnPage;
		}
		//return "addPatientRegistration";
	}

	@SkipValidation
	public String addMCPatientRegistration() {
		String userID = getUserID();
		String methodName = "addPatientRegistration";
		String returnPage = "addPatientRegistration";
		mcdApplicationUser = getLoginUser();
		if (!ApplicationUtil.isNull(mcdApplicationUser)) {
			getModel().setHospitalId(mcdApplicationUser.getPrimaryOfficeId());
		}
		loadDefaultInfo(userID);
		getModel().setRegistrationType("MC");
		if (ApplicationUtil.isNull(getModel().getIsPatientRegistered())) {
			returnPage = "patientRegistered";
			return returnPage;
		} else {
			return returnPage;
		}

	}

	@Validations(requiredStrings = {
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "patientDetailForm.firstName", message = "${getText('health.hospitaladmin.newenrollment.firstname.label')} ${getText('health.common.inputrequired')}"),
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "patientDetailForm.fathersName", message = "${getText('health.hospitaladmin.newenrollment.fathername.label')} ${getText('health.common.inputrequired')}"),
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "patientDetailForm.gender", message = "${getText('health.hospitaladmin.newenrollment.gender.label')} ${getText('health.common.inputrequired')}"),
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "hospitalId", message = "${getText('health.hospitaladmin.newenrollment.registeredat.label')} ${getText('health.common.inputrequired')}"),
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "patientDetailForm.cmnPresentAddress.state", message = "${getText('health.hospitaladmin.newenrollment.presentaddress.state.label')} ${getText('health.common.inputrequired')}"),
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "patientDetailForm.cmnPresentAddress.address1", message = "${getText('health.hospitaladmin.newenrollment.presentaddress.addressline1.label')} ${getText('health.common.inputrequired')}") }, regexFields = {
			@RegexFieldValidator(type = ValidatorType.FIELD, fieldName = "patientDetailForm.firstName", expression = "[a-zA-Z.\\s]*", message = "Only Alphabets and dot(.) are allowed Characters"),
			@RegexFieldValidator(type = ValidatorType.FIELD, fieldName = "patientDetailForm.middleName", expression = "[a-zA-Z.\\s]*", message = "Only Alphabets and dot(.) are allowed Characters"),
			@RegexFieldValidator(type = ValidatorType.FIELD, fieldName = "patientDetailForm.lastName", expression = "[a-zA-Z.\\s]*", message = "Only Alphabets and dot(.) are allowed Characters"),
			@RegexFieldValidator(type = ValidatorType.FIELD, fieldName = "patientDetailForm.fathersName", expression = "[a-zA-Z.\\s]*", message = "Only Alphabets and dot(.) are allowed Characters"),
			@RegexFieldValidator(type = ValidatorType.FIELD, fieldName = "patientDetailForm.husbandsName", expression = "[a-zA-Z.\\s]*", message = "Only Alphabets and dot(.) are allowed Characters"),
			@RegexFieldValidator(type = ValidatorType.FIELD, fieldName = "patientDetailForm.mothersName", expression = "[a-zA-Z.\\s]*", message = "Only Alphabets and dot(.) are allowed Characters"),
			@RegexFieldValidator(type = ValidatorType.FIELD, fieldName = "patientDetailForm.cmnPresentAddress.city", expression = "[a-zA-Z\\s]*", message = "Should Contain Only Alphabets"),
			@RegexFieldValidator(type = ValidatorType.FIELD, fieldName = "cmnReferralAddress.city", expression = "[a-zA-Z\\s]*", message = "Should Contain Only Alphabets"),
			@RegexFieldValidator(type = ValidatorType.FIELD, fieldName = "patientDetailForm.cmnPermanentAddress.city", expression = "[a-zA-Z\\s]*", message = "Should Contain Only Alphabets"),
			@RegexFieldValidator(type = ValidatorType.FIELD, fieldName = "patientDetailForm.ageYear", expression = "[0-9\\s]*", message = "Should contain Numerics only."),
			@RegexFieldValidator(type = ValidatorType.FIELD, fieldName = "patientDetailForm.ageMonth", expression = "[0-9\\s]*", message = "Should contain Numerics only."),
			@RegexFieldValidator(type = ValidatorType.FIELD, fieldName = "patientDetailForm.ageYear", expression = "[0-9\\s]*", message = "Should contain Numerics only."),
			@RegexFieldValidator(type = ValidatorType.FIELD, fieldName = "patientDetailForm.stdCode", expression = "[0-9\\s]*", message = "Should contain Numerics only.") }

	)
	public String savePatientRegistration() {
		String methodName = "savePatientRegistration";
		String userID = getUserID(), returnPage = "", requestId = "";
		// loadDefaultInfo(userID);
		Logger.logInfo(userID, CommonConstants.LOG_DEBUG, this.getClass(),
				methodName, "RegistrationType >> "
						+ getModel().getRegistrationType());
		try {
			if (ApplicationUtil.isNull(getModel().getPatientDetailForm()
					.getMaritalStatus())) {
				getModel().getPatientDetailForm().setMaritalStatus("N");
			}
			if (ApplicationUtil.isNull(getModel().getPatientDetailForm()
					.getIsSameAsPresentAddress())) {
				getModel().getPatientDetailForm()
						.setIsSameAsPresentAddress("N");
			}
			if (ApplicationUtil.isNull(getModel().getIsPatientReferred())) {
				getModel().setIsPatientReferred("N");
			}
			if (ApplicationUtil.isNull(getModel().getIsTbConfirmed())
					|| !getModel().getIsTbConfirmed().trim().equals("Y")) {
				getModel().setIsTbConfirmed("N");
			}
			HltPatientEnrollment hltPatientEnrollment = new HltPatientEnrollment();
			bindToVO(userID, hltPatientEnrollment, getModel());
			if(!ApplicationUtil.isNull(getModel().getRelationship()))
			{
				String trimRelationship=getModel().getRelationship().substring(getModel().getRelationship().indexOf("(")+1, getModel().getRelationship().lastIndexOf(")"));
				getModel().setRealtionship(trimRelationship);
				if(trimRelationship.equals("DAUGHTER"))
				{
					hltPatientEnrollment.setRelationship((short)2);
				}
				else if(trimRelationship.equals("FATHER"))
				{
					hltPatientEnrollment.setRelationship((short)3);
				}
				else if(trimRelationship.equals("MOTHER"))
				{
					hltPatientEnrollment.setRelationship((short)4);
				}
				else if(trimRelationship.equals("BROTHER"))
				{
					hltPatientEnrollment.setRelationship((short)1);
				}
				else if(trimRelationship.equals("SISTER"))
				{
					hltPatientEnrollment.setRelationship((short)5);
				}
				else if(trimRelationship.equals("SON"))
				{
					hltPatientEnrollment.setRelationship((short)6);
				}
				else if(trimRelationship.equals("WIFE"))
				{
					hltPatientEnrollment.setRelationship((short)7);
				}
				else if(trimRelationship.equals("HUSBAND"))
				{
					hltPatientEnrollment.setRelationship((short)10);
				}
				else if(trimRelationship.equals("FATHER-IN-LAW"))
				{
					hltPatientEnrollment.setRelationship((short)9);
				}
				else if(trimRelationship.equals("MOTHER-IN-LAW"))
				{
					hltPatientEnrollment.setRelationship((short)8);
				}
				
			}
			
			java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(System
					.currentTimeMillis());
			hltPatientEnrollment.setRegistrationDtm(currentTimestamp);
			hltPatientEnrollment.setRegisteredBy(userID);
			if (!ApplicationUtil.isNull(getModel().getIsPatientReferred())
					&& getModel().getIsPatientReferred().equals("Y")) {
				hltPatientEnrollment.setHltCmnAddress(new HltCmnAddress());
				bindToVO(userID, hltPatientEnrollment.getHltCmnAddress(),
						getModel().getCmnReferralAddress());
			} else {
				hltPatientEnrollment.setReferredFrom(null);
				hltPatientEnrollment.setReferredRemark(null);
			}
			hltPatientEnrollment.setHltPatientDetail(new HltPatientDetail());
			bindToVO(userID, hltPatientEnrollment.getHltPatientDetail(),
					getModel().getPatientDetailForm());
			if (!ApplicationUtil.isNull(getModel().getPatientDetailForm()
					.getBirthDateForm())) {
				hltPatientEnrollment.getHltPatientDetail().setBirthDate(
						ApplicationUtil.getDate(getModel()
								.getPatientDetailForm().getBirthDateForm()));
			}
			hltPatientEnrollment.getHltPatientDetail().setHltCmnAddress2(
					new HltCmnAddress());
			bindToVO(userID, hltPatientEnrollment.getHltPatientDetail()
					.getHltCmnAddress2(), getModel().getPatientDetailForm()
					.getCmnPresentAddress());
			if (getModel().getPatientDetailForm().getIsSameAsPresentAddress() != null
					&& getModel().getPatientDetailForm()
							.getIsSameAsPresentAddress().equals("Y")) {
				hltPatientEnrollment.getHltPatientDetail().setHltCmnAddress1(
						hltPatientEnrollment.getHltPatientDetail()
								.getHltCmnAddress2());
			} else {
				hltPatientEnrollment.getHltPatientDetail().setHltCmnAddress1(
						new HltCmnAddress());
				bindToVO(userID, hltPatientEnrollment.getHltPatientDetail()
						.getHltCmnAddress1(), getModel().getPatientDetailForm()
						.getCmnPermanentAddress());
			}

			requestId = hospitalAdministrationBD.savePatientEnrollment(userID,
					hltPatientEnrollment);
			returnPage = "success";
			addActionMessage("Patient Registration Successful !");
			getModel().setPatientRegistrationId("" + requestId);
			if (getModel().getPatientTypeId() > 0
					&& getModel().getPatientTypeId() < 4) {
				setValidMCDRefrence("Y");
			} else {
				setValidMCDRefrence("N");
			}
			boolean genderIsFemale = false;
			if (!ApplicationUtil.isNull(getModel().getPatientDetailForm())
					&& !ApplicationUtil.isNull(getModel()
							.getPatientDetailForm().getGender())
					&& getModel().getPatientDetailForm().getGender()
							.equals("F")) {
				genderIsFemale = true;
			}
			// boolean isUpdated = false;
			if (genderIsFemale) {
				DMSThreadPool threadPool = new DMSThreadPool(1);
				threadPool.runTask(new PatientEnrollmentThread(userID,
						getModel(), requestId, currentTimestamp));

				// StringBuffer insertValues = new StringBuffer();
				// String dateOfBirth =
				// getModel().getPatientDetailForm().getBirthDateForm();
				// if(!ApplicationUtil.isNull(getModel().getPatientDetailForm())){
				// dateOfBirth =
				// ""+ApplicationUtil.convertStringDateToSQLDate(dateOfBirth);
				// }
				// insertValues.append(getModel().getPatientTypeId()+" ,'"+getModel().getHospitalId()+"' ,'"+getModel().getPatientDetailForm().getFirstName()+"', ");
				// insertValues.append("'"+getModel().getPatientDetailForm().getMiddleName()+"','"+getModel().getPatientDetailForm().getLastName()+"', ");
				// insertValues.append("'"+getModel().getPatientDetailForm().getFathersName()+"','"+getModel().getPatientDetailForm().getMothersName()+"', ");
				// insertValues.append("'"+getModel().getPatientDetailForm().getGender()+"','"+getModel().getPatientDetailForm().getMaritalStatus()+"', ");
				// insertValues.append("'"+getModel().getPatientDetailForm().getHusbandsName()+"', ");
				// if(!ApplicationUtil.isNull(dateOfBirth)){
				// insertValues.append(" '"+dateOfBirth+"', ");
				// }else{
				// insertValues.append(" null, ");
				// }
				//				
				// insertValues.append(" "+getModel().getPatientDetailForm().getAgeDay()+" ,"+getModel().getPatientDetailForm().getAgeMonth()+" , ");
				// insertValues.append(" "+getModel().getPatientDetailForm().getAgeYear()+" ,"+getModel().getPatientDetailForm().getOccupationId()+" , ");
				// insertValues.append(" "+getModel().getPatientDetailForm().getEducationId()+" ,"+getModel().getPatientDetailForm().getReligion()+" , ");
				// insertValues.append("'"+getModel().getPatientDetailForm().getReligionOther()+"',"+getModel().getPatientDetailForm().getCategory()+", ");
				// insertValues.append("'"+getModel().getPatientDetailForm().getCategoryOther()+"', ");
				// if(!ApplicationUtil.isNull(getModel().getPatientDetailForm().getCmnPresentAddress())){
				// insertValues.append("'"+getModel().getPatientDetailForm().getCmnPresentAddress().getAddress1()+"','"+getModel().getPatientDetailForm().getCmnPresentAddress().getAddress2()+"', ");
				// insertValues.append(" "+getModel().getPatientDetailForm().getCmnPresentAddress().getState()+" , ");
				// insertValues.append("'"+getModel().getPatientDetailForm().getCmnPresentAddress().getCity()+"','"+getModel().getPatientDetailForm().getCmnPresentAddress().getZoneId()+"', ");
				// insertValues.append("'"+getModel().getPatientDetailForm().getCmnPresentAddress().getWardId()+"','"+getModel().getPatientDetailForm().getCmnPresentAddress().getColonyId()+"', ");
				// insertValues.append("'"+getModel().getPatientDetailForm().getCmnPresentAddress().getDistrictId()+"','"+getModel().getPatientDetailForm().getCmnPresentAddress().getPinCode()+"', ");
				// }else{
				// insertValues.append(" null,null,0,null,null,null,null,null,null, ");
				// }
				// insertValues.append("'"+getModel().getPatientDetailForm().getIsSameAsPresentAddress()+"', ");
				// if(!ApplicationUtil.isNull(getModel().getPatientDetailForm().getIsSameAsPresentAddress())
				// &&
				// !getModel().getPatientDetailForm().getIsSameAsPresentAddress().equals("Y")){
				// if(!ApplicationUtil.isNull(getModel().getPatientDetailForm().getCmnPermanentAddress())){
				// insertValues.append("'"+getModel().getPatientDetailForm().getCmnPermanentAddress().getAddress1()+"','"+getModel().getPatientDetailForm().getCmnPermanentAddress().getAddress2()+"', ");
				// insertValues.append(" "+getModel().getPatientDetailForm().getCmnPermanentAddress().getState()+" , ");
				// insertValues.append("'"+getModel().getPatientDetailForm().getCmnPermanentAddress().getCity()+"','"+getModel().getPatientDetailForm().getCmnPermanentAddress().getZoneId()+"', ");
				// insertValues.append("'"+getModel().getPatientDetailForm().getCmnPermanentAddress().getWardId()+"','"+getModel().getPatientDetailForm().getCmnPermanentAddress().getColonyId()+"', ");
				// insertValues.append("'"+getModel().getPatientDetailForm().getCmnPermanentAddress().getDistrictId()+"','"+getModel().getPatientDetailForm().getCmnPermanentAddress().getPinCode()+"', ");
				// }else{
				// insertValues.append(" null,null,0,null,null,null,null,null,null, ");
				// }
				// }else{
				// insertValues.append(" null,null,0,null,null,null,null,null,null, ");
				// }
				// insertValues.append("'"+getModel().getPatientDetailForm().getStdCode()+"', ");
				// insertValues.append("'"+getModel().getPatientDetailForm().getLandLine()+"', ");
				// insertValues.append("'"+getModel().getPatientDetailForm().getMobile()+"', ");
				// insertValues.append("'"+getModel().getIsPatientReferred()+"', ");
				// if(!ApplicationUtil.isNull(getModel().getIsPatientReferred())
				// && getModel().getIsPatientReferred().equals("Y")){
				// insertValues.append("'"+getModel().getReferredFrom()+"', ");
				// insertValues.append("'"+getModel().getReferredFromOther()+"', ");
				// insertValues.append("'"+getModel().getReferredRemark()+"', ");
				// if(!ApplicationUtil.isNull(getModel().getCmnReferralAddress())){
				// insertValues.append("'"+getModel().getCmnReferralAddress().getAddress1()+"','"+getModel().getCmnReferralAddress().getAddress2()+"', ");
				// insertValues.append(" "+getModel().getCmnReferralAddress().getState()+" , ");
				// insertValues.append("'"+getModel().getCmnReferralAddress().getCity()+"','"+getModel().getCmnReferralAddress().getPinCode()+"', ");
				// }else{
				// insertValues.append(" null,null,0,null,null, ");
				// }
				// }else{
				// insertValues.append(" null,null,null,null,null,0,null,null, ");
				// }
				//				
				// insertValues.append("'"+requestId+"', ");
				// insertValues.append("'"+getModel().getRegistrationType()+"', ");
				//				
				// insertValues.append(" '"+currentTimestamp+"', ");
				//				
				// insertValues.append("'"+userID+"', ");
				// if(ApplicationUtil.isNull(getModel().getRegFor())){
				// insertValues.append(" "+0+", ");
				// }else{
				// insertValues.append(" "+getModel().getRegFor()+", ");
				// }
				// if(ApplicationUtil.isNull(getModel().getRelationship())){
				// insertValues.append(" "+0+", ");
				// }else{
				// insertValues.append(" "+getModel().getRelationship()+", ");
				// }
				//				
				// insertValues.append(" "+getModel().getEmployeeName());
				//				
				// String regStr = requestId;
				// if(requestId == null){
				// regStr = "";
				// }
				// String[] temp;
				//				 
				// String delimiter = "/";
				// temp = regStr.split(delimiter);
				// System.out.println(temp.length);
				// int year = 2000;
				// int regNo = 0;
				// if(temp.length > 3){
				// year += Integer.parseInt(temp[2]);
				// regNo = Integer.parseInt(temp[3].replaceAll("^0*", ""));
				// }
				//				 
				// insertValues.append(" , "+year + ", "+regNo +" ");
				//				
				// isUpdated = insertIntoMcwis(userID,insertValues.toString());
				// String mcwisInsert = "N";
				// if(isUpdated){
				// mcwisInsert = "Y";
				// }
				// hospitalAdministrationBD.updateMcwisPatientEnrollment(userID,requestId,mcwisInsert);
			}

		} catch (ServiceException serviceException) {
			addActionError("" + HealthConstants.HLT_NEWENROLLMENT_SAVE_ERROR
					+ ":: "
					+ getText(HealthConstants.HLT_PROBLEM_OCCURED_WHILE_SAVING));
			PresentationException presentationException = new PresentationException(
					serviceException,
					HealthConstants.HLT_NEWENROLLMENT_SAVE_ERROR, this
							.getClass(), methodName);
			Logger.logError(userID, HealthConstants.HLT_NEWENROLLMENT_SAVE_ERROR, this.getClass(), "", "Problem occurred While Saving");
			returnPage = "addPatientRegistration";
			// throw presentationException;
		} catch (Exception exception) {
			addActionError("" + HealthConstants.HLT_NEWENROLLMENT_SAVE_ERROR
					+ ":: "
					+ getText(HealthConstants.HLT_PROBLEM_OCCURED_WHILE_SAVING));
			PresentationException presentationException = new PresentationException(
					exception, HealthConstants.HLT_NEWENROLLMENT_SAVE_ERROR,
					this.getClass(), methodName);
			Logger.logError(userID, HealthConstants.HLT_NEWENROLLMENT_SAVE_ERROR, this.getClass(), "", "Problem occurred While Saving");
			returnPage = "addPatientRegistration";
			// throw presentationException;
		} finally {
			loadDefaultInfo(userID);
		}
		return returnPage;

	}
	
	
	@SkipValidation
	public String printPatientCardWithoutLogo() {
		String methodName = "printPatientCard";
		String userID = getUserID();
		String returnPage = "printPatientCardWithoutLogo";
		try {
			if (!ApplicationUtil.isNull(getModel().getPatientRegistrationId())) {
				getPatientRegistrationDetails(userID, getModel()
						.getPatientRegistrationId(), null);
				if (!ApplicationUtil.isNull(hltPatientEnrollmentList)
						&& !hltPatientEnrollmentList.isEmpty()) {
					if (hltPatientEnrollmentList.size() == 1) {
						setPatientDetails();
					}
				}
			}

			/*
			 * ; private String hospitalName; private String stateName;
			 */

			if (!ApplicationUtil.isNull(getModel())) {
				if (!ApplicationUtil.isNull(getModel().getPatientDetailForm())) {

					String patientNm = getModel().getPatientDetailForm()
							.getFirstName();
					if (!ApplicationUtil.isNull(getModel()
							.getPatientDetailForm().getMiddleName())) {
						patientNm = patientNm
								+ " "
								+ getModel().getPatientDetailForm()
										.getMiddleName();
					}
					if (!ApplicationUtil.isNull(getModel()
							.getPatientDetailForm().getLastName())) {
						patientNm = patientNm
								+ " "
								+ getModel().getPatientDetailForm()
										.getLastName();
					}
					setPatientFullName(patientNm);

					String genderCd = getModel().getPatientDetailForm()
							.getGender();
					if (!ApplicationUtil.isNull(genderCd)) {
						if (genderCd.equalsIgnoreCase("M")) {
							setSex("Male");
						} else if (genderCd.equalsIgnoreCase("F")) {
							setSex("Female");
						} else {
							setSex("Other");
						}
					}

					String issuedByName = getLoginUser().getFirstName();
					if (!ApplicationUtil.isNull(getLoginUser().getLastName())) {
						issuedByName = issuedByName + " "
								+ getLoginUser().getLastName();
					}
					setIssuedByName(issuedByName);

					String patientAddress = "";
					if (!ApplicationUtil.isNull(getModel()
							.getPatientDetailForm().getCmnPresentAddress())) {
						if (!ApplicationUtil.isNull(getModel()
								.getPatientDetailForm().getCmnPresentAddress()
								.getAddress1())) {
							patientAddress = getModel().getPatientDetailForm()
									.getCmnPresentAddress().getAddress1();
						}
						if (!ApplicationUtil.isNull(getModel()
								.getPatientDetailForm().getCmnPresentAddress()
								.getAddress2())) {
							patientAddress = patientAddress
									+ " "
									+ getModel().getPatientDetailForm()
											.getCmnPresentAddress()
											.getAddress2();
						}
					}
					setPatientAddress(patientAddress);

				}
			}
		} catch (ServiceException serviceException) {
			addActionError(""
					+ HealthConstants.HLT_YELLOW_FEVER_REGISTRATION_READ_ERROR
					+ ":: "
					+ getText(HealthConstants.HLT_PROBLEM_OCCURED_WHILE_READING));
			PresentationException presentationException = new PresentationException(
					serviceException,
					HealthConstants.HLT_YELLOW_FEVER_REGISTRATION_READ_ERROR,
					this.getClass(), methodName);
			Logger.logError(userID, presentationException);
			returnPage = "printPatientCard";
			// throw presentationException;
		} catch (Exception exception) {
			addActionError(""
					+ HealthConstants.HLT_YELLOW_FEVER_REGISTRATION_READ_ERROR
					+ ":: "
					+ getText(HealthConstants.HLT_PROBLEM_OCCURED_WHILE_READING));
			PresentationException presentationException = new PresentationException(
					exception,
					HealthConstants.HLT_YELLOW_FEVER_REGISTRATION_READ_ERROR,
					this.getClass(), methodName);
			Logger.logError(userID, presentationException);

			returnPage = "printPatientCard";
			// throw presentationException;
		} finally {
			loadDefaultInfo(userID);
		}
		return "printPatientCardWithoutLogo";
	}
	

	public String updatePatientRegistration() {
		String methodName = "updatePatientRegistration";
		String userID = getUserID();
		loadDefaultInfo(userID);
		Logger.logInfo(userID, CommonConstants.LOG_DEBUG, this.getClass(),
				methodName, "RegistrationType >>>>>>:::"
						+ getModel().getRegistrationType());
		String returnPage = "";
		String requestId = "";
		Logger.logDebug(userID, CommonConstants.LOG_DEBUG, this.getClass(),
				methodName);
		try {
			HltPatientEnrollment hltPatientEnrollment = new HltPatientEnrollment();
			if (getModel().getPatientEnrollmentGenId() > 0) {
				hltPatientEnrollment = hospitalAdministrationBD
						.getHltPatientEnrollmentDetails(userID, getModel()
								.getPatientEnrollmentGenId());
			}
			if (ApplicationUtil.isNull(getModel().getIsTbConfirmed())
					|| !getModel().getIsTbConfirmed().trim().equals("Y")) {
				getModel().setIsTbConfirmed("N");
			}
			bindToVO(userID, hltPatientEnrollment, getModel());
			Calendar calendar = Calendar.getInstance();
			java.util.Date now = calendar.getTime();
			java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now
					.getTime());
			// hltPatientEnrollment.setRegistrationDtm(currentTimestamp);
			hltPatientEnrollment.setRegisteredBy(userID);
			if (!ApplicationUtil.isNull(getModel().getIsTbConfirmed())
					&& getModel().getIsTbConfirmed().trim().equals("")) {
				getModel().setIsTbConfirmed(null);
			}
			if (!ApplicationUtil.isNull(getModel().getIsPatientReferred())
					&& getModel().getIsPatientReferred().equals("Y")) {
				hltPatientEnrollment.setHltCmnAddress(new HltCmnAddress());
				bindToVO(userID, hltPatientEnrollment.getHltCmnAddress(),
						getModel().getCmnReferralAddress());
			} else {
				hltPatientEnrollment.setReferredFrom(null);
				hltPatientEnrollment.setReferredRemark(null);
			}
			hltPatientEnrollment.setHltPatientDetail(new HltPatientDetail());
			bindToVO(userID, hltPatientEnrollment.getHltPatientDetail(),
					getModel().getPatientDetailForm());
			if (!ApplicationUtil.isNull(getModel().getPatientDetailForm()
					.getBirthDateForm())) {
				hltPatientEnrollment.getHltPatientDetail().setBirthDate(
						ApplicationUtil.getDate(getModel()
								.getPatientDetailForm().getBirthDateForm()));
			}
			hltPatientEnrollment.getHltPatientDetail().setHltCmnAddress2(
					new HltCmnAddress());
			bindToVO(userID, hltPatientEnrollment.getHltPatientDetail()
					.getHltCmnAddress2(), getModel().getPatientDetailForm()
					.getCmnPresentAddress());
			if (getModel().getPatientDetailForm().getIsSameAsPresentAddress() != null
					&& getModel().getPatientDetailForm()
							.getIsSameAsPresentAddress().equals("Y")) {
				hltPatientEnrollment.getHltPatientDetail().setHltCmnAddress1(
						hltPatientEnrollment.getHltPatientDetail()
								.getHltCmnAddress2());
			} else {
				hltPatientEnrollment.getHltPatientDetail().setHltCmnAddress1(
						new HltCmnAddress());
				if (getModel().getPatientDetailForm().getCmnPresentAddress() != null
						&& getModel().getPatientDetailForm()
								.getCmnPermanentAddress() != null) {
					if (getModel().getPatientDetailForm()
							.getCmnPresentAddress().getCmnAddressId() == getModel()
							.getPatientDetailForm().getCmnPermanentAddress()
							.getCmnAddressId()) {
						getModel().getPatientDetailForm()
								.getCmnPermanentAddress().setCmnAddressId(0);
					}
				}
				bindToVO(userID, hltPatientEnrollment.getHltPatientDetail()
						.getHltCmnAddress1(), getModel().getPatientDetailForm()
						.getCmnPermanentAddress());
			}

			requestId = hospitalAdministrationBD.updatePatientEnrollment(
					userID, hltPatientEnrollment);
			returnPage = "updatesuccess";
			// addActionMessage(""+requestId);Patient Details updated
			// successfully
			addActionMessage("Patient Details Updated Successfully !");
			getModel().setPatientRegistrationId("" + requestId);

			if (getModel().getPatientTypeId() > 0
					&& getModel().getPatientTypeId() < 4) {
				setValidMCDRefrence("Y");
			} else {
				setValidMCDRefrence("N");
			}
		} catch (ServiceException serviceException) {
			addActionError("" + HealthConstants.HLT_NEWENROLLMENT_SAVE_ERROR
					+ ":: "
					+ getText(HealthConstants.HLT_PROBLEM_OCCURED_WHILE_SAVING));
			PresentationException presentationException = new PresentationException(
					serviceException,
					HealthConstants.HLT_NEWENROLLMENT_SAVE_ERROR, this
							.getClass(), methodName);
			Logger.logError(userID, presentationException);
			returnPage = "patientDetailsEdit";
			// throw presentationException;
		} catch (Exception exception) {
			addActionError("" + HealthConstants.HLT_NEWENROLLMENT_SAVE_ERROR
					+ ":: "
					+ getText(HealthConstants.HLT_PROBLEM_OCCURED_WHILE_SAVING));
			PresentationException presentationException = new PresentationException(
					exception, HealthConstants.HLT_NEWENROLLMENT_SAVE_ERROR,
					this.getClass(), methodName);
			Logger.logError(userID, presentationException);
			returnPage = "patientDetailsEdit";
			// throw presentationException;
		} finally {
			loadDefaultInfo(userID);
		}
		return returnPage;

	}

	@SkipValidation
	public String getMCDReference() {
		String methodName = "getMCDReference";
		String userID = getUserID();//userID.substring(userID.indexOf("N")); 
		short patientTypeId = getModel().getPatientTypeId();
		getModel().setPatientDetailForm(new PatientDetailForm());

		try {
			if (patientTypeId == 1) {
				setValidMCDRefrence("N");
				getModel().setIsTrue("N");
				if (!ApplicationUtil.isNull(getModel().getMcdReferenceId())
						&& getModel().getMcdReferenceId().length() < 17) {
					if (getModel().getRegFor().equals("2")) {
						List<MCDDependantsDetail> dependentsList = commonBD
								.getDependantsList(userID, getModel()
										.getMcdReferenceId(), "B");
						getRelationshipList(dependentsList);

						MCDEmployeeDetail employee = null;
						try {
							employee = commonBD.getMCDEmployeeDetail(getModel()
									.getMcdReferenceId(), userID);
						} catch (Exception e) {
							employee = null;
						}

						if (!ApplicationUtil.isNull(employee)) {
							if (patientTypeId == 1 && !ApplicationUtil.isNull(employee.getPpoNumber())) {
								addActionMessage("User is not an employee");
								getModel().setIsTrue("Y");
							}
							else if (patientTypeId == 3 && ApplicationUtil.isNull(employee.getPpoNumber())) {
								addActionMessage("User is not a pensioner");
								getModel().setIsTrue("Y");
							}else{
							getModel().setMcdReferenceId(
									employee.getBiometricId());
							String employeeName = "";
							if (!ApplicationUtil
									.isNull(employee.getFirstName())) {
								employeeName = employee.getFirstName();
							}
							if (!ApplicationUtil.isNull(employee
									.getMiddleName())) {
								employeeName = employeeName + " "
										+ employee.getMiddleName();
							}
							if (!ApplicationUtil.isNull(employee.getLastName())) {
								employeeName = employeeName + " "
										+ employee.getLastName();
							}
							// if(!ApplicationUtil.isNull(employee.getFirstName()))
							// {
							// employeeName = employee.getFirstName();
							// }
							getModel().setEmployeeName(employeeName);
							if (!ApplicationUtil.isNull(employee.getGender()))
								getModel().setEmployeeGender(
										employee.getGender());
							if (!ApplicationUtil
									.isNull(employee.getBirthDate()))
								getModel().setEmployeeBirthDate(
										employee.getBirthDate());

							getModel().setRelationship("");
							setValidMCDRefrence("Y");
							}
						} else {
							addActionMessage("Enter valid Biometric Id");
							setValidMCDRefrence("N");
						}
					
					}else{
					getMCDEmployeeDetails(patientTypeId);
					if (!ApplicationUtil.isNull(getModel().getIsTrue())){
						if (getModel().getIsTrue().equalsIgnoreCase("N")){
							String patientRegistrationId = hospitalAdministrationBD
							.isPatientRegistered(userID, getModel()
									.getMcdReferenceId(), patientTypeId);
					if (getModel().getRegFor().equals("1")) {
						if (!ApplicationUtil.isNull(patientRegistrationId)) {
							addActionMessage("MCD Employee already registered :- "
									+ patientRegistrationId);
							setValidMCDRefrence("N");
							return INPUT;
						}
						
					}
						}
					}
					}
					
				} else {
					addActionMessage("Enter valid Biometric Id");
					setValidMCDRefrence("N");
				}

			} else if (patientTypeId == 2) {

				if (!ApplicationUtil.isNull(getModel().getMcdId())
						&& getModel().getMcdId().length() < 17) {
					String patientRegistrationId = hospitalAdministrationBD
							.isPatientRegistered(userID, getModel()
									.getMcdId(), patientTypeId);
					if (!ApplicationUtil.isNull(patientRegistrationId)) {
						addActionMessage("MCD Student already registered :- "
								+ patientRegistrationId);
						setValidMCDRefrence("N");
						return INPUT;
					} else {

						// addActionMessage("MCD Affiliated Student yet to be integrated with EIS");
						getMCDStudentDetails();
						return INPUT;
					}
				}

			} else if (patientTypeId == 3) {
				if (true) {
					setValidMCDRefrence("N");
					getModel().setIsTrue("N");
					if (!ApplicationUtil.isNull(getModel().getMcdReferenceId())
							&& getModel().getMcdReferenceId().length() < 17){
						if (getModel().getRegFor().equals("2")) {
							List<MCDDependantsDetail> dependentsList = commonBD
									.getDependantsList(userID, getModel()
											.getMcdReferenceId(), "B");
							getRelationshipList(dependentsList);


							MCDEmployeeDetail employee = null;
							try {
								employee = commonBD.getMCDEmployeeDetail(getModel()
										.getMcdReferenceId(), userID);
							} catch (Exception e) {
								employee = null;
							}

							if (!ApplicationUtil.isNull(employee)) {
								if (patientTypeId == 1 && !ApplicationUtil.isNull(employee.getPpoNumber())) {
									addActionMessage("User is not an employee");
									getModel().setIsTrue("Y");
								}
								else if (patientTypeId == 3 && ApplicationUtil.isNull(employee.getPpoNumber())) {
									addActionMessage("User is not a pensioner");
									getModel().setIsTrue("Y");
								}else{
								getModel().setMcdReferenceId(
										employee.getBiometricId());
								String employeeName = "";
								if (!ApplicationUtil
										.isNull(employee.getFirstName())) {
									employeeName = employee.getFirstName();
								}
								if (!ApplicationUtil.isNull(employee
										.getMiddleName())) {
									employeeName = employeeName + " "
											+ employee.getMiddleName();
								}
								if (!ApplicationUtil.isNull(employee.getLastName())) {
									employeeName = employeeName + " "
											+ employee.getLastName();
								}
								// if(!ApplicationUtil.isNull(employee.getFirstName()))
								// {
								// employeeName = employee.getFirstName();
								// }
								getModel().setEmployeeName(employeeName);
								if (!ApplicationUtil.isNull(employee.getGender()))
									getModel().setEmployeeGender(
											employee.getGender());
								if (!ApplicationUtil
										.isNull(employee.getBirthDate()))
									getModel().setEmployeeBirthDate(
											employee.getBirthDate());

								getModel().setRelationship("");
								setValidMCDRefrence("Y");
								}
							} else {
								addActionMessage("Enter valid Biometric Id");
								setValidMCDRefrence("N");
							}
						
						}else{
						getMCDEmployeeDetails(patientTypeId);
						if (!ApplicationUtil.isNull(getModel().getIsTrue())){
							if (getModel().getIsTrue().equalsIgnoreCase("N")){
								String patientRegistrationId = hospitalAdministrationBD
								.isPatientRegistered(userID, getModel()
										.getMcdReferenceId(), patientTypeId);
								if (getModel().getRegFor().equals("1")) {
									if (!ApplicationUtil.isNull(patientRegistrationId)) {
										addActionMessage("MCD Pensioner already registered :- "
												+ patientRegistrationId);
										setValidMCDRefrence("N");
										return INPUT;
									}
									//setValidMCDRefrence("Y");
									
								} 
							}
						}
						}
						
					}else {
						addActionMessage("Enter valid Biometric Id");
						setValidMCDRefrence("N");
					}
					/*if (getModel().getRegFor().equals("1")) {
						String patientRegistrationId = hospitalAdministrationBD
								.isPatientRegistered(userID, getModel()
										.getMcdReferenceId(), patientTypeId);
						if (!ApplicationUtil.isNull(patientRegistrationId)) {
							addActionMessage("MCD Pensioner already registered :- "
									+ patientRegistrationId);
							setValidMCDRefrence("N");
							return INPUT;
						}
					}
					// addActionMessage("MCD Pensioner not yet integrated");
					setValidMCDRefrence("Y");
					List<MCDDependantsDetail> dependentsList = commonBD
							.getDependantsList(userID, getModel()
									.getMcdReferenceId(), "P");
					getRelationshipList(dependentsList);
					return INPUT;
				}
				if (!ApplicationUtil.isNull(getModel().getMcdReferenceId())
						&& getModel().getMcdReferenceId().length() < 17) {
					String patientRegistrationId = hospitalAdministrationBD
							.isPatientRegistered(userID, getModel()
									.getMcdReferenceId(), patientTypeId);
					if (!ApplicationUtil.isNull(patientRegistrationId)) {
						addActionMessage("MCD Pensioner already registered :- "
								+ patientRegistrationId);
						setValidMCDRefrence("N");
						return INPUT;
					}
					getMCDPensioner();
				} else {
					addActionMessage("Enter valid Biometric Id");
					setValidMCDRefrence("N");
				}*/
			}
			}
		} catch (ServiceException serviceException) {
			addActionError(""
					+ HealthConstants.HLT_NEWENROLLMENT_MCDREFERENCE_READ_ERROR
					+ ":: "
					+ getText(HealthConstants.HLT_PROBLEM_OCCURED_WHILE_SAVING));
			PresentationException presentationException = new PresentationException(
					serviceException,
					HealthConstants.HLT_NEWENROLLMENT_MCDREFERENCE_READ_ERROR,
					this.getClass(), methodName);
			Logger.logError(userID, presentationException);
		} finally {
			loadDefaultInfo(userID);
		}
		return INPUT;
	}

	private void getRelationshipList(List<MCDDependantsDetail> dependentsList) {
		getModel().setDependantsList(dependentsList);
		this.relationshipList = new ArrayList<String>();
		if (!ApplicationUtil.isNull(dependentsList)) {
			for (int i = 0; i < dependentsList.size(); i++) {
				this.relationshipList.add(dependentsList.get(i).getName()+"("+dependentsList.get(i)
						.getRelationship()+")");
			}
			this.setRelationshipList(relationshipList);
		}
	}

	private void getMCDEmployeeDetails(short patientTypeId) throws ServiceException {
		// MCDApplicationUser employeeDetails =
		// commonBD.getEmployeeDetailsById(getModel().getMcdReferenceId(),
		// userID);
		// MCDEmployeeDetail employee =
		// commonBD.getMCDEmployeeDetail(getModel().getMcdReferenceId(),
		// userID);
		MCDEmployeeDetail employee = null;
		try {
			employee = commonBD.getMCDEmployeeDetail(getModel()
					.getMcdReferenceId(), userID);
		} catch (Exception e) {
			employee = null;
		}
		if (!ApplicationUtil.isNull(employee)) {				
			if (patientTypeId == 1 && !ApplicationUtil.isNull(employee.getPpoNumber())) {
				addActionMessage("User is not an employee");
				getModel().setIsTrue("Y");
			}
			else if (patientTypeId == 3 && ApplicationUtil.isNull(employee.getPpoNumber())) {
				addActionMessage("User is not a pensioner");
				getModel().setIsTrue("Y");
			}
			else {
				getModel().getPatientDetailForm().setFirstName(
						employee.getFirstName());
				getModel().getPatientDetailForm().setLastName(
						employee.getLastName());
				getModel().getPatientDetailForm().setBirthDateForm(
						ApplicationUtil.getDateString(employee.getBirthDate(),
								HealthConstants.DD_MM_YYYY));
				getModel().getPatientDetailForm().setFathersName(
						employee.getFatherhusbandname());
				getModel().getPatientDetailForm().setHusbandsName(
						employee.getFatherhusbandname());

				getModel().getPatientDetailForm().setGender(employee.getGender());
				if (!ApplicationUtil.isNull(employee.getLandlineNumber())) {
					String[] temp;

					String delimiter = "-";
					temp = employee.getLandlineNumber().split(delimiter);
					System.out.println(temp.length);
					if (temp.length == 2) {
						getModel().getPatientDetailForm().setStdCode(temp[0]);
						getModel().getPatientDetailForm().setLandLine(temp[1]);
					} else
						getModel().getPatientDetailForm().setLandLine(
								employee.getLandlineNumber());
				}
				if (!ApplicationUtil.isNull(employee.getMaritalStatus())
						&& employee.getMaritalStatus().equals("M")) {
					getModel().getPatientDetailForm().setMaritalStatus("M");
				} else {
					getModel().getPatientDetailForm().setMaritalStatus("U");
				}
				// getModel().getPatientDetailForm().setMaritalStatus(employee.getMaritalStatus());
				getModel().getPatientDetailForm().setMiddleName(
						employee.getMiddleName());
				getModel().getPatientDetailForm().setMobile(
						employee.getMobileNumber());

				setValidMCDRefrence("Y");
			}
			
		} else {
			addActionMessage("Enter valid Biometric Id");
			setValidMCDRefrence("N");
		}
	}

	@SkipValidation
	public String getDependantsDetail() throws ServiceException {
		String relationship = getModel().getRelationship();
		String flag = null;
		/*if (getModel().getPatientTypeId() == 1) {
			flag = "B";
		} else if (getModel().getPatientTypeId() == 3) {
			flag = "P";
		}*/
		int selectedIndex = getModel().getIndex() - 1;
		if (!ApplicationUtil.isNull(relationship)) {
			List<MCDDependantsDetail> dependentsList = commonBD
					.getDependantsList(userID, getModel().getMcdReferenceId(),
							"B");
			getRelationshipList(dependentsList);
			loadDefaultInfo(getUserID());
			PatientDetailForm patientDetailForm = getModel()
					.getPatientDetailForm();
			// for (int i = 0; i < dependentsList.size(); i++) {
			// if (relationship.equals(dependentsList.get(i).getRelationship()))
			// {
			MCDDependantsDetail dependent = dependentsList.get(selectedIndex);
			patientDetailForm.setFirstName(dependent.getName());
		
			if(!ApplicationUtil.isNull(getModel().getEmployeeName())){
				if (dependent.getRelationship().equalsIgnoreCase(("SON"))
						|| dependent.getRelationship().equalsIgnoreCase("DAUGHTER"))
				{
					MCDEmployeeDetail employee = null;
				try {
					employee = commonBD.getMCDEmployeeDetail(getModel()
							.getMcdReferenceId(), userID);
				} catch (Exception e) {
					employee = null;
				}
				String gender=employee.getGender();
				if(!ApplicationUtil.isNull(gender)&& gender.equalsIgnoreCase("M"))
				{
				patientDetailForm.setFathersName(getModel().getEmployeeName());
				}
				if(!ApplicationUtil.isNull(gender)&& gender.equalsIgnoreCase("F"))
				{
					patientDetailForm.setMothersName(getModel().getEmployeeName());
				}
			}
			}
			if (("Y").equals(dependent.getMarried())
					|| ("Y").equals(dependent.getMaritalStatus())) {
				patientDetailForm.setMaritalStatus("M");
			}
			if (dependent.getRelationship().equalsIgnoreCase(("WIFE"))
					|| dependent.getRelationship().equalsIgnoreCase("DAUGHTER")
					|| dependent.getRelationship().equalsIgnoreCase("sister")
					|| dependent.getRelationship().equalsIgnoreCase("mother")) {
				patientDetailForm.setGender("F");
			} else
				patientDetailForm.setGender("M");
			patientDetailForm.setBirthDateForm(dependent.getDateOfBirth());

			// }

		}
		return "addPatientRegistration";
	}

	private void getMCDStudentDetails() {
		try {
			IHospitalAdministrationBD hAdminBD = new HospitalAdministrationBD();
			HltSchStudentRegistration student = null;
			try {

				student = hAdminBD.getSHStudentByRollNumber(userID, getModel()
						.getMcdId(), mcdApplicationUser
						.getPrimaryOfficeId());
			} catch (Exception e) {
				student = null;
			}
			if (!ApplicationUtil.isNull(student)) {
				if (!ApplicationUtil.isNull(student.getStudentRegInformation()
						.getStudentPartyId())) {
					setValidMCDRefrence("Y");
					getModel().getPatientDetailForm().setFirstName(
							student.getStudentRegInformation().getFirstName());
					getModel().getPatientDetailForm().setLastName(
							student.getStudentRegInformation().getLastName());
					getModel().getPatientDetailForm().setBirthDateForm(
							student.getStudentRegInformation().getBirthDate());
					getModel().getPatientDetailForm().setGender(
							student.getStudentRegInformation().getGender());
					getModel().getPatientDetailForm().setEducationId((short) 3);
					getModel().getPatientDetailForm().setOccupationId(
							(short) 21);
					getModel().getPatientDetailForm().setFathersName(
							student.getStudentRegInformation().getFatherName());
					getModel().getPatientDetailForm()
							.setMothersName(
									student.getStudentRegInformation()
											.getMothersName());
					if (!ApplicationUtil.isNull(student
							.getStudentRegInformation().getReligionId())) {
						getModel().getPatientDetailForm().setReligion(
								Short.valueOf(student
										.getStudentRegInformation()
										.getReligionId()));
					}
				} else {
					addActionMessage("Enter valid Student ID");
					setValidMCDRefrence("N");
				}
			} else {
				addActionMessage("Enter valid Student ID");
				setValidMCDRefrence("N");
			}
		} catch (Exception exception) {
			addActionError(""
					+ HealthConstants.HLT_NEWENROLLMENT_MCDREFERENCE_READ_ERROR
					+ ":: "
					+ getText(HealthConstants.HLT_PROBLEM_OCCURED_WHILE_SAVING));
			PresentationException presentationException = new PresentationException(
					exception,
					HealthConstants.HLT_NEWENROLLMENT_MCDREFERENCE_READ_ERROR,
					this.getClass(), "getMCDStudentDetails");
			Logger.logError(userID, presentationException);
		}

	}

	private void getMCDPensioner() {
		setValidMCDRefrence("Y");
	}

	private <V, F> void bindToVO(String userid, V dataObject, F formObject) {
		String methodName = "bindToVO";
		try {
			BeanUtils.copyProperties(dataObject, formObject);
		} catch (IllegalAccessException exception) {
			ServiceException serviceException = new ServiceException(exception,
					HealthConstants.HLT_NEWENROLLMENT_BINDTOVO_ERROR, this
							.getClass(), methodName);
			Logger.logError(userid, serviceException);
		} catch (InvocationTargetException exception) {
			ServiceException serviceException = new ServiceException(exception,
					HealthConstants.HLT_NEWENROLLMENT_BINDTOVO_ERROR, this
							.getClass(), methodName);
			Logger.logError(userid, serviceException);
		}
	}

	private <V, F> void bindToForm(String userid, F formObject, V dataObject) {
		String methodName = "bindToVO";
		try {
			BeanUtils.copyProperties(formObject, dataObject);
		} catch (IllegalAccessException exception) {
			ServiceException serviceException = new ServiceException(exception,
					HealthConstants.HLT_NEWENROLLMENT_BINDTOVO_ERROR, this
							.getClass(), methodName);
			Logger.logError(userid, serviceException);
		} catch (InvocationTargetException exception) {
			ServiceException serviceException = new ServiceException(exception,
					HealthConstants.HLT_NEWENROLLMENT_BINDTOVO_ERROR, this
							.getClass(), methodName);
			Logger.logError(userid, serviceException);
		}
	}

	/**
	 * @return the userID
	 */
	public String getUserID() {
		if (userID.equals("")) {
			setUserID();
		}
		return userID;
	}

	public void setUserID() {
		userID = getLoginUserId();
	}

	protected String getLoginUserId() {
		mcdApplicationUser = getLoginUser();
		return mcdApplicationUser.getLoginId();
	}

	private MCDApplicationUser getLoginUser() {
		if (mcdApplicationUser == null) {
			ValueStack stack = ActionContext.getContext().getValueStack();
			mcdApplicationUser = (MCDApplicationUser) stack
					.findValue(CommonConstants.LOGGED_IN_USER);
			setAuthCode(mcdApplicationUser.getAuthCode());
			setLid(mcdApplicationUser.getLoginId());
		}
		return mcdApplicationUser;
	}

	protected String getLoginUserName() {
		String empName = "";
		String methodName = "getLoginUserName";
		mcdApplicationUser = getLoginUser();
		if (mcdApplicationUser != null
				&& mcdApplicationUser.getFirstName() != null
				&& !mcdApplicationUser.getFirstName().isEmpty()) {
			empName = mcdApplicationUser.getFirstName();
		}
		if (mcdApplicationUser != null
				&& mcdApplicationUser.getLastName() != null
				&& !mcdApplicationUser.getLastName().isEmpty()) {
			empName = empName + mcdApplicationUser.getLastName();
		}
		return empName;
	}

	private void loadDefaultSearchInfo(String userid) {
		String methodName = "loadDefaultSearchInfo";
		try {
			// hospitalList = new ArrayList<MCDOffice>();
			hospitalList = CacheUtil.loadFirstAidMasterData(userid,
					HealthConstants.MCD_ALL_HEALTH_UNITS, 12);
			// List<HltCodeValue> hltHospitalList =
			// CacheUtil.loadFirstAidMasterData(userid,HealthConstants.MCD_ALL_HEALTH_UNITS,12);
			// if(hltHospitalList!=null && !hltHospitalList.isEmpty()){
			// Iterator<HltCodeValue> hltHospitalItr =
			// hltHospitalList.iterator();
			// while(hltHospitalItr.hasNext()){
			// HltCodeValue hltCodeValue = hltHospitalItr.next();
			// MCDOffice office = new MCDOffice();
			// office.setOfficeId(hltCodeValue.getCodeValueKey());
			// office.setOfficeName(hltCodeValue.getCodeValue());
			// hospitalList.add(office);
			// }
			// }
			/*
			 * publicHospitalList =
			 * CacheUtil.loadMCDHospitalsListFromMCDOffice(userid);
			 * chestClinicsList =CacheUtil.loadMCDHealthUnitListFromMCDOffice(
			 * OfficeServiceOfficeSubType.CHEST_CLINCS, userid);
			 * 
			 * IHospitalAdministrationBD hAdminBD = new
			 * HospitalAdministrationBD();
			 * 
			 * List<MCDOffice> colonyHospitalList =
			 * hAdminBD.getHospitalList(OfficeServiceOfficeSubType
			 * .COLONY_HOSPITALS, userid); List<MCDOffice> polyClinicList =
			 * hAdminBD.getHospitalList(OfficeServiceOfficeSubType.POLY_CLINIC,
			 * userid); List<MCDOffice> phcList =
			 * hAdminBD.getHospitalList(OfficeServiceOfficeSubType.PHC, userid);
			 * List<MCDOffice> allopathyDispensaryList =
			 * hAdminBD.getHospitalList
			 * (OfficeServiceOfficeSubType.ALLOPATHY_DISPENSARY, userid);
			 * 
			 * List<MCDOffice> dispensaryList =
			 * hAdminBD.getHospitalList(OfficeServiceOfficeSubType.DISPENSARY,
			 * userid); List<MCDOffice> maternityHomeList =
			 * hAdminBD.getHospitalList
			 * (OfficeServiceOfficeSubType.MATERNITY_HOME, userid);
			 * 
			 * if(!ApplicationUtil.isNull(chestClinicsList)){
			 * hospitalList.addAll(chestClinicsList); }
			 * if(!ApplicationUtil.isNull(publicHospitalList)){
			 * hospitalList.addAll(publicHospitalList); }
			 * 
			 * if(!ApplicationUtil.isNull(colonyHospitalList)){
			 * hospitalList.addAll(colonyHospitalList); }
			 * 
			 * if(!ApplicationUtil.isNull(polyClinicList)){
			 * hospitalList.addAll(polyClinicList); }
			 * 
			 * if(!ApplicationUtil.isNull(phcList)){
			 * hospitalList.addAll(phcList); }
			 * 
			 * if(!ApplicationUtil.isNull(allopathyDispensaryList)){
			 * hospitalList.addAll(allopathyDispensaryList); }
			 * 
			 * if(!ApplicationUtil.isNull(dispensaryList)){
			 * hospitalList.addAll(dispensaryList); }
			 * 
			 * if(!ApplicationUtil.isNull(maternityHomeList)){
			 * hospitalList.addAll(maternityHomeList); }
			 */
			yearList = null;
			yearList = new ArrayList<String>();
			Calendar calendar = Calendar.getInstance();
			java.util.Date now = calendar.getTime();
			java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now
					.getTime());
			int startYear = 2011;
			String currentYear = new SimpleDateFormat("yyyy")
					.format(currentTimestamp);
			int curYearInt = Integer.parseInt(currentYear);
			for (int i = startYear; i <= curYearInt; i++) {
				yearList.add("" + i);
			}

		} catch (Exception exception) {
			ServiceException serviceException = new ServiceException(exception,
					HealthConstants.HLT_NEWENROLLMENT_LOADDEFAULTINFO_ERROR,
					this.getClass(), methodName);
			Logger.logError(userid, serviceException);
		}

	}

	private void loadDefaultInfo(String userid) {
		String methodName = "loadDefaultInfo";
		try {
			stateMap = CacheUtil.loadStateList(userid);
			zonesMap = CacheUtil.loadMCDZoneList(userid);
			wardMap = new ArrayList<MCDWard>();
			colonyMap = new ArrayList<HltColony>();
			districtMap = CacheUtil.loadDistrictList(userid);
			educationMap = CacheUtil.loadEducationTypesMap(userid);
			occupationMap = CacheUtil.loadOccupationMap(userid);
			religionTypesList = CacheUtil.loadCodeValueMasterData(userid,
					HealthConstants.RELIGION_TYPE, 8);
			categoryTypesList = CacheUtil.loadCodeValueMasterData(userid,
					HealthConstants.CATEGORY_TYPE, 9);

			/*
			 * MCDOffice mcdOffice = new MCDOffice();
			 * mcdOffice.setOfficeId("1");
			 * mcdOffice.setOfficeName("MCD Hospital"); hospitalList = new
			 * ArrayList<MCDOffice>(); hospitalList.add(mcdOffice);
			 */

			// hospitalList = new ArrayList<MCDOffice>();
			hospitalList = CacheUtil.loadFirstAidMasterData(userid,
					HealthConstants.MCD_ALL_HEALTH_UNITS, 12);
			// List<HltCodeValue> hltHospitalList =
			// CacheUtil.loadFirstAidMasterData(userid,HealthConstants.MCD_ALL_HEALTH_UNITS,12);
			// if(hltHospitalList!=null && !hltHospitalList.isEmpty()){
			// Iterator<HltCodeValue> hltHospitalItr =
			// hltHospitalList.iterator();
			// while(hltHospitalItr.hasNext()){
			// HltCodeValue hltCodeValue = hltHospitalItr.next();
			// MCDOffice office = new MCDOffice();
			// office.setOfficeId(hltCodeValue.getCodeValueKey());
			// office.setOfficeName(hltCodeValue.getCodeValue());
			// hospitalList.add(office);
			// }
			// }
			/*
			 * publicHospitalList =
			 * CacheUtil.loadMCDHospitalsListFromMCDOffice(userid);
			 * chestClinicsList =CacheUtil.loadMCDHealthUnitListFromMCDOffice(
			 * OfficeServiceOfficeSubType.CHEST_CLINCS, userid);
			 * 
			 * IHospitalAdministrationBD hAdminBD = new
			 * HospitalAdministrationBD();
			 * 
			 * List<MCDOffice> colonyHospitalList =
			 * hAdminBD.getHospitalList(OfficeServiceOfficeSubType
			 * .COLONY_HOSPITALS, userid); List<MCDOffice> polyClinicList =
			 * hAdminBD.getHospitalList(OfficeServiceOfficeSubType.POLY_CLINIC,
			 * userid); List<MCDOffice> phcList =
			 * hAdminBD.getHospitalList(OfficeServiceOfficeSubType.PHC, userid);
			 * List<MCDOffice> allopathyDispensaryList =
			 * hAdminBD.getHospitalList
			 * (OfficeServiceOfficeSubType.ALLOPATHY_DISPENSARY, userid);
			 * 
			 * List<MCDOffice> dispensaryList =
			 * hAdminBD.getHospitalList(OfficeServiceOfficeSubType.DISPENSARY,
			 * userid); List<MCDOffice> maternityHomeList =
			 * hAdminBD.getHospitalList
			 * (OfficeServiceOfficeSubType.MATERNITY_HOME, userid);
			 * 
			 * if(!ApplicationUtil.isNull(chestClinicsList)){
			 * hospitalList.addAll(chestClinicsList); }
			 * if(!ApplicationUtil.isNull(publicHospitalList)){
			 * hospitalList.addAll(publicHospitalList); }
			 * 
			 * if(!ApplicationUtil.isNull(colonyHospitalList)){
			 * hospitalList.addAll(colonyHospitalList); }
			 * 
			 * if(!ApplicationUtil.isNull(polyClinicList)){
			 * hospitalList.addAll(polyClinicList); }
			 * 
			 * if(!ApplicationUtil.isNull(phcList)){
			 * hospitalList.addAll(phcList); }
			 * 
			 * if(!ApplicationUtil.isNull(allopathyDispensaryList)){
			 * hospitalList.addAll(allopathyDispensaryList); }
			 * 
			 * if(!ApplicationUtil.isNull(dispensaryList)){
			 * hospitalList.addAll(dispensaryList); }
			 * 
			 * if(!ApplicationUtil.isNull(maternityHomeList)){
			 * hospitalList.addAll(maternityHomeList); }
			 */

			IPublicHealthBD pHealthBD = new PublicHealthBD();

			if (!ApplicationUtil.isNull(getModel().getPatientDetailForm())) {
				if (!ApplicationUtil.isNull(getModel().getPatientDetailForm()
						.getCmnPresentAddress())) {
					if (getModel().getPatientDetailForm()
							.getCmnPresentAddress().getZoneId() != null
							&& !getModel().getPatientDetailForm()
									.getCmnPresentAddress().getZoneId()
									.isEmpty())
						wardPresentMap = pHealthBD.getMCDAllWards(userid,
								getModel().getPatientDetailForm()
										.getCmnPresentAddress().getZoneId());

					if (getModel().getPatientDetailForm()
							.getCmnPresentAddress() != null
							&& getModel().getPatientDetailForm()
									.getCmnPresentAddress().getWardId() != null
							&& !getModel().getPatientDetailForm()
									.getCmnPresentAddress().getWardId()
									.isEmpty())
						colonyPresentMap = pHealthBD.getAllColonies(userid,
								getModel().getPatientDetailForm()
										.getCmnPresentAddress().getWardId());

				}
				if (!ApplicationUtil.isNull(getModel().getPatientDetailForm()
						.getCmnPermanentAddress())) {
					if (getModel().getPatientDetailForm()
							.getCmnPermanentAddress().getZoneId() != null
							&& !getModel().getPatientDetailForm()
									.getCmnPermanentAddress().getZoneId()
									.isEmpty())
						wardMap = pHealthBD.getMCDAllWards(userid, getModel()
								.getPatientDetailForm()
								.getCmnPermanentAddress().getZoneId());

					if (getModel().getPatientDetailForm()
							.getCmnPermanentAddress() != null
							&& getModel().getPatientDetailForm()
									.getCmnPermanentAddress().getWardId() != null
							&& !getModel().getPatientDetailForm()
									.getCmnPermanentAddress().getWardId()
									.isEmpty())
						colonyMap = pHealthBD.getAllColonies(userid, getModel()
								.getPatientDetailForm()
								.getCmnPermanentAddress().getWardId());
				}
			}

		} catch (Exception exception) {
			ServiceException serviceException = new ServiceException(exception,
					HealthConstants.HLT_NEWENROLLMENT_LOADDEFAULTINFO_ERROR,
					this.getClass(), methodName);
			Logger.logError(userid, serviceException);
		}
	}

	@Override
	public void validate() {

		Date currDate = ApplicationUtil.getDate(ApplicationUtil.getDateString(
				new Date(), "dd/MM/yyyy"));
		int dateDiff = 0;
		String userID = getUserID();
		loadDefaultInfo(userID);
		boolean relationship = false;
		if (!ApplicationUtil.isNull(getModel().getIsPatientReferred())
				&& getModel().getIsPatientReferred().equals("Y")) {
			if (ApplicationUtil.isNull(getModel().getReferredFrom())) {
				addFieldError(
						"referredFrom",
						getText("health.hospitaladmin.newenrollment.referredfrom.label")
								+ " " + getText("health.common.inputrequired"));
			}
			if (ApplicationUtil.isNull(getModel().getCmnReferralAddress()
					.getAddress1())) {
				addFieldError(
						"cmnReferralAddress.address1",
						getText("health.hospitaladmin.newenrollment.refaddress.label")
								+ " " + getText("health.common.inputrequired"));

			}
			if (ApplicationUtil.isNull(getModel().getCmnReferralAddress()
					.getState())) {
				addFieldError(
						"cmnReferralAddress.state",
						getText("health.hospitaladmin.newenrollment.refstate.label")
								+ " " + getText("health.common.inputrequired"));
			}
			if (!ApplicationUtil.isNull(getModel().getReferredRemark())
					&& getModel().getReferredRemark().length() > 255) {
				addFieldError("referredRemark",
						"Reason should be less than 255 characters  ");
			}
		}

		/*
		 * if(!ApplicationUtil.isNull(getModel().getPatientDetailForm().getCmnPresentAddress
		 * ().getState()) &&
		 * getModel().getPatientDetailForm().getCmnPresentAddress
		 * ().getState().equals("5")) {
		 * if(!ApplicationUtil.isNull(getModel().getPatientDetailForm
		 * ().getCmnPresentAddress().getZoneId())){
		 * addFieldError("patientDetailForm.cmnPresentAddress.zoneId"
		 * ,getText("health.hospitaladmin.newenrollment.presentaddress.zone.label"
		 * ) +" "+getText("health.common.inputrequired")); } }
		 * 
		 * if(!ApplicationUtil.isNull(getModel().getPatientDetailForm().
		 * getCmnPermanentAddress()) &&
		 * !ApplicationUtil.isNull(getModel().getPatientDetailForm
		 * ().getCmnPermanentAddress().getState()) &&
		 * getModel().getPatientDetailForm
		 * ().getCmnPermanentAddress().getState().equals("5")) {
		 * if(!ApplicationUtil
		 * .isNull(getModel().getPatientDetailForm().getCmnPermanentAddress
		 * ().getZoneId())){
		 * addFieldError("patientDetailForm.cmnPermanentAddress.zoneId"
		 * ,getText(
		 * "health.hospitaladmin.newenrollment.permanentaddress.zone.label" )
		 * +" "+getText("health.common.inputrequired")); } }
		 */

		if (!ApplicationUtil.isNull(getModel().getPatientDetailForm()
				.getCmnPresentAddress().getPinCode())) {
			if (!getModel().getPatientDetailForm().getCmnPresentAddress()
					.getPinCode().matches(HealthConstants.VALID_NUMERIC)) {
				addFieldError("patientDetailForm.cmnPresentAddress.pinCode",
						"Should contain Numerics only.");
			}

			else if (getModel().getPatientDetailForm().getCmnPresentAddress()
					.getPinCode().length() < 6) {
				addFieldError("patientDetailForm.cmnPresentAddress.pinCode",
						"Should contain 6 Digits");
			}
		}
		if (!ApplicationUtil.isNull(getModel().getPatientDetailForm()
				.getIsSameAsPresentAddress())) {
			if (getModel().getPatientDetailForm().getIsSameAsPresentAddress()
					.equalsIgnoreCase("N")) {
				if (!ApplicationUtil.isNull(getModel().getPatientDetailForm()
						.getCmnPermanentAddress().getPinCode())) {
					if (!getModel().getPatientDetailForm()
							.getCmnPermanentAddress().getPinCode().matches(
									HealthConstants.VALID_NUMERIC)) {
						addFieldError(
								"patientDetailForm.cmnPermanentAddress.pinCode",
								"Should contain Numerics only.");
					}

					else if (getModel().getPatientDetailForm()
							.getCmnPermanentAddress().getPinCode().length() < 6) {
						addFieldError(
								"patientDetailForm.cmnPermanentAddress.pinCode",
								"Should contain 6 Digits");
					}
				}
			}
		}

		if (!ApplicationUtil.isNull(getModel().getCmnReferralAddress()
				.getPinCode())) {
			if (!getModel().getCmnReferralAddress().getPinCode().matches(
					HealthConstants.VALID_NUMERIC)) {
				addFieldError("cmnReferralAddress.pinCode",
						"Should contain Numerics only.");
			}

			else if (getModel().getCmnReferralAddress().getPinCode().length() < 6) {
				addFieldError("cmnReferralAddress.pinCode",
						"Should contain 6 Digits");
			}
		}

		if (getModel().getPatientTypeId() == 1) {
			if (!ApplicationUtil.isNull(getModel().getRegFor())
					&& getModel().getRegFor().equals("2")) {

				if (ApplicationUtil.isNull(getModel().getRelationship())) {
					addFieldError(
							"relationship",
							getText("health.hospitaladmin.newenrollment.relationship.label")
									+ " "
									+ getText("health.common.inputrequired"));
					relationship = true;
				}
			}
		}

		if (getModel().getPatientTypeId() == 3) {
			if (!ApplicationUtil.isNull(getModel().getRegFor())
					&& getModel().getRegFor().equals("2")) {
				if (ApplicationUtil.isNull(getModel().getEmployeeName())) {
					addFieldError("employeeName", getText("Pensioner Name")
							+ " " + getText("health.common.inputrequired"));
				}
				if (ApplicationUtil.isNull(getModel().getRelationship())) {
					addFieldError(
							"relationship",
							getText("health.hospitaladmin.newenrollment.relationship.label")
									+ " "
									+ getText("health.common.inputrequired"));
				}
			}
		}

		if (ApplicationUtil.isNull(getModel().getPatientDetailForm()
				.getIsSameAsPresentAddress())
				|| getModel().getPatientDetailForm()
						.getIsSameAsPresentAddress().equalsIgnoreCase("N")) {

			if (ApplicationUtil.isNull(getModel().getPatientDetailForm()
					.getCmnPermanentAddress())
					|| ApplicationUtil.isNull(getModel().getPatientDetailForm()
							.getCmnPermanentAddress().getAddress1())
					|| getModel().getPatientDetailForm()
							.getCmnPermanentAddress().getAddress1().trim()
							.equals("")) {
				addFieldError(
						"patientDetailForm.cmnPermanentAddress.address1",
						getText("health.hospitaladmin.newenrollment.permanentaddress.addressline1.label")
								+ " " + getText("health.common.inputrequired"));
				relationship = true;
			}
			if (ApplicationUtil.isNull(getModel().getPatientDetailForm()
					.getCmnPermanentAddress())
					|| ApplicationUtil.isNull(getModel().getPatientDetailForm()
							.getCmnPermanentAddress().getState())
					|| getModel().getPatientDetailForm()
							.getCmnPermanentAddress().getState().trim().equals(
									"")) {
				addFieldError(
						"patientDetailForm.cmnPermanentAddress.state",
						getText("health.hospitaladmin.newenrollment.permanentaddress.state.label")
								+ " " + getText("health.common.inputrequired"));
				relationship = true;
			}
		}

		if (getModel().getPatientDetailForm().getReligion() <= 0) {
			addFieldError("patientDetailForm.religion", getText("Religion")
					+ " " + getText("health.common.inputrequired"));
			relationship = true;
		}

		if (getModel().getPatientDetailForm().getReligion() == 58
				&& ApplicationUtil.isNull(getModel().getPatientDetailForm()
						.getReligionOther())) {
			addFieldError("patientDetailForm.religionOther",
					getText("Other Religion") + " "
							+ getText("health.common.inputrequired"));
		}

		if (!ApplicationUtil.isNull(getModel().getPatientDetailForm()
				.getMobile())) {
			if (!getModel().getPatientDetailForm().getMobile().matches(
					HealthConstants.VALID_NUMERIC)) {
				addFieldError("patientDetailForm.mobile",
						"Should contain Numerics only.");
			} else if (getModel().getPatientDetailForm().getMobile().length() < 10)
				addFieldError("patientDetailForm.mobile",
						getText("Mobile No. Should be 10 digits"));
		}
		if (!ApplicationUtil.isNull(getModel().getPatientDetailForm()
				.getLandLine())) {
			if (!getModel().getPatientDetailForm().getLandLine().matches(
					HealthConstants.VALID_NUMERIC)) {
				addFieldError("patientDetailForm.landLine",
						"Should contain Numerics only.");
			} else if (getModel().getPatientDetailForm().getLandLine().length() < 5)
				addFieldError("patientDetailForm.landLine",
						getText("Landline No. Should be atleast 5 digits"));
		}
		/*
		 * if(!ApplicationUtil.isNull(getModel().getPatientDetailForm().getCmnPresentAddress
		 * ().getState())){
		 * 
		 * 
		 * 
		 * 
		 * if(Short.valueOf(getModel().getPatientDetailForm().getCmnPresentAddress
		 * ().getState())== 5 ) { setPresentState("Delhi");
		 * 
		 * } }if(!ApplicationUtil.isNull(getModel().getPatientDetailForm().
		 * getCmnPermanentAddress().getState())) {
		 * if(Short.valueOf(getModel().getPatientDetailForm
		 * ().getCmnPermanentAddress().getState())== 5 ) {
		 * setPermanantState("Delhi");
		 * 
		 * } }
		 */

		if (!ApplicationUtil.isNull(getModel().getPatientDetailForm()
				.getBirthDateForm())
				&& !getModel().getPatientDetailForm().getBirthDateForm()
						.isEmpty()) {
			dateDiff = ApplicationUtil.getDateDiff(ApplicationUtil
					.getDate(getModel().getPatientDetailForm()
							.getBirthDateForm()), currDate);
			boolean isValid = true;
			if (dateDiff <= 0) {
				addFieldError("patientDetailForm.birthDateForm",
						getText("health.hospitaladmin.newenrollment.dob.label")
								+ " should be less than Today's Date");
				isValid = false;
			}
			if (getModel().getPatientDetailForm().getAgeYear() < 0
					|| getModel().getPatientDetailForm().getAgeYear() > 150) {
				addFieldError("patientDetailForm.birthDateForm",
						"Year is invalid.");
				isValid = false;
			}
			if (isValid) {
				if (getModel().getPatientTypeId() == 1
						&& getModel().getRegFor().equals("2")
						&& (getModel().getRelationship().equals("2") || (getModel()
								.getRelationship().equals("6")))) {
					relationship = true;
					if (getAge(getModel().getEmployeeBirthDate())
							- (getModel().getPatientDetailForm().getAgeYear()) <= 0) {
						addFieldError("patientDetailForm.ageYear",
								"Son/Daughter's Age Cannot be greater than/equal to Father's Age");
						getModel().getPatientDetailForm().setBirthDateForm("");
						getModel().getPatientDetailForm().setAgeYear((short) 0);
						getModel().getPatientDetailForm()
								.setAgeMonth((short) 0);
						getModel().getPatientDetailForm().setAgeDay((short) 0);

					}

				}
				/*if (getModel().getPatientTypeId() == 3
						&& getModel().getRegFor().equals("1")) {
					if (getModel().getPatientDetailForm().getAgeYear() < 60) {
						addFieldError("patientDetailForm.ageYear",
								"Age of Pensioner should be greater than 60");
					}
				}*/

			}
		} else {
			boolean isValid = true;
			if (getModel().getPatientDetailForm().getAgeYear() != 0
					|| getModel().getPatientDetailForm().getAgeMonth() != 0
					|| getModel().getPatientDetailForm().getAgeDay() != 0) {
				if (getModel().getPatientDetailForm().getAgeYear() < 0
						|| getModel().getPatientDetailForm().getAgeYear() > 150) {
					addFieldError("patientDetailForm.ageYear",
							"Year is invalid.");
					isValid = false;
				}
				if (getModel().getPatientDetailForm().getAgeMonth() < 0
						|| getModel().getPatientDetailForm().getAgeMonth() > 11) {
					addFieldError("patientDetailForm.ageMonth",
							"Month is invalid.");
					isValid = false;
				}
				if (getModel().getPatientDetailForm().getAgeDay() < 0
						|| getModel().getPatientDetailForm().getAgeDay() > 29) {
					addFieldError("patientDetailForm.ageDay",
							"Days is invalid.");
					isValid = false;
				}
				if (isValid) {
					if (getModel().getPatientTypeId() == 1
							&& getModel().getRegFor().equals("2")
							&& (getModel().getRelationship().equals("2") || (getModel()
									.getRelationship().equals("6")))) {
						relationship = true;
						if (getAge(getModel().getEmployeeBirthDate())
								- getModel().getPatientDetailForm()
										.getAgeYear() <= 0) {
							addFieldError("patientDetailForm.ageYear",
									"Son/Daughter's Age Cannot be greater than/equal to Father's Age");
							getModel().getPatientDetailForm().setBirthDateForm(
									"");
							getModel().getPatientDetailForm().setAgeYear(
									(short) 0);
							getModel().getPatientDetailForm().setAgeMonth(
									(short) 0);
							getModel().getPatientDetailForm().setAgeDay(
									(short) 0);
						}

					}
					if (getModel().getPatientTypeId() == 3
							&& getModel().getRegFor().equals("1")) {
						if (getModel().getPatientDetailForm().getAgeYear() < 60) {
							addFieldError("patientDetailForm.ageYear",
									"Age of Pensioner should be greater than 60");
						}
					}
				}

			} else {
				addFieldError("patientDetailForm.ageYear",
						getText("health.hospitaladmin.newenrollment.age.label")
								+ " is invalid.");
			}
		}
		if (relationship == true) {
			try {
				String val = getMCDReference();
			} catch (Exception exception) {
				ServiceException serviceException = new ServiceException(
						exception,
						HealthConstants.HLT_NEWENROLLMENT_LOADDEFAULTINFO_ERROR,
						this.getClass(), "validate");
				Logger.logError(userID, serviceException);
			}
		}
		if (getModel().getPatientEnrollmentGenId() > 0) {
			if (!ApplicationUtil.isNull(getModel().getModule())
					&& getModel().getModule().equalsIgnoreCase("TB"))
				setNextAction("patientDetailsEdit");
			else {
				setNextAction("patientDetailsEditMc");
			}
		} else {
			setNextAction("addPatientRegistration");
		}

	}

	public int getAge(Date DOB) {
		Date currDate = ApplicationUtil.getDate(ApplicationUtil.getDateString(
				new Date(), "dd/MM/yyyy"));
		int diff = currDate.getYear() - DOB.getYear();
		return diff;

	}

	public List<String> getYearList() {
		return yearList;
	}

	public void setYearList(List<String> yearList) {
		this.yearList = yearList;
	}

	public List<HltColony> getColonyPresentMap() {
		return colonyPresentMap;
	}

	public void setColonyPresentMap(List<HltColony> colonyPresentMap) {
		this.colonyPresentMap = colonyPresentMap;
	}

	public List<MCDWard> getWardPresentMap() {
		return wardPresentMap;
	}

	public void setWardPresentMap(List<MCDWard> wardPresentMap) {
		this.wardPresentMap = wardPresentMap;
	}

	// private boolean insertIntoMcwis(String userid,String insertValues) {
	// //String connectionURL = "jdbc:mysql://172.16.1.36:3306/MCWIS1";
	// String methodName = "insertIntoMcwis";
	// Connection con = null;
	// boolean insertFlag = false;
	// String dbURL = "";
	// try {
	//			
	// if(ApplicationUtil.isNull(dbHost) || ApplicationUtil.isNull(dbPort) ||
	// ApplicationUtil.isNull(dbInstance)){
	// dbURL = ApplicationEnvProperties.getProperty(userid, "MCWIS_DB_URL");
	// }else{
	// dbURL = "jdbc:postgresql://"+dbHost+":"+dbPort+"/"+dbInstance;
	// }
	// if(ApplicationUtil.isNull(dbUser)){
	// dbUser = ApplicationEnvProperties.getProperty(userid, "MCWIS_DB_USER");
	// }
	// if(ApplicationUtil.isNull(dbPwd)){
	// dbPwd = ApplicationEnvProperties.getProperty(userid, "MCWIS_DB_PWD");
	// }
	//			
	// StringBuffer query = new StringBuffer();
	//			
	// String schema = "";
	//			
	// if(ApplicationUtil.isNull(schema)){
	// schema = ApplicationEnvProperties.getProperty(userid, "MCWIS_SCHEMA");
	// }else{
	// schema = "MCWIS1";
	// }
	//			
	// insertValues = insertValues.replaceAll("'null'", "null");
	// query.append(" insert into ");
	// query.append(schema+".patMsatVot_t");
	//			      
	// query.append(" ( PATIENT_TYPE_ID, HOSPITAL_ID,   FIRST_NAME, MIDDLE_NAME, LAST_NAME, FATHERS_NAME, MOTHERS_NAME, ");
	// query.append(" GENDER, MARITAL_STATUS,   HUSBANDS_NAME, BIRTH_DATE, AGE_DAY, AGE_MONTH, AGE_YEAR, OCCUPATION_ID, EDUCATION_ID, ");
	// query.append(" RELIGION, RELIGION_OTHER, CATEGORY, CATEGORY_OTHER, ADDRESS1, ADDRESS2, STATE1, CITY1, ZONE_ID1, WARD_ID1, ");
	// query.append(" COLONY_ID1, DISTRICT_ID1, PIN_CODE1, IS_SAME_AS_PRESENT_ADDRESS, ADDRESS3, ADDRESS4, STATE2, CITY2, ZONE_ID2, ");
	// query.append(" WARD_ID2, COLONY_ID2, DISTRICT_ID2, PIN_CODE2, STD_CODE, LAND_LINE, MOBILE, IS_PATIENT_REFERRED, REFERRED_FROM, ");
	// query.append(" REFERRED_FROM_OTHER, REFERRED_REMARK, ADDRESS5, ADDRESS6, STATE3, CITY3, PIN_CODE3, PATIENT_REGISTRATION_ID, REGISTRATION_TYPE, ");
	// query.append(" REGISTRATION_DTM, REGISTERED_BY, REG_FOR, RELATIONSHIP, EMPLOYEE_NAME, PATIENT_REGISTRATION_YR, PATIENT_REGISTRATION_NO ) ");
	// query.append(" VALUES ( ");
	// query.append(insertValues);
	// query.append(" )");
	//			
	// Class.forName("com.mysql.jdbc.Driver").newInstance();
	// //con = DriverManager.getConnection(connectionURL, "msat", "msat");
	// con = DriverManager.getConnection(dbURL, dbUser,dbPwd);
	//			
	// Statement stmt = con.createStatement();
	//			
	// stmt.executeUpdate(query.toString());
	// stmt.close();
	// insertFlag = true;
	// } catch (SQLException exception) {
	// PresentationException presentationException = new PresentationException(
	// exception,
	// HealthConstants.HLT_NEWENROLLMENT_SAVE_ERROR,
	// this.getClass(), methodName);
	// Logger.logError(userID, presentationException);
	// } catch (Exception exception) {
	// PresentationException presentationException = new PresentationException(
	// exception,
	// HealthConstants.HLT_NEWENROLLMENT_SAVE_ERROR,
	// this.getClass(), methodName);
	// Logger.logError(userID, presentationException);
	// } finally {
	// // Close the connection
	// try {
	// con.close();
	// } catch (Exception exception) {
	// PresentationException presentationException = new PresentationException(
	// exception,
	// HealthConstants.HLT_NEWENROLLMENT_SAVE_ERROR,
	// this.getClass(), methodName);
	// Logger.logError(userID, presentationException);
	// }
	// }
	// return insertFlag;
	// }

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public String getLid() {
		return lid;
	}

	public void setLid(String lid) {
		this.lid = lid;
	}

	public String getResultSuccessPage() {
		return resultSuccessPage;
	}

	public void setResultSuccessPage(String resultSuccessPage) {
		this.resultSuccessPage = resultSuccessPage;
	}

	public String getActionSuccessMsg() {
		return actionSuccessMsg;
	}

	public void setActionSuccessMsg(String actionSuccessMsg) {
		this.actionSuccessMsg = actionSuccessMsg;
	}

	public List<String> getRelationshipList() {
		return relationshipList;
	}

	public void setRelationshipList(List<String> relationshipList) {
		this.relationshipList = relationshipList;
	}

}
