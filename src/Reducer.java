import java.util.ArrayList;
import java.util.List;


public class Reducer {


    public Grammar reduce(Grammar cfg) {
        deleteLambda(cfg);
        deleteUnitProduction(cfg);
        deleteUselessStates(cfg);
        return cfg;
    }

    private void deleteLambda(Grammar cfg) {
        List<State> nullableList = new ArrayList<>();
        ArrayList<State> grammarStates = cfg.getStates();

        boolean isNullable;

        for (State state : grammarStates) {
            isNullable = false;
            for (String derivation : state.getDerivations())
                if (derivation.equals("$")) isNullable = true;
            if (isNullable) nullableList.add(state);
        }

        boolean newNullableAdded;

        ArrayList<State> nonReducibleStates = (ArrayList<State>) cfg.getStates().clone();
        nonReducibleStates.removeAll(nullableList);

        do {
            newNullableAdded = false;
            int nullableVariableCount;

            for (State state : nonReducibleStates)
                for (String derivation : state.getDerivations()) {
                    nullableVariableCount = 0;

                    char[] splitDerivation = derivation.toCharArray();
                    for (char variable : splitDerivation)
                        for (State nullableState : nullableList)
                            if (nullableState.getNonTerminal() == variable) nullableVariableCount++;
                    if (nullableVariableCount == derivation.length()) {
                        nullableList.add(state);
                        nonReducibleStates.remove(state);
                        newNullableAdded = true;
                    }
                }

        } while (newNullableAdded);


        ArrayList<String> derivations;
        for (State state : grammarStates) {
            derivations = state.getDerivations();
            ArrayList<String> derivationsToAdd = new ArrayList<>();
            ArrayList<String> derivationsToReduce = new ArrayList<>();

            for (String derivation : derivations)
                for (State nullableState : nullableList)
                    if (derivation.contains(((Character) nullableState.getNonTerminal()).toString())
                            && derivation.length() != 1) {

                        char[] derivationToChar = derivation.toCharArray();
                        char[] newDerivationToAdd = new char[derivation.length() - 1];
                        int index = 0;
                        for (char c : derivationToChar)
                            if (c != nullableState.getNonTerminal()) {
                                newDerivationToAdd[index] = c;
                                index++;
                            }
                        String newDerivation = new String(newDerivationToAdd);
                        derivationsToAdd.add(newDerivation);
                    } else if (derivation.equals("$")) derivationsToReduce.add(derivation);

            derivations.addAll(derivationsToAdd);
            derivations.removeAll(derivationsToReduce);
        }
    }

    private void deleteUnitProduction(Grammar cfg) {
        ArrayList<String> deleteUnitProductionsPerState;
        ArrayList<State> states = cfg.getStates();

        for (State state : states) {
            deleteUnitProductionsPerState = new ArrayList<>();
            for (String derivation : state.getDerivations())
                if (derivation.length() == 1 && Character.isUpperCase(derivation.charAt(0)))
                    deleteUnitProductionsPerState.add(derivation);
            if (deleteUnitProductionsPerState.size() > 0) for (String toRemove : deleteUnitProductionsPerState) {
                state.deleteDerivation(toRemove);

                for (String derivation : cfg.getStateWithName(toRemove.toCharArray()[0]).getDerivations())
                    if (!derivation.equals(toRemove)) state.addDerivation(derivation);
            }
        }

        cfg.deleteEmptyStates();
    }

    private void deleteUselessStates(Grammar cfg) {
        ArrayList<Character> nonproductive = new ArrayList<>();

        for (State state : cfg.getStates()) nonproductive.add(state.getNonTerminal());

        ArrayList<Character> productive = new ArrayList<>(cfg.getTerminals());

        int q = 0;
        boolean placeHolder = true;

        while (placeHolder && q < 10) {
            placeHolder = false;

            for (State state1 : cfg.getStates())
                for (String deriv : state1.getDerivations()) {

                    char[] derived = deriv.toCharArray();
                    int safeVerify = 0;

                    for (char i : derived)
                        if (productive.contains(i)) safeVerify++;

                    if (safeVerify == deriv.length()) if (!productive.contains(state1.getNonTerminal())) {
                        productive.add(state1.getNonTerminal());
                        int index = nonproductive.indexOf(state1.getNonTerminal());
                        nonproductive.remove(index);
                        placeHolder = true;

                    }
                }
            q++;
        }

        boolean toDelete;
        for (State ridDerive : cfg.getStates()) {
            ArrayList<String> toRemove = new ArrayList<>();

            for (String d : ridDerive.getDerivations()) {
                char[] derivation = d.toCharArray();
                toDelete = false;

                for (char c : derivation)
                    if (nonproductive.contains(c))
                        toDelete = true;
                if (toDelete)
                    toRemove.add(d);
            }

            ridDerive.getDerivations().removeAll(toRemove);

        }

        ArrayList<State> newStates = new ArrayList<>();

        for (char productiveState : productive)
            if (Character.isUpperCase(productiveState))
                for (State check : cfg.getStates())
                    if (check.getNonTerminal() == productiveState) newStates.add(check);

        cfg.setStates(newStates);

        ArrayList<State> reachableStates = new ArrayList<>();
        reachableStates.add(cfg.getStartState());

        for (State currentState : cfg.getStates())
            if (reachableStates.contains(currentState)) for (String currentDerivation : currentState.getDerivations()) {
                char[] splitDerivation = currentDerivation.toCharArray();
                for (char currentChar : splitDerivation)
                    if (Character.isUpperCase(currentChar)) {
                        State reachableState = cfg.getStateWithName(currentChar);
                        if (!(reachableStates.contains(reachableState))) reachableStates.add(reachableState);
                    }
            }

        cfg.setStates(reachableStates);

        ArrayList<Character> newTerminals = new ArrayList<>();
        for (State s : cfg.getStates())
            for (String derive : s.getDerivations()) {

                char[] derived = derive.toCharArray();
                for (char i : derived)
                    if (Character.isLowerCase(i) && !newTerminals.contains(i))
                        newTerminals.add(i);
            }
        cfg.setTerminals(newTerminals);
    }
}