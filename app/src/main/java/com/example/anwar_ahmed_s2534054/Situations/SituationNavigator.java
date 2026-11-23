package com.example.anwar_ahmed_s2534054.Situations;

import com.example.anwar_ahmed_s2534054.DataObjects.PlannedRWData;
import com.example.anwar_ahmed_s2534054.DataObjects.UnplannedRWData;

public interface SituationNavigator {
    void showPlannedDetail(PlannedRWData roadworks);
    void showUnplannedDetail(UnplannedRWData event);
}
