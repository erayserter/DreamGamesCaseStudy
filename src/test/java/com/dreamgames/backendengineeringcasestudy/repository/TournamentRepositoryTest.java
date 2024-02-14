package com.dreamgames.backendengineeringcasestudy.repository;

import com.dreamgames.backendengineeringcasestudy.model.Tournament;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class TournamentRepositoryTest {

    @Autowired
    private TournamentRepository underTest;

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
    }

    @Test
    void shouldFindOngoingTournament() {
        // given
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime start = now.minusDays(1);
        ZonedDateTime end = now.plusDays(1);
        underTest.save(
                Tournament
                        .builder()
                        .startDateTime(Date.from(start.toInstant()))
                        .endDateTime(Date.from(end.toInstant()))
                        .build());

        // when
        Optional<Tournament> expected = underTest.findOngoingTournament(Date.from(now.toInstant()));

        // then
        assertThat(expected).isPresent();
    }

    @Test
    void shouldNotFindOngoingTournament() {
        // given
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime start = now.minusDays(2);
        ZonedDateTime end = now.minusDays(1);
        underTest.save(
                Tournament
                        .builder()
                        .startDateTime(Date.from(start.toInstant()))
                        .endDateTime(Date.from(end.toInstant()))
                        .build());

        // when
        Optional<Tournament> expected = underTest.findOngoingTournament(Date.from(now.toInstant()));

        // then
        assertThat(expected).isNotPresent();
    }
}