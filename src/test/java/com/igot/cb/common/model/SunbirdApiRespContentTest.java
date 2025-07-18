package com.igot.cb.common.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SunbirdApiRespContentTest {

    @Test
    void testGettersAndSetters() {
        SunbirdApiRespContent content = new SunbirdApiRespContent();

        String rootOrgName = "Root Org";
        String channel = "Channel1";
        String id = "user123";
        String identifier = "identifier123";
        String rootOrgId = "rootOrgId123";
        String firstName = "John";
        String dob = "1990-01-01";
        String userType = "admin";
        String lastName = "Doe";
        String gender = "Male";
        List<String> roles = Arrays.asList("role1", "role2");
        String countryCode = "IN";
        String email = "john.doe@example.com";
        String userName = "johndoe";
        List<SunbirdApiRespOragainsation> organisations = Arrays.asList(new SunbirdApiRespOragainsation(), new SunbirdApiRespOragainsation());
        Boolean isMdo = true;
        Boolean isCbp = false;

        content.setRootOrgName(rootOrgName);
        content.setChannel(channel);
        content.setId(id);
        content.setIdentifier(identifier);
        content.setRootOrgId(rootOrgId);
        content.setFirstName(firstName);
        content.setDob(dob);
        content.setUserType(userType);
        content.setLastName(lastName);
        content.setGender(gender);
        content.setRoles(roles);
        content.setCountryCode(countryCode);
        content.setEmail(email);
        content.setUserName(userName);
        content.setOrganisations(organisations);
        content.setIsMdo(isMdo);
        content.setIsCbp(isCbp);

        assertEquals(rootOrgName, content.getRootOrgName());
        assertEquals(channel, content.getChannel());
        assertEquals(id, content.getId());
        assertEquals(identifier, content.getIdentifier());
        assertEquals(rootOrgId, content.getRootOrgId());
        assertEquals(firstName, content.getFirstName());
        assertEquals(dob, content.getDob());
        assertEquals(userType, content.getUserType());
        assertEquals(lastName, content.getLastName());
        assertEquals(gender, content.getGender());
        assertEquals(roles, content.getRoles());
        assertEquals(countryCode, content.getCountryCode());
        assertEquals(email, content.getEmail());
        assertEquals(userName, content.getUserName());
        assertEquals(organisations, content.getOrganisations());
        assertEquals(isMdo, content.getIsMdo());
        assertEquals(isCbp, content.getIsCbp());
    }

    @Test
    void testDefaultValues() {
        SunbirdApiRespContent content = new SunbirdApiRespContent();

        assertNull(content.getRootOrgName());
        assertNull(content.getChannel());
        assertNull(content.getId());
        assertNull(content.getIdentifier());
        assertNull(content.getRootOrgId());
        assertNull(content.getFirstName());
        assertNull(content.getDob());
        assertNull(content.getUserType());
        assertNull(content.getLastName());
        assertNull(content.getGender());
        assertNull(content.getRoles());
        assertNull(content.getCountryCode());
        assertNull(content.getEmail());
        assertNull(content.getUserName());
        assertNull(content.getOrganisations());
        assertNull(content.getIsMdo());
        assertNull(content.getIsCbp());
    }
}
