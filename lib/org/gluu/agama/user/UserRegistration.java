package org.gluu.agama.user;

import java.util.Map;

import org.gluu.agama.registration.JansUserRegistration;



public abstract class UserRegistration {
    public abstract Map<String, Object> validateInputs(Map<String, String> profile);

    public abstract String addNewUser(Map<String, String> profile) throws Exception;

    // public abstract boolean usernamePolicyMatch(String userName);

    // public abstract boolean passwordPolicyMatch(String userPassword);

    public abstract String sendEmail(String to, String lang);
    
    public static UserRegistration getInstance(){
        return  JansUserRegistration.getInstance();
    }    
}
