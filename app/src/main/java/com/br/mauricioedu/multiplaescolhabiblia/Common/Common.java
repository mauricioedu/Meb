package com.br.mauricioedu.multiplaescolhabiblia.Common;

import android.os.CountDownTimer;

import com.br.mauricioedu.multiplaescolhabiblia.Model.Category;
import com.br.mauricioedu.multiplaescolhabiblia.Model.CurrentQuestion;
import com.br.mauricioedu.multiplaescolhabiblia.Model.Question;
import com.br.mauricioedu.multiplaescolhabiblia.QuestionFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class Common {

    public static final int TOTAL_TIME = 20*60*1000; //20min
    public static List<Question> questionList= new ArrayList<>();
    public static List<CurrentQuestion> answerSheetlist = new ArrayList<>();
    public static Category selectCategory = new Category();

    public static CountDownTimer countDownTimer;
    public static int right_answer_count = 0;
    public static int wrong_answer_count = 0;

    public static ArrayList<QuestionFragment> fragmentList = new ArrayList<>();

    public static TreeSet<String> selected_values = new TreeSet<>();

    public enum ANSWER_TYPE{
        NO_ANSWER,
        WRONG_ANSWER,
        RIGHT_ANSWER
    }
}
