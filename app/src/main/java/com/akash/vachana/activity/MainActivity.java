package com.akash.vachana.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.akash.vachana.R;
import com.akash.vachana.Util.KathruListListenerAbstract;
import com.akash.vachana.Util.UpdateKathruFavorite;
import com.akash.vachana.Util.VachanaListListenerAbstract;
import com.akash.vachana.dbUtil.KathruMini;
import com.akash.vachana.dbUtil.MainDbHelper;
import com.akash.vachana.dbUtil.VachanaMini;
import com.akash.vachana.fragment.KathruListFragment;
import com.akash.vachana.fragment.VachanaListFragment;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        UpdateKathruFavorite.INotifyActivityChange, Serializable {

    private static final String TAG = "MainActivity";

    public static final String[] fragments ={
            "com.akash.vachana.fragment.VachanaFragment",
            "com.akash.vachana.fragment.VachanaListFragment",
            "com.akash.vachana.fragment.KathruListFragment",
            "com.akash.vachana.fragment.SearchFragment"
    };
    private static final long SMOOTH_DRAWER_DELAY = 175;

    public static MainDbHelper db;
    public static boolean vachanaFavoriteChanged = false;
    public static boolean kathruFavoriteChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean value = sharedPreferences.getBoolean("theme", false);
//        String hexColor = String.format("#%06X", (0xFFFFFF & color));
        int color = sharedPreferences.getInt("themeColor", 0);
        matchColor(0xFFFFFF & color);
        AppCompatDelegate.setDefaultNightMode(value? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(0);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        db = new MainDbHelper(this);
        try {
            db.getDataBase();
            Log.d(TAG, "onCreate: Database Created\n");
        } catch (IOException ioe) {
            //throw new Error("Unable to create database");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                InputMethodManager inputMethodManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {
                InputMethodManager inputMethodManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        });

        selectItem(R.id.nav_vachana);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void updateBackStack() {
        if (vachanaFavoriteChanged){
            FragmentManager fm = getSupportFragmentManager();
            for(int entry = 0; entry < fm.getBackStackEntryCount(); entry++){
                String name = fm.getBackStackEntryAt(entry).getName();
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(name);

/*
                if (fragment == null)
                    Log.d(TAG, "updateBackStack: NULL!!!");
*/

                if (fragment instanceof VachanaListFragment){
//                    Log.d(TAG, "onBackStackChanged: !!!!!!!  "+name);
                    VachanaListFragment fragment1 = (VachanaListFragment) fragment;
                    fragment1.needToUpdate = true;
                }
            }
            vachanaFavoriteChanged = false;
        }
        if (kathruFavoriteChanged){
            Log.d(TAG, "onBackStackChanged: kathruFavoriteChanged");
            FragmentManager fm = getSupportFragmentManager();
            for(int entry = 0; entry < fm.getBackStackEntryCount(); entry++){
                String name = fm.getBackStackEntryAt(entry).getName();
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(name);

/*
                if (fragment == null)
                    Log.d(TAG, "updateBackStack: NULL!!!");
*/

                if (fragment instanceof KathruListFragment){
//                    Log.d(TAG, "onBackStackChanged: !!!!!!!  "+name);
                    KathruListFragment fragment1 = (KathruListFragment) fragment;
                    fragment1.needToUpdate = true;
                }
            }
            kathruFavoriteChanged = false;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

/*
        switch (id) {
            case R.id.action_settings:
                return true;
        }
*/
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                selectItem(item.getItemId());
            }
        },SMOOTH_DRAWER_DELAY);

        return true;
    }

    private void selectItem(int itemId) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = null;

        Bundle bundle = new Bundle();
        switch (itemId){

            case R.id.nav_vachana:
                Random r = new Random();
                ArrayList<KathruMini> k = db.getAllKathruMinis();
                final int id = k.get(r.nextInt(247)).getId();
                final KathruMini kathru = db.getKathruMiniById(id);
                bundle.putString("title", kathru.getName());
                bundle.putSerializable("listener", new VachanaListListenerAbstract(this) {
                    @Override
                    public ArrayList<VachanaMini> getVachanaMinis() {
                        return db.getVachanaMinisByKathruId(id);
                    }
                });

                fragment = Fragment.instantiate(MainActivity.this, fragments[1]);
                assert fragment != null;
                fragment.setArguments(bundle);

                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentManager.beginTransaction()
                        .replace(R.id.main_content, fragment, "vachana_list")
                        .commit();
                return;


            case R.id.nav_kathru:
                fragment = Fragment.instantiate(MainActivity.this, fragments[2]);
                bundle.putString("title", "ವಚನಕಾರರು");
                bundle.putSerializable("listener", allKathruListListener);
                try {
                    fragment.setArguments(bundle);
                } catch (NullPointerException e){
                    Log.d(TAG, "selectItem: Fragment is null!!");
                }
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentManager.beginTransaction()
                        .replace(R.id.main_content, fragment, "kathru_list")
                        .addToBackStack( "kathru_list" )
                        .commit();
                return;


            case R.id.nav_favorite:
                bundle.putString("title", "ನೆಚ್ಚಿನ ವಚನಗಳು");
                bundle.putSerializable("listener", new VachanaListListenerAbstract(this) {
                    @Override
                    public ArrayList<VachanaMini> getVachanaMinis() {
                        return db.getFavoriteVachanaMinis();
                    }
                });
                fragment = Fragment.instantiate(MainActivity.this, fragments[1]);
                fragment.setArguments(bundle);
                fragmentManager.popBackStack("fav_vachana_drawer", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentManager.beginTransaction()
                        .replace(R.id.main_content, fragment, "fav_vachana_drawer")
                        .addToBackStack( "fav_vachana_drawer" )
                        .commit();
                return;


            case R.id.nav_favorite_kathru:
                fragment = Fragment.instantiate(MainActivity.this, fragments[2]);
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                bundle.putSerializable("listener", favoriteKathruListListener);
                bundle.putString("title", "ನೆಚ್ಚಿನ ವಚನಕಾರರು");
                try {
                    fragment.setArguments(bundle);
                } catch (NullPointerException e){
                    Log.d(TAG, "selectItem: Fragment is null!!");
                }
                fragmentManager.popBackStack("kathru_favorite_drawer", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentManager.beginTransaction()
                        .replace(R.id.main_content, fragment, "kathru_favorite_drawer")
                        .addToBackStack( "kathru_favorite_drawer" )
                        .commit();
                return;


            case R.id.nav_search:
                fragment = Fragment.instantiate(MainActivity.this, fragments[3]);
                try {
                    fragment.setArguments(bundle);
                } catch (NullPointerException e){
                    Log.d(TAG, "selectItem: Fragment is null!!");
                }
                fragmentManager.popBackStack("search_view_drawer", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentManager.beginTransaction()
                        .replace(R.id.main_content, fragment, "search_view_drawer")
                        .addToBackStack( "search_view_drawer" )
                        .commit();
                return;


            case R.id.nav_settings:
                Intent intent = new Intent(this, MyPreferencesActivity.class);
                startActivityForResult(intent, 0);
                return;


            default:
                Log.e(TAG, "selectItem: Error, Wrong id");
        }
    }

    public ArrayList<KathruMini> getAllKathruMinis() {
        return db.getAllKathruMinis();
    }

    private KathruListListenerAbstract allKathruListListener = new KathruListListenerAbstract(MainActivity.this) {
        @Override
        protected Bundle getBundle(final KathruMini kathruMini) {
            Bundle bundle = new Bundle();
            bundle.putString("title", kathruMini.getName());
            bundle.putSerializable("listener", new VachanaListListenerAbstract(MainActivity.this) {
                @Override
                public ArrayList<VachanaMini> getVachanaMinis() {
                    return db.getVachanaMinisByKathruId(kathruMini.getId());
                }
            });
            return bundle;
        }

        @Override
        public ArrayList<KathruMini> getKathruMinis() {
            return db.getAllKathruMinis();
        }
    };

    private KathruListListenerAbstract favoriteKathruListListener = new KathruListListenerAbstract(MainActivity.this) {
        @Override
        protected Bundle getBundle(final KathruMini kathruMini) {
            Bundle bundle = new Bundle();
            bundle.putString("title", kathruMini.getName());
            bundle.putSerializable("listener", new VachanaListListenerAbstract(MainActivity.this) {
                @Override
                public ArrayList<VachanaMini> getVachanaMinis() {
                    return db.getVachanaMinisByKathruId(kathruMini.getId());
                }
            });
            return bundle;
        }

        @Override
        public ArrayList<KathruMini> getKathruMinis() {
            return db.getFavoriteKathruMinis();
        }
    };

    void matchColor(int id){
        switch (id) {
            case 0x4fc3f7: Log.d(TAG, "Color: Theme1"); setTheme(R.style.theme1); break;
            case 0x42bd41: Log.d(TAG, "Color: Theme2"); setTheme(R.style.theme2); break;
            case 0xffb74d: Log.d(TAG, "Color: Theme3"); setTheme(R.style.theme3); break;
            case 0xff8a65: Log.d(TAG, "Color: Theme4"); setTheme(R.style.theme4); break;
            case 0x3F51B5: Log.d(TAG, "Color: Theme5"); setTheme(R.style.theme5); break;
            default: Log.d(TAG, "Color: unknown theme");
        }
    }

    @Override
    public void notifyChange() {
        updateBackStack();
    }
}
