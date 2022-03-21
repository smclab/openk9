package io.openk9.auth.common;

import java.util.List;
import java.util.Map;

public interface UserInfo {

	long getExp();

	long getIat();

	String getJti();

	String getIss();

	String getAud();

	String getSub();

	String getTyp();

	String getAzp();

	String getSessionState();

	String getName();

	String getGivenName();

	String getFamilyName();

	String getPreferredUsername();

	String getEmail();

	boolean isEmailVerified();

	String getAcr();

	Map<String, List<String>> getRealmAccess();

	Map<String, Map<String, List<String>>> getResourceAccess();

	String getScope();

	String getClientId();

	String getUsername();

	boolean isActive();

}
