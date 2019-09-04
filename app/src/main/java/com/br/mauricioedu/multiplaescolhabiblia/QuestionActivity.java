package com.br.mauricioedu.multiplaescolhabiblia;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;

import android.support.design.animation.Positioning;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.br.mauricioedu.multiplaescolhabiblia.Adapter.AnswerSheetAdapter;
import com.br.mauricioedu.multiplaescolhabiblia.Adapter.QuestionFragmentAdapter;
import com.br.mauricioedu.multiplaescolhabiblia.Common.Common;
import com.br.mauricioedu.multiplaescolhabiblia.DBHelper.DBHelper;
import com.br.mauricioedu.multiplaescolhabiblia.Model.CurrentQuestion;
import com.br.mauricioedu.multiplaescolhabiblia.Model.Question;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;


import java.util.concurrent.TimeUnit;

public class QuestionActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    int time_play = Common.TOTAL_TIME;
    boolean isAnswerModelView = false;


    TextView txt_right_answer, txt_timer;

    RecyclerView answer_sheet_view;

    AnswerSheetAdapter answerSheetAdapter;

    ViewPager viewPager;
    TabLayout tabLayout;



    @Override
    protected void onDestroy() {
        if (Common.countDownTimer != null)
            Common.countDownTimer.cancel();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(Common.selectCategory.getName());
        setSupportActionBar(toolbar);


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        takeQuestion();


        if (Common.questionList.size() > 0) {



            txt_right_answer = (TextView)findViewById(R.id.txt_question_right);
            txt_timer = (TextView)findViewById(R.id.txt_timer);

            txt_timer.setVisibility(View.VISIBLE);
            txt_right_answer.setVisibility(View.VISIBLE);

            txt_right_answer.setText(new StringBuilder(String.format("%d/%d", Common.right_answer_count, Common.questionList.size())));

            countTimer();


            answer_sheet_view = (RecyclerView) findViewById (R.id.grid_answer);
            answer_sheet_view.setHasFixedSize(true);

            if (Common.questionList.size() > 5)
                answer_sheet_view.setLayoutManager(new GridLayoutManager(this, Common.questionList.size()/2));
            answerSheetAdapter = new AnswerSheetAdapter(this, Common.answerSheetlist);
            answer_sheet_view.setAdapter(answerSheetAdapter);


            viewPager = (ViewPager)findViewById(R.id.view_pager);
            tabLayout = (TabLayout)findViewById(R.id.sliding_tabs);

            genFragmentList();


            final QuestionFragmentAdapter questionFragmentAdapter = new QuestionFragmentAdapter(getSupportFragmentManager(),
                    this,Common.fragmentList);

            viewPager.setAdapter(questionFragmentAdapter);

            tabLayout.setupWithViewPager(viewPager);

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                int SCROLLING_RIGHT = 0;
                int SCROLLING_LEFT = 1;
                int SCROLLING_UNDETERMINED = 2;

                int currentScrollDirection = 2;

                private void setScrollingDirection(float positionOffSet){

                     if ((1-positionOffSet) >= 0.5)
                        this.currentScrollDirection=SCROLLING_RIGHT;
                    else if ((1-positionOffSet) <= 0.5)
                        this.currentScrollDirection=SCROLLING_LEFT;
                }

                private boolean isScrollDirectionUndetermined(){
                    return currentScrollDirection == SCROLLING_UNDETERMINED;
                }

                private boolean isScrollingRight(){
                    return currentScrollDirection == SCROLLING_RIGHT;
                }

                private boolean isScrollingLeft(){
                    return currentScrollDirection == SCROLLING_LEFT;
                }

                @Override
                public void onPageScrolled(int i, float v, int i1) {

                    if (isScrollDirectionUndetermined())
                        setScrollingDirection(v);

                }

                @Override
                public void onPageSelected(int i) {

                    QuestionFragment questionFragment;
                    int position= 0;

                    if (i>0){
                        if (isScrollingRight()){
                            questionFragment = Common.fragmentList.get(i-1);
                            position = i-1;
                        }
                        else if (isScrollingLeft()){
                            questionFragment = Common.fragmentList.get(i+1);
                            position = i+1;
                        }else {
                            questionFragment = Common.fragmentList.get(position);
                        }
                    }else {
                        questionFragment = Common.fragmentList.get(0);
                        position = 0;
                    }

                    CurrentQuestion question_state = questionFragment.getSelectAnswer();
                    Common.answerSheetlist.set(position,question_state);
                    answerSheetAdapter.notifyDataSetChanged();

                    countCorrectAnswer();

                    txt_right_answer.setText(new StringBuilder(String.format("%d", Common.right_answer_count))
                            .append("/")
                            .append(String.format("%d", Common.questionList.size())).toString());

                    if (question_state.getType() == Common.ANSWER_TYPE.NO_ANSWER){
                        questionFragment.showCorrectAnswer();
                        questionFragment.disableAnswer();
                    }

                }



                @Override
                public void onPageScrollStateChanged(int i) {
                    if (i == ViewPager.SCROLL_STATE_IDLE)
                        this.currentScrollDirection = SCROLLING_UNDETERMINED;

                }
            });
        }
    }

    private void countCorrectAnswer() {

        Common.right_answer_count = Common.wrong_answer_count = 0;
        for (CurrentQuestion item:Common.answerSheetlist)
            if (item.getType() == Common.ANSWER_TYPE.RIGHT_ANSWER)
                Common.right_answer_count++;
            else if (item.getType() == Common.ANSWER_TYPE.WRONG_ANSWER)
                Common.wrong_answer_count++;
    }

    private void genFragmentList() {

        for (int i=0;i<Common.questionList.size();i++){

            Bundle bundle = new Bundle();
            bundle.putInt("index",i);
            QuestionFragment fragment = new QuestionFragment();
            fragment.setArguments(bundle);

            Common.fragmentList.add(fragment);


        }
    }


    private void countTimer() {
        if (Common.countDownTimer == null)
        {

            Common.countDownTimer = new CountDownTimer(Common.TOTAL_TIME,1000) {
                @Override
                public void onTick(long l) {
                    txt_timer.setText(String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(l),
                            TimeUnit.MILLISECONDS.toSeconds(l)-
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l))));
                    time_play-=1000;

                }

                @Override
                public void onFinish() {

                }
            }.start();
        }
        else
        {
            Common.countDownTimer.cancel();
            Common.countDownTimer = new CountDownTimer(Common.TOTAL_TIME,1000) {
                @Override
                public void onTick(long l) {
                    txt_timer.setText(String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(l),
                            TimeUnit.MILLISECONDS.toSeconds(l)-
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l))));
                    time_play-=1000;

                }

                @Override
                public void onFinish() {

                }
            }.start();
        }
    }




    private void takeQuestion() {
        Common.questionList = DBHelper.getInstance(this).getQuestionByCategory(Common.selectCategory.getId());

        if (Common.questionList.size()==0){
            new MaterialStyledDialog.Builder(this)
                    .setTitle("Opps !")
                    .setIcon(R.drawable.ic_sentiment_very_dissatisfied_black_24dp)
                    .setDescription("Não temos nenhuma questão para essa categoria "+Common.selectCategory.getName())
                    .setPositiveText("OK")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            finish();
                        }
                    }).show();
        }
        else{

            if (Common.answerSheetlist.size() > 0)
                Common.answerSheetlist.clear();

            for (int i=0;i<Common.questionList.size();i++){
                Common.answerSheetlist.add(new CurrentQuestion(i,Common.ANSWER_TYPE.NO_ANSWER));
            }
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.question, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
