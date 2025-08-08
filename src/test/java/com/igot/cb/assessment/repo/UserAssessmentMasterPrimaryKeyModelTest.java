package com.igot.cb.assessment.repo;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserAssessmentMasterPrimaryKeyModelTest {

    @Test
    void testNoArgsConstructorAndSettersAndGetters() {
        UserAssessmentMasterPrimaryKeyModel model = new UserAssessmentMasterPrimaryKeyModel();

        String rootOrg = "org1";
        Date tsCreated = new Date();
        String parentSourceId = "parent123";
        BigDecimal resultPercent = new BigDecimal("85.5");
        UUID id = UUID.randomUUID();

        model.setRootOrg(rootOrg);
        model.setTsCreated(tsCreated);
        model.setParentSourceId(parentSourceId);
        model.setResultPercent(resultPercent);
        model.setId(id);

        assertEquals(rootOrg, model.getRootOrg());
        assertEquals(tsCreated, model.getTsCreated());
        assertEquals(parentSourceId, model.getParentSourceId());
        assertEquals(resultPercent, model.getResultPercent());
        assertEquals(id, model.getId());
    }

    @Test
    void testAllArgsConstructor() {
        String rootOrg = "org2";
        Date tsCreated = new Date();
        String parentSourceId = "parent456";
        BigDecimal resultPercent = new BigDecimal("92.0");
        UUID id = UUID.randomUUID();

        UserAssessmentMasterPrimaryKeyModel model = new UserAssessmentMasterPrimaryKeyModel(
                rootOrg, tsCreated, parentSourceId, resultPercent, id
        );

        assertEquals(rootOrg, model.getRootOrg());
        assertEquals(tsCreated, model.getTsCreated());
        assertEquals(parentSourceId, model.getParentSourceId());
        assertEquals(resultPercent, model.getResultPercent());
        assertEquals(id, model.getId());
    }

    @Test
    void testToString() {
        UserAssessmentMasterPrimaryKeyModel model = new UserAssessmentMasterPrimaryKeyModel(
                "org3", new Date(0), "parent789", new BigDecimal("77.7"), UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        );
        String str = model.toString();
        assertTrue(str.contains("rootOrg=org3"));
        assertTrue(str.contains("tsCreated="));
        assertTrue(str.contains("parentSourceId=parent789"));
        assertTrue(str.contains("resultPercent=77.7"));
        assertTrue(str.contains("id=123e4567-e89b-12d3-a456-426614174000"));
    }
}
