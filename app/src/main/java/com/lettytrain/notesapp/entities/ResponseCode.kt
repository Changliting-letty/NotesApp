package com.lettytrain.notesapp.entities

/**
 * author: Chang Liting
 * created on: 2021/11/6 9:08
 * description:
 */
enum class ResponseCode(var num:Int) {
    //状态码管理
    IS_SIGNUP_SUCCESS(0),// "注册成功"
    IS_LOGIN_SUCCESS(1),  //"登录成功"
    USERNAME_NOT_EMPTY(2), //"用户名不能为空"
    PASSWORD_NOT_EMPTY(3), //"密码不能为空"
    USERNAME_NOT_EXISTS(4), //"用户名不存在"
    PASSWORD_NOT_Right(5), //"密码错误"
    PARAMTER_NOT_EMPTY(6), //"参数不能为空"
    USERNAME_EXITS(7), //"用户名已存在"
    SIGNUP_FAIL(8),  //"注册失败"
    NEED_LOGIN(9 ),//"用户需要登录"
    NOTE_NOT_EMPTY(10), //"Note不能为空"
    CREATENOTE_FALI(11 ),//Note新建失败"
    UPDATENOTE_FAIL(12), //"Note修改失败"
    DELETENOTE_FAIL(13)//"Notes删除失败"
}