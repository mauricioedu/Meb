package com.br.mauricioedu.multiplaescolhabiblia;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Display;

import com.br.mauricioedu.multiplaescolhabiblia.Adapter.CategoryAdapter;
import com.br.mauricioedu.multiplaescolhabiblia.Common.SpaceDecoration;
import com.br.mauricioedu.multiplaescolhabiblia.DBHelper.DBHelper;

public class MainActivity extends AppCompatActivity {


    Toolbar toolbar;
    RecyclerView recycler_category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("MEB-Multipla Escolha Biblia");
        setSupportActionBar(toolbar);

        recycler_category = (RecyclerView)findViewById(R.id.recycler_category);
        recycler_category.setHasFixedSize(true);
        recycler_category.setLayoutManager(new GridLayoutManager(this,2));



        CategoryAdapter adapter = new CategoryAdapter(MainActivity.this, DBHelper.getInstance(this).getCategorias());
        int spaceInPixel = 4;
        recycler_category.addItemDecoration(new SpaceDecoration(spaceInPixel));
        recycler_category.setAdapter(adapter);


    }
}
