package com.igot.cb.common.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SunbirdApiRespOragainsationTest {

    @Test
    void testGettersAndSetters() {
        SunbirdApiRespOragainsation org = new SunbirdApiRespOragainsation();

        String organisationId = "org123";
        List<String> roles = Arrays.asList("admin", "user");
        String userId = "user456";
        String parentOrgId = "parent789";
        String id = "id001";

        org.setOrganisationId(organisationId);
        org.setRoles(roles);
        org.setUserId(userId);
        org.setParentOrgId(parentOrgId);
        org.setId(id);

        assertEquals(organisationId, org.getOrganisationId());
        assertEquals(roles, org.getRoles());
        assertEquals(userId, org.getUserId());
        assertEquals(parentOrgId, org.getParentOrgId());
        assertEquals(id, org.getId());
    }

    @Test
    void testDefaultValues() {
        SunbirdApiRespOragainsation org = new SunbirdApiRespOragainsation();

        assertNull(org.getOrganisationId());
        assertNull(org.getRoles());
        assertNull(org.getUserId());
        assertNull(org.getParentOrgId());
        assertNull(org.getId());
    }
}
