package com.example.anwar_ahmed_s2534054;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.anwar_ahmed_s2534054.DataObjects.PlannedRWData;
import com.example.anwar_ahmed_s2534054.DataObjects.UnplannedRWData;
import com.example.anwar_ahmed_s2534054.Situations.SituationDetailFragment;
import com.example.anwar_ahmed_s2534054.Situations.SituationListFragment;
import com.example.anwar_ahmed_s2534054.Situations.SituationMapFragment;
import com.example.anwar_ahmed_s2534054.Situations.SituationNavigator;
import com.example.anwar_ahmed_s2534054.Situations.SituationsViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements SituationNavigator {
    private static final String TAG_LIST = "list";
    private static final String TAG_MAP = "map";
    private static final String TAG_DETAIL = "detail";
    private static final String BACK_STACK_DETAIL = "detail";

    private SituationsViewModel viewModel;
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNav;
    private View detailContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            final Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(SituationsViewModel.class);

        detailContainer = findViewById(R.id.detailContainer);
        bottomNav = findViewById(R.id.bottomNav);

        setUpToolbar();

        final FragmentManager fm = getSupportFragmentManager();
        if (savedInstanceState == null) {
            fm.beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.listContainer, new SituationListFragment(), TAG_LIST)
                    .add(R.id.mapContainer, new SituationMapFragment(), TAG_MAP)
                    .commitNow();
        }
        fm.addOnBackStackChangedListener(this::updateDetailState);

        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                viewModel.setShowingMap(item.getItemId() == R.id.nav_map);
                showListOrMap();
                return true;
            });
            bottomNav.setSelectedItemId(viewModel.isShowingMap() ? R.id.nav_map : R.id.nav_list);
        }
        showListOrMap();
        updateDetailState();

        viewModel.loadIfNeeded();
    }

    private void setUpToolbar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.main_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_refresh) {
                viewModel.refresh();
                return true;
            }
            return false;
        });
        toolbar.setNavigationOnClickListener(v -> getSupportFragmentManager().popBackStack());
    }

    private void showListOrMap() {
        final FragmentManager fm = getSupportFragmentManager();
        final Fragment list = fm.findFragmentByTag(TAG_LIST);
        final Fragment map = fm.findFragmentByTag(TAG_MAP);
        if (list == null || map == null) return;

        final boolean twoPane = bottomNav == null;
        final boolean showMap = viewModel.isShowingMap();

        final FragmentTransaction t = fm.beginTransaction().setReorderingAllowed(true);
        if (twoPane || !showMap) t.show(list); else t.hide(list);
        if (twoPane || showMap) t.show(map); else t.hide(map);
        t.commitNow();
    }

    @Override
    public void showPlannedDetail(PlannedRWData roadworks) {
        viewModel.selectPlanned(roadworks);
        openDetailFragment();
    }

    @Override
    public void showUnplannedDetail(UnplannedRWData event) {
        viewModel.selectUnplanned(event);
        openDetailFragment();
    }

    private void openDetailFragment() {
        final FragmentManager fm = getSupportFragmentManager();
        fm.popBackStackImmediate(BACK_STACK_DETAIL, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fm.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.detailContainer, new SituationDetailFragment(), TAG_DETAIL)
                .addToBackStack(BACK_STACK_DETAIL)
                .commit();
    }

    private void updateDetailState() {
        final boolean detailOpen = getSupportFragmentManager().findFragmentById(R.id.detailContainer) != null;

        detailContainer.setVisibility(detailOpen ? View.VISIBLE : View.GONE);
        if (bottomNav != null) {
            bottomNav.setVisibility(detailOpen ? View.GONE : View.VISIBLE);
        }

        if (detailOpen) {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            toolbar.setTitle(R.string.title_detail);
        } else {
            toolbar.setNavigationIcon(null);
            toolbar.setTitle(R.string.title_main);
        }
    }
}
