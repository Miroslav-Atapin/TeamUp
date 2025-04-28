package com.example.teamup;

import java.util.List;

public interface FilterResultListener {
    void onFiltersApplied(List<String> categories, List<String> levels);
}