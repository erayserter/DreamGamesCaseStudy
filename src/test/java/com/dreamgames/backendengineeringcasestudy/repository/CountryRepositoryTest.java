package com.dreamgames.backendengineeringcasestudy.repository;

import com.dreamgames.backendengineeringcasestudy.model.Country;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;


import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CountryRepositoryTest {

    @Autowired
    private CountryRepository underTest;

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
    }

    @Test
    void itShouldGetRandomCountry() {
        // given
        String[][] countries = {
                {"Turkey", "TR"},
                {"The United Kingdom", "GB"},
                {"France", "FR"},
                {"Germany", "DE"},
                {"The United States", "US"}};
        for (String[] country : countries) {
            underTest.save(new Country(country[1], country[0]));
        }

        // when
        Country randomCountry = underTest.getRandomCountry();

        // then
        String[] country = {randomCountry.getName(), randomCountry.getCode()};
        assertThat(country).isIn(countries);
    }
}