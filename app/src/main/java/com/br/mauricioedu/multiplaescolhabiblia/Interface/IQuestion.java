package com.br.mauricioedu.multiplaescolhabiblia.Interface;

import com.br.mauricioedu.multiplaescolhabiblia.Model.CurrentQuestion;

public interface IQuestion {

    CurrentQuestion getSelectAnswer();
    void showCorrectAnswer();
    void disableAnswer();
    void resetQuestion();
}
