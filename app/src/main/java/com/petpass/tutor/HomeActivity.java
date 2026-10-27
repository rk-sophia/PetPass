package com.petpass.tutor;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView navView = findViewById(R.id.bottom_navigation);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int id = item.getItemId();
                
                if (id == R.id.nav_checkin) {
                    selectedFragment = new CheckinFragment();
                } else if (id == R.id.nav_explore) {
                    selectedFragment = new ExploreFragment();
                } else if (id == R.id.nav_perfil) {
                    selectedFragment = new PerfilDashboardFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }
                return true;
            }
        });

        // Fragment inicial
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new PerfilDashboardFragment())
                .commit();
        navView.setSelectedItemId(R.id.nav_perfil);
    }
}
