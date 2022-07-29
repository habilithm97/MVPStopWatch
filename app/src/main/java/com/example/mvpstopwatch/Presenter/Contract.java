package com.example.mvpstopwatch.Presenter;

// View와 Presenter를 연결하기 위한 상호작용 인터페이스
public interface Contract {

    interface View {
        void mainResult(); // 메인 버튼 액션에 대한 결과
        void subResult(); // 서브 버튼 액션에 대한 결과
    }

    interface Presenter {
        void mainAction(); // 메인 버튼 액션
        void subAction(); // 서브 버튼 액션
    }
}