package org.gluu.agama.registration;

import io.jans.as.common.model.common.User;
import io.jans.as.common.service.common.EncryptionService;
import io.jans.as.common.service.common.UserService;
import io.jans.orm.exception.operation.EntryNotFoundException;
import io.jans.service.MailService;
import io.jans.model.SmtpConfiguration;
import io.jans.service.cdi.util.CdiUtil;
import io.jans.util.StringHelper;

import org.gluu.agama.user.UserRegistration;
import io.jans.agama.engine.script.LogUtils;
import java.io.IOException;
import io.jans.as.common.service.common.ConfigurationService;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.regex.Pattern;

import org.gluu.agama.EmailTemplate;
import org.gluu.agama.registration.Labels;




public class JansUserRegistration extends UserRegistration {
    
    private static final String SN = "sn";
    private static final String CONFIRM_PASSWORD = "confirmPassword";
    private static final String LANG = "lang";
    private static final String REFERRAL_CODE = "referralCode";
    private static final String RESIDENCE_COUNTRY = "residenceCountry";

    private static final String MAIL = "mail";
    private static final String UID = "uid";
    private static final String DISPLAY_NAME = "displayName";
    private static final String GIVEN_NAME = "givenName";
    private static final String PASSWORD = "userPassword";
    private static final String INUM_ATTR = "inum";
    private static final String EXT_ATTR = "jansExtUid";
    private static final String USER_STATUS = "jansStatus";
    private static final String EXT_UID_PREFIX = "github:";
    private static final int OTP_LENGTH = 6;
    private static final String SUBJECT_TEMPLATE = "Here's your verification code: %s";
    private static final String MSG_TEMPLATE_TEXT = "%s is the code to complete your verification";   
    private static final SecureRandom RAND = new SecureRandom();

    private static JansUserRegistration INSTANCE = null;

    public JansUserRegistration() {}

    public static synchronized JansUserRegistration getInstance()
    {
        if (INSTANCE == null)
            INSTANCE = new JansUserRegistration();

        return INSTANCE;
    }

    // public boolean passwordPolicyMatch(String userPassword) {
    // // Regex Explanation:
    // // - (?=.*[!-~&&[^ ]]) ensures at least one printable ASCII character except space (also helps exclude space)
    // // - (?=.*[!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~]) ensures at least one special character
    // // - (?=.*[A-Za-z]) ensures at least one letter
    // // - (?=.*\\d) ensures at least one digit
    // // - [!-~&&[^ ]] limits all characters to printable ASCII excluding space (ASCII 33–126)
    // String regex = '''^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!"#$%&'()*+,-./:;<=>?@[\\\\]^_`{|}~])[!-~&&[^ ]]{12,24}$''';
    //     Pattern pattern = Pattern.compile(regex);
    //     return pattern.matcher(userPassword).matches();
    // }

    // public boolean usernamePolicyMatch(String userName) {
    // // Username must:
    // // - Start with an English letter
    // // - Contain only English letters and digits
    // // - Be 6 to 20 characters long
    // String regex = '''^[A-Za-z][A-Za-z0-9]{5,19}$''';
    //     Pattern pattern = Pattern.compile(regex);
    //     return pattern.matcher(userName).matches();
    // }
    
    public  Map<String, Object> validateInputs(Map<String, String> profile) {
        LogUtils.log("Validate inputs ");
        Map<String, Object> result = new HashMap<>();

        if (profile.get(UID)== null || !Pattern.matches('''^[A-Za-z][A-Za-z0-9]{5,19}$''', profile.get(UID))) {
            result.put("valid", false);
            result.put("message", "Invalid username. Must be 6-20 characters, start with a letter, and contain only letters, digits");
            return result;
        }
        if (profile.get(PASSWORD)==null || !Pattern.matches('''^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!"#$%&'()*+,-./:;<=>?@[\\\\]^_`{|}~])[!-~&&[^ ]]{12,24}$''', profile.get(PASSWORD))) {
            result.put("valid", false);
            result.put("message", "Invalid password. Must be at least 12 to 24 characters with uppercase, lowercase, digit, and special character.");
            return result;
        }

        if (profile.get(LANG) == null || !Pattern.matches('''^(ar|en|es|fr|pt|id)$''', profile.get(LANG))) {
            result.put("valid", false);
            result.put("message", "Invalid language code. Must be one of ar, en, es, fr, pt, or id.");
            return result;
        }

        if (profile.get(RESIDENCE_COUNTRY) == null || !Pattern.matches('''^[A-Z]{2}$''', profile.get(RESIDENCE_COUNTRY))) {
            result.put("valid", false);
            result.put("message", "Invalid residence country. Must be exactly two uppercase letters.");
            return result;
        }

        if (!profile.get(PASSWORD).equals(profile.get(CONFIRM_PASSWORD))) {
            result.put("valid", false);
            result.put("message", "Password and confirm password do not match");
            return result;
        }

        result.put("valid", true);
        result.put("message", "All inputs are valid.");
        return result;
    }      

    public Map<String, String> getUserEntityByMail(String email) {
        User user = getUser(MAIL, email);
        boolean local = user != null;
        LogUtils.log("There is % local account for %", local ? "a" : "no", email);
    
        if (local) {            
            String uid = getSingleValuedAttr(user, UID);
            String inum = getSingleValuedAttr(user, INUM_ATTR);
            String name = getSingleValuedAttr(user, GIVEN_NAME);
    
            if (name == null) {
                name = getSingleValuedAttr(user, DISPLAY_NAME);
                if (name == null && email != null && email.contains("@")) {
                    name = email.substring(0, email.indexOf("@"));
                }
            }
    
            // Creating a truly modifiable map
            Map<String, String> userMap = new HashMap<>();
            userMap.put(UID, uid);
            userMap.put(INUM_ATTR, inum);
            userMap.put("name", name);
            userMap.put("email", email);
    
            return userMap;
        }
    
        return new HashMap<>();
    }
    

    public Map<String, String> getUserEntityByUsername(String username) {
        User user = getUser(UID, username);
        boolean local = user != null;
        LogUtils.log("There is % local account for %", local ? "a" : "no", username);
    
        if (local) {
            String email = getSingleValuedAttr(user, MAIL);
            String inum = getSingleValuedAttr(user, INUM_ATTR);
            String name = getSingleValuedAttr(user, GIVEN_NAME);
            String uid = getSingleValuedAttr(user, UID); // Define uid properly
    
            if (name == null) {
                name = getSingleValuedAttr(user, DISPLAY_NAME);
                if (name == null && email != null && email.contains("@")) {
                    name = email.substring(0, email.indexOf("@"));
                }
            }    
            // Creating a modifiable HashMap directly
            Map<String, String> userMap = new HashMap<>();
            userMap.put(UID, uid);
            userMap.put(INUM_ATTR, inum);
            userMap.put("name", name);
            userMap.put("email", email);
    
            return userMap;
        }
    
        return new HashMap<>();
    }

    public String sendEmail(String to, String lang) {
        Map<String, String> labels = Labels.LANG_LABELS.getOrDefault(lang, Labels.LANG_LABELS.get("en"));

        IntStream digits = RAND.ints(OTP_LENGTH, 0, 10);
        String otp = digits.mapToObj(i -> "" + i).collect(Collectors.joining());

        // Fetch each piece of text from the bundle
        String subject = labels.get("subject");
        String msgText = labels.get("msgText").replace("{0}", otp);
        String line0 = labels.get("line0");
        String line1 = labels.get("line1");
        String line2 = labels.get("line2");
        String line3 = labels.get("line3");
        String line4 = labels.get("line4");

        String htmlBody = EmailTemplate.get(otp, line0, line1, line2, line3, line4);

        SmtpConfiguration smtpConfiguration = getSmtpConfiguration();
        String from = smtpConfiguration.getFromEmailAddress();

        MailService mailService = CdiUtil.bean(MailService.class);
        if (mailService.sendMailSigned(from, from, to, null, subject, msgText, htmlBody)) {
            LogUtils.log("E-mail has been delivered to % with code %", to, otp);
            return otp;
        }

        LogUtils.log("E-mail delivery failed, check jans-auth logs");
        return null;
    }    

    private SmtpConfiguration getSmtpConfiguration() {
        ConfigurationService configurationService = CdiUtil.bean(ConfigurationService.class);
        SmtpConfiguration smtpConfiguration = configurationService.getConfiguration().getSmtpConfiguration();
        LogUtils.log("Your smtp configuration is %", smtpConfiguration);
        return smtpConfiguration;

    }     


    public String addNewUser(Map<String, String> profile) throws Exception {
        Set<String> attributes = Set.of("uid", "mail", "displayName","givenName", "sn", "userPassword", "lang", "residenceCountry", "referralCode");
        User user = new User();
    
        attributes.forEach(attr -> {
            String val = profile.get(attr);
            if (StringHelper.isNotEmpty(val)) {
                user.setAttribute(attr, val);      
            }
        });

        UserService userService = CdiUtil.bean(UserService.class);
        user = userService.addUser(user, true); // Set user status active
    
        if (user == null) {
            throw new EntryNotFoundException("Added user not found");
        }
    
        return getSingleValuedAttr(user, INUM_ATTR);
    } 

    private String getSingleValuedAttr(User user, String attribute) {
        Object value = null;
        if (attribute.equals(UID)) {
            //user.getAttribute("uid", true, false) always returns null :(
            value = user.getUserId();
        } else {
            value = user.getAttribute(attribute, true, false);
        }
        return value == null ? null : value.toString();

    }

    private static User getUser(String attributeName, String value) {
        UserService userService = CdiUtil.bean(UserService.class);
        return userService.getUserByAttribute(attributeName, value, true);
    }    
}

