package com.tournamenthost.connect.frontend.with.backend.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TournamentUtil {
    public static int nextPowerOfTwo(int n) {
        if (n < 1) return 1;
        int power = 1;
        while (power < n) {
            power *= 2;
        }
        return power;
    }

    public static ArrayList<String> generateDrawUsingSeeding(List<String> players, int matchAmount) {
        ArrayList<String> copyOfPlayers = new ArrayList<>(players);
        Collections.shuffle(copyOfPlayers);
        
        // create the top matches
        ArrayList<String> answer = new ArrayList<>();
        ArrayList<String> top = new ArrayList<>();

        for(int i = 0; i < matchAmount; i++) {
            top.add(copyOfPlayers.get(0));
            copyOfPlayers.remove(0);
        }

        ArrayList<String> bottom = new ArrayList<>(copyOfPlayers);

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
