import java.util.ArrayList;

public class ChomskyConverter {


    public Grammar convertToChomsky(Grammar cfg) {
        deleteMixed(cfg);
        deleteLong(cfg);
        return cfg;
    }

    //derivations containing one or more terminals AND nonTerminals
    private void deleteMixed(Grammar cfg) {
        ArrayList<Character> nonTerminals = new ArrayList<>();

        ArrayList<State> updatedStateList = cfg.getStates();
        int count = 0;

        for (State s : cfg.getStates())
            nonTerminals.add(s.getNonTerminal());

        for (char toNonTerminal : cfg.getTerminals()) {
            //find a character that is not currently being used as a non-terminal for this grammar
            while (nonTerminals.contains((char) (count % 26 + 65)) && count < 91) count++;

            State newState = new State((char) (count % 26 + 65));
            nonTerminals.add((char) (count % 26 + 65));
            ArrayList<String> term = new ArrayList<>();
            String charToString = "" + toNonTerminal;
            term.add(charToString);

            for (State toModify : updatedStateList) {
                ArrayList<String> newDerivations = new ArrayList<>();

                for (String derive : toModify.getDerivations()) {
                    if (derive.length() > 1 && !(derive.length() == 2 && derive.contains(" ")))
                        for (char c : derive.toCharArray())
                            if (c == toNonTerminal)
                                derive = derive.replace(c, newState.getNonTerminal());

                    newDerivations.add(derive);
                }

                toModify.setDerivations(newDerivations);
            }

            newState.setDerivations(term);
            updatedStateList.add(newState);

        }
        cfg.setStates(updatedStateList);
    }


    private void deleteLong(Grammar cfg) {

        ArrayList<State> newStates = new ArrayList<>();
        ArrayList<Character> nonTerminals = new ArrayList<>();
        ArrayList<Character> newNonTerminals = new ArrayList<>();

        for (State s : cfg.getStates())
            nonTerminals.add(s.getNonTerminal());

        for (int alpha = 0; alpha < 26; alpha++)
            if (!nonTerminals.contains((char) ((alpha) % 26 + 65)))
                newNonTerminals.add((char) (alpha % 26 + 65));

        int next = 0;

        for (State old : cfg.getStates()) {
            ArrayList<Character> newNonTermList = new ArrayList<>();
            ArrayList<String> forNewOrigin = new ArrayList<>();

            State lengthLess3 = new State(old.getNonTerminal());

            ArrayList<String> forLengthLess3 = new ArrayList<>();

            for (String checkLength : old.getDerivations()) {
                if (checkLength.length() > 2 && !checkLength.contains(" ")) {

                    int iterator = 2;
                    while (iterator < checkLength.length()) {
                        newNonTermList.add(newNonTerminals.get(next % 26));
                        next++;
                        iterator++;
                    }

                    char[] derivation = checkLength.toCharArray();
                    char[] newDeriv = {derivation[0], newNonTermList.get(0)};
                    String newOriginalDerivation = new String(newDeriv);
                    State newOrig = new State(old.getNonTerminal());
                    newOrig.setDerivations(forNewOrigin);
                    newOrig.addDerivation(newOriginalDerivation);
                    newStates.add(newOrig);
                    char leftSide = newNonTermList.get(0);
                    int index = 1;

                    ArrayList<String> newList = new ArrayList<>();
                    while (index < newNonTermList.size()) {
                        State toAdd = new State(leftSide);
                        toAdd.setDerivations(newList);
                        leftSide = newNonTermList.get(index);
                        char[] rightSide = {derivation[index], leftSide};
                        String rightToString = new String(rightSide);
                        toAdd.addDerivation(rightToString);
                        newList.add(rightToString);
                        index++;
                        newStates.add(toAdd);
                    }

                    ArrayList<String> forFinalNewState = new ArrayList<>();
                    State finalNewState = new State(leftSide);
                    finalNewState.setDerivations(forFinalNewState);
                    char[] rightSide = {derivation[index], derivation[index + 1]};
                    String rightToString = new String(rightSide);
                    finalNewState.addDerivation(rightToString);
                    newStates.add(finalNewState);
                } else {
                    forLengthLess3.add(checkLength);
                    lengthLess3.setDerivations(forLengthLess3);
                    newStates.add(lengthLess3);
                }
            }

        }

        ArrayList<State> finalNewStates = new ArrayList<>();
        for (State toCombine : newStates) {
            boolean toAdd = true;
            if (!finalNewStates.contains(toCombine)) {
                for (State inFinalSet : finalNewStates)
                    if (toCombine.getNonTerminal() == inFinalSet.getNonTerminal()) {
                        toAdd = false;
                        for (String derivationCheck : toCombine.getDerivations())
                            if (!inFinalSet.getDerivations().contains(derivationCheck))
                                inFinalSet.addDerivation(derivationCheck);
                    }
                if (toAdd) finalNewStates.add(toCombine);
            }
        }
        cfg.setStates(finalNewStates);
    }
}