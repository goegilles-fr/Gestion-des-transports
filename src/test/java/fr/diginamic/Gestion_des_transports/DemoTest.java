package fr.diginamic.Gestion_des_transports;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class DemoTest {

    @BeforeAll
    static void beforeAll() {
        System.out.println("Début de la campagne de tests ");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("Fin de la campagne de tests   ");
    }

    @BeforeEach
    public void beforeEach() {
        System.out.println("créer des éléments nécessaires à tous les tests, par exemple un faux référentiel");
    }
    @AfterEach
    void afterEach() {
        System.out.println("choses claires qui ont été créées dans BeforeEach ");
    }



    @Test
    void inscrireUtilisateur_DevraitToujoursReussir() {

        assertTrue(true, "Ce test réussit toujours");
    }
}
