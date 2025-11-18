package com.tournamenthost.connect.frontend.with.backend.util;

import java.util.List;
import java.util.stream.Collectors;

import com.tournamenthost.connect.frontend.with.backend.DTO.GameDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.MatchDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.TeamDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.UserDTO;
import com.tournamenthost.connect.frontend.with.backend.Model.Game;
import com.tournamenthost.connect.frontend.with.backend.Model.Match;
import com.tournamenthost.connect.frontend.with.backend.Model.Team;
import com.tournamenthost.connect.frontend.with.backend.Model.User;

/**
 * Utility class for converting entities to DTOs
 */
public class DTOConverter {

    /**
     * Convert User entity to UserDTO (simple version without tournaments)
     */
    public static UserDTO convertToUserDTO(User user) {
        if (user == null) return null;

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        return dto;
    }

    /**
     * Convert Team entity to TeamDTO
     */
    public static TeamDTO convertToTeamDTO(Team team) {
        if (team == null) return null;

        TeamDTO dto = new TeamDTO();
        dto.setId(team.getId());
        dto.setPlayer1(convertToUserDTO(team.getPlayer1()));
        dto.setPlayer2(convertToUserDTO(team.getPlayer2()));
        dto.setTeamType(team.getTeamType().name());
        dto.setTeamName(team.getTeamName());
        return dto;
    }

    /**
     * Convert Game entity to GameDTO
     */
    public static GameDTO convertToGameDTO(Game game) {
        if (game == null) return null;

        GameDTO dto = new GameDTO();
        dto.setId(game.getId());
        dto.setGameNumber(game.getGameNumber());
        dto.setTeamAScore(game.getTeamAScore());
        dto.setTeamBScore(game.getTeamBScore());
        dto.setWinnerTeamId(game.getWinnerTeam() != null ? game.getWinnerTeam().getId() : null);
        return dto;
    }

    /**
     * Convert Match entity to MatchDTO
     */
    public static MatchDTO convertToMatchDTO(Match match) {
        if (match == null) return null;

        MatchDTO dto = new MatchDTO();
        dto.setId(match.getId());
        dto.setCompleted(match.isCompleted());

        // NEW: Team-based fields
        dto.setTeamA(convertToTeamDTO(match.getTeamA()));
        dto.setTeamB(convertToTeamDTO(match.getTeamB()));
        dto.setWinnerTeam(convertToTeamDTO(match.getWinnerTeam()));
        dto.setMatchType(match.getMatchType().name());
        dto.setGamesRequiredToWin(match.getGamesRequiredToWin());

        // Convert games
        if (match.getGames() != null) {
            List<GameDTO> gameDTOs = match.getGames().stream()
                .map(DTOConverter::convertToGameDTO)
                .collect(Collectors.toList());
            dto.setGames(gameDTOs);
        }

        // OLD: Player-based fields (for backward compatibility)
        dto.setPlayerA(convertToUserDTO(match.getPlayerA()));
        dto.setPlayerB(convertToUserDTO(match.getPlayerB()));
        dto.setWinner(convertToUserDTO(match.getWinner()));
        dto.setScore(match.getScore());

        return dto;
    }
}
