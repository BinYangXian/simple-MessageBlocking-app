package com.jikexueyuan.messageblocking;

/**
 * Created by fangc on 2016/2/14.
 */
interface ICallBack {   //在延时回调函数法中当程序延时后，通过接口确定要执行哪个方法（回调函数必须有接口，如果没有，无法指定最终要执行哪个方法）
    void callMainActivityViewRefresh();
}
