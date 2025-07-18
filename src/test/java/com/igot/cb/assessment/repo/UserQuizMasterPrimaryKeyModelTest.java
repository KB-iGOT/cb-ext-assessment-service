package com.igot.cb.assessment.repo;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserQuizMasterPrimaryKeyModelTest {

    @Test
    void testNoArgsConstructorAndSettersGetters() {
        UserQuizMasterPrimaryKeyModel model = new UserQuizMasterPrimaryKeyModel();
        String rootOrg = "org1";
        Date tsCreated = new Date();
        BigDecimal resultPercent = new BigDecimal("85.5");
        UUID id = UUID.randomUUID();

        model.setRootOrg(rootOrg);
        model.setTsCreated(tsCreated);
        model.setResultPercent(resultPercent);
        model.setId(id);

        assertEquals(rootOrg, model.getRootOrg());
        assertEquals(tsCreated, model.getTsCreated());
        assertEquals(resultPercent, model.getResultPercent());
        assertEquals(id, model.getId());
    }

    @Test
    void testAllArgsConstructor() {
        String rootOrg = "org2";
        Date tsCreated = new Date();
        BigDecimal resultPercent = new BigDecimal("90.0");
        UUID id = UUID.randomUUID();

        UserQuizMasterPrimaryKeyModel model = new UserQuizMasterPrimaryKeyModel(rootOrg, tsCreated, resultPercent, id);

        assertEquals(rootOrg, model.getRootOrg());
        assertEquals(tsCreated, model.getTsCreated());
        assertEquals(resultPercent, model.getResultPercent());
        assertEquals(id, model.getId());
    }

    @Test
    void testToString() {
        UserQuizMasterPrimaryKeyModel model = new UserQuizMasterPrimaryKeyModel(
                "org3", new Date(), new BigDecimal("70.0"), UUID.randomUUID());
        String str = model.toString();
        assertTrue(str.contains("UserQuizMasterPrimaryKeyModel"));
        assertTrue(str.contains("org3"));
    }
}
