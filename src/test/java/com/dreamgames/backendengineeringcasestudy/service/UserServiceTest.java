package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.dto.UserResponseMapper;
import com.dreamgames.backendengineeringcasestudy.exception.EntityNotFoundException;
import com.dreamgames.backendengineeringcasestudy.model.Country;
import com.dreamgames.backendengineeringcasestudy.model.User;
import com.dreamgames.backendengineeringcasestudy.repository.CountryRepository;
import com.dreamgames.backendengineeringcasestudy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private CountryRepository countryRepository;
    @Mock private UserRepository userRepository;
    @Mock private TournamentService tournamentService;
    @Mock private UserResponseMapper userResponseMapper;
    private UserService underTest;

    @BeforeEach
    void setUp() {
        underTest = new UserService(
                userRepository,
                countryRepository,
                tournamentService,
                userResponseMapper
        );
    }


    @Test
    void shouldCreate() {
        // given
        Country country = new Country("TR", "Turkey");
        User user = new User(country);
        user.setId(UUID.randomUUID());
        given(countryRepository.getRandomCountry()).willReturn(country);
        given(userRepository.save(any())).willReturn(user);

        // when
        underTest.create();

        // then
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUser.getCountry()).isEqualTo(country);
        verify(userResponseMapper).apply(userArgumentCaptor.capture());
        capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUser).isEqualTo(user);
    }

    @Test
    void shouldUpdateLevelWhenNotInTournament() {
        // given
        int initialLevel = 1;
        int initialCoins = 100;

        UUID userId = UUID.randomUUID();
        User user = new User(new Country("TR", "Turkey"));
        user.setId(userId);
        user.setLevel(initialLevel);
        user.setCoins(initialCoins);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(tournamentService.isInActiveTournament(user)).willReturn(false);

        // when
        underTest.updateLevel(userId);

        // then
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUser.getLevel()).isEqualTo(initialLevel + 1);
        assertThat(capturedUser.getCoins()).isEqualTo(initialCoins + UserService.LEVEL_UP_REWARD);
    }

    @Test
    void shouldUpdateLevelWhenInTournament() {
        // given
        int initialLevel = 1;
        int initialCoins = 100;

        UUID userId = UUID.randomUUID();
        User user = new User(new Country("TR", "Turkey"));
        user.setId(userId);
        user.setLevel(initialLevel);
        user.setCoins(initialCoins);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(tournamentService.isInActiveTournament(user)).willReturn(true);

        // when
        underTest.updateLevel(userId);

        // then
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUser.getLevel()).isEqualTo(initialLevel + 1);
        assertThat(capturedUser.getCoins()).isEqualTo(initialCoins + UserService.LEVEL_UP_REWARD);

        verify(tournamentService).updateUserLevel(userArgumentCaptor.capture());
        capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUser).isEqualTo(user);
    }

    @Test
    void willThrowWhenUpdateLevelUserNotFound() {
        // given
        UUID userId = UUID.randomUUID();
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> underTest.updateLevel(userId))
                .isInstanceOf(EntityNotFoundException.class);

        verify(userRepository, never()).save(any());
        verify(tournamentService, never()).updateUserLevel(any());
    }
}