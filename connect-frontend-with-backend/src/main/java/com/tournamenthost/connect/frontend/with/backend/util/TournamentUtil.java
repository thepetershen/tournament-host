package com.tournamenthost.connect.frontend.with.backend.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.tournamenthost.connect.frontend.with.backend.Model.User;

public class TournamentUtil {
    public static int nextPowerOfTwo(int n) {
        if (n < 1) return 1;
        int power = 1;
        while (power < n) {
            power *= 2;
        }
        return power;
    }

    public static ArrayList<User> generateDrawUsingSeeding(List<User> players, int matchAmount) {
        ArrayList<User> copyOfPlayers = new ArrayList<>(players);
        Collections.shuffle(copyOfPlayers);
        
        // create the top matches
        ArrayList<User> answer = new ArrayList<>();
        ArrayList<User> top = new ArrayList<>();

        for(int i = 0; i < matchAmount; i++) {
            top.add(copyOfPlayers.get(0));
            copyOfPlayers.remove(0);
        }

        ArrayList<User> bottom = new ArrayList<>(copyOfPlayers);

        while(bottom.size() < matchAmount) {
            bottom.add(null);
        }

        Collections.shuffle(bottom);

        for(int i = 0; i < matchAmount; i++) {
            if(i % 2 == 0) {
                answer.add(top.get(i));
                answer.add(bottom.get(i));
            } else {
                answer.add(bottom.get(i));
                answer.add(top.get(i));
            }
        }

        return answer;

    }
}
